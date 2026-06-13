package com.olc1.ast.exp;

import com.olc1.ast.ASTNode;
import com.olc1.visitor.Visitor;

public class RuneLiteral implements ASTNode {
    public final Context ctx;

    public RuneLiteral(String text, int line, int column) {
        this.ctx = new Context(text, line, column);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this.ctx);
    }

    public static record Context(String text, int line, int column) {}
}