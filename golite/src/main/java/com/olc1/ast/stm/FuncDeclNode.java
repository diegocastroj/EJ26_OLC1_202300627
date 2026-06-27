package com.olc1.ast.stm;

import com.olc1.ast.ASTNode;
import com.olc1.visitor.Visitor;
import java.util.ArrayList;

public class FuncDeclNode implements ASTNode {
    public final String id;
    public final ArrayList<Parametro> parametros;
    public final ASTNode cuerpo;

    public FuncDeclNode(String id, ArrayList<Parametro> parametros, ASTNode cuerpo) {
        this.id = id;
        this.parametros = parametros;
        this.cuerpo = cuerpo;
    }

    public class Context {
        public final String id;
        public final ArrayList<Parametro> parametros;
        public final ASTNode cuerpo;

        public Context(FuncDeclNode node) {
            this.id = node.id;
            this.parametros = node.parametros;
            this.cuerpo = node.cuerpo;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}