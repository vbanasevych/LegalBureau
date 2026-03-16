package com.legalbureau.controller;

import com.legalbureau.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import com.legalbureau.entity.User;
import com.legalbureau.exception.DuplicateResourceException;
import com.legalbureau.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PasswordResetService passwordResetService;

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
        } catch (DuplicateResourceException | IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/login-required")
    public String loginRequired(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("info", "👨‍⚖️ Консультацію може отримати лише зареєстрований у бюро користувач. Будь ласка, увійдіть в акаунт або зареєструйтесь.");
        return "redirect:/login";
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, jakarta.servlet.http.HttpServletRequest request, RedirectAttributes attributes) {
        String appUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        passwordResetService.createTokenAndSendEmail(email, appUrl);
        attributes.addFlashAttribute("info", "Якщо такий email існує, ми надіслали на нього інструкції для відновлення.");
        return "redirect:/login";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam String token, org.springframework.ui.Model model) {
        if (!passwordResetService.validateToken(token)) {
            model.addAttribute("error", "Посилання недійсне або прострочене (минуло 15 хвилин).");
            return "auth/login";
        }
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String token, @RequestParam String password, RedirectAttributes attributes) {
        passwordResetService.updatePassword(token, password);
        attributes.addFlashAttribute("success", "Пароль успішно змінено! Тепер ви можете увійти.");
        return "redirect:/login";
    }
}
