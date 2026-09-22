package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.iotstar.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}