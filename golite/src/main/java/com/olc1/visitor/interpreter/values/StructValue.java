package com.olc1.visitor.interpreter.values;

import java.util.HashMap;

public record StructValue(String tipoStruct, HashMap<String, ValueWrapper> propiedades, int line, int column) implements ValueWrapper {
    
    @Override
    public String getTypeName() {
        return tipoStruct; 
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(tipoStruct).append("{");
        
        int iterador = 0;
        for (java.util.Map.Entry<String, ValueWrapper> entry : propiedades.entrySet()) {
            sb.append(entry.getKey()).append(": ");
            
            if (entry.getValue() instanceof StringValue s) {
                sb.append("\"").append(s.value()).append("\""); // Le ponemos comillas para que se vea claro
            } else {
                sb.append(entry.getValue().toString());
            }
            
            iterador++;
            if (iterador < propiedades.size()) {
                sb.append(", ");
            }
        }
        sb.append("}");
        
        return sb.toString(); 
    }
}