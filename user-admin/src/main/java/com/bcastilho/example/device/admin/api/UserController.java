package com.bcastilho.example.device.admin.api;

import com.bcastilho.example.device.admin.domain.User;
import com.bcastilho.example.device.admin.service.UserAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/v1/user")
public class UserController {

    UserAdminService service;

    public UserController(UserAdminService service) {

        this.service = service;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {

        User createdUser = service.createUser(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);

    }

    @GetMapping
    public ResponseEntity<User> getUser(@RequestParam BigInteger id) {

        User createdUser = service.getUser(id.toString());
        return new ResponseEntity<>(createdUser, HttpStatus.OK);

    }

    @GetMapping("all")
    public ResponseEntity<List<User>> getUsers() {

        List<User> users = service.getUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);

    }

}
