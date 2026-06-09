package com.olc1.visitor.interpreter.values;

public record IntValue(int value, int line, int column) implements ValueWrapper {
    
    @Override
    public String getTypeName() {
        return "int";
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
