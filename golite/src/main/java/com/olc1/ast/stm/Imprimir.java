package com.olc1.ast.stm;

import com.olc1.ast.ASTNode;
import com.olc1.visitor.Visitor;
import java.util.List;

public class Imprimir implements ASTNode {
    public final Context ctx;

    public Imprimir(List<ASTNode> expressions) {
        this.ctx = new Context(expressions);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this.ctx);
    }

    // El contexto ahora guarda una lista dinámica de nodos a evaluar
    public static record Context(List<ASTNode> expressions) {}
}