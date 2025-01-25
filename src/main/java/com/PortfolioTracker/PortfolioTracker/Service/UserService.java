package com.PortfolioTracker.PortfolioTracker.Service;

import com.PortfolioTracker.PortfolioTracker.Entity.Users;
import com.PortfolioTracker.PortfolioTracker.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private UserRepository repo;

    public String verify(Users user) {
        Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUserName(), user.getPassword()));
        if (authentication.isAuthenticated()) {
            System.out.println("User name : "+user.getUserName());
            return jwtService.generateToken(user.getUserName());

        } else {
            return "fail";
        }
    }

    public Users getCurrentUser(String username){
        return repo.findByUserName(username);
    }

}
