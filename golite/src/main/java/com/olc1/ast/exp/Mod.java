package com.olc1.ast.exp;

import com.olc1.ast.ASTNode;
import com.olc1.visitor.Visitor;

public class Mod implements ASTNode {
    public final Context ctx;

    public Mod(ASTNode left, ASTNode right, int line, int column) {
        this.ctx = new Context(left, right, line, column);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this.ctx);
    }

    public static record Context(ASTNode left, ASTNode right, int line, int column) {}
}