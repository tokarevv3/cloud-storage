package ru.tokarev.cloudstorage.http.controller;

import org.springframework.web.bind.annotation.GetMapping;

public class LoginController {

    @GetMapping("/login")
    public String loginPage() {
        return "user/login";
    }
}
