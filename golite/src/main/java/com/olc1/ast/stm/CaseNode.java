package com.olc1.ast.stm;

import com.olc1.ast.ASTNode;

public class CaseNode implements ASTNode {
    public final ASTNode expression;
    public final ASTNode instrucciones;

    public CaseNode(ASTNode expression, ASTNode instrucciones) {
        this.expression = expression;
        this.instrucciones = instrucciones;
    }

    // === CONTEXT PARA MANTENER LA ARQUITECTURA ===
    public class Context {
        public final ASTNode expression;
        public final ASTNode instrucciones;

        public Context(CaseNode node) {
            this.expression = node.expression;
            this.instrucciones = node.instrucciones;
        }
    }

    @Override
    public <T> T accept(com.olc1.visitor.Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}