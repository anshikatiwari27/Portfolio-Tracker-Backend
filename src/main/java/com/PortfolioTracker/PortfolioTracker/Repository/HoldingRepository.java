package com.PortfolioTracker.PortfolioTracker.Repository;

import com.PortfolioTracker.PortfolioTracker.Entity.HoldingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HoldingRepository extends JpaRepository<HoldingEntity, Long> {
    List<HoldingEntity> findByUsername(String username);
}
