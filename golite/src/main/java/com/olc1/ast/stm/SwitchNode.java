package com.olc1.ast.stm;

import com.olc1.ast.ASTNode;
import java.util.ArrayList;

public class SwitchNode implements ASTNode {
    public final ASTNode expression;
    public final ArrayList<ASTNode> cases;

    public SwitchNode(ASTNode expression, ArrayList<ASTNode> cases) {
        this.expression = expression;
        this.cases = cases;
    }

    // === AQUÍ ESTÁ EL CONTEXT QUE FALTABA ===
    public class Context {
        public final ASTNode expression;
        public final ArrayList<ASTNode> cases;

        public Context(SwitchNode node) {
            this.expression = node.expression;
            this.cases = node.cases;
        }
    }

    @Override
    public <T> T accept(com.olc1.visitor.Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}