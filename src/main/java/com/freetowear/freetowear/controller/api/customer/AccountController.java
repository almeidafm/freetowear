package com.freetowear.freetowear.controller.api.customer;

import com.freetowear.freetowear.entity.Cliente;
import com.freetowear.freetowear.entity.Endereco;
import com.freetowear.freetowear.repository.ClienteRepository;
import com.freetowear.freetowear.repository.EnderecoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

/*
 * AccountController — manages customer account operations.
 * POST   /account/register ✔
 * POST   /account/{id}/address ✔
 * GET    /account/{id} ⏳
 * PATCH  /account/{id} ✔
 * PATCH  /account/{id}/email ⏳
 * PATCH  /account/{id}/password ⏳
 * PATCH  /account/password/reset ⏳
 * DELETE  /account/{id} ⏳
 * */
@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private ClienteRepository accountRepository;

    @Autowired
    private EnderecoRepository addressRepository;

    @PostMapping("/register")
    public String register(@Valid Cliente cliente, BindingResult result) {
        if (result.hasErrors()) return "redirect:/register";
        accountRepository.save(cliente);
        return "redirect:/";
    }

    @DeleteMapping("/{id}")
    public String deleteAccount(@PathVariable Integer id) {
        accountRepository.findById(id).ifPresent(cliente -> {
            cliente.setAtivo(false);
            accountRepository.save(cliente);
        });
        return "redirect:/";
    }

    @PatchMapping("/{id}")
    public String updateAccount(
            @PathVariable Integer id,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String dataNascimento,
            @RequestParam(required = false) String telefone
    ) {
        accountRepository.findById(id).ifPresent(cliente -> {
            if (nome != null && !nome.isEmpty()) cliente.setNome(nome);
            if (cpf != null && !cpf.isEmpty()) cliente.setCpf(cpf);
            if (dataNascimento != null && !dataNascimento.isEmpty())
                cliente.setData_nascimento(LocalDate.parse(dataNascimento));
            if (telefone != null && !telefone.isEmpty()) cliente.setTelefone(telefone);
            accountRepository.save(cliente);
        });
        return "redirect:/account/" + id;
    }

    @PostMapping("/{id}/address")
    public String addAddress(
            @PathVariable Integer id,
            @RequestParam String cep,
            @RequestParam String logradouro,
            @RequestParam(required = false) String numero,
            @RequestParam(required = false) String complemento,
            @RequestParam String bairro,
            @RequestParam String cidade,
            @RequestParam String uf,
            @RequestParam(required = false, defaultValue = "false") Boolean padrao
    ) {
        accountRepository.findById(id).ifPresent(cliente -> {
            Endereco endereco = new Endereco();
            endereco.setCliente(cliente);
            endereco.setCep(cep);
            endereco.setLogradouro(logradouro);
            endereco.setNumero(numero);
            endereco.setComplemento(complemento);
            endereco.setBairro(bairro);
            endereco.setCidade(cidade);
            endereco.setUf(uf);
            endereco.setPadrao(padrao);
            addressRepository.save(endereco);
        });
        return "redirect:/account/" + id;
    }
}