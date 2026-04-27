package com.freetowear.freetowear.repository;

import com.freetowear.freetowear.model.Cliente;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ClienteRepository extends CrudRepository<Cliente, Integer> {
    Optional<Cliente> findByEmailAndSenhaHash(String email, String senhaHash);
    Optional<Cliente> findByEmail(String email);
}


