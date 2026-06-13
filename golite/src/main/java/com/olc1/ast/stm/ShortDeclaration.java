package com.olc1.ast.stm;

import com.olc1.ast.ASTNode;
import com.olc1.visitor.Visitor;

public class ShortDeclaration implements ASTNode {
    private final Context ctx;

    public ShortDeclaration(String name, ASTNode value, int line, int column) {
        this.ctx = new Context(name, value, line, column);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return (T) visitor.visit(this.ctx);
    }

    public static class Context {
        public final String name;
        public final ASTNode value;
        public final int line;
        public final int column;

        public Context(String name, ASTNode value, int line, int column) {
            this.name = name;
            this.value = value;
            this.line = line;
            this.column = column;
        }
    }
}