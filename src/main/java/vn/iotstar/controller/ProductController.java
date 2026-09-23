package vn.iotstar.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.iotstar.entity.Product;
import vn.iotstar.repository.ProductRepository;

@Controller
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/products")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "12") int size,
                       @RequestParam(required = false) String kw,
                       Model model) {
        Page<Product> p = productRepository.search(kw, PageRequest.of(page, size));
        model.addAttribute("page", p);
        model.addAttribute("kw", kw);
        return "products/list";
    }
}