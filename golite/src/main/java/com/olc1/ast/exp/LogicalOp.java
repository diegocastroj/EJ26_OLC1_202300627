package com.olc1.ast.exp;

import com.olc1.ast.ASTNode;
import com.olc1.visitor.Visitor;

public class LogicalOp implements ASTNode {
    public final Context ctx;

    public LogicalOp(String operator, ASTNode left, ASTNode right, int line, int column) {
        this.ctx = new Context(operator, left, right, line, column);
    }

    public LogicalOp(String operator, ASTNode left, int line, int column) {
        this.ctx = new Context(operator, left, null, line, column);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this.ctx);
    }

    public static record Context(String operator, ASTNode left, ASTNode right, int line, int column) {}
}