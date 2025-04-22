package ru.tokarev.cloudstorage.http.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
public class UserController {


//    @GetMapping
//    public String getCurrentUser() {
//        UserDetails currentUserObject = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//
//
//    }


}
