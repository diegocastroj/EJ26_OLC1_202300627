package com.olc1.ast.exp;

import com.olc1.ast.ASTNode;
import com.olc1.visitor.Visitor;

public class StructAccessNode implements ASTNode {
    public final ASTNode objeto;   // El objeto (ej. la variable 'p')
    public final String atributo;  // Lo que queremos leer (ej. "nombre")

    public StructAccessNode(ASTNode objeto, String atributo) {
        this.objeto = objeto;
        this.atributo = atributo;
    }

    public class Context {
        public final ASTNode objeto;
        public final String atributo;
        public Context(StructAccessNode node) { this.objeto = node.objeto; this.atributo = node.atributo; }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) { return visitor.visit(new Context(this)); }
}