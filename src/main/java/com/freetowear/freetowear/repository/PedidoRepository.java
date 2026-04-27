package com.freetowear.freetowear.repository;

import com.freetowear.freetowear.model.Cliente;
import com.freetowear.freetowear.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    List<Pedido> findByCliente(Cliente cliente);
}
