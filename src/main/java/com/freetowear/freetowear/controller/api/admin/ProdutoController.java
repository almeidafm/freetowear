package com.freetowear.freetowear.controller.api.admin;

import com.freetowear.freetowear.model.Produto;
import com.freetowear.freetowear.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

/*
 * ProductController — manages products.
 * POST   /produto/create
 * GET    /produto
 * GET    /produto/{id}
 * PATCH  /produto/{id}
 * */

@Controller
public class ProdutoController {

    @Autowired
    private ProdutoRepository produtoRepository;

    @GetMapping("/produtos")
    public String listarProdutos(Model model) {

        List<Produto> produtos = produtoRepository.findByAtivoTrue();

        model.addAttribute("produtos", produtos);

        return "produtos";
    }
}
