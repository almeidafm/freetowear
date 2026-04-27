package com.freetowear.freetowear.repository;

import com.freetowear.freetowear.model.Categoria;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends CrudRepository<Categoria, Integer> {

    Optional<Categoria> findByNome(String nome);

    List<Categoria> findByNomeContainingIgnoreCase(String nome);

    List<Categoria> findByAtivoTrue();

}