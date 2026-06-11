package com.olc1.visitor.interpreter.values;

public record NullValue(int line, int column) implements ValueWrapper {
    @Override
    public String getTypeName() {
        return "null";
    }

    @Override
    public String toString() {
        return "null";
    }
}