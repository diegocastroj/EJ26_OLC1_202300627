package com.olc1.ast.exp;
import com.olc1.ast.ASTNode;
import com.olc1.visitor.Visitor;

public class SlicesIndexNode implements ASTNode {
    public final ASTNode arreglo;
    public final ASTNode valor;

    public SlicesIndexNode(ASTNode arreglo, ASTNode valor) {
        this.arreglo = arreglo;
        this.valor = valor;
    }

    public class Context {
        public final ASTNode arreglo;
        public final ASTNode valor;
        public Context(SlicesIndexNode node) { this.arreglo = node.arreglo; this.valor = node.valor; }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) { return visitor.visit(new Context(this)); }
}