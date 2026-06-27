package com.olc1.visitor.interpreter.values;

import com.olc1.ast.stm.FuncDeclNode;

public record FunctionValue(FuncDeclNode.Context funcion, int line, int column) implements ValueWrapper {
    @Override
    public String getTypeName() { return "funcion"; }
    
    @Override
    public String toString() { return "<funcion " + funcion.id + ">"; }
}