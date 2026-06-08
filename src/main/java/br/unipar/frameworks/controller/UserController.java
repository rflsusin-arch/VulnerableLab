package br.unipar.frameworks.controller;

import br.unipar.frameworks.model.User;
import br.unipar.frameworks.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Paginado
    // antes retornava List<User> sem limite
    // Exemplo: GET /api/users?page=0&size=10&sort=name,asc
    @GetMapping
    public ResponseEntity<Page<User>> listUsers(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(userRepository.findAll(pageable));
    }

    // Busca por ID sem paginação (retorna um único registro)
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(
                userRepository.findById(id).orElseThrow()
        );
    }

    // Busca segura paginada usa parâmetro preparado (evita SQL Injection)
    // Exemplo: GET /api/users/search-safe?term=admin&page=0&size=5
    @GetMapping("/search-safe")
    public ResponseEntity<Page<User>> safeSearch(
            @RequestParam String term,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(userRepository.safeSearchByName(term, pageable));
    }

    // Busca INSEGURA mantida para fins de estudo (SQL Injection intencional)
    // NÃO paginada intencionalmente para demonstrar o risco
    @GetMapping("/search-unsafe")
    public ResponseEntity<?> unsafeSearch(@RequestParam String term) {
        String jpql = "select u from User u where lower(u.name) like lower('%" + term + "%')";
        return ResponseEntity.ok(
                entityManager.createQuery(jpql, User.class).getResultList()
        );
    }
}
