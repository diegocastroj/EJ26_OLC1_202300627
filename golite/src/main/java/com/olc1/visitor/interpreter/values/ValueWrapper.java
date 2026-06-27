package com.olc1.visitor.interpreter.values;

public sealed interface ValueWrapper
    permits IntValue, DecimalValue, VoidValue, BoolValue, StringValue, NullValue, SliceValue, FunctionValue {
    
    int line();
    int column();
    String getTypeName();
}
