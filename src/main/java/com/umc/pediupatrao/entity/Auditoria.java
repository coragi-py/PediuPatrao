package com.umc.pediupatrao.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "auditoria")
public class Auditoria {
    @Id
    private String id;
    private String usuario;
    private String perfilUsuario;
    private LocalDateTime dataHora;
    private String tipoOperacao;
    private String entidadeAfetada;
    private String identificadorEntidade;
    private String campoAlterado;
    private String valorAnterior;
    private String novoValor;
    private String justificativa;

    // --- GETTERS E SETTERS ---
    public String getId() { 
        return id; 
    }
    public void setId(String id) { 
        this.id = id; 
    }
    
    public String getUsuario() { 
        return usuario; 
    }
    public void setUsuario(String usuario) { 
        this.usuario = usuario; 
    }
    
    public String getPerfilUsuario() { 
        return perfilUsuario; 
    }
    public void setPerfilUsuario(String perfilUsuario) { 
        this.perfilUsuario = perfilUsuario; 
    }
    
    public LocalDateTime getDataHora() { 
        return dataHora; 
    }
    public void setDataHora(LocalDateTime dataHora) { 
        this.dataHora = dataHora; 
    }
    
    public String getTipoOperacao() { 
        return tipoOperacao; 
    }
    public void setTipoOperacao(String tipoOperacao) { 
        this.tipoOperacao = tipoOperacao; 
    }
    
    public String getEntidadeAfetada() { 
        return entidadeAfetada; 
    }
    public void setEntidadeAfetada(String entidadeAfetada) { 
        this.entidadeAfetada = entidadeAfetada; 
    }
    
    public String getIdentificadorEntidade() { 
        return identificadorEntidade; 
    }
    public void setIdentificadorEntidade(String identificadorEntidade) { 
        this.identificadorEntidade = identificadorEntidade; 
    }
    
    public String getCampoAlterado() { 
        return campoAlterado; 
    }
    public void setCampoAlterado(String campoAlterado) { 
        this.campoAlterado = campoAlterado; 
    }
    
    public String getValorAnterior() { 
        return valorAnterior; 
    }
    public void setValorAnterior(String valorAnterior) { 
        this.valorAnterior = valorAnterior; 
    }
    
    public String getNovoValor() { 
        return novoValor; 
    }
    public void setNovoValor(String novoValor) { 
        this.novoValor = novoValor; 
    }
    
    public String getJustificativa() { 
        return justificativa; 
    }
    public void setJustificativa(String justificativa) { 
        this.justificativa = justificativa; 
    }
}