package com.olc1.ast.stm;

import com.olc1.ast.ASTNode;

public class ForRangeNode implements ASTNode {
    public final String idIndice;
    public final String idValor;
    public final ASTNode sliceExpr;
    public final ASTNode instrucciones;

    public ForRangeNode(String idIndice, String idValor, ASTNode sliceExpr, ASTNode instrucciones) {
        this.idIndice = idIndice;
        this.idValor = idValor;
        this.sliceExpr = sliceExpr;
        this.instrucciones = instrucciones;
    }

    public class Context {
        public final String idIndice;
        public final String idValor;
        public final ASTNode sliceExpr;
        public final ASTNode instrucciones;

        public Context(ForRangeNode node) {
            this.idIndice = node.idIndice;
            this.idValor = node.idValor;
            this.sliceExpr = node.sliceExpr;
            this.instrucciones = node.instrucciones;
        }
    }

    @Override
    public <T> T accept(com.olc1.visitor.Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}