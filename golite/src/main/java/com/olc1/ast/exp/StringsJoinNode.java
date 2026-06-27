package com.olc1.ast.exp;
import com.olc1.ast.ASTNode;
import com.olc1.visitor.Visitor;

public class StringsJoinNode implements ASTNode {
    public final ASTNode arreglo;
    public final ASTNode delimitador;

    public StringsJoinNode(ASTNode arreglo, ASTNode delimitador) {
        this.arreglo = arreglo;
        this.delimitador = delimitador;
    }

    public class Context {
        public final ASTNode arreglo;
        public final ASTNode delimitador;
        public Context(StringsJoinNode node) { this.arreglo = node.arreglo; this.delimitador = node.delimitador; }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) { return visitor.visit(new Context(this)); }
}