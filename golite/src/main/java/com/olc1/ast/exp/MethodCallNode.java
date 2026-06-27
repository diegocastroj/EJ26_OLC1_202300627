package com.olc1.ast.exp;

import com.olc1.ast.ASTNode;
import com.olc1.visitor.Visitor;
import java.util.ArrayList;

public class MethodCallNode implements ASTNode {
    public final ASTNode target;
    public final String methodName;
    public final ArrayList<ASTNode> args;

    public MethodCallNode(ASTNode target, String methodName, ArrayList<ASTNode> args) {
        this.target = target;
        this.methodName = methodName;
        this.args = args;
    }

    public static class Context {
        public final MethodCallNode node;
        public Context(MethodCallNode node) { this.node = node; }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(new Context(this));
    }
}