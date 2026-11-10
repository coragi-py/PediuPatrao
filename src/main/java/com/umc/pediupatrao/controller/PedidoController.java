package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'ATENDENTE')")
    public List<Pedido> listarPedidos() {
        return pedidoService.listarPedidos();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE')")
    public Pedido atualizarStatus(@PathVariable String id, @RequestParam String status) {
        return pedidoService.atualizarStatus(id, status);
    }

    // Exclusivo do GERENTE (diretamente logado ou via autorização de supervisor no caixa)
    @PutMapping("/{id}/desconto")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE')")
    public Pedido aplicarDesconto(
            @PathVariable String id,
            @RequestParam Double percentual,
            @RequestParam(required = false) String usernameGerente,
            @RequestParam(required = false) String senhaGerente) {

        validarAutorizacaoExclusivaGerente(usernameGerente, senhaGerente);
        return pedidoService.aplicarDesconto(id, percentual, usernameGerente);
    }

    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE')")
    public Pedido cancelarPedido(
            @PathVariable String id,
            @RequestParam String justificativa,
            @RequestParam(required = false) String usernameGerente,
            @RequestParam(required = false) String senhaGerente) {

        validarAutorizacaoExclusivaGerente(usernameGerente, senhaGerente);
        return pedidoService.cancelarPedido(id, justificativa, usernameGerente);
    }

    private void validarAutorizacaoExclusivaGerente(String username, String senha) {
        Authentication authAtual = SecurityContextHolder.getContext().getAuthentication();
        
        // CORREÇÃO: Exclusividade estrita do GERENTE (ADMIN não tem permissão)
        boolean jaEhGerente = authAtual.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_GERENTE"));

        if (jaEhGerente) return;

        if (username == null || senha == null || username.isBlank() || senha.isBlank()) {
            throw new AccessDeniedException("Ação exclusiva de GERENTE. Informe as credenciais de autorização.");
        }

        try {
            Authentication authGerente = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, senha)
            );
            boolean isGerente = authGerente.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_GERENTE"));

            if (!isGerente) {
                throw new AccessDeniedException("O usuário autorizador informado NÃO possui perfil de GERENTE.");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new AccessDeniedException("Credenciais de GERENTE inválidas. Operação bloqueada.");
        }
    }
}