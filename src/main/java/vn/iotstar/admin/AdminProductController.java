package vn.iotstar.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import vn.iotstar.entity.Product;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.service.LocalUploadService;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private final ProductRepository productRepository;
    private final LocalUploadService localUploadService;

    public AdminProductController(
            ProductRepository productRepository,
            LocalUploadService localUploadService) {

        this.productRepository = productRepository;
        this.localUploadService = localUploadService;
    }

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String kw,
            Model model) {

        page = Math.max(page, 0);
        size = Math.max(size, 1);

        Page<Product> result =
                productRepository.search(
                        kw,
                        PageRequest.of(page, size)
                );

        model.addAttribute("page", result);
        model.addAttribute("kw", kw);

        return "admin/products/list";
    }

    @GetMapping("/create")
    public String create(Model model) {

        model.addAttribute(
                "form",
                new ProductForm()
        );

        return "admin/products/form";
    }

    @PostMapping("/create")
    public String createPost(
            @Valid
            @ModelAttribute("form")
            ProductForm form,

            BindingResult br,

            @RequestParam(
                name = "imageFile",
                required = false
            )
            MultipartFile imageFile) {

        if (br.hasErrors()) {
            return "admin/products/form";
        }

        Product product = new Product();

        product.setName(form.getName());
        product.setDescription(form.getDescription());
        product.setPrice(form.getPrice());
        product.setQuantity(form.getQuantity());

        String url =
                localUploadService.saveImage(
                        imageFile,
                        "products"
                );

        if (url != null) {
            product.setImage(url);
        }

        productRepository.save(product);

        return "redirect:/admin/products";
    }

    @GetMapping("/{id}/edit")
    public String edit(
            @PathVariable Long id,
            Model model) {

        Product product =
                productRepository
                    .findById(id)
                    .orElseThrow();

        ProductForm form =
                new ProductForm();

        form.setName(product.getName());
        form.setDescription(
                product.getDescription()
        );
        form.setPrice(product.getPrice());
        form.setQuantity(
                product.getQuantity()
        );

        model.addAttribute("id", id);
        model.addAttribute(
                "product",
                product
        );
        model.addAttribute("form", form);

        return "admin/products/form";
    }

    @PostMapping("/{id}/edit")
    public String editPost(
            @PathVariable Long id,

            @Valid
            @ModelAttribute("form")
            ProductForm form,

            BindingResult br,

            @RequestParam(
                name = "imageFile",
                required = false
            )
            MultipartFile imageFile,

            Model model) {

        Product product =
                productRepository
                    .findById(id)
                    .orElseThrow();

        /*
         * Quan trọng:
         * Khi validation fail phải truyền lại
         * id + product cho template.
         */
        if (br.hasErrors()) {

            model.addAttribute("id", id);

            model.addAttribute(
                    "product",
                    product
            );

            return "admin/products/form";
        }

        product.setName(form.getName());

        product.setDescription(
                form.getDescription()
        );

        product.setPrice(
                form.getPrice()
        );

        product.setQuantity(
                form.getQuantity()
        );

        if (imageFile != null &&
                !imageFile.isEmpty()) {

            String url =
                    localUploadService.saveImage(
                            imageFile,
                            "products"
                    );

            if (url != null) {
                product.setImage(url);
            }
        }

        productRepository.save(product);

        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id) {

        productRepository.deleteById(id);

        return "redirect:/admin/products";
    }

    public static class ProductForm {

        @NotBlank(
            message = "Tên sản phẩm không được để trống"
        )
        private String name;

        private String description;

        @NotNull(
            message = "Giá không được để trống"
        )
        @DecimalMin(
            value = "0.0",
            message = "Giá không được âm"
        )
        private BigDecimal price;

        @Min(
            value = 0,
            message = "Số lượng không được âm"
        )
        private int quantity;

        public ProductForm() {
        }

        public String getName() {
            return name;
        }

        public void setName(
                String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(
                String description) {
            this.description = description;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(
                BigDecimal price) {
            this.price = price;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(
                int quantity) {
            this.quantity = quantity;
        }
    }
}