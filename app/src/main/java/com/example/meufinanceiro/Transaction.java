package com.example.meufinanceiro; // ajuste para o seu pacote

public class Transaction {
    private String id;
    private String descricao;
    private double valor;
    private String data;
    private String categoria;
    private String formaPagamento;
    private String tipo; // "Ganho" ou "Gasto"
    private String usuarioId; // ✅ novo campo

    public Transaction() {
        // Necessário para o Firebase
    }

    public Transaction(String id, String descricao, double valor, String data, String categoria, String formaPagamento, String tipo) {
        this.id = id;
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        this.categoria = categoria;
        this.formaPagamento = formaPagamento;
        this.tipo = tipo;
    }

    // Getters e Setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }
}
