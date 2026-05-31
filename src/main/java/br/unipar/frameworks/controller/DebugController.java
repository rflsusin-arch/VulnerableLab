package br.unipar.frameworks.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @GetMapping("/config")
    public Map<String, String> config() {
        return Map.of(
                "database", "H2 em memória",
                "h2Console", "/h2-console",
                "profile", "lab",
                "warning", "Endpoint"
        );
    }

    @GetMapping("/error-example")
    public String errorExample() {
        throw new RuntimeException("Erro interno simulado: falha ao consultar tabela");
    }
}
