package br.unipar.frameworks;

import br.unipar.frameworks.model.Comment;
import br.unipar.frameworks.model.Product;
import br.unipar.frameworks.model.User;
import br.unipar.frameworks.repository.CommentRepository;
import br.unipar.frameworks.repository.ProductRepository;
import br.unipar.frameworks.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;

@SpringBootApplication
public class VulnerableLabApplication {

    public static void main(String[] args) {
        SpringApplication.run(VulnerableLabApplication.class, args);
    }

    @Bean
    CommandLineRunner seedData(UserRepository userRepository,
                               ProductRepository productRepository,
                               CommentRepository commentRepository) {
        return args -> {
            User admin = new User(null, "Admin", "admin@lab.local", "admin123", "ADMIN");
            User student = new User(null, "Aluno", "aluno@lab.local", "aluno123", "USER");
            userRepository.save(admin);
            userRepository.save(student);

            Product p1 = new Product(null, "Notebook", "Notebook para testes em laboratório", new BigDecimal("3500.00"));
            Product p2 = new Product(null, "Mouse", "Mouse USB", new BigDecimal("50.00"));
            productRepository.save(p1);
            productRepository.save(p2);

            commentRepository.save(new Comment(null, "Produto interessante", p1));
            commentRepository.save(new Comment(null, "Comentário inicial do laboratório", p2));
        };
    }
}
