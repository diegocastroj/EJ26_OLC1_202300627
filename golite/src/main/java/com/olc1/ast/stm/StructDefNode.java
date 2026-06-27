package com.olc1.ast.stm;

import com.olc1.ast.ASTNode;
import com.olc1.visitor.Visitor;
import java.util.ArrayList;

public class StructDefNode implements ASTNode {
    public final String id;
    public final ArrayList<Atributo> atributos;

    public StructDefNode(String id, ArrayList<Atributo> atributos) {
        this.id = id;
        this.atributos = atributos;
    }

    public class Context {
        public final String id;
        public final ArrayList<Atributo> atributos;

        public Context(StructDefNode node) {
            this.id = node.id;
            this.atributos = node.atributos;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}