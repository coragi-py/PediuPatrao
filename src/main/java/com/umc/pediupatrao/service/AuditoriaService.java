package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Auditoria;
import com.umc.pediupatrao.repository.AuditoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class AuditoriaService {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    public void registrar(String tipoOperacao, String entidade, String idEntidade, String campo, String valorAnt, String valorNov, String justificativa) {
        Auditoria log = new Auditoria();
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            log.setUsuario(auth.getName());
            String roles = auth.getAuthorities().stream()
                    .map(grantedAuthority -> grantedAuthority.getAuthority())
                    .collect(Collectors.joining(", "));
            log.setPerfilUsuario(roles);
        } else {
            log.setUsuario("SISTEMA");
            log.setPerfilUsuario("N/A");
        }

        log.setDataHora(LocalDateTime.now());
        log.setTipoOperacao(tipoOperacao);
        log.setEntidadeAfetada(entidade);
        log.setIdentificadorEntidade(idEntidade);
        log.setCampoAlterado(campo);
        log.setValorAnterior(valorAnt);
        log.setNovoValor(valorNov);
        log.setJustificativa(justificativa);

        auditoriaRepository.save(log);
    }
}