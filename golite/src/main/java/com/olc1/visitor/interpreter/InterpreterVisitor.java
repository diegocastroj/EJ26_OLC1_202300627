package com.olc1.visitor.interpreter;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import com.olc1.ast.ASTNode;
import com.olc1.ast.stm.*;
import com.olc1.ast.exp.*;
import com.olc1.visitor.Visitor;
import com.olc1.visitor.interpreter.values.*;

public class InterpreterVisitor implements Visitor<ValueWrapper> {
    public String output = "";
    private final ValueWrapper defaultVoid = new VoidValue(-1, -1);
    
    // CONTROL DE ÁMBITOS: Pila dinámica de entornos para aislamiento de bloques locales
    private final List<Map<String, ValueWrapper>> ambitos = new ArrayList<>();

    public InterpreterVisitor() {
        // Inicializamos el entorno con el Ámbito Global (Posición 0)
        ambitos.add(new HashMap<>());
    }

    // Método de despacho principal del patrón Visitor
    public ValueWrapper Visit(ASTNode node) {
        return node.accept(this);
    }

    // MÉTODOS DE CONTROL SEMÁNTICO Y ÁMBITOS (SCOPES)
    
    private ValueWrapper obtenerVariable(String nombre) {
        // Busca de forma jerárquica desde el ámbito más interno hacia el global
        for (int i = ambitos.size() - 1; i >= 0; i--) {
            if (ambitos.get(i).containsKey(nombre)) {
                return ambitos.get(i).get(nombre);
            }
        }
        return null;
    }

    private boolean existeEnAmbitoActual(String nombre) {
        // Valida duplicados únicamente en el entorno actual en ejecución
        return ambitos.get(ambitos.size() - 1).containsKey(nombre);
    }

    private void asignarVariable(String nombre, ValueWrapper valor) {
        // Modifica el valor en el entorno exacto donde la variable fue creada originalmente
        for (int i = ambitos.size() - 1; i >= 0; i--) {
            if (ambitos.get(i).containsKey(nombre)) {
                ambitos.get(i).put(nombre, valor);
                return;
            }
        }
        throw new RuntimeException("Error semantico: Variable no definida: " + nombre);
    }

    private void declararVariable(String nombre, ValueWrapper valor) {
        // Registra la variable de forma estricta en el entorno del tope de la pila
        ambitos.get(ambitos.size() - 1).put(nombre, valor);
    }

    // IMPLEMENTACIÓN DE LOS MÉTODOS VISIT, LÓGICA DEL COMPILADOR

    @Override
    public ValueWrapper visit(Integers.Context ctx) {
        return new IntValue(ctx.value, ctx.line, ctx.column);
    }

    @Override
    public ValueWrapper visit(Decimal.Context ctx) {
        return new DecimalValue(ctx.value, ctx.line, ctx.column);
    }

    @Override
    public ValueWrapper visit(Add.Context ctx) {
        ValueWrapper left  = Visit(ctx.left);
        ValueWrapper right = Visit(ctx.right);

        return switch (left) {
            case IntValue     l when right instanceof IntValue     r -> new IntValue(l.value() + r.value(), l.line(), l.column());
            case IntValue     l when right instanceof DecimalValue r -> new DecimalValue(l.value() + r.value(), l.line(), l.column());
            case DecimalValue l when right instanceof IntValue     r -> new DecimalValue(l.value() + r.value(), l.line(), l.column());
            case DecimalValue l when right instanceof DecimalValue r -> new DecimalValue(l.value() + r.value(), l.line(), l.column());
            case StringValue  l when right instanceof StringValue  r -> new StringValue(l.value() + r.value(), l.line(), l.column());
            
            default -> throw new RuntimeException("Error semántico: Operación inválida " + left.getTypeName() + " + " + right.getTypeName());
        };
    }

    @Override
    public ValueWrapper visit(Sub.Context ctx) {
        ValueWrapper left  = Visit(ctx.left);
        ValueWrapper right = Visit(ctx.right);
        return switch (left) {
            case IntValue     l when right instanceof IntValue     r -> new IntValue((int)(l.value() - r.value()), l.line(), l.column());
            case IntValue     l when right instanceof DecimalValue r -> new DecimalValue(l.value() - r.value(), l.line(), l.column());
            case DecimalValue l when right instanceof IntValue     r -> new DecimalValue(l.value() - r.value(), l.line(), l.column());
            case DecimalValue l when right instanceof DecimalValue r -> new DecimalValue(l.value() - r.value(), l.line(), l.column());
            default -> throw new RuntimeException("Operacion invalida: " + left.getTypeName() + " - " + right.getTypeName());
        };
    }

    @Override
    public ValueWrapper visit(Mul.Context ctx) {
        ValueWrapper left  = Visit(ctx.left);
        ValueWrapper right = Visit(ctx.right);
        return switch (left) {
            case IntValue     l when right instanceof IntValue     r -> new IntValue(l.value() * r.value(), l.line(), l.column());
            case IntValue     l when right instanceof DecimalValue r -> new DecimalValue(l.value() * r.value(), l.line(), l.column());
            case DecimalValue l when right instanceof IntValue     r -> new DecimalValue(l.value() * r.value(), l.line(), l.column());
            case DecimalValue l when right instanceof DecimalValue r -> new DecimalValue(l.value() * r.value(), l.line(), l.column());
            default -> throw new RuntimeException("Operacion invalida: " + left.getTypeName() + " * " + right.getTypeName());
        };
    }

    @Override
    public ValueWrapper visit(Div.Context ctx) {
        ValueWrapper left  = Visit(ctx.left);
        ValueWrapper right = Visit(ctx.right);
        return switch (left) {
            case IntValue     l when right instanceof IntValue     r -> {
                if (r.value() == 0) throw new RuntimeException("Error semantico: Division entre cero.");
                yield new IntValue(l.value() / r.value(), l.line(), l.column());
            }
            case IntValue     l when right instanceof DecimalValue r -> new DecimalValue(l.value() / r.value(), l.line(), l.column());
            case DecimalValue l when right instanceof IntValue     r -> new DecimalValue(l.value() / r.value(), l.line(), l.column());
            case DecimalValue l when right instanceof DecimalValue r -> new DecimalValue(l.value() / r.value(), l.line(), l.column());
            default -> throw new RuntimeException("Operacion invalida: " + left.getTypeName() + " / " + right.getTypeName());
        };
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.exp.Mod.Context ctx) {
        ValueWrapper left  = Visit(ctx.left());
        ValueWrapper right = Visit(ctx.right());
        return switch (left) {
            case IntValue l when right instanceof IntValue r -> {
                if (r.value() == 0) {
                    throw new RuntimeException("Error semantico: No se puede dividir entre cero en la operacion de modulo.");
                }
                yield new IntValue(l.value() % r.value(), l.line(), l.column());
            }
            default -> throw new RuntimeException("Error semantico: El operador '%' solo es valido entre tipos enteros (int).");
        };
    }

    @Override
    public ValueWrapper visit(Negate.Context ctx) {
        ValueWrapper operand = Visit(ctx.expression);
        return switch (operand) {
            case IntValue     v -> new IntValue(-v.value(), v.line(), v.column());
            case DecimalValue v -> new DecimalValue(-v.value(), v.line(), v.column());
            default -> throw new RuntimeException("Operacion invalida: -" + operand.getTypeName());
        };
    }

    @Override
    public ValueWrapper visit(Imprimir.Context ctx) {
        StringBuilder salida = new StringBuilder();
        
        // Si la lista viene vacía ( fmt.Println() ), simplemente el ciclo no entra
        // y se imprime únicamente el salto de línea al final.
        for (int i = 0; i < ctx.expressions().size(); i++) {
            ValueWrapper val = Visit(ctx.expressions().get(i));
            
            // Extracción del valor primitivo
            String strOut = (val instanceof StringValue s) ? s.value() : val.toString();
            salida.append(strOut);
            
            // Separador de espacio automático (no se pone después del último elemento)
            if (i < ctx.expressions().size() - 1) {
                salida.append(" ");
            }
        }
        
        // Salto de línea obligatorio al final
        output += salida.toString() + "\n";
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(Statments.Context ctx) {
        for (ASTNode statment : ctx.statements) {
            Visit(statment);
        }
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(Paren.Context ctx) {
        return Visit(ctx.expression);
    }

    @Override
    public ValueWrapper visit(BoolLiteral.Context ctx) {
        return new BoolValue(ctx.value, ctx.line, ctx.column);
    }

    @Override
    public ValueWrapper visit(StringLiteral.Context ctx) {
        return new StringValue(ctx.value, ctx.line, ctx.column);
    }

   @Override
    public ValueWrapper visit(VarRef.Context ctx) {
        ValueWrapper val = obtenerVariable(ctx.name);
        if (val == null) {
            throw new RuntimeException("Error semántico: La variable '" + ctx.name + "' no está definida en este contexto.");
        }
        return val;
    }

   @Override
    public ValueWrapper visit(Assign.Context ctx) {
        ValueWrapper variableExistente = obtenerVariable(ctx.name);
        if (variableExistente == null) {
            throw new RuntimeException("Error semántico: La variable '" + ctx.name + "' no está definida en este contexto.");
        }
        
        ValueWrapper nuevoValor = Visit(ctx.value);
        
        // VALIDACIONES DE TIPOS CON MENSAJES EXACTOS
        if (variableExistente instanceof IntValue) {
            if (!(nuevoValor instanceof IntValue)) {
                throw new RuntimeException("Error semántico: No se puede asignar un " + nuevoValor.getTypeName() + " a un int.");
            }
        } else if (variableExistente instanceof DecimalValue) {
            if (nuevoValor instanceof IntValue iv) {
                nuevoValor = new DecimalValue((double) iv.value(), iv.line(), iv.column());
            } else if (!(nuevoValor instanceof DecimalValue)) {
                throw new RuntimeException("Error semántico: No se puede asignar un " + nuevoValor.getTypeName() + " a un float64.");
            }
        } else if (variableExistente instanceof StringValue) {
            if (!(nuevoValor instanceof StringValue)) {
                throw new RuntimeException("Error semántico: No se puede asignar un " + nuevoValor.getTypeName() + " a un string.");
            }
        } else if (variableExistente instanceof BoolValue) {
            if (!(nuevoValor instanceof BoolValue)) {
                throw new RuntimeException("Error semántico: No se puede asignar un " + nuevoValor.getTypeName() + " a un bool.");
            }
        }
        
        asignarVariable(ctx.name, nuevoValor);
        return defaultVoid;
    }
    @Override
    public ValueWrapper visit(IfNode.Context ctx) {
        ValueWrapper cond = Visit(ctx.condition());
        if (cond instanceof BoolValue b && b.value()) {
            Visit(ctx.body());
        } else if (ctx.elseBody() != null) {
            Visit(ctx.elseBody());
        }
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.exp.RelationalOp.Context ctx) {
        ValueWrapper left  = Visit(ctx.left());
        ValueWrapper right = Visit(ctx.right());

        double lVal = (left instanceof IntValue cv) ? cv.value() : (left instanceof DecimalValue cv) ? cv.value() : 0;
        double rVal = (right instanceof IntValue cv) ? cv.value() : (right instanceof DecimalValue cv) ? cv.value() : 0;
        
        boolean res = false;
        switch (ctx.operator()) {
            case "==" -> {
                if (left instanceof StringValue lStr && right instanceof StringValue rStr) {
                    res = lStr.value().equals(rStr.value());
                } else if (left instanceof BoolValue lBool && right instanceof BoolValue rBool) {
                    res = lBool.value() == rBool.value();
                } else if (left instanceof NullValue || right instanceof NullValue) {
                    res = (left instanceof NullValue && right instanceof NullValue);
                } else {
                    res = lVal == rVal;
                }
            }
            case "!=" -> {
                if (left instanceof StringValue lStr && right instanceof StringValue rStr) {
                    res = !lStr.value().equals(rStr.value());
                } else if (left instanceof BoolValue lBool && right instanceof BoolValue rBool) {
                    res = lBool.value() != rBool.value();
                } else if (left instanceof NullValue || right instanceof NullValue) {
                    res = !(left instanceof NullValue && right instanceof NullValue);
                } else {
                    res = lVal != rVal;
                }
            }
            case "<"  -> res = lVal < rVal;
            case ">"  -> res = lVal > rVal;
            case "<=" -> res = lVal <= rVal;
            case ">=" -> res = lVal >= rVal;
            default -> throw new RuntimeException("Operador relacional desconocido: " + ctx.operator());
        }
        return new BoolValue(res, ctx.line(), ctx.column());
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.exp.LogicalOp.Context ctx) {
        ValueWrapper left = Visit(ctx.left());
        if (ctx.operator().equals("!")) {
            if (left instanceof BoolValue b) {
                return new BoolValue(!b.value(), ctx.line(), ctx.column());
            }
            throw new RuntimeException("El operador '!' requiere un valor booleano");
        }
        
        ValueWrapper right = Visit(ctx.right());
        if (left instanceof BoolValue l && right instanceof BoolValue r) {
            boolean res = switch (ctx.operator()) {
                case "&&" -> l.value() && r.value();
                case "||" -> l.value() || r.value();
                default -> throw new RuntimeException("Operador logico desconocido: " + ctx.operator());
            };
            return new BoolValue(res, ctx.line(), ctx.column());
        }
        throw new RuntimeException("Los operadores '&&' y '||' requieren valores booleanos");
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.stm.ForNode.Context ctx) {
        // 1. ÁMBITO DEL CICLO: Creamos un entorno exclusivo para la variable de control (ej: i, j, k, l)
        ambitos.add(new HashMap<>());

        try {
            if (ctx.init() != null) {
                Visit(ctx.init());
            }

            while (true) {
                ValueWrapper cond = Visit(ctx.condition());
                if (!(cond instanceof BoolValue b && b.value())) {
                    break;
                }

                // 2. ÁMBITO DE ITERACIÓN: Cada vuelta del cuerpo tiene su propio entorno limpio
                ambitos.add(new HashMap<>());
                try {
                    Visit(ctx.body());
                } catch (BreakException ex) {
                    break; // Corta el bucle 'while' principal
                } catch (ContinueException ex) {
                    // Salto de flujo controlado, continúa hacia el incremento
                } finally {
                    // Siempre removemos el ámbito del cuerpo al terminar la iteración actual
                    ambitos.remove(ambitos.size() - 1);
                }

                if (ctx.increment() != null) {
                    Visit(ctx.increment());
                }
            }
        } finally {
            // 3. LIMPIEZA TOTAL: Al salir del FOR por completo, destruimos su variable de control
            ambitos.remove(ambitos.size() - 1);
        }

        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.stm.BreakNode node) {
        throw new BreakException();
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.stm.ContinueNode node) {
        throw new ContinueException();
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.exp.StrConvNode.Context ctx) {
        ValueWrapper val = Visit(ctx.expression());
        if (!(val instanceof StringValue str)) {
            throw new RuntimeException("Error semantico: Las funciones de strconv requieren una cadena de texto.");
        }
        
        String textoLimpio = str.value();
        try {
            if (ctx.type().equals("atoi")) {
                if (textoLimpio.contains(".")) {
                    throw new RuntimeException("Error semantico: strconv.Atoi no procesa flotantes: " + textoLimpio);
                }
                return new IntValue(Integer.parseInt(textoLimpio), ctx.line(), ctx.column());
            } else {
                return new DecimalValue(Double.parseDouble(textoLimpio), ctx.line(), ctx.column());
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("Error semantico: No se pudo convertir la cadena '" + textoLimpio + "' a numerico.");
        }
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.exp.ReflectNode.Context ctx) {
        ValueWrapper val = Visit(ctx.expression());
        String typeName = "unknown";
        if (val instanceof IntValue) typeName = "int";
        else if (val instanceof DecimalValue) typeName = "float64";
        else if (val instanceof StringValue) typeName = "string";
        else if (val instanceof BoolValue) typeName = "bool";
        else if (val instanceof NullValue) typeName = "null";
        
        return new StringValue(typeName, ctx.line(), ctx.column());
    }
    
    @Override
    public ValueWrapper visit(com.olc1.ast.exp.NullLiteral.Context ctx) {
        return new NullValue(ctx.line(), ctx.column());
    }

    // MÉTODOS VISIT DE DECLARACIÓN Y CONTROL DE BLOQUES (ÁMBITOS)

    @Override
    public ValueWrapper visit(com.olc1.ast.stm.ShortDeclaration.Context ctx) {
        if (existeEnAmbitoActual(ctx.name)) {
            throw new RuntimeException("Error semantico: La variable '" + ctx.name + "' ya fue declarada en este ambito.");
        }
        ValueWrapper val = Visit(ctx.value);
        declararVariable(ctx.name, val);
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.stm.VarDeclaration.Context ctx) {
        if (existeEnAmbitoActual(ctx.name())) {
            throw new RuntimeException("Error semantico: La variable '" + ctx.name() + "' ya fue declarada en este ambito.");
        }
        
        ValueWrapper val;
        if (ctx.value() != null) {
            val = Visit(ctx.value());
            
            switch (ctx.typeStr()) {
                case "int":
                    if (!(val instanceof IntValue)) {
                        throw new RuntimeException("Error semántico: No se puede asignar un " + val.getTypeName() + " a un int.");
                    }
                    break;
                case "float64":
                    if (val instanceof IntValue iv) {
                        val = new DecimalValue((double) iv.value(), iv.line(), iv.column());
                    } else if (!(val instanceof DecimalValue)) {
                        throw new RuntimeException("Error semántico: No se puede asignar un " + val.getTypeName() + " a un float64.");
                    }
                    break;
                case "string":
                    if (!(val instanceof StringValue)) {
                        throw new RuntimeException("Error semántico: No se puede asignar un " + val.getTypeName() + " a un string.");
                    }
                    break;
                case "bool":
                    if (!(val instanceof BoolValue)) {
                        throw new RuntimeException("Error semántico: No se puede asignar un " + val.getTypeName() + " a un bool.");
                    }
                    break;
                case "rune":
                    if (!(val instanceof IntValue)) {
                        throw new RuntimeException("Error semántico: No se puede asignar un " + val.getTypeName() + " a un rune.");
                    }
                    break;
            }
        } else {
            // ASIGNACIÓN AUTOMÁTICA DE VALORES POR DEFECTO
            val = switch (ctx.typeStr()) {
                case "int", "rune" -> new IntValue(0, ctx.line(), ctx.column());
                case "float64" -> new DecimalValue(0.0, ctx.line(), ctx.column());
                case "string" -> new StringValue("", ctx.line(), ctx.column());
                case "bool" -> new BoolValue(false, ctx.line(), ctx.column());
                default -> new VoidValue(ctx.line(), ctx.column());
            };
        }
        
        declararVariable(ctx.name(), val);
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.stm.BlockNode.Context ctx) {
        // ENTRADA AL BLOQUE: Empujamos un nuevo mapa de variables a la pila
        ambitos.add(new HashMap<>());
        
        if (ctx.statements() != null) {
            Visit(ctx.statements());
        }
        
        // SALIDA DEL BLOQUE: Removemos el entorno local devolviendo el control al anterior
        ambitos.remove(ambitos.size() - 1);
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.exp.RuneLiteral.Context ctx) {
        // Traduce la literal del carácter (ej: 'A') a su entero representativo ASCII (65)
        int asciiValue = ctx.text().charAt(1);
        return new IntValue(asciiValue, ctx.line(), ctx.column());
    }
}