package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @GetMapping
    public ResponseEntity<List<Produto>> listarProdutos() {
        return ResponseEntity.ok(produtoService.listarProdutos());
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<Produto>> listarProdutosAtivos() {
        return ResponseEntity.ok(produtoService.listarProdutosAtivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Produto> buscarPorId(@PathVariable String id) {
        return produtoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<?> salvarProduto(@RequestBody Produto produto) {
        try {
            Produto salvo = produtoService.salvar(produto);
            return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<?> atualizarProduto(
            @PathVariable String id,
            @RequestBody Produto produto) {

        try {
            produto.setId(id);
            Produto atualizado = produtoService.salvar(produto);
            return ResponseEntity.ok(atualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<?> excluirProduto(@PathVariable String id) {
        try {
            produtoService.excluir(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}