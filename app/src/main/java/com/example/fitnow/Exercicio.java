package com.example.fitnow;

public class Exercicio {
    private String nome;
    private String tempo;       // Ex: "20 min"
    private String dificuldade; // Ex: "Fácil"

    public Exercicio(String nome, String tempo, String dificuldade) {
        this.nome = nome;
        this.tempo = tempo;
        this.dificuldade = dificuldade;
    }

    public String getNome() {
        return nome;
    }

    public String getTempo() {
        return tempo;
    }

    public String getDificuldade() {
        return dificuldade;
    }
}
