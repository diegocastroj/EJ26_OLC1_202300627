package com.olc1.ast.exp;

import com.olc1.ast.ASTNode;
import com.olc1.visitor.Visitor;
import java.util.HashMap;

public class NamedStructCreationNode implements ASTNode {
    public final String id; // Ej. "Persona"
    public final HashMap<String, ASTNode> valores; // Ej. {Nombre: "Alice"}

    public NamedStructCreationNode(String id, HashMap<String, ASTNode> valores) {
        this.id = id;
        this.valores = valores;
    }

    public class Context {
        public final String id;
        public final HashMap<String, ASTNode> valores;
        public Context(NamedStructCreationNode node) { this.id = node.id; this.valores = node.valores; }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}