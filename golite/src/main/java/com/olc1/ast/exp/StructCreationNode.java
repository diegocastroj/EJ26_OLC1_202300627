package com.olc1.ast.exp;

import com.olc1.ast.ASTNode;
import com.olc1.visitor.Visitor;
import java.util.ArrayList;

public class StructCreationNode implements ASTNode {
    public final String id; // El nombre del molde (ej. "Persona")
    public final ArrayList<ASTNode> valores; // Los datos que le pasamos

    public StructCreationNode(String id, ArrayList<ASTNode> valores) {
        this.id = id;
        this.valores = valores;
    }

    public class Context {
        public final String id;
        public final ArrayList<ASTNode> valores;

        public Context(StructCreationNode node) {
            this.id = node.id;
            this.valores = node.valores;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}