package com.bcastilho.example.webserver.api;


import com.bcastilho.example.webserver.domain.User;
import com.bcastilho.example.webserver.service.UserService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class UserControllerGraphql {

    UserService service;

    public UserControllerGraphql(UserService service) {
        this.service = service;
    }

    @QueryMapping
    public User getUser(@Argument Integer id) {
        return service.getUser(id);
    }

    @QueryMapping
    public List<User> getUsers() {
        return service.getUsers();
    }

}
