package br.unipar.frameworks.controller;

import br.unipar.frameworks.model.User;
import br.unipar.frameworks.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    @GetMapping("/search-safe")
    public List<User> safeSearch(@RequestParam String term) {
        return userRepository.safeSearchByName(term);
    }

    @GetMapping("/search-unsafe")
    public List<User> unsafeSearch(@RequestParam String term) {
        String jpql = "select u from User u where lower(u.name) like lower('%" + term + "%')";
        return entityManager.createQuery(jpql, User.class).getResultList();
    }
}
