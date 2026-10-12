package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;
    
    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping
    @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE')") 
    public Pedido criarPedido(@RequestBody Pedido pedido) {
        return pedidoService.criarPedido(pedido);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'GERENTE')")
    public List<Pedido> listarPedidos() {
        return pedidoService.listarPedidos();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE')")
    public Pedido atualizarStatus(@PathVariable String id, @RequestParam String status) {
        return pedidoService.atualizarStatus(id, status);
    }
    
    @PutMapping("/{id}/desconto")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE')") 
    public Pedido aplicarDesconto(
            @PathVariable String id, 
            @RequestParam Double percentual,
            @RequestParam(required = false) String usernameGerente,
            @RequestParam(required = false) String senhaGerente) {
        
        validarAutorizacaoGerente(usernameGerente, senhaGerente);
        return pedidoService.aplicarDesconto(id, percentual, usernameGerente);
    }
    
    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE')")
    public Pedido cancelarPedido(
            @PathVariable String id, 
            @RequestParam String justificativa,
            @RequestParam(required = false) String usernameGerente,
            @RequestParam(required = false) String senhaGerente) {
        
        validarAutorizacaoGerente(usernameGerente, senhaGerente);
        return pedidoService.cancelarPedido(id, justificativa, usernameGerente);
    }
    
    private void validarAutorizacaoGerente(String username, String senha) {
        Authentication authAtual = SecurityContextHolder.getContext().getAuthentication();
        boolean jaEhGerente = authAtual.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_GERENTE") || a.getAuthority().equals("ROLE_ADMIN"));
        
        if (jaEhGerente) return; // Se for o Gerente logado, passa direto sem pedir senha

        if (username == null || senha == null || username.isEmpty() || senha.isEmpty()) {
            throw new RuntimeException("Autorização do Gerente é obrigatória para o Atendente realizar esta ação.");
        }

        try {
            // Tenta fazer o login silencioso só para validar a senha
            Authentication authGerente = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, senha)
            );
            boolean isGerente = authGerente.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_GERENTE") || a.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isGerente) {
                throw new RuntimeException("O usuário informado não possui perfil de GERENTE.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Credenciais do Gerente inválidas! Acesso Negado.");
        }
    }
    
}
