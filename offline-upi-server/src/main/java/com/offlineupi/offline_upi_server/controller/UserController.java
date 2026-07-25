package com.offlineupi.offline_upi_server.controller;

import com.offlineupi.offline_upi_server.entity.User;
import com.offlineupi.offline_upi_server.repository.UserRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public UserController(UserRepository userRepository,
                      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
}

    @PostMapping
    public User createUser(@RequestBody User user) {

    user.setPassword(
        passwordEncoder.encode(user.getPassword())
    );

    return userRepository.save(user);
}
    @GetMapping
    public List<User> getUsers() {
    return userRepository.findAll();
}
}