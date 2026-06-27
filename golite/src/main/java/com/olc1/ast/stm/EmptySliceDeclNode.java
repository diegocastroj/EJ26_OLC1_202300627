package com.olc1.ast.stm;

import com.olc1.ast.ASTNode;
import com.olc1.visitor.Visitor;

public class EmptySliceDeclNode implements ASTNode {
    public final String id;
    public final String tipoBase; // ej. "int"

    public EmptySliceDeclNode(String id, String tipoBase) {
        this.id = id;
        this.tipoBase = tipoBase;
    }

    public class Context {
        public final String id;
        public final String tipoBase;
        public Context(EmptySliceDeclNode node) { this.id = node.id; this.tipoBase = node.tipoBase; }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}