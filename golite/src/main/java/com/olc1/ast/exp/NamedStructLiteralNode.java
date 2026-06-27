package com.olc1.ast.exp;

import com.olc1.ast.ASTNode;
import com.olc1.visitor.Visitor;
import java.util.HashMap;

public class NamedStructLiteralNode implements ASTNode {
    public final HashMap<String, ASTNode> valores;

    public NamedStructLiteralNode(HashMap<String, ASTNode> valores) {
        this.valores = valores;
    }

    public class Context {
        public final HashMap<String, ASTNode> valores;
        public Context(NamedStructLiteralNode node) { this.valores = node.valores; }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}