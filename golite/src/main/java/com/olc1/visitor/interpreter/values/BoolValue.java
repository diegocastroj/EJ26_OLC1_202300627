package com.olc1.visitor.interpreter.values;

public record BoolValue(boolean value, int line, int column) implements ValueWrapper {

    @Override
    public String getTypeName() {
        return "bool";
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}