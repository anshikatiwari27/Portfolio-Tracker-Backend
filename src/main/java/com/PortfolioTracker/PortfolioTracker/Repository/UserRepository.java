package com.PortfolioTracker.PortfolioTracker.Repository;

import com.PortfolioTracker.PortfolioTracker.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<Users, UUID> {

    // Custom query method to find a user by username
    Users findByUserName(String userName);
}
