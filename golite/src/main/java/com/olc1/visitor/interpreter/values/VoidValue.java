package com.olc1.visitor.interpreter.values;

public record VoidValue(int line, int column) implements ValueWrapper {
    
    @Override
    public String getTypeName() {
        return "void";
    }

    @Override
    public String toString() {
        return "void";
    }
}
