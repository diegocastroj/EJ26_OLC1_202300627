package com.olc1.visitor.interpreter.values;
import com.olc1.ast.stm.StructDefNode;

public record StructBlueprint(StructDefNode.Context definicion, int line, int column) implements ValueWrapper {
    @Override
    public String getTypeName() { return "struct_blueprint"; }
    @Override
    public String toString() { return "<molde_struct " + definicion.id + ">"; }
}