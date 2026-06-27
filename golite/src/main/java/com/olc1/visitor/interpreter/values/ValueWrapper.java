package com.olc1.visitor.interpreter.values;

public sealed interface ValueWrapper
    permits IntValue, DecimalValue, VoidValue, BoolValue, StringValue, NullValue, SliceValue, FunctionValue, StructBlueprint, StructValue {
    
    int line();
    int column();
    String getTypeName();
}
