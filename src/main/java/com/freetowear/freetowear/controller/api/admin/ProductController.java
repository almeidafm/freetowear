package com.freetowear.freetowear.controller.api.admin;

import com.freetowear.freetowear.entity.Categoria;
import com.freetowear.freetowear.entity.Produto;
import com.freetowear.freetowear.entity.ProdutoVariacao;
import com.freetowear.freetowear.repository.CategoriaRepository;
import com.freetowear.freetowear.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/*
 * ProductController — manages products.
 * POST   /produto/create
 * GET    /produto
 * GET    /produto/{id}
 * PATCH  /produto/{id}
 * */
@Controller
@RequestMapping("/product")
public class ProductController {

    private final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    @Autowired
    private ProdutoRepository productRepository;

    @Autowired
    private CategoriaRepository categoryRepository;

    @PostMapping("/create")
    public String createProduct(
            @RequestParam String nome,
            @RequestParam(required = false) String descricao,
            @RequestParam BigDecimal preco,
            @RequestParam String cor,
            @RequestParam ProdutoVariacao.Tamanho tamanho,
            @RequestParam Integer estoque,
            @RequestParam Integer categoriaId,
            @RequestParam("imagem") MultipartFile imagem
    ) {

        try {
            String nomeArquivo = UUID.randomUUID() + "_" + imagem.getOriginalFilename();
            Path caminho = Paths.get(UPLOAD_DIR + nomeArquivo);

            Files.createDirectories(caminho.getParent());
            Files.write(caminho, imagem.getBytes());

            String imagemPath = "uploads/" + nomeArquivo;

            Optional<Categoria> categoriaOpt = categoryRepository.findById(categoriaId);
            if (categoriaOpt.isEmpty()) {
                return "redirect:/erro";
            }

            Produto produto = new Produto();
            produto.setNome(nome);
            produto.setDescricao(descricao);
            produto.setPreco(preco);
            produto.setImagemUrl(imagemPath);
            produto.setCategoria(categoriaOpt.get());

            ProdutoVariacao variacao = new ProdutoVariacao();
            variacao.setCor(cor);
            variacao.setTamanho(tamanho);
            variacao.setEstoque(estoque);
            variacao.setProduto(produto);

            produto.setVariacoes(List.of(variacao));

            productRepository.save(produto);

        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/erro";
        }

        return "redirect:/";
    }

    @GetMapping
    public String listProducts(Model model) {

        List<Produto> produtos = productRepository.findByAtivoTrue();

        model.addAttribute("produtos", produtos);

        return "produtos";
    }

    @GetMapping("/{id}")
    public String showProduct(@PathVariable Integer id, Model model) {

        Produto produto = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        model.addAttribute("produto", produto);

        return "produto";
    }
}