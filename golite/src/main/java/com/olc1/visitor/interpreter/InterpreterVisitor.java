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
import com.olc1.reports.GoliteError; // Importación necesaria para atrapar los errores

public class InterpreterVisitor implements Visitor<ValueWrapper> {
    public String output = "";
    private final ValueWrapper defaultVoid = new VoidValue(-1, -1);
    
    // CONTROL DE ÁMBITOS: Pila dinámica de entornos para aislamiento de bloques locales
    private final List<Map<String, ValueWrapper>> ambitos = new ArrayList<>();
    private int profundidadCiclos = 0;

    // ---> NUEVO: Lista global para recolectar errores semánticos en frío <---
    public ArrayList<GoliteError> erroresSemanticos = new ArrayList<>();

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
        throw new RuntimeException("Variable no definida: " + nombre);
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
            
            default -> throw new RuntimeException("Operación inválida " + left.getTypeName() + " + " + right.getTypeName());
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
                if (r.value() == 0) throw new RuntimeException("Division entre cero.");
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
                    throw new RuntimeException("No se puede dividir entre cero en la operacion de modulo.");
                }
                yield new IntValue(l.value() % r.value(), l.line(), l.column());
            }
            default -> throw new RuntimeException("El operador '%' solo es valido entre tipos enteros (int).");
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
        
        for (int i = 0; i < ctx.expressions().size(); i++) {
            ValueWrapper val = Visit(ctx.expressions().get(i));
            String strOut = (val instanceof StringValue s) ? s.value() : val.toString();
            salida.append(strOut);
            if (i < ctx.expressions().size() - 1) {
                salida.append(" ");
            }
        }
        
        output += salida.toString() + "\n";
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(Statments.Context ctx) {
        for (ASTNode statment : ctx.statements) {
            if (statment == null) {
                continue;
            }
            try {
                Visit(statment);
            } catch (BreakException | ContinueException e) {
                if (profundidadCiclos > 0) {
                    throw e;
                }

                erroresSemanticos.add(new GoliteError(
                    "Semántico",
                    e instanceof BreakException ? "sentencia break fuera de un ciclo" : "sentencia continue fuera de un ciclo",
                    0,
                    0
                ));
            } catch (Exception e) {
                // Si la instrucción falla, la atrapamos, la guardamos y dejamos que el for pase a la siguiente
                erroresSemanticos.add(new GoliteError(
                    "Semántico", 
                    e.getMessage(), 
                    0, 0 // El error aparecerá en 0,0 a menos que el ASTNode exponga un método de getFila()
                ));
                
            }
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
            throw new RuntimeException("La variable '" + ctx.name + "' no está definida en este contexto.");
        }
        return val;
    }

   @Override
    public ValueWrapper visit(Assign.Context ctx) {
        ValueWrapper variableExistente = obtenerVariable(ctx.name);
        if (variableExistente == null) {
            throw new RuntimeException("La variable '" + ctx.name + "' no está definida en este contexto.");
        }
        
        ValueWrapper nuevoValor = Visit(ctx.value);
        
        if (variableExistente instanceof IntValue) {
            if (!(nuevoValor instanceof IntValue)) {
                throw new RuntimeException("No se puede asignar un " + nuevoValor.getTypeName() + " a un int.");
            }
        } else if (variableExistente instanceof DecimalValue) {
            if (nuevoValor instanceof IntValue iv) {
                nuevoValor = new DecimalValue((double) iv.value(), iv.line(), iv.column());
            } else if (!(nuevoValor instanceof DecimalValue)) {
                throw new RuntimeException("No se puede asignar un " + nuevoValor.getTypeName() + " a un float64.");
            }
        } else if (variableExistente instanceof StringValue) {
            if (!(nuevoValor instanceof StringValue)) {
                throw new RuntimeException("No se puede asignar un " + nuevoValor.getTypeName() + " a un string.");
            }
        } else if (variableExistente instanceof BoolValue) {
            if (!(nuevoValor instanceof BoolValue)) {
                throw new RuntimeException("No se puede asignar un " + nuevoValor.getTypeName() + " a un bool.");
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

        boolean leftNumeric = left instanceof IntValue || left instanceof DecimalValue;
        boolean rightNumeric = right instanceof IntValue || right instanceof DecimalValue;
        
        boolean res = false;
        switch (ctx.operator()) {
            case "==" -> {
                if (left instanceof StringValue lStr && right instanceof StringValue rStr) {
                    res = lStr.value().equals(rStr.value());
                } else if (left instanceof BoolValue lBool && right instanceof BoolValue rBool) {
                    res = lBool.value() == rBool.value();
                } else if (left instanceof NullValue || right instanceof NullValue) {
                    res = (left instanceof NullValue && right instanceof NullValue);
                } else if (leftNumeric && rightNumeric) {
                    double lVal = (left instanceof IntValue cv) ? cv.value() : ((DecimalValue) left).value();
                    double rVal = (right instanceof IntValue cv) ? cv.value() : ((DecimalValue) right).value();
                    res = lVal == rVal;
                } else {
                    throw new RuntimeException("Tipos incompatibles en comparacion: " + left.getTypeName() + " == " + right.getTypeName());
                }
            }
            case "!=" -> {
                if (left instanceof StringValue lStr && right instanceof StringValue rStr) {
                    res = !lStr.value().equals(rStr.value());
                } else if (left instanceof BoolValue lBool && right instanceof BoolValue rBool) {
                    res = lBool.value() != rBool.value();
                } else if (left instanceof NullValue || right instanceof NullValue) {
                    res = !(left instanceof NullValue && right instanceof NullValue);
                } else if (leftNumeric && rightNumeric) {
                    double lVal = (left instanceof IntValue cv) ? cv.value() : ((DecimalValue) left).value();
                    double rVal = (right instanceof IntValue cv) ? cv.value() : ((DecimalValue) right).value();
                    res = lVal != rVal;
                } else {
                    throw new RuntimeException("Tipos incompatibles en comparacion: " + left.getTypeName() + " != " + right.getTypeName());
                }
            }
            case "<"  -> {
                if (!leftNumeric || !rightNumeric) throw new RuntimeException("Comparacion invalida entre " + left.getTypeName() + " y " + right.getTypeName());
                double lVal = (left instanceof IntValue cv) ? cv.value() : ((DecimalValue) left).value();
                double rVal = (right instanceof IntValue cv) ? cv.value() : ((DecimalValue) right).value();
                res = lVal < rVal;
            }
            case ">"  -> {
                if (!leftNumeric || !rightNumeric) throw new RuntimeException("Comparacion invalida entre " + left.getTypeName() + " y " + right.getTypeName());
                double lVal = (left instanceof IntValue cv) ? cv.value() : ((DecimalValue) left).value();
                double rVal = (right instanceof IntValue cv) ? cv.value() : ((DecimalValue) right).value();
                res = lVal > rVal;
            }
            case "<=" -> {
                if (!leftNumeric || !rightNumeric) throw new RuntimeException("Comparacion invalida entre " + left.getTypeName() + " y " + right.getTypeName());
                double lVal = (left instanceof IntValue cv) ? cv.value() : ((DecimalValue) left).value();
                double rVal = (right instanceof IntValue cv) ? cv.value() : ((DecimalValue) right).value();
                res = lVal <= rVal;
            }
            case ">=" -> {
                if (!leftNumeric || !rightNumeric) throw new RuntimeException("Comparacion invalida entre " + left.getTypeName() + " y " + right.getTypeName());
                double lVal = (left instanceof IntValue cv) ? cv.value() : ((DecimalValue) left).value();
                double rVal = (right instanceof IntValue cv) ? cv.value() : ((DecimalValue) right).value();
                res = lVal >= rVal;
            }
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
        ambitos.add(new HashMap<>());
        profundidadCiclos++;

        try {
            if (ctx.init() != null) {
                Visit(ctx.init());
            }

            while (true) {
                ValueWrapper cond = Visit(ctx.condition());
                if (!(cond instanceof BoolValue b && b.value())) {
                    break;
                }

                ambitos.add(new HashMap<>());
                try {
                    Visit(ctx.body());
                } catch (BreakException ex) {
                    break; 
                } catch (ContinueException ex) {
                    // Continúa hacia el incremento
                } finally {
                    ambitos.remove(ambitos.size() - 1);
                }

                if (ctx.increment() != null) {
                    Visit(ctx.increment());
                }
            }
        } finally {
            profundidadCiclos--;
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
            throw new RuntimeException("Las funciones de strconv requieren una cadena de texto.");
        }
        
        String textoLimpio = str.value();
        try {
            if (ctx.type().equals("atoi")) {
                if (textoLimpio.contains(".")) {
                    throw new RuntimeException("strconv.Atoi no procesa flotantes: " + textoLimpio);
                }
                return new IntValue(Integer.parseInt(textoLimpio), ctx.line(), ctx.column());
            } else {
                return new DecimalValue(Double.parseDouble(textoLimpio), ctx.line(), ctx.column());
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("No se pudo convertir la cadena '" + textoLimpio + "' a numerico.");
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

    @Override
    public ValueWrapper visit(com.olc1.ast.stm.ShortDeclaration.Context ctx) {
        if (existeEnAmbitoActual(ctx.name)) {
            throw new RuntimeException("La variable '" + ctx.name + "' ya fue declarada en este ambito.");
        }
        ValueWrapper val = Visit(ctx.value);
        declararVariable(ctx.name, val);
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.stm.VarDeclaration.Context ctx) {
        if (existeEnAmbitoActual(ctx.name())) {
            throw new RuntimeException("La variable '" + ctx.name() + "' ya fue declarada en este ambito.");
        }
        
        ValueWrapper val;
        if (ctx.value() != null) {
            val = Visit(ctx.value());
            
            switch (ctx.typeStr()) {
                case "int":
                    if (!(val instanceof IntValue)) {
                        throw new RuntimeException("No se puede asignar un " + val.getTypeName() + " a un int.");
                    }
                    break;
                case "float64":
                    if (val instanceof IntValue iv) {
                        val = new DecimalValue((double) iv.value(), iv.line(), iv.column());
                    } else if (!(val instanceof DecimalValue)) {
                        throw new RuntimeException("No se puede asignar un " + val.getTypeName() + " a un float64.");
                    }
                    break;
                case "string":
                    if (!(val instanceof StringValue)) {
                        throw new RuntimeException("No se puede asignar un " + val.getTypeName() + " a un string.");
                    }
                    break;
                case "bool":
                    if (!(val instanceof BoolValue)) {
                        throw new RuntimeException("No se puede asignar un " + val.getTypeName() + " a un bool.");
                    }
                    break;
                case "rune":
                    if (!(val instanceof IntValue)) {
                        throw new RuntimeException("No se puede asignar un " + val.getTypeName() + " a un rune.");
                    }
                    break;
            }
        } else {
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
        ambitos.add(new HashMap<>());
        
        if (ctx.statements() != null) {
            Visit(ctx.statements());
        }
        
        ambitos.remove(ambitos.size() - 1);
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.stm.EmptyNode node) {
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.exp.RuneLiteral.Context ctx) {
        int asciiValue = ctx.text().charAt(1);
        return new IntValue(asciiValue, ctx.line(), ctx.column());
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.stm.SwitchNode.Context ctx) {
        ValueWrapper valorSwitch = Visit(ctx.expression);
        boolean ejecutado = false;

        for (ASTNode caso : ctx.cases) {
            CaseNode c = (CaseNode) caso;
            
            // Caso Default
            if (c.expression == null) {
                if (!ejecutado) Visit(c.instrucciones);
                break;
            }

            // Casos normales
            ValueWrapper valorCaso = Visit(c.expression);
            if (valorSwitch.toString().equals(valorCaso.toString())) {
                Visit(c.instrucciones);
                ejecutado = true;
                break; // Break implícito de GoLite
            }
        }
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.stm.CaseNode.Context ctx) {
        // Retornamos void porque la ejecución real la hace el SwitchNode arriba
        return defaultVoid; 
    }   
}