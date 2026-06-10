package com.olc1.ast.stm;

import com.olc1.ast.ASTNode;
import com.olc1.visitor.Visitor;

public class IfNode implements ASTNode {
    public final Context ctx;

    // Constructor para If con Else
    public IfNode(ASTNode condition, ASTNode body, ASTNode elseBody) {
        this.ctx = new Context(condition, body, elseBody);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this.ctx);
    }

    // Expandimos el Record para incluir el bloque else
    public static record Context(ASTNode condition, ASTNode body, ASTNode elseBody) {}
}