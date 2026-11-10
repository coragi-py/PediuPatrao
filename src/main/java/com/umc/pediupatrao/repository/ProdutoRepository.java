package com.umc.pediupatrao.repository;

import com.umc.pediupatrao.entity.Produto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends MongoRepository<Produto, String> {

    List<Produto> findByAtivoTrueOrderByNomeAsc();

    List<Produto> findByNomeIgnoreCaseOrderByNomeAsc(String nome);
}