package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.ItemPedido;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private AuditoriaService auditoriaService;

    @Transactional
    public Pedido criarPedido(Pedido pedido) {
        if (pedido.getItens() == null || pedido.getItens().isEmpty()) {
            throw new IllegalArgumentException("O pedido deve conter pelo menos um item.");
        }

        List<Produto> produtosCadastrados = produtoRepository.findAll();
        double totalCalculadoNoServidor = 0.0;

        for (ItemPedido item : pedido.getItens()) {
            if (item.getQuantidade() == null || item.getQuantidade() <= 0) {
                throw new IllegalArgumentException("A quantidade do item deve ser maior que zero.");
            }

            // Busca o produto no banco pelo ID ou pelo Nome/Sabor para obter o preço oficial
            Produto produtoOficial = produtosCadastrados.stream()
                    .filter(p -> (p.getId() != null && p.getId().equals(item.getSabor())) ||
                                 (p.getNome() != null && p.getNome().equalsIgnoreCase(item.getSabor())))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Produto não encontrado no catálogo oficial: " + item.getSabor()));

            // CORREÇÃO: Ignora qualquer preço enviado pelo cliente e força o preço oficial do banco
            double precoOficial = produtoOficial.getPreco();
            item.setSabor(produtoOficial.getNome());
            item.setPrecoUnitario(precoOficial);

            totalCalculadoNoServidor += item.getQuantidade() * precoOficial;
        }

        // CORREÇÃO: Ignora qualquer desconto enviado na criação do pedido (sempre inicia zerado)
        pedido.setDesconto(0.0);
        pedido.setValorTotal(totalCalculadoNoServidor);
        pedido.setStatus("RECEBIDO");
        pedido.setDataHoraEntrada(LocalDateTime.now());
        pedido.setResponsavelRegistro(getUsuarioLogado());

        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        auditoriaService.registrar(
                "CRIAÇÃO",
                "Pedido",
                pedidoSalvo.getId(),
                "valorTotal",
                "0.0",
                String.valueOf(totalCalculadoNoServidor),
                "Registro de entrada com preço validado via catálogo"
        );
        return pedidoSalvo;
    }

    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAll();
    }

    @Transactional
    public Pedido atualizarStatus(String id, String novoStatus) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado"));
        String statusAtual = pedido.getStatus();

        if ("FINALIZADO".equals(statusAtual) || "CANCELADO".equals(statusAtual)) {
            throw new IllegalStateException("Não é possível alterar o status de um pedido Finalizado ou Cancelado.");
        }

        boolean transicaoValida = false;
        if ("RECEBIDO".equals(statusAtual) && "EM PREPARAÇÃO".equals(novoStatus)) {
            transicaoValida = true;
        } else if ("EM PREPARAÇÃO".equals(statusAtual) && "PRONTO".equals(novoStatus)) {
            transicaoValida = true;
        } else if ("PRONTO".equals(statusAtual) && ("SAIU PARA ENTREGA".equals(novoStatus) || "RETIRADO".equals(novoStatus))) {
            transicaoValida = true;
            pedido.setDataHoraSaida(LocalDateTime.now());
            pedido.setResponsavelSaida(getUsuarioLogado());
        } else if (("SAIU PARA ENTREGA".equals(statusAtual) || "RETIRADO".equals(statusAtual)) && "FINALIZADO".equals(novoStatus)) {
            transicaoValida = true;
        }

        if (!transicaoValida) {
            throw new IllegalStateException("Transição inválida de '" + statusAtual + "' para '" + novoStatus + "'");
        }

        pedido.setStatus(novoStatus);
        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        auditoriaService.registrar("ALTERAÇÃO DE STATUS", "Pedido", pedidoSalvo.getId(), "status", statusAtual, novoStatus, null);
        return pedidoSalvo;
    }

    @Transactional
    public Pedido aplicarDesconto(String id, Double percentualDesconto, String autorizador) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado"));
        String status = pedido.getStatus();

        if ("SAIU PARA ENTREGA".equals(status) || "RETIRADO".equals(status) || "FINALIZADO".equals(status) || "CANCELADO".equals(status)) {
            throw new IllegalStateException("Não é possível aplicar desconto neste estágio do pedido.");
        }

        if (percentualDesconto == null || percentualDesconto < 0 || percentualDesconto > 20) {
            throw new IllegalArgumentException("Desconto inválido. O limite máximo permitido é de 20%.");
        }

        double totalItens = 0.0;
        if (pedido.getItens() != null) {
            for (ItemPedido item : pedido.getItens()) {
                totalItens += item.getQuantidade() * item.getPrecoUnitario();
            }
        }

        double valorDesconto = totalItens * (percentualDesconto / 100.0);
        Double descontoAnterior = pedido.getDesconto() != null ? pedido.getDesconto() : 0.0;

        pedido.setDesconto(valorDesconto);
        pedido.setValorTotal(totalItens - valorDesconto);

        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        String justificativa = "Desconto de " + percentualDesconto + "% aplicado.";
        if (autorizador != null && !autorizador.isBlank()) {
            justificativa += " (Autorizado pelo Gerente: " + autorizador + ")";
        }

        auditoriaService.registrar("APLICAÇÃO DE DESCONTO", "Pedido", pedidoSalvo.getId(), "desconto",
                String.valueOf(descontoAnterior), String.valueOf(valorDesconto), justificativa);
        return pedidoSalvo;
    }

    @Transactional
    public Pedido cancelarPedido(String id, String justificativaFront, String autorizador) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado"));
        String status = pedido.getStatus();

        if ("SAIU PARA ENTREGA".equals(status) || "RETIRADO".equals(status) || "FINALIZADO".equals(status) || "CANCELADO".equals(status)) {
            throw new IllegalStateException("Não é possível cancelar um pedido que já saiu para entrega ou foi concluído.");
        }

        if (justificativaFront == null || justificativaFront.trim().isEmpty()) {
            throw new IllegalArgumentException("O cancelamento exige uma justificativa obrigatória.");
        }

        String statusAnterior = pedido.getStatus();
        String justificativaFinal = justificativaFront.trim();
        if (autorizador != null && !autorizador.isBlank()) {
            justificativaFinal += " (Autorizado pelo Gerente: " + autorizador + ")";
        }

        pedido.setStatus("CANCELADO");
        pedido.setJustificativaCancelamento(justificativaFinal);

        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        auditoriaService.registrar("CANCELAMENTO", "Pedido", pedidoSalvo.getId(), "status", statusAnterior, "CANCELADO", justificativaFinal);
        return pedidoSalvo;
    }

    public String getUsuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "sistema";
    }
}