package com.olc1.ast.stm;
import com.olc1.visitor.interpreter.values.ValueWrapper;

public class ReturnException extends RuntimeException {
    public final ValueWrapper value;

    public ReturnException(ValueWrapper value) {
        super(null, null, false, false); 
        this.value = value;
    }
}