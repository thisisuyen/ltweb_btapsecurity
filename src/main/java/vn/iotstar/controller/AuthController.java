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

@Controller
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @GetMapping("/login")
  public String login() {
    return "auth/login";
  }

  @GetMapping("/register")
  public String registerForm(Model model) {
    model.addAttribute("registerDTO", new RegisterDTO());
    return "auth/register";
  }

  @PostMapping("/register")
  public String register(@Valid @ModelAttribute("registerDTO") RegisterDTO dto,
                         BindingResult result,
                         Model model) {
    if (result.hasErrors()) return "auth/register";
    try {
      String email = authService.register(dto);
      return "redirect:/verify-otp?email=" + email;
    } catch (Exception ex) {
      model.addAttribute("error", ex.getMessage());
      return "auth/register";
    }
  }

  @GetMapping("/verify-otp")
  public String verifyOtpForm(@RequestParam("email") String email, Model model) {
    model.addAttribute("email", email);
    return "auth/verify-otp";
  }

  @PostMapping("/verify-otp")
  public String verifyOtp(@RequestParam("email") String email,
                          @RequestParam("code") String code,
                          Model model) {
    boolean ok = authService.verifyRegisterOtp(email, code);
    if (ok) return "redirect:/login?verified=true";
    model.addAttribute("email", email);
    model.addAttribute("error", "OTP không đúng hoặc đã hết hạn.");
    return "auth/verify-otp";
  }

  @PostMapping("/register/resend-otp")
  public String resendOtp(@RequestParam("email") String email, Model model) {
    try {
      authService.resendRegisterOtp(email);
      return "redirect:/verify-otp?email=" + email + "&resent=true";
    } catch (Exception ex) {
      return "redirect:/verify-otp?email=" + email + "&resent=false";
    }
  }

  @GetMapping("/forgot-password")
  public String forgotForm(Model model) {
    model.addAttribute("forgotDTO", new ForgotPasswordDTO());
    return "auth/forgot-password";
  }

  @PostMapping("/forgot-password")
  public String forgot(@Valid @ModelAttribute("forgotDTO") ForgotPasswordDTO dto,
                       BindingResult result,
                       Model model) {
    if (result.hasErrors()) return "auth/forgot-password";
    try {
      authService.forgotPassword(dto);
      return "redirect:/reset-password?email=" + dto.getEmail() + "&sent=true";
    } catch (Exception ex) {
      model.addAttribute("error", ex.getMessage());
      return "auth/forgot-password";
    }
  }

  @GetMapping("/reset-password")
  public String resetForm(@RequestParam("email") String email,
                          @RequestParam(value = "sent", required = false) String sent,
                          Model model) {
    ResetPasswordDTO dto = new ResetPasswordDTO();
    dto.setEmail(email);
    model.addAttribute("resetDTO", dto);
    model.addAttribute("sent", sent);
    return "auth/reset-password";
  }

  @PostMapping("/reset-password")
  public String reset(@Valid @ModelAttribute("resetDTO") ResetPasswordDTO dto,
                      BindingResult result,
                      Model model) {
    if (result.hasErrors()) return "auth/reset-password";
    try {
      authService.resetPassword(dto);
      return "redirect:/login?reset=true";
    } catch (Exception ex) {
      model.addAttribute("error", ex.getMessage());
      return "auth/reset-password";
    }
  }
}