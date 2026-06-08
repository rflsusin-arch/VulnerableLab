package br.unipar.frameworks.controller;

import br.unipar.frameworks.model.Product;
import br.unipar.frameworks.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Antes: List<Product>  retornava TODOS os produtos de uma vez
    // Agora: Page<Product> retorna paginado
    // Parâmetros na URL:
    // ?page=0  número da página (começa em 0)
    // ?size=10  quantidade de itens por pagina
    // ?sort=name,asc  ordenação por campo
    // Exemplo: GET /api/products?page=0&size=5&sort=name,asc
    @GetMapping
    public ResponseEntity<Page<Product>> listProducts(
            // @PageableDefault define os valores quando não informados na URL
            // size=10 = 10 itens por página
            // sort="id" = ordena por id
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        // findAll(pageable) é fornecido automaticamente pelo JpaRepository
        return ResponseEntity.ok(productRepository.findAll(pageable));
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.ok(productRepository.save(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id,
                                                  @RequestBody Product product) {
        product.setId(id);
        return ResponseEntity.ok(productRepository.save(product));
    }
}
