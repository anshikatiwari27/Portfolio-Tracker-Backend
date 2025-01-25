package com.PortfolioTracker.PortfolioTracker.Repository;

import com.PortfolioTracker.PortfolioTracker.Entity.Stocks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StocksRepository extends JpaRepository<Stocks, UUID> {
}
