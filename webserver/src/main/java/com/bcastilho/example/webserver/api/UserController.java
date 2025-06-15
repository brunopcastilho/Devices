package com.bcastilho.example.webserver.api;

import com.bcastilho.example.webserver.domain.User;
import com.bcastilho.example.webserver.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/webserver/user")
public class UserController {

    UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {

        User createdUser = service.createUser(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);

    }

    @GetMapping
    public ResponseEntity<User> getUser(@RequestParam Integer id) {

        User createdUser = service.getUser(id);
        return new ResponseEntity<>(createdUser, HttpStatus.OK);

    }

    @GetMapping("all")
    public ResponseEntity<List<User>> getUsers() {

        List<User> users = service.getUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);

    }

}
