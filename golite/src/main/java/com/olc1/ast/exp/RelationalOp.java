package com.olc1.ast.exp;

import com.olc1.ast.ASTNode;
import com.olc1.visitor.Visitor;

public class RelationalOp implements ASTNode {
    public final Context ctx;

    public RelationalOp(String operator, ASTNode left, ASTNode right, int line, int column) {
        this.ctx = new Context(operator, left, right, line, column);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this.ctx);
    }

    public static record Context(String operator, ASTNode left, ASTNode right, int line, int column) {}
}