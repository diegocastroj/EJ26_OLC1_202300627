package com.olc1.ast.stm;

import com.olc1.ast.ASTNode;
import com.olc1.visitor.Visitor;

public class CustomTypeVarDeclNode implements ASTNode {
    public final String tipo;
    public final String id;
    public final ASTNode expresion;

    public CustomTypeVarDeclNode(String tipo, String id, ASTNode expresion) {
        this.tipo = tipo;
        this.id = id;
        this.expresion = expresion;
    }

    public class Context {
        public final String tipo;
        public final String id;
        public final ASTNode expresion;

        public Context(CustomTypeVarDeclNode node) {
            this.tipo = node.tipo;
            this.id = node.id;
            this.expresion = node.expresion;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}