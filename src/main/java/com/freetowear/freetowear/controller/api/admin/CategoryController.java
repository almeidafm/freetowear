package com.freetowear.freetowear.controller.api.admin;

import com.freetowear.freetowear.model.Categoria;
import com.freetowear.freetowear.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/*
 * CategoriaController — manages product categories.
 * POST   /category/create ✔
 * GET    /category ⏳
 * GET    /category/{id} ⏳
 * PATCH  /category/{id} ✔
 * */
@Controller
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoriaRepository categoryRepository;

    @PostMapping("/create")
    public String createCategory(@RequestParam String nome) {
        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        categoryRepository.save(categoria);

        return "redirect:/";
    }

    @PatchMapping("/{id}")
    public String updateCategory(
            @PathVariable Integer id,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Boolean ativo
    ) {
        categoryRepository.findById(id).ifPresent(categoria -> {
            if (nome != null && !nome.isEmpty()) categoria.setNome(nome);
            if (ativo != null) categoria.setAtivo(ativo);

            categoryRepository.save(categoria);
        });

        return "redirect:/";
    }
}
