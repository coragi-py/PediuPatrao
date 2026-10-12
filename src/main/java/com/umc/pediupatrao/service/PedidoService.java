package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.ItemPedido;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private AuditoriaService auditoriaService;

    public Pedido criarPedido(Pedido pedido) {
        double total = 0.0;
        if (pedido.getItens() != null) {
            for (ItemPedido item : pedido.getItens()) {
                total += item.getQuantidade() * item.getPrecoUnitario();
            }
        }
        
        if (pedido.getDesconto() == null) pedido.setDesconto(0.0);
        pedido.setValorTotal(total - pedido.getDesconto());

        pedido.setStatus("RECEBIDO");
        pedido.setDataHoraEntrada(LocalDateTime.now());
        pedido.setResponsavelRegistro(getUsuarioLogado());

        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        auditoriaService.registrar("CRIAÇÃO", "Pedido", pedidoSalvo.getId(), "Todos", "N/A", "Novo Pedido", "Registro de entrada");
        return pedidoSalvo;
    }

    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAll();
    }

    public Pedido atualizarStatus(String id, String novoStatus) {
        Pedido pedido = pedidoRepository.findById(id).orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        String statusAtual = pedido.getStatus();

        if ("FINALIZADO".equals(statusAtual) || "CANCELADO".equals(statusAtual)) {
            throw new RuntimeException("Não é possível alterar o status de um pedido Finalizado ou Cancelado.");
        }

        boolean transicaoValida = false;
        if ("RECEBIDO".equals(statusAtual) && "EM PREPARAÇÃO".equals(novoStatus)) transicaoValida = true;
        else if ("EM PREPARAÇÃO".equals(statusAtual) && "PRONTO".equals(novoStatus)) transicaoValida = true;
        else if ("PRONTO".equals(statusAtual) && ("SAIU PARA ENTREGA".equals(novoStatus) || "RETIRADO".equals(novoStatus))) {
            transicaoValida = true;
            pedido.setDataHoraSaida(LocalDateTime.now());
            pedido.setResponsavelSaida(getUsuarioLogado());
        }
        else if (("SAIU PARA ENTREGA".equals(statusAtual) || "RETIRADO".equals(statusAtual)) && "FINALIZADO".equals(novoStatus)) {
            transicaoValida = true;
        }

        if (!transicaoValida) {
            throw new RuntimeException("Transição inválida de '" + statusAtual + "' para '" + novoStatus + "'");
        }

        pedido.setStatus(novoStatus);
        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        auditoriaService.registrar("ALTERAÇÃO DE STATUS", "Pedido", pedidoSalvo.getId(), "status", statusAtual, novoStatus, null);
        return pedidoSalvo;
    }

    public Pedido aplicarDesconto(String id, Double percentualDesconto, String autorizador) {
        Pedido pedido = pedidoRepository.findById(id).orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        String status = pedido.getStatus();

        if ("SAIU PARA ENTREGA".equals(status) || "RETIRADO".equals(status) || "FINALIZADO".equals(status) || "CANCELADO".equals(status)) {
            throw new RuntimeException("Não é possível aplicar desconto neste estágio do pedido.");
        }

        if (percentualDesconto < 0 || percentualDesconto > 20) {
            throw new RuntimeException("Desconto inválido. O limite máximo é de 20%.");
        }

        double totalItens = 0.0;
        if (pedido.getItens() != null) {
            for (ItemPedido item : pedido.getItens()) {
                totalItens += item.getQuantidade() * item.getPrecoUnitario();
            }
        }

        double valorDesconto = totalItens * (percentualDesconto / 100.0);
        Double descontoAnterior = pedido.getDesconto();
        
        pedido.setDesconto(valorDesconto);
        pedido.setValorTotal(totalItens - valorDesconto);

        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        // Prepara a justificativa evidenciando quem autorizou a ação
        String justificativa = "Desconto de " + percentualDesconto + "% aplicado.";
        if (autorizador != null && !autorizador.isEmpty()) {
            justificativa += " (Ação física autorizada por: " + autorizador + ")";
        }

        auditoriaService.registrar("APLICAÇÃO DE DESCONTO", "Pedido", pedidoSalvo.getId(), "desconto", String.valueOf(descontoAnterior), String.valueOf(valorDesconto), justificativa);
        return pedidoSalvo;
    }

    public Pedido cancelarPedido(String id, String justificativaFront, String autorizador) {
        Pedido pedido = pedidoRepository.findById(id).orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        String status = pedido.getStatus();

        if ("SAIU PARA ENTREGA".equals(status) || "RETIRADO".equals(status) || "FINALIZADO".equals(status) || "CANCELADO".equals(status)) {
            throw new RuntimeException("Não é possível cancelar um pedido que já saiu para entrega ou foi concluído.");
        }

        if (justificativaFront == null || justificativaFront.trim().isEmpty()) {
            throw new RuntimeException("O cancelamento exige uma justificativa obrigatória.");
        }

        String statusAnterior = pedido.getStatus();
        
        String justificativaFinal = justificativaFront;
        if (autorizador != null && !autorizador.isEmpty()) {
            justificativaFinal += " (Ação física autorizada por: " + autorizador + ")";
        }

        pedido.setStatus("CANCELADO");
        pedido.setJustificativaCancelamento(justificativaFinal);

        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        auditoriaService.registrar("CANCELAMENTO", "Pedido", pedidoSalvo.getId(), "status", statusAnterior, "CANCELADO", justificativaFinal);
        return pedidoSalvo;
    }

    public String getUsuarioLogado() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}