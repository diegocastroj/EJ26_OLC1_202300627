package com.olc1.reports;

public class Simbolo {
    public String id;
    public String tipoSimbolo; 
    public String tipoDato;   
    public String ambito;    
    public int linea;
    public int columna;

    public Simbolo(String id, String tipoSimbolo, String tipoDato, String ambito, int linea, int columna) {
        this.id = id;
        this.tipoSimbolo = tipoSimbolo;
        this.tipoDato = tipoDato;
        this.ambito = ambito;
        this.linea = linea;
        this.columna = columna;
    }
}