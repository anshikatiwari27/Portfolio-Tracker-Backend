package com.PortfolioTracker.PortfolioTracker.Service;

import com.PortfolioTracker.PortfolioTracker.Entity.HoldingEntity;
import com.PortfolioTracker.PortfolioTracker.Entity.Stocks;
import com.PortfolioTracker.PortfolioTracker.Exception.ApiException;
import com.PortfolioTracker.PortfolioTracker.Repository.HoldingRepository;
import com.PortfolioTracker.PortfolioTracker.Repository.StocksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class StocksService {

    @Autowired
    private StocksRepository stocksRepository;

    @Autowired
    private HoldingService holdingService;

    @Autowired
    private HoldingRepository holdingRepository;

    public void addStocks(Stocks stocks){
        stocksRepository.save(stocks);
    }

    public List<Stocks> getAllStocks(){
       return stocksRepository.findAll();
    }


    // Extracting the stocks from stock table and allocating randomly to a new user
    public void addStocksToNewUser(String username) throws ApiException {
        List<Stocks> stocks = getAllStocks();

        Set<Stocks> uniqueStocks = new HashSet<>();
        do {
            Random random = new Random();
            uniqueStocks.add(stocks.get(random.nextInt(7)));
        } while (uniqueStocks.size() != 5);

        // Allocating random stock
        for (Stocks randomStock : uniqueStocks) {
            System.out.println("Ticker in random stock: " + randomStock.getTicker());

            // Creating current date
            LocalDate currDate = LocalDate.now();

            // Creating holding entity
            HoldingEntity currHolding = new HoldingEntity();
            currHolding.setUsername(username);
            currHolding.setStock(randomStock.getStockname());
            currHolding.setTicker(randomStock.getTicker());
            currHolding.setQuantity(1);
            currHolding.setDate(currDate.toString());
            double stockRealtimePrice = holdingService.fetchRealTimePrice(randomStock.getTicker());
            currHolding.setRealtimePrice(stockRealtimePrice);
            currHolding.setBuyPrice(stockRealtimePrice);
            currHolding.setTotalValue(stockRealtimePrice);

            holdingRepository.save(currHolding);
        }
    }


}
