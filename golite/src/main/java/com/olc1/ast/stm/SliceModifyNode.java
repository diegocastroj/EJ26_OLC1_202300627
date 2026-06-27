package com.olc1.ast.stm;

import com.olc1.ast.ASTNode;
import com.olc1.visitor.Visitor;

public class SliceModifyNode implements ASTNode {
    public final String id;       
    public final ASTNode indice;  
    public final ASTNode valor;   
    public SliceModifyNode(String id, ASTNode indice, ASTNode valor) {
        this.id = id;
        this.indice = indice;
        this.valor = valor;
    }

    public class Context {
        public final String id;
        public final ASTNode indice;
        public final ASTNode valor;

        public Context(SliceModifyNode node) {
            this.id = node.id;
            this.indice = node.indice;
            this.valor = node.valor;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}