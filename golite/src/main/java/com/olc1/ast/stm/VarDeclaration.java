package com.olc1.ast.stm;

import com.olc1.ast.ASTNode;
import com.olc1.visitor.Visitor;

public class VarDeclaration implements ASTNode {
    public final Context ctx;

    public VarDeclaration(String name, String typeStr, ASTNode value, int line, int column) {
        this.ctx = new Context(name, typeStr, value, line, column);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return (T) visitor.visit(this.ctx);
    }

    public static record Context(String name, String typeStr, ASTNode value, int line, int column) {}
}
