package com.olc1.ast.exp;

import com.olc1.ast.ASTNode;
import com.olc1.visitor.Visitor;

public class LenNode implements ASTNode {
    public final ASTNode expresion;

    public LenNode(ASTNode expresion) {
        this.expresion = expresion;
    }

    public class Context {
        public final ASTNode expresion;
        public Context(LenNode node) { this.expresion = node.expresion; }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}