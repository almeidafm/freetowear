package com.freetowear.freetowear.repository;

import com.freetowear.freetowear.model.Produto;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProdutoRepository extends CrudRepository<Produto, Integer> {

    List<Produto> findByAtivoTrue();

}