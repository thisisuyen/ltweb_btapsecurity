package vn.iotstar.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.entity.User;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.LocalUploadService;

@Controller
public class ProfileController {

    private final UserRepository userRepository;
    private final LocalUploadService localUploadService;

    public ProfileController(UserRepository userRepository, LocalUploadService localUploadService) {
        this.userRepository = userRepository;
        this.localUploadService = localUploadService;
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        User u = userRepository.findById(principal.getUser().getId()).orElseThrow();
        model.addAttribute("user", u);
        return "profile/index";
    }

    @PostMapping("/profile/avatar")
    public String uploadAvatar(@AuthenticationPrincipal CustomUserDetails principal,
                               @RequestParam("avatarFile") MultipartFile avatarFile) {
        if (avatarFile == null || avatarFile.isEmpty()) {
            return "redirect:/profile?err=empty";
        }

        User u = userRepository.findById(principal.getUser().getId()).orElseThrow();
        String url = localUploadService.saveImage(avatarFile, "avatars");
        if (url != null) {
            u.setImages(url);
            userRepository.save(u);
        }
        return "redirect:/profile?ok=true";
    }
}