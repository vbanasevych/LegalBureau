package com.legalbureau.controller;

import lombok.RequiredArgsConstructor;
import com.legalbureau.entity.User;
import com.legalbureau.exception.DuplicateResourceException;
import com.legalbureau.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerClient(@ModelAttribute User user, Model model) {
        try {
            userService.registerClient(user);
            return "redirect:/login";
        } catch (DuplicateResourceException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }
}
