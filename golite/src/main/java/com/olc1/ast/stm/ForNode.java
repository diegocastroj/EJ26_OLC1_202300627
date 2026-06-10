package com.olc1.ast.stm;

import com.olc1.ast.ASTNode;
import com.olc1.visitor.Visitor;

public class ForNode implements ASTNode {
    public final Context ctx;

    public ForNode(ASTNode init, ASTNode condition, ASTNode increment, ASTNode body) {
        this.ctx = new Context(init, condition, increment, body);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this.ctx);
    }

    // Contexto de 4 partes: la inicialización e incremento pueden ser nulos
    public static record Context(ASTNode init, ASTNode condition, ASTNode increment, ASTNode body) {}
}