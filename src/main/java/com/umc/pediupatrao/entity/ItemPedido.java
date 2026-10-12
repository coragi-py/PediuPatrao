package com.umc.pediupatrao.entity;

public class ItemPedido {
    private String sabor;
    private int quantidade;
    private Double precoUnitario;
    
    // ---Getters e Setters---
    public String getSabor() {
        return sabor;
    }
    public void setSabor(String sabor) {
        this.sabor = sabor;
    }
    
    public int getQuantidade() { 
        return quantidade; 
    }
    public void setQuantidade(int quantidade) { 
        this.quantidade = quantidade; 
    }
    
    public Double getPrecoUnitario() { 
        return precoUnitario; 
    }
    public void setPrecoUnitario(Double precoUnitario) { 
        this.precoUnitario = precoUnitario; 
    }
    
}
