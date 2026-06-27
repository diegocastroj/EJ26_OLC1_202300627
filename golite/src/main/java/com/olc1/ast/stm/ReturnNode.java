package com.olc1.ast.stm;

import com.olc1.ast.ASTNode;
import com.olc1.visitor.Visitor;

public class ReturnNode implements ASTNode {
    public final ASTNode expresion; // Lo que vamos a retornar (puede ser null si es un return vacío)

    public ReturnNode(ASTNode expresion) {
        this.expresion = expresion;
    }

    public class Context {
        public final ASTNode expresion;
        public Context(ReturnNode node) { this.expresion = node.expresion; }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}