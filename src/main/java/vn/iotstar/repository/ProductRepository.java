package vn.iotstar.repository;

import vn.iotstar.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
        select p from Product p
        where (:kw is null or :kw = ''
           or lower(p.name) like lower(concat('%', :kw, '%'))
           or lower(p.description) like lower(concat('%', :kw, '%')))
    """)
    Page<Product> search(String kw, Pageable pageable);

    @Query(value = "SELECT COALESCE(SUM(CAST(price as decimal(18,2)) * quantity), 0) FROM products", nativeQuery = true)
    BigDecimal totalInventoryValue();

    @Query(value = "SELECT COUNT(*) FROM products", nativeQuery = true)
    long countProducts();
}