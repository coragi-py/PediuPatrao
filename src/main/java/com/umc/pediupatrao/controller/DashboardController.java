package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/indicadores")
    public ResponseEntity<Map<String, Object>> obterIndicadores(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String status) {

        validarDatas(dataInicio, dataFim);
        return ResponseEntity.ok(dashboardService.obterIndicadoresGerais(dataInicio, dataFim, status));
    }
    
    @GetMapping("/vendas-periodo")
    public ResponseEntity<List<Map<String, Object>>> obterVendasPorPeriodo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String status) {

        validarDatas(dataInicio, dataFim);
        return ResponseEntity.ok(dashboardService.obterVendasPorPeriodo(dataInicio, dataFim, status));
    }

    @GetMapping("/status-pedidos")
    public ResponseEntity<List<Map<String, Object>>> obterSituacaoPedidos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        validarDatas(dataInicio, dataFim);
        return ResponseEntity.ok(dashboardService.obterSituacaoPedidos(dataInicio, dataFim));
    }

    @GetMapping("/produtos-mais-vendidos")
    public ResponseEntity<List<Map<String, Object>>> obterProdutosMaisVendidos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "5") int limite) {

        validarDatas(dataInicio, dataFim);
        return ResponseEntity.ok(dashboardService.obterProdutosMaisVendidos(dataInicio, dataFim, status, limite));
    }

    private void validarDatas(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("A data inicial não pode ser posterior à data final.");
        }
    }
}