package br.unipar.frameworks.repository;

import br.unipar.frameworks.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
