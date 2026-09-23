package vn.iotstar.controller;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import vn.iotstar.dto.ForgotPasswordDTO;
import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.dto.ResetPasswordDTO;
import vn.iotstar.service.AuthService;
import vn.iotstar.service.impl.AuthServiceImpl;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // =========================
    // LOGIN
    // =========================

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    // =========================
    // REGISTER
    // =========================

    @GetMapping("/register")
    public String register(Model model) {

        model.addAttribute(
                "form",
                new RegisterDTO()
        );

        return "auth/register";
    }

    @PostMapping("/register")
    public String registerPost(
            @Valid
            @ModelAttribute("form")
            RegisterDTO form,

            BindingResult br,

            Model model) {

        System.out.println();
        System.out.println(
                "======================================"
        );
        System.out.println("REGISTER POST RECEIVED");
        System.out.println(
                "username = " + form.getUsername()
        );
        System.out.println(
                "email = " + form.getEmail()
        );
        System.out.println(
                "fullName = " + form.getFullName()
        );
        System.out.println(
                "password received = "
                + (form.getPassword() != null)
        );
        System.out.println(
                "confirmPassword received = "
                + (form.getConfirmPassword() != null)
        );

        /*
         * VALIDATION
         */
        if (br.hasErrors()) {

            System.err.println(
                    "REGISTER VALIDATION FAILED"
            );

            br.getFieldErrors().forEach(error -> {

                System.err.println(
                        "Field: "
                        + error.getField()
                        + " | value: "
                        + error.getRejectedValue()
                        + " | error: "
                        + error.getDefaultMessage()
                );
            });

            System.out.println(
                    "======================================"
            );

            return "auth/register";
        }

        System.out.println(
                "Validation OK"
        );

        try {

            System.out.println(
                    "Calling AuthService.register()..."
            );

            authService.register(form);

            System.out.println(
                    "REGISTER SUCCESS"
            );

            System.out.println(
                    "======================================"
            );

            return "redirect:/auth/verify-otp?email="
                    + form.getEmail();

        } catch (Exception e) {

            System.err.println(
                    "REGISTER SERVICE FAILED"
            );

            System.err.println(
                    "Error: " + e.getMessage()
            );

            e.printStackTrace();

            System.out.println(
                    "======================================"
            );

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            return "auth/register";
        }
    }

    // =========================
    // VERIFY REGISTER OTP
    // =========================

    @GetMapping("/verify-otp")
    public String verifyOtp(
            @RequestParam String email,
            Model model) {

        model.addAttribute(
                "email",
                email
        );

        if (authService instanceof AuthServiceImpl impl) {

            String fallback =
                    impl.consumeLastOtpFallback();

            if (fallback != null &&
                    !fallback.isBlank()) {

                model.addAttribute(
                        "otpFallback",
                        fallback
                );
            }
        }

        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtpPost(
            @RequestParam String email,
            @RequestParam String otp,
            Model model) {

        try {

            authService.verifyRegisterOtp(
                    email,
                    otp
            );

            return "redirect:/auth/login?verified=true";

        } catch (Exception e) {

            e.printStackTrace();

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            model.addAttribute(
                    "email",
                    email
            );

            return "auth/verify-otp";
        }
    }

    // =========================
    // RESEND REGISTER OTP
    // =========================

    @PostMapping("/resend-otp")
    public String resendOtp(
            @RequestParam String email) {

        try {

            authService.resendRegisterOtp(
                    email
            );

            return "redirect:/auth/verify-otp?email="
                    + email;

        } catch (Exception e) {

            e.printStackTrace();

            return "redirect:/auth/verify-otp?email="
                    + email;
        }
    }

    // =========================
    // FORGOT PASSWORD
    // =========================

    @GetMapping("/forgot-password")
    public String forgotPassword(
            Model model) {

        model.addAttribute(
                "form",
                new ForgotPasswordDTO()
        );

        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPasswordPost(
            @Valid
            @ModelAttribute("form")
            ForgotPasswordDTO form,

            BindingResult br,

            Model model) {

        if (br.hasErrors()) {
            return "auth/forgot-password";
        }

        try {

            authService.forgotPassword(form);

            return "redirect:/auth/reset-password?email="
                    + form.getEmail();

        } catch (Exception e) {

            e.printStackTrace();

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            return "auth/forgot-password";
        }
    }

    // =========================
    // RESET PASSWORD
    // =========================

    @GetMapping("/reset-password")
    public String resetPassword(
            @RequestParam String email,
            Model model) {

        ResetPasswordDTO dto =
                new ResetPasswordDTO();

        dto.setEmail(email);

        model.addAttribute(
                "form",
                dto
        );

        if (authService instanceof AuthServiceImpl impl) {

            String fallback =
                    impl.consumeLastOtpFallback();

            if (fallback != null &&
                    !fallback.isBlank()) {

                model.addAttribute(
                        "otpFallback",
                        fallback
                );
            }
        }

        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPasswordPost(
            @Valid
            @ModelAttribute("form")
            ResetPasswordDTO form,

            BindingResult br,

            Model model) {

        if (br.hasErrors()) {
            return "auth/reset-password";
        }

        try {

            authService.resetPassword(form);

            return "redirect:/auth/login?reset=true";

        } catch (Exception e) {

            e.printStackTrace();

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            return "auth/reset-password";
        }
    }
}