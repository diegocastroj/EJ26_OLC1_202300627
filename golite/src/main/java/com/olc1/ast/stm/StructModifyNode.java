package com.olc1.ast.stm;

import com.olc1.ast.ASTNode;
import com.olc1.visitor.Visitor;

public class StructModifyNode implements ASTNode {
    public final String idVariable; // El nombre de la variable (ej. "p")
    public final String atributo;   // La propiedad (ej. "nombre")
    public final ASTNode nuevoValor; // El valor a guardar (ej. "Diego")

    public StructModifyNode(String idVariable, String atributo, ASTNode nuevoValor) {
        this.idVariable = idVariable;
        this.atributo = atributo;
        this.nuevoValor = nuevoValor;
    }

    public class Context {
        public final String idVariable;
        public final String atributo;
        public final ASTNode nuevoValor;
        public Context(StructModifyNode node) { this.idVariable = node.idVariable; this.atributo = node.atributo; this.nuevoValor = node.nuevoValor; }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) { return visitor.visit(new Context(this)); }
}