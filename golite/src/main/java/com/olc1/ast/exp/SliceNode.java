package com.olc1.ast.exp;

import com.olc1.ast.ASTNode;
import java.util.ArrayList;

public class SliceNode implements ASTNode {
    public final String tipo; // Guardará "int", "float64", etc.
    public final ArrayList<ASTNode> expresiones; // Los valores dentro de las llaves

    public SliceNode(String tipo, ArrayList<ASTNode> expresiones) {
        this.tipo = tipo;
        this.expresiones = expresiones;
    }

    public class Context {
        public final String tipo;
        public final ArrayList<ASTNode> expresiones;

        public Context(SliceNode node) {
            this.tipo = node.tipo;
            this.expresiones = node.expresiones;
        }
    }

    @Override
    public <T> T accept(com.olc1.visitor.Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}