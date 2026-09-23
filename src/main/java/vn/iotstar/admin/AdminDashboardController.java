package vn.iotstar.admin;

import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;

@Controller
public class AdminDashboardController {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public AdminDashboardController(UserRepository userRepository, ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @GetMapping("/admin")
    public String dashboard(Model model) {
        model.addAttribute("userCount", userRepository.count());
        model.addAttribute("productCount", productRepository.countProducts());
        BigDecimal totalValue = productRepository.totalInventoryValue();
        model.addAttribute("totalInventoryValue", totalValue);
        return "admin/dashboard";
    }
}