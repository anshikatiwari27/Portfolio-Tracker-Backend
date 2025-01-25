package com.PortfolioTracker.PortfolioTracker.Controller;

import com.PortfolioTracker.PortfolioTracker.Entity.Users;
import com.PortfolioTracker.PortfolioTracker.Exception.ApiException;
import com.PortfolioTracker.PortfolioTracker.Repository.UserRepository;
import com.PortfolioTracker.PortfolioTracker.Service.JWTService;
import com.PortfolioTracker.PortfolioTracker.Service.StocksService;
import com.PortfolioTracker.PortfolioTracker.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private StocksService stocksService;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    // 1. Add a new user (signup)
    @Transactional(rollbackFor = {ApiException.class, Exception.class})
    @PostMapping("/add")
    public ResponseEntity<?> addUser(@RequestBody Users user) {
        // Check if the username already exists
        Users existingUser = userRepository.findByUserName(user.getUserName());
        if (existingUser != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }

        try {
            user.setUuid(null);
            user.setPassword(encoder.encode(user.getPassword()));
            // Added the stocks to user
             userRepository.save(user);
            stocksService.addStocksToNewUser(user.getUserName());
            String token = jwtService.generateToken(user.getUserName());
            // Save new user
           
            return ResponseEntity.ok(token); // Return the token on success
        } catch (ApiException e) {
            // Handle API exceptions (e.g., Alpha Vantage API limit exhausted)
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            // Handle other exceptions
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "An unexpected error occurred.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 2. Delete a user with the username
    @DeleteMapping("/delete/{username}")
    public String deleteUser(@PathVariable String username) {
        Users user = userRepository.findByUserName(username);
        if (user != null) {
            userRepository.delete(user);
            return "User deleted successfully.";
        } else {
            return "User not found.";
        }
    }

    // 3. Get all users
    @GetMapping("/all")
    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    // 2. User login (validate username and password)
    @PostMapping("/login")
    public String loginUser(@RequestBody Users user) {
        // Find user by username
        Users existingUser = userRepository.findByUserName(user.getUserName());
        if (existingUser == null) {
            return "User not found";
        }

//        // Check if the password matches
//        Users foundUser = existingUser;
//        if (!foundUser.getPassword().equals(user.getPassword())) {
//            return "Invalid credentials";
//        }

        // Login successful
        return userService.verify(user);
    }

    // 4. Update the user password
    @PutMapping("/update/{username}")
    public String updateUserPassword(@PathVariable String username, @RequestBody Users updatedUser) {
        Users user = userRepository.findByUserName(username);
        if (user != null) {
            Users existingUser = user;
            // Update the user's password
            existingUser.setPassword(updatedUser.getPassword());
            userRepository.save(existingUser);
            return "Password updated successfully.";
        } else {
            return "User not found.";
        }
    }

    @GetMapping("/{username}")
    public Users getCurrentUser(@PathVariable String username){
       return userService.getCurrentUser(username);
    }

}
