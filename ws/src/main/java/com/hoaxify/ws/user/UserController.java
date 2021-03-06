package com.hoaxify.ws.user;

import com.hoaxify.ws.error.ApiError;
import com.hoaxify.ws.shared.CurrentUser;
import com.hoaxify.ws.shared.GenericResponse;
import com.hoaxify.ws.user.vm.UserUpdateVm;
import com.hoaxify.ws.user.vm.UserVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@RestController
@RequestMapping("/api/1.0")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/users")
    public GenericResponse createUser(@Valid @RequestBody User user) {

        userService.save(user);
        return new GenericResponse("user created");
    }

    @GetMapping("/users")
    Page<UserVM> getUsers(Pageable page, @CurrentUser User user) {
        return userService.getUsers(page, user).map(UserVM::new);
    }

    @GetMapping("/users/{username}")
    UserVM getUser(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        return new UserVM(user);
    }

    @PutMapping("/users/{username}")
    @PreAuthorize("#username == principal.username")
    UserVM updateUser(@Valid @RequestBody UserUpdateVm updatedUser, @PathVariable String username) {
        User user = userService.updateUser(username, updatedUser);
        return new UserVM(user);
    }

    @DeleteMapping("/users/{username}")
    @PreAuthorize("#username == principal.username")
    public GenericResponse deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        return new GenericResponse("user is removed");
    }

}
