package com.PortfolioTracker.PortfolioTracker.Controller;


import com.PortfolioTracker.PortfolioTracker.Entity.HoldingEntity;
import com.PortfolioTracker.PortfolioTracker.Entity.Stocks;
import com.PortfolioTracker.PortfolioTracker.Service.HoldingService;
import com.PortfolioTracker.PortfolioTracker.Service.StocksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/stocks")
public class StocksController {

    @Autowired
    private StocksService stocksService;

    @Autowired
    private HoldingService holdingService;

    @Autowired
    private HoldingController holdingController;

    @PostMapping
    public void addStocks(@RequestBody Stocks stocks){
        stocksService.addStocks(stocks);
    }

    @GetMapping
    public List<Stocks> getAllStocks(){
       return stocksService.getAllStocks();
    }

    @PostMapping("/{username}")
    public void addStocksToNewUser(@PathVariable String username) throws Exception {
         stocksService.addStocksToNewUser(username);
    }
}
