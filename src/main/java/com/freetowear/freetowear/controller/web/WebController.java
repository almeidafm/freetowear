package com.freetowear.freetowear.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/*
 * WebController — maps GET routes to Thymeleaf templates. No business logic.
 * */

@Controller
public class WebController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/camisaslisas")
    public String camisasLisas() {
        return "camisaslisas";
    }

    @GetMapping("/trabalhos")
    public String trabalhos() {
        return "trabalhos";
    }

    @GetMapping("/carrinho")
    public String carrinho() {
        return "carrinho";
    }

    @GetMapping("/confirmacao")
    public String confirmacao() {
        return "confirmacao";
    }

    @GetMapping("/contato")
    public String contato() {
        return "contato";
    }

    @GetMapping("/comprar")
    public String comprar() {
        return "comprar";
    }

    @GetMapping("/finalizar")
    public String finalizar() {
        return "finalizar";
    }

    @GetMapping("/sobrenos")
    public String sobreNos() {
        return "sobrenos";
    }
}
