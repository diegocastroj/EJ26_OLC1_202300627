package com.olc1.visitor.interpreter.values;

import java.util.ArrayList;


public record SliceValue(ArrayList<ValueWrapper> value, String innerType, int line, int column) implements ValueWrapper {
    
    @Override
    public String getTypeName() {
        return "slice_" + innerType;
    }

    @Override
    public String toString() {
        return value.toString(); 
    }
}