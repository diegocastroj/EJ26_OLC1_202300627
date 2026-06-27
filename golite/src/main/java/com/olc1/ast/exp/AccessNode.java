package com.olc1.ast.exp;

import com.olc1.ast.ASTNode;
import com.olc1.visitor.Visitor;

public class AccessNode implements ASTNode {
    public final ASTNode arreglo; // El ID del slice o el slice en sí
    public final ASTNode indice;  // La posición a la que queremos acceder

    public AccessNode(ASTNode arreglo, ASTNode indice) {
        this.arreglo = arreglo;
        this.indice = indice;
    }

    public class Context {
        public final ASTNode arreglo;
        public final ASTNode indice;

        public Context(AccessNode node) {
            this.arreglo = node.arreglo;
            this.indice = node.indice;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}