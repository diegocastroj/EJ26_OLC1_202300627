package com.olc1.ast.exp;

import com.olc1.ast.ASTNode;
import com.olc1.visitor.Visitor;

public class ReflectNode implements ASTNode {
    public final Context ctx;

    public ReflectNode(ASTNode expression, int line, int column) {
        this.ctx = new Context(expression, line, column);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this.ctx);
    }

    public static record Context(ASTNode expression, int line, int column) {}
}