package com.example.fitnow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Treino {
    private String id;
    private String nome;
    private String tipo;
    private int duracao;
    private String userId;
    private List<Map<String, Object>> exercicios = new ArrayList<>();

    public Treino() {
        // Necessário para Firestore
    }

    public Treino(String nome, String tipo, int duracao, String userId) {
        this.nome = nome;
        this.tipo = tipo;
        this.duracao = duracao;
        this.userId = userId;
    }

    // Getters e setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getDuracao() {
        return duracao;
    }

    public void setDuracao(int duracao) {
        this.duracao = duracao;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<Map<String, Object>> getExercicios() {
        return exercicios;
    }

    public void setExercicios(List<Map<String, Object>> exercicios) {
        this.exercicios = exercicios;
    }
}