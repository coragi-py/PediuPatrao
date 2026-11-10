package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    public Produto salvar(Produto produto) {
        validarProduto(produto);

        if (produto.getId() == null || produto.getId().isBlank()) {
            produto.setId(null);
        }

        if (produto.getId() != null) {
            Optional<Produto> produtoExistente = produtoRepository.findById(produto.getId());

            if (produtoExistente.isEmpty()) {
                throw new IllegalArgumentException("Produto não encontrado para atualização.");
            }
        }

        if (produto.getAtivo() == null) {
            produto.setAtivo(true);
        }

        return produtoRepository.save(produto);
    }

    public List<Produto> listarProdutos() {
        return produtoRepository.findAll();
    }

    public List<Produto> listarProdutosAtivos() {
        return produtoRepository.findByAtivoTrueOrderByNomeAsc();
    }

    public Optional<Produto> buscarPorId(String id) {
        return produtoRepository.findById(id);
    }

    public void excluir(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("O identificador do produto é obrigatório.");
        }

        if (!produtoRepository.existsById(id)) {
            throw new IllegalArgumentException("Produto não encontrado.");
        }

        produtoRepository.deleteById(id);
    }

    private void validarProduto(Produto produto) {
        if (produto == null) {
            throw new IllegalArgumentException("Os dados do produto são obrigatórios.");
        }

        if (produto.getNome() == null || produto.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome do produto é obrigatório.");
        }

        if (produto.getTipo() == null || produto.getTipo().isBlank()) {
            throw new IllegalArgumentException("O tipo do produto é obrigatório.");
        }

        if (produto.getPreco() == null || produto.getPreco() <= 0) {
            throw new IllegalArgumentException("O preço do produto deve ser maior que zero.");
        }

        String nome = produto.getNome().trim();

        boolean nomeDuplicado = produtoRepository
                .findByNomeIgnoreCaseOrderByNomeAsc(nome)
                .stream()
                .anyMatch(produtoSalvo ->
                        produtoSalvo.getId() != null
                                && !produtoSalvo.getId().equals(produto.getId())
                );

        if (nomeDuplicado) {
            throw new IllegalArgumentException("Já existe um produto cadastrado com este nome.");
        }

        produto.setNome(nome);
        produto.setTipo(produto.getTipo().trim());
    }
}