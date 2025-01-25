package com.PortfolioTracker.PortfolioTracker.Service;

import com.PortfolioTracker.PortfolioTracker.Entity.HoldingEntity;
import com.PortfolioTracker.PortfolioTracker.Exception.ApiException;
import com.PortfolioTracker.PortfolioTracker.Repository.HoldingRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class HoldingService {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    private final HoldingRepository holdingRepository;
    private final RestTemplate restTemplate;


    private static String API_KEY="CYR6Q8UUI10WWTV2";// API secret key fetched from application properties.

    private static final String BASE_URL = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY";

    // Rate limiter to enforce 5 requests per minute
    private static final RateLimiter rateLimiter = RateLimiter.create(5.0 / 60.0);

    // Cache to store stock prices (expires after 1 day)
    private static final Cache<String, CachedPrice> priceCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS) // Cache expires after 1 day
            .maximumSize(100) // Maximum of 100 tickers in the cache
            .build();

    // Custom object to store cached price and date
    private static class CachedPrice {
        private final double price;
        private final LocalDate date;

        public CachedPrice(double price, LocalDate date) {
            this.price = price;
            this.date = date;
        }

        public double getPrice() {
            return price;
        }

        public LocalDate getDate() {
            return date;
        }
    }


    public double fetchRealTimePrice(String symbol) throws ApiException {
        LocalDate currentDate = LocalDate.now();

        // Check cache first
        CachedPrice cachedPrice = priceCache.getIfPresent(symbol);
        if (cachedPrice != null && cachedPrice.getDate().equals(currentDate)) {
            // Use cached data if available and up-to-date
            return cachedPrice.getPrice();
        }

        // If cache is empty or outdated, call the API
        Map<String, Object> apiResponse = fetchPriceFromApi(symbol);

        // Check if the API response contains an error
        if (apiResponse.get("status").equals("error")) {
            throw new ApiException(apiResponse.get("message").toString());
        }

        // Update the cache with the new price and current date
        double price = (double) apiResponse.get("price");
        priceCache.put(symbol, new CachedPrice(price, currentDate));

        return price;
    }

    private Map<String, Object> fetchPriceFromApi(String symbol) throws ApiException {
        try {
            String apiUrl = BASE_URL + "&symbol=" + symbol + "&apikey=" + API_KEY;
            ResponseEntity<Map> responseEntity = restTemplate.getForEntity(apiUrl, Map.class);

            // Log the entire response for debugging
            System.out.println("API Response: " + responseEntity.getBody());

            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                Map<String, Object> response = responseEntity.getBody();

                // Check for API limit exhaustion message
                if (response.containsKey("Information") && response.get("Information").toString().contains("API rate limit")) {
                    // Return a custom error response
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("status", "error");
                    errorResponse.put("message", "Alpha Vantage API Limit Exhausted");
                    return errorResponse;
                }

                if (response.containsKey("Time Series (Daily)")) {
                    Map<String, Object> timeSeriesDaily = (Map<String, Object>) response.get("Time Series (Daily)");
                    String latestDate = timeSeriesDaily.keySet().iterator().next(); // Get the latest date
                    Map<String, Object> latestData = (Map<String, Object>) timeSeriesDaily.get(latestDate);
                    String closingPrice = latestData.get("4. close").toString();
                    Map<String, Object> successResponse = new HashMap<>();
                    successResponse.put("status", "success");
                    successResponse.put("price", Double.parseDouble(closingPrice));
                    return successResponse;
                } else {
                    throw new ApiException("Invalid response structure: 'Time Series (Daily)' not found");
                }
            } else {
                throw new ApiException("Failed to fetch data: " + responseEntity.getStatusCode());
            }
        } catch (Exception e) {
            throw new ApiException("Failed to fetch real-time price: " + e.getMessage(), e);
        }
    }

    @Autowired
    public HoldingService(HoldingRepository holdingRepository, RestTemplate restTemplate) {
        this.holdingRepository = holdingRepository;
        this.restTemplate = restTemplate;
    }


    public List<HoldingEntity> getHoldingsBySQLQuery(String username, String ticker, String date) {
        // SQL query
        String sql = "SELECT * FROM stock_holdings WHERE username = ? AND ticker = ? AND date = ?";
        // Execute the query
        System.out.println("SQL username : " + username);
        System.out.println("SQL ticker :" + ticker);
        System.out.println("SQL date : " + date);
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, username, ticker, date);
        // List to hold the mapped entities
        List<HoldingEntity> holdings = new ArrayList<>();

        if (!results.isEmpty()) {
            System.out.println("Records found: " + results.size());

            // Iterate over each row in the result set
            for (Map<String, Object> row : results) {
                // Map the row data to a HoldingEntity object
                HoldingEntity holding = new HoldingEntity();
                holding.setId((Long) row.get("id"));
                holding.setUsername((String) row.get("username"));
                holding.setTicker((String) row.get("ticker"));
                holding.setQuantity((Integer) row.get("quantity"));
                holding.setBuyPrice((Double) row.get("buy_price"));
                holding.setDate( (String) row.get("date"));
                holding.setStock((String) row.get("stock"));
                holding.setRealtimePrice( (Double) row.get("realtime_price"));
                holding.setTotalValue( (Double) row.get("total_value"));

                // Add the holding to the list
                holdings.add(holding);
            }
        } else {
            System.out.println("No records found.");
        }
        // Return the list of holdings
        return holdings;
    }


    public void addHolding(HoldingEntity holding) throws ApiException {
        String apiUrl = BASE_URL + "&symbol=" + holding.getTicker() + "&apikey=" + API_KEY;
        LocalDate dateWhenUserBuyStock = LocalDate.now();
        holding.setDate(dateWhenUserBuyStock.toString());

        // Fetch the existing stocks from DB.
        List<HoldingEntity> existingStock = getHoldingsBySQLQuery(holding.getUsername(), holding.getTicker(), holding.getDate());

        try {
            // If there is no stock in the DB, add the stock directly
            if (existingStock.isEmpty()) {
                // Fetch the real-time price
                double realtimePrice = fetchRealTimePrice(holding.getTicker());
                LocalDate currDate = LocalDate.now();
                holding.setRealtimePrice(realtimePrice);
                holding.setDate(currDate.toString());
                holding.setTotalValue(realtimePrice * holding.getQuantity());

                // Set the buy price if not already set
                if (holding.getBuyPrice() == 0.0) {
                    holding.setBuyPrice(realtimePrice);
                }
                holdingRepository.save(holding);
            } else {
                // If stock with the same date exists, update its quantity and total value
                for (HoldingEntity currExistingStock : existingStock) {
                    if (currExistingStock.getDate().equals(holding.getDate())) {
                        int qty = currExistingStock.getQuantity() + holding.getQuantity();
                        currExistingStock.setTotalValue(currExistingStock.getRealtimePrice() * qty);
                        currExistingStock.setQuantity(qty);
                        currExistingStock.setBuyPrice(holding.getBuyPrice());
                        holdingRepository.save(currExistingStock);
                    }
                }
            }
        } catch (ApiException e) {
            // Propagate the exception to the caller
            throw e;
        }
    }



    public List<HoldingEntity> getHoldingsByUsername(String username) throws ApiException {
        List<HoldingEntity> holdings = holdingRepository.findByUsername(username);
        LocalDate currentDate = LocalDate.now();

        for (HoldingEntity holding : holdings) {
            if (!holding.getDate().equals(currentDate.toString())) {
                try {
                    double realtimePrice = fetchRealTimePrice(holding.getTicker());
                    holding.setRealtimePrice(realtimePrice);
                    holding.setDate(currentDate.toString());
                    holdingRepository.save(holding);
                } catch (ApiException e) {
                    // Log the error and continue with the next holding
                    System.err.println("Error fetching real-time price for " + holding.getTicker() + ": " + e.getMessage());
                }
            }
        }

        return holdings;
    }

    /**
     * Deletes a holding by its ID.
     *
     * @param id The ID of the holding to delete.
     */
    public void deleteHolding(Long id) {
        holdingRepository.deleteById(id);
    }

    /**
     * Finds holdings by username.
     *
     * @param username The username to filter holdings.
     * @return A list of holdings for the specified username.
     */
    public List<HoldingEntity> findHoldingsByUsername(String username) {
        return holdingRepository.findByUsername(username);
    }
}