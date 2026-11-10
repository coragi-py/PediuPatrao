package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.ItemPedido;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private PedidoRepository pedidoRepository;

    private List<Pedido> filtrarPedidos(LocalDate dataInicio, LocalDate dataFim, String status) {
        return pedidoRepository.findAll().stream()
                .filter(p -> {
                    if (dataInicio != null && (p.getDataHoraEntrada() == null
                            || p.getDataHoraEntrada().toLocalDate().isBefore(dataInicio))) {
                        return false;
                    }

                    if (dataFim != null && (p.getDataHoraEntrada() == null
                            || p.getDataHoraEntrada().toLocalDate().isAfter(dataFim))) {
                        return false;
                    }

                    if (status != null && !status.isBlank() && !"TODOS".equalsIgnoreCase(status)) {
                        return status.equalsIgnoreCase(p.getStatus());
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    // RF01 — Indicadores Gerais de Vendas
    public Map<String, Object> obterIndicadoresGerais(LocalDate dataInicio, LocalDate dataFim, String status) {
        List<Pedido> filtrados = filtrarPedidos(dataInicio, dataFim, status);

        long totalPedidos = filtrados.size();

        long pedidosCancelados = filtrados.stream()
                .filter(p -> "CANCELADO".equalsIgnoreCase(p.getStatus()))
                .count();

        List<Pedido> pedidosValidos = filtrados.stream()
                .filter(p -> !"CANCELADO".equalsIgnoreCase(p.getStatus()))
                .collect(Collectors.toList());

        double faturamentoTotal = pedidosValidos.stream()
                .mapToDouble(p -> p.getValorTotal() != null ? p.getValorTotal() : 0.0)
                .sum();

        double totalDescontos = pedidosValidos.stream()
                .mapToDouble(p -> p.getDesconto() != null ? p.getDesconto() : 0.0)
                .sum();

        double ticketMedio = pedidosValidos.isEmpty()
                ? 0.0
                : faturamentoTotal / pedidosValidos.size();

        double taxaCancelamento = totalPedidos == 0
                ? 0.0
                : ((double) pedidosCancelados / totalPedidos) * 100.0;

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("totalPedidos", totalPedidos);
        resposta.put("pedidosValidos", pedidosValidos.size());
        resposta.put("pedidosCancelados", pedidosCancelados);
        resposta.put("faturamentoTotal", Math.round(faturamentoTotal * 100.0) / 100.0);
        resposta.put("ticketMedio", Math.round(ticketMedio * 100.0) / 100.0);
        resposta.put("totalDescontos", Math.round(totalDescontos * 100.0) / 100.0);
        resposta.put("taxaCancelamentoPercentual", Math.round(taxaCancelamento * 100.0) / 100.0);

        return resposta;
    }

    // RF02 — Análise de Vendas por Período (Gráfico Temporal)
    public List<Map<String, Object>> obterVendasPorPeriodo(LocalDate dataInicio, LocalDate dataFim, String status) {
        List<Pedido> pedidosValidos = filtrarPedidos(dataInicio, dataFim, status).stream()
                .filter(p -> !"CANCELADO".equalsIgnoreCase(p.getStatus()) && p.getDataHoraEntrada() != null)
                .collect(Collectors.toList());

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        Map<LocalDate, List<Pedido>> agrupadoPorDia = pedidosValidos.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getDataHoraEntrada().toLocalDate(),
                        TreeMap::new,
                        Collectors.toList()
                ));

        List<Map<String, Object>> serieTemporal = new ArrayList<>();

        for (Map.Entry<LocalDate, List<Pedido>> entry : agrupadoPorDia.entrySet()) {
            double receitaDia = entry.getValue().stream()
                    .mapToDouble(p -> p.getValorTotal() != null ? p.getValorTotal() : 0.0)
                    .sum();

            Map<String, Object> ponto = new LinkedHashMap<>();
            ponto.put("data", entry.getKey().format(fmt));
            ponto.put("quantidadePedidos", entry.getValue().size());
            ponto.put("faturamento", Math.round(receitaDia * 100.0) / 100.0);

            serieTemporal.add(ponto);
        }

        return serieTemporal;
    }

    // RF03 — Situação dos Pedidos
    public List<Map<String, Object>> obterSituacaoPedidos(LocalDate dataInicio, LocalDate dataFim) {
        List<Pedido> filtrados = filtrarPedidos(dataInicio, dataFim, null);
        long total = filtrados.size();

        List<String> statusPadrao = Arrays.asList(
                "RECEBIDO",
                "EM PREPARAÇÃO",
                "PRONTO",
                "SAIU PARA ENTREGA",
                "RETIRADO",
                "FINALIZADO",
                "CANCELADO"
        );

        Map<String, Long> contagem = filtrados.stream()
                .filter(p -> p.getStatus() != null)
                .collect(Collectors.groupingBy(Pedido::getStatus, Collectors.counting()));

        List<Map<String, Object>> resultado = new ArrayList<>();

        for (String st : statusPadrao) {
            long qtd = contagem.getOrDefault(st, 0L);
            double percentual = total == 0 ? 0.0 : ((double) qtd / total) * 100.0;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("status", st);
            item.put("quantidade", qtd);
            item.put("percentual", Math.round(percentual * 100.0) / 100.0);

            resultado.add(item);
        }

        return resultado;
    }

    // RF04 — Produtos Mais Vendidos (Ranking)
    public List<Map<String, Object>> obterProdutosMaisVendidos(
            LocalDate dataInicio, LocalDate dataFim, String status, int limite) {

        List<Pedido> pedidosValidos = filtrarPedidos(dataInicio, dataFim, status).stream()
                .filter(p -> !"CANCELADO".equalsIgnoreCase(p.getStatus()) && p.getItens() != null)
                .collect(Collectors.toList());

        Map<String, int[]> qtdMap = new HashMap<>();
        Map<String, Double> receitaMap = new HashMap<>();

        for (Pedido p : pedidosValidos) {
            for (ItemPedido item : p.getItens()) {
                String nome = item.getSabor() != null ? item.getSabor() : "Item sem nome";
                int qtd = item.getQuantidade() != null ? item.getQuantidade() : 0;
                double preco = item.getPrecoUnitario() != null ? item.getPrecoUnitario() : 0.0;

                qtdMap.computeIfAbsent(nome, k -> new int[]{0})[0] += qtd;
                receitaMap.put(
                        nome,
                        receitaMap.getOrDefault(nome, 0.0) + (qtd * preco)
                );
            }
        }

        return qtdMap.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue()[0], a.getValue()[0]))
                .limit(limite > 0 ? limite : 5)
                .map(entry -> {
                    Map<String, Object> prod = new LinkedHashMap<>();
                    prod.put("produto", entry.getKey());
                    prod.put("quantidadeVendida", entry.getValue()[0]);
                    prod.put("receitaGerada", Math.round(
                            receitaMap.getOrDefault(entry.getKey(), 0.0) * 100.0
                    ) / 100.0);
                    return prod;
                })
                .collect(Collectors.toList());
    }
}