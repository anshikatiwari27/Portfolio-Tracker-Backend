package com.PortfolioTracker.PortfolioTracker.Controller;

import com.PortfolioTracker.PortfolioTracker.Entity.HoldingEntity;
import com.PortfolioTracker.PortfolioTracker.Exception.ApiException;
import com.PortfolioTracker.PortfolioTracker.Service.HoldingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/holdings")
public class HoldingController {
    @Autowired
    private HoldingService holdingService;

    @GetMapping("/{username}")
    public List<HoldingEntity> getHoldingsByUsername(@PathVariable String username) throws ApiException {
        return holdingService.getHoldingsByUsername(username);
    }

    @PostMapping
    public ResponseEntity<?> addHolding(@RequestBody HoldingEntity holding) {
        try {
            holdingService.addHolding(holding);
            return ResponseEntity.ok("Stock added successfully");
        } catch (Exception e) {
            // Return a structured error response
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    public void deleteHolding(@PathVariable Long id) {
        holdingService.deleteHolding(id);
    }

    @GetMapping("/get-portfolio-value/{username}")
    public double getProfolioValue(@PathVariable String username) throws Exception {
        double portfolioValue = 0.0;

       List<HoldingEntity> holdings = holdingService.findHoldingsByUsername(username);
       for (HoldingEntity currHolding : holdings){
           portfolioValue  +=(currHolding.getQuantity() * currHolding.getRealtimePrice());
       }
        return portfolioValue;
    }

}
