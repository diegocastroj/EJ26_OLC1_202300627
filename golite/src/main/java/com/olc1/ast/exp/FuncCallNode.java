package com.olc1.ast.exp;

import com.olc1.ast.ASTNode;
import com.olc1.visitor.Visitor;
import java.util.ArrayList;

public class FuncCallNode implements ASTNode {
    public final String id;
    public final ArrayList<ASTNode> argumentos;

    public FuncCallNode(String id, ArrayList<ASTNode> argumentos) {
        this.id = id;
        this.argumentos = argumentos;
    }

    public class Context {
        public final String id;
        public final ArrayList<ASTNode> argumentos;

        public Context(FuncCallNode node) {
            this.id = node.id;
            this.argumentos = node.argumentos;
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}