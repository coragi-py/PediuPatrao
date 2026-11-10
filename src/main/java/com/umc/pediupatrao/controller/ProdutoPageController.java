package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/produtos")
@PreAuthorize("hasRole('GERENTE')")
public class ProdutoPageController {

    @Autowired
    private ProdutoService produtoService;

    @GetMapping
    public String listarProdutos(Model model) {
        model.addAttribute("produtos", produtoService.listarProdutos());
        return "produtos/lista";
    }

    @GetMapping("/novo")
    public String abrirFormularioNovoProduto(Model model) {
        model.addAttribute("produto", new Produto());
        return "produtos/produto-form";
    }

    @GetMapping("/editar/{id}")
    public String abrirFormularioEdicao(@PathVariable String id, Model model) {
        Produto produto = produtoService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));

        model.addAttribute("produto", produto);
        return "produtos/produto-form";
    }

    @PostMapping("/salvar")
    public String salvarProduto(
            @ModelAttribute Produto produto,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            produtoService.salvar(produto);

            redirectAttributes.addFlashAttribute(
                    "mensagemSucesso",
                    produto.getId() == null || produto.getId().isBlank()
                            ? "Produto cadastrado com sucesso."
                            : "Produto atualizado com sucesso."
            );

            return "redirect:/produtos";
        } catch (IllegalArgumentException e) {
            model.addAttribute("mensagemErro", e.getMessage());
            model.addAttribute("produto", produto);
            return "produtos/produto-form";
        }
    }

    @PostMapping("/excluir/{id}")
    public String excluirProduto(
            @PathVariable String id,
            RedirectAttributes redirectAttributes) {

        try {
            produtoService.excluir(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Produto excluído com sucesso.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        }

        return "redirect:/produtos";
    }
}