package com.freetowear.freetowear.controller.api.customer;

import com.freetowear.freetowear.model.Cliente;
import com.freetowear.freetowear.model.Endereco;
import com.freetowear.freetowear.repository.ClienteRepository;
import com.freetowear.freetowear.repository.EnderecoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Optional;

/*
 * ClientController — manages customer account operations.
 * POST   /account/register
 * POST   /account/login
 * POST   /account/logout
 * POST   /account/delete
 * POST   /account/address
 * GET    /account
 * PATCH  /account
 * PATCH  /account/email
 * PATCH  /account/password
 * PATCH  /account/password/reset
 * */

@Controller
public class ClienteController {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EnderecoRepository enderecoRepository;

    @RequestMapping(value = "/cadastrar", method = RequestMethod.POST)
    public String cadastrarCliente(@Valid Cliente cliente, BindingResult result){

        if(result.hasErrors()){
            return "redirect:/cadastroCliente";
        }

        clienteRepository.save(cliente);

        return "redirect:/";
    }

    @PostMapping("/login")
    public String login(@RequestParam("email") String email,
                        @RequestParam("senhaHash") String senhaHash,
                        Model model) {

        Optional<Cliente> cliente = clienteRepository.findByEmailAndSenhaHash(email, senhaHash);

        if (cliente.isPresent()) {
            String nomeCliente = cliente.get().getNome();

            model.addAttribute("nome", nomeCliente);
            model.addAttribute("mensagem", "Bem-vindo, " + nomeCliente + "!");
            return "login"; // retorna para a view "login.html" com a mensagem
        } else {
            model.addAttribute("erro", "Email ou senha incorretos");
            return "login";
        }
    }

    @PostMapping("/deletarconta")
    public String deletarCliente(@RequestParam("email") String email,
                                 @RequestParam("senhaHash") String senhaHash,
                                 Model model) {
        Optional<Cliente> cliente = clienteRepository.findByEmailAndSenhaHash(email, senhaHash);

        if (cliente.isPresent()) {
            clienteRepository.delete(cliente.get());
            model.addAttribute("mensagem", "Conta deletada com sucesso!");
            return "deletarconta";
        } else {
            model.addAttribute("erro", "Email ou senha incorretos!");
            return "deletarconta";
        }
    }

    @PostMapping("/atualizarcadastro")
    public String atualizarCadastro(
            @RequestParam("email") String email,
            @RequestParam("senhaHash") String senhaHash,
            @RequestParam("nome") String nome,
            @RequestParam("cpf") String cpf,
            @RequestParam("data_nascimento") String dataNascimento,
            @RequestParam("telefone") String telefone,
            Model model) {

        Optional<Cliente> clienteOpt = clienteRepository.findByEmailAndSenhaHash(email, senhaHash);

        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();

            cliente.setNome(nome);
            cliente.setCpf(cpf);
            cliente.setData_nascimento(java.time.LocalDate.parse(dataNascimento));
            cliente.setTelefone(telefone);

            clienteRepository.save(cliente);

            model.addAttribute("mensagem", "Cadastro atualizado com sucesso!");
            return "atualizarcadastro";

        } else {
            model.addAttribute("erro", "Email ou senha incorretos!");
            return "atualizarcadastro";
        }
    }

    // =========================
    // ADICIONAR ENDEREÇO
    // =========================
    @PostMapping("/endereco/adicionar")
    public String adicionarEndereco(
            @RequestParam Integer idCliente,
            @RequestParam String cep,
            @RequestParam String logradouro,
            @RequestParam(required = false) String numero,
            @RequestParam(required = false) String complemento,
            @RequestParam String bairro,
            @RequestParam String cidade,
            @RequestParam String uf,
            @RequestParam(required = false, defaultValue = "false") Boolean padrao,
            Model model
    ) {

        Optional<Cliente> clienteOpt = clienteRepository.findById(idCliente);

        if (clienteOpt.isEmpty()) {
            model.addAttribute("erro", "Cliente não encontrado");
            return "erro";
        }

        Endereco endereco = new Endereco();
        endereco.setCliente(clienteOpt.get());
        endereco.setCep(cep);
        endereco.setLogradouro(logradouro);
        endereco.setNumero(numero);
        endereco.setComplemento(complemento);
        endereco.setBairro(bairro);
        endereco.setCidade(cidade);
        endereco.setUf(uf);
        endereco.setPadrao(padrao);

        enderecoRepository.save(endereco);

        model.addAttribute("mensagem", "Endereço adicionado com sucesso");
        return "redirect:/";
    }

}
