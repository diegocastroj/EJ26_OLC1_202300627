package com.olc1.ast.stm;

import com.olc1.ast.ASTNode;
import com.olc1.visitor.Visitor;

public class BlockNode implements ASTNode {
    public final Context ctx;

    public BlockNode(ASTNode statements) {
        this.ctx = new Context(statements);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return (T) visitor.visit(this.ctx);
    }

    public static record Context(ASTNode statements) {}
}