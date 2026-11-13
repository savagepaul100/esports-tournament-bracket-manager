package com.esports.manager.service;

import com.esports.manager.model.Role;
import com.esports.manager.model.User;
import com.esports.manager.repository.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final DataStore dataStore;

    @Autowired
    public AuthService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public Optional<User> authenticate(String identifier, String password) {
        if (identifier == null || password == null) {
            return Optional.empty();
        }

        // Try by username
        Optional<User> userOpt = dataStore.findUserByUsername(identifier);
        if (userOpt.isEmpty()) {
            userOpt = dataStore.findUserByEmail(identifier);
        }

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (password.equals(user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public User register(String username, String email, String password, Role role, String gamerTag) {
        if (dataStore.findUserByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }
        if (dataStore.findUserByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email '" + email + "' is already registered.");
        }

        User user = new User();
        user.setId("usr-" + UUID.randomUUID().toString().substring(0, 8));
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role != null ? role : Role.PARTICIPANT);
        user.setGamerTag(gamerTag != null && !gamerTag.isBlank() ? gamerTag : username + "#0001");

        return dataStore.saveUser(user);
    }

    public Optional<User> getUserById(String id) {
        return dataStore.findUserById(id);
    }
}
