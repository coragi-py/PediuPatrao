package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.entity.Auditoria;
import com.umc.pediupatrao.repository.AuditoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    // Apenas ADMIN e GERENTE podem consultar a auditoria
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public List<Auditoria> listarAuditoria() {
        return auditoriaRepository.findAll();
    }
}