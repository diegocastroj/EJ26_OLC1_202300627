package com.olc1.ast.stm; // Asegúrate de que este paquete coincida con tu estructura

public class SemanticException extends RuntimeException {
    public final int linea;
    public final int columna;

    public SemanticException(String mensaje, int linea, int columna) {
        super(mensaje);
        this.linea = linea;
        this.columna = columna;
    }
}