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

    public ArrayList<GoliteError> erroresSemanticos = new ArrayList<>();
    public java.util.ArrayList<com.olc1.reports.Simbolo> tablaSimbolos = new java.util.ArrayList<>();

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

    private void declararVariable(String nombre, ValueWrapper valor, int linea, int columna) {
        ambitos.get(ambitos.size() - 1).put(nombre, valor);
        
        String ambitoActual = (ambitos.size() == 1) ? "Global" : "Local";
        String tipoSimbolo = "Variable";
        
        if (valor.getTypeName().equals("funcion")) {
            tipoSimbolo = "Función";
        } else if (valor.getTypeName().startsWith("[]")) {
            tipoSimbolo = "Slice";
        } else if (valor instanceof com.olc1.visitor.interpreter.values.StructValue) {
            tipoSimbolo = "Struct (Instancia)";
        } 
        boolean existe = false;
        for (com.olc1.reports.Simbolo s : tablaSimbolos) {
            if (s.id.equals(nombre) && s.ambito.equals(ambitoActual)) {
                existe = true; 
                break;
            }
        }
        
        if (!existe) {
            tablaSimbolos.add(new com.olc1.reports.Simbolo(nombre, tipoSimbolo, valor.getTypeName(), ambitoActual, linea, columna));
        }
    }


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

           } catch (SemanticException e) {
                erroresSemanticos.add(new GoliteError(
                    "Semántico", 
                    e.getMessage(), 
                    e.linea, 
                    e.columna
                ));
            } catch (Exception e) {
                erroresSemanticos.add(new GoliteError("Ejecución", e.getMessage(), 0, 0));
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
            throw new SemanticException("La variable '" + ctx.name + "' no está definida.", ctx.line, ctx.column);
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
        else if (val instanceof com.olc1.visitor.interpreter.values.StructValue structVal) typeName = structVal.getTypeName();
        else if (val instanceof com.olc1.visitor.interpreter.values.SliceValue sliceVal) typeName = sliceVal.getTypeName();
        
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
        declararVariable(ctx.name, val, ctx.line, ctx.column);
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
        
        declararVariable(ctx.name(), val, ctx.line(), ctx.column());
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
    
    @Override
    public ValueWrapper visit(com.olc1.ast.stm.ForRangeNode.Context ctx) {
        // 1. Evaluamos la expresión para obtener el Slice
        ValueWrapper sliceWrapper = Visit(ctx.sliceExpr);
        
        // Verificamos que realmente sea un slice
        if (!(sliceWrapper instanceof SliceValue slice)) {
            erroresSemanticos.add(new GoliteError("Semántico", "El operador 'range' solo funciona con slices.", 0, 0));
            return defaultVoid;
        }
        
        // Extraemos la lista nativa de Java usando el método del Record
        java.util.ArrayList<ValueWrapper> lista = slice.value();
        
        // 2. Preparamos el entorno para el ciclo
        ambitos.add(new java.util.HashMap<>());
        profundidadCiclos++;
        
        try {
            // 3. Hacemos el recorrido nativo
            for (int i = 0; i < lista.size(); i++) {
                
                // Sobrescribimos el índice y el valor en el entorno actual en cada vuelta
                ambitos.get(ambitos.size() - 1).put(ctx.idIndice, new IntValue(i, 0, 0));
                ambitos.get(ambitos.size() - 1).put(ctx.idValor, lista.get(i));
                
                try {
                    // Ejecutamos las instrucciones dentro del for
                    Visit(ctx.instrucciones);
                } catch (BreakException ex) {
                    break; // Rompe el ciclo actual
                } catch (ContinueException ex) {
                    // No hace nada, simplemente deja que el for pase a la siguiente iteración (i++)
                }
            }
        } finally {
            // Limpiamos el entorno al terminar el ciclo completo
            profundidadCiclos--;
            ambitos.remove(ambitos.size() - 1);
        }
        
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.exp.SliceNode.Context ctx) {
        // Creamos la lista nativa de Java
        java.util.ArrayList<ValueWrapper> lista = new java.util.ArrayList<>();
        
        // Llenamos la lista evaluando cada expresión
        if (ctx.expresiones != null) {
            for (ASTNode exp : ctx.expresiones) {
                lista.add(Visit(exp));
            }
        }
        
        return new SliceValue(lista, ctx.tipo, 0, 0); 
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.exp.LenNode.Context ctx) {
        ValueWrapper val = Visit(ctx.expresion);
        
        if (val instanceof SliceValue slice) {
            return new IntValue(slice.value().size(), 0, 0);
        }
        
        if (val instanceof StringValue str) {
            return new IntValue(str.value().length(), 0, 0);
        }
        
        erroresSemanticos.add(new GoliteError("Semántico", "La funcion len() requiere un slice o string, se recibio: " + val.getTypeName(), 0, 0));
        return new IntValue(0, 0, 0); 
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.exp.AccessNode.Context ctx) {
        ValueWrapper arrVal = Visit(ctx.arreglo);
        
        ValueWrapper idxVal = Visit(ctx.indice);
        
        if (arrVal instanceof SliceValue slice && idxVal instanceof IntValue indice) {
            java.util.ArrayList<ValueWrapper> lista = slice.value();
            int i = indice.value();
            
            if (i >= 0 && i < lista.size()) {
                return lista.get(i);
            } else {
                erroresSemanticos.add(new GoliteError("Semántico", "Indice fuera de rango: " + i + " con longitud " + lista.size(), 0, 0));
                return new VoidValue(0,0);
            }
        }
        
        erroresSemanticos.add(new GoliteError("Semántico", "No se puede acceder al indice de un tipo no-slice.", 0, 0));
        return new VoidValue(0,0);
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.exp.AppendNode.Context ctx) {
        ValueWrapper arrVal = Visit(ctx.arreglo);
        
        ValueWrapper nuevoValor = Visit(ctx.valor);
        
        if (arrVal instanceof SliceValue slice) {
            java.util.ArrayList<ValueWrapper> nuevaLista = new java.util.ArrayList<>(slice.value());
            
            nuevaLista.add(nuevoValor);
            
            return new SliceValue(nuevaLista, slice.innerType(), 0, 0);
        }
        
        erroresSemanticos.add(new GoliteError("Semántico", "La funcion append requiere un slice como primer parametro.", 0, 0));
        return new VoidValue(0,0);
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.exp.SlicesIndexNode.Context ctx) {
        ValueWrapper arrVal = Visit(ctx.arreglo);
        ValueWrapper valorBuscado = Visit(ctx.valor);

        if (arrVal instanceof SliceValue slice) {
            java.util.ArrayList<ValueWrapper> lista = slice.value();
            
            String strBuscado = (valorBuscado instanceof StringValue s) ? s.value() : valorBuscado.toString();
            
            for (int i = 0; i < lista.size(); i++) {
                ValueWrapper elemento = lista.get(i);
                
                String strElemento = (elemento instanceof StringValue s) ? s.value() : elemento.toString();
                
                if (strElemento.equals(strBuscado)) {
                    return new IntValue(i, 0, 0); // Retorna el índice si lo encuentra
                }
            }
            return new IntValue(-1, 0, 0); // Retorna -1 si no existe
        }
        
        erroresSemanticos.add(new GoliteError("Semántico", "slices.Index requiere un slice como primer parametro.", 0, 0));
        return new IntValue(-1, 0, 0);
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.exp.StringsJoinNode.Context ctx) {
        ValueWrapper arrVal = Visit(ctx.arreglo);
        ValueWrapper delimVal = Visit(ctx.delimitador);

        if (arrVal instanceof SliceValue slice && delimVal instanceof StringValue delimitador) {
            java.util.ArrayList<ValueWrapper> lista = slice.value();
            StringBuilder resultado = new StringBuilder();
            
            for (int i = 0; i < lista.size(); i++) {
                // Si el valor es StringValue, extraemos su texto sin las comillas internas
                String strOut = (lista.get(i) instanceof StringValue s) ? s.value() : lista.get(i).toString();
                resultado.append(strOut);
                
                // Agregamos el delimitador si no es el último elemento
                if (i < lista.size() - 1) {
                    resultado.append(delimitador.value());
                }
            }
            return new StringValue(resultado.toString(), 0, 0);
        }
        erroresSemanticos.add(new GoliteError("Semántico", "strings.Join requiere un slice y un delimitador string.", 0, 0));
        return new StringValue("", 0, 0);
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.stm.ReturnNode.Context ctx) {
        ValueWrapper valorARetornar = defaultVoid; 
        
        if (ctx.expresion != null) {
            valorARetornar = Visit(ctx.expresion); 
        }
        
        throw new ReturnException(valorARetornar); 
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.stm.SliceModifyNode.Context ctx) {
        // 1. Obtenemos el slice de la memoria
        ValueWrapper variable = obtenerVariable(ctx.id);
        
        if (variable == null) {
            erroresSemanticos.add(new GoliteError("Semántico", "La variable '" + ctx.id + "' no existe.", 0, 0));
            return defaultVoid;
        }

        ValueWrapper idxVal = Visit(ctx.indice);
        ValueWrapper nuevoValor = Visit(ctx.valor);

        if (variable instanceof SliceValue slice && idxVal instanceof IntValue indice) {
            java.util.ArrayList<ValueWrapper> lista = slice.value();
            int i = indice.value();

            if (i >= 0 && i < lista.size()) {
                lista.set(i, nuevoValor); 
            } else {
                erroresSemanticos.add(new GoliteError("Semántico", "Indice fuera de rango para el slice '" + ctx.id + "'.", 0, 0));
            }
        } else {
            erroresSemanticos.add(new GoliteError("Semántico", "No se puede modificar por indice un tipo no-slice.", 0, 0));
        }

        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.stm.FuncDeclNode.Context ctx) {

        if (ambitos.size() > 1) {
            erroresSemanticos.add(new GoliteError("Semántico", "Las funciones solo pueden declararse en el ambito global.", 0, 0));
            return defaultVoid;
        }

        if (existeEnAmbitoActual(ctx.id)) {
            erroresSemanticos.add(new GoliteError("Semántico", "El identificador '" + ctx.id + "' ya esta en uso.", 0, 0));
            return defaultVoid;
        }

        java.util.HashSet<String> nombresParams = new java.util.HashSet<>();
        if (ctx.parametros != null) {
            for (com.olc1.ast.stm.Parametro param : ctx.parametros) {
                if (!nombresParams.add(param.id())) {
                    erroresSemanticos.add(new GoliteError("Semántico", "El parametro '" + param.id() + "' esta duplicado en la funcion '" + ctx.id + "'.", 0, 0));
                    return defaultVoid;
                }
            }
        }

        declararVariable(ctx.id, new FunctionValue(ctx, 0, 0), 0, 0);
        
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.exp.FuncCallNode.Context ctx) {
        ValueWrapper variable = obtenerVariable(ctx.id);
        
        if (!(variable instanceof FunctionValue funcionWrapper)) {
            erroresSemanticos.add(new GoliteError("Semántico", "El identificador '" + ctx.id + "' no es una funcion declarada.", 0, 0));
            return defaultVoid;
        }

        com.olc1.ast.stm.FuncDeclNode.Context funcion = funcionWrapper.funcion();

        if (ctx.argumentos.size() != funcion.parametros.size()) {
            erroresSemanticos.add(new GoliteError("Semántico", "La funcion '" + ctx.id + "' esperaba " + funcion.parametros.size() + " argumentos, pero recibio " + ctx.argumentos.size(), 0, 0));
            return defaultVoid;
        }

        java.util.ArrayList<ValueWrapper> valoresEvaluados = new java.util.ArrayList<>();
        for (ASTNode arg : ctx.argumentos) {
            valoresEvaluados.add(Visit(arg));
        }

        ambitos.add(new java.util.HashMap<>());

        try {
            for (int i = 0; i < funcion.parametros.size(); i++) {
                com.olc1.ast.stm.Parametro param = funcion.parametros.get(i);
                declararVariable(param.id(), valoresEvaluados.get(i), 0, 0);
            }

            if (funcion.cuerpo != null) {
                Visit(funcion.cuerpo);
            }
            
        } catch (com.olc1.ast.stm.ReturnException e) {

            String tipoEsperado = funcion.tipoRetorno;
            String tipoObtenido = e.value.getTypeName();
            
            // Si la función era void pero retornó algo
            if (tipoEsperado.equals("void") && !(e.value instanceof VoidValue)) {
                erroresSemanticos.add(new GoliteError("Semántico", "La funcion '" + ctx.id + "' no debe retornar valores.", 0, 0));
                return defaultVoid;
            }
            
            // Si la función tenía tipo, validar que coincidan (con una pequeña tolerancia para los nulos)
            if (!tipoEsperado.equals("void") && !tipoEsperado.equals(tipoObtenido) && !(e.value instanceof NullValue)) {
                erroresSemanticos.add(new GoliteError("Semántico", "La funcion '" + ctx.id + "' esperaba retornar '" + tipoEsperado + "' pero retorno '" + tipoObtenido + "'.", 0, 0));
                return defaultVoid;
            }
            return e.value;
        } finally {
            ambitos.remove(ambitos.size() - 1);
        }

        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.stm.StructDefNode.Context ctx) {
        if (ambitos.size() > 1) {
            erroresSemanticos.add(new GoliteError("Semántico", "Los structs solo pueden ser declarados en el ambito global.", 0, 0));
            return defaultVoid;
        }

        if (existeEnAmbitoActual(ctx.id)) {
            erroresSemanticos.add(new GoliteError("Semántico", "El nombre de struct '" + ctx.id + "' ya esta en uso.", 0, 0));
            return defaultVoid;
        }

        declararVariable(ctx.id, new StructBlueprint(ctx, 0, 0), 0, 0);
        
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.exp.StructCreationNode.Context ctx) {
        ValueWrapper moldeVal = obtenerVariable(ctx.id);
        
        if (!(moldeVal instanceof StructBlueprint molde)) {
            erroresSemanticos.add(new GoliteError("Semántico", "El struct '" + ctx.id + "' no ha sido definido.", 0, 0));
            return new VoidValue(0,0);
        }

        com.olc1.ast.stm.StructDefNode.Context def = molde.definicion();

        if (def.atributos.size() != ctx.valores.size()) {
            erroresSemanticos.add(new GoliteError("Semántico", "El struct '" + ctx.id + "' espera " + def.atributos.size() + " valores, pero recibio " + ctx.valores.size(), 0, 0));
            return new VoidValue(0,0);
        }

        java.util.HashMap<String, ValueWrapper> propiedades = new java.util.HashMap<>();
        
        for (int i = 0; i < def.atributos.size(); i++) {
            com.olc1.ast.stm.Atributo attr = def.atributos.get(i);
            ValueWrapper valorEvaluado = Visit(ctx.valores.get(i));
            
            propiedades.put(attr.id(), valorEvaluado);
        }

        return new StructValue(ctx.id, propiedades, 0, 0);
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.exp.StructAccessNode.Context ctx) {
        ValueWrapper objVal = Visit(ctx.objeto);

        if (objVal instanceof StructValue structObj) {
            if (structObj.propiedades().containsKey(ctx.atributo)) {
                return structObj.propiedades().get(ctx.atributo);
            } else {
                erroresSemanticos.add(new GoliteError("Semántico", "El struct '" + structObj.tipoStruct() + "' no tiene el atributo '" + ctx.atributo + "'.", 0, 0));
                return new VoidValue(0,0);
            }
        }
        
        erroresSemanticos.add(new GoliteError("Semántico", "No se puede usar el operador punto en un tipo no-struct.", 0, 0));
        return new VoidValue(0,0);
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.stm.StructModifyNode.Context ctx) {
        ValueWrapper variable = obtenerVariable(ctx.idVariable);

        if (variable == null) {
            erroresSemanticos.add(new GoliteError("Semántico", "La variable '" + ctx.idVariable + "' no existe.", 0, 0));
            return defaultVoid;
        }

        if (variable instanceof StructValue structObj) {
            if (structObj.propiedades().containsKey(ctx.atributo)) {
                ValueWrapper valorEvaluado = Visit(ctx.nuevoValor);
                
                structObj.propiedades().put(ctx.atributo, valorEvaluado);
            } else {
                erroresSemanticos.add(new GoliteError("Semántico", "El struct '" + structObj.tipoStruct() + "' no tiene el atributo '" + ctx.atributo + "'.", 0, 0));
            }
        } else {
            erroresSemanticos.add(new GoliteError("Semántico", "La variable '" + ctx.idVariable + "' no es un struct.", 0, 0));
        }

        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.exp.NamedStructLiteralNode.Context ctx) {
        // El nodo literal solo sirve de contenedor de ASTs, no necesita evaluarse solo
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.stm.CustomTypeVarDeclNode.Context ctx) {
        if (existeEnAmbitoActual(ctx.id)) {
            erroresSemanticos.add(new GoliteError("Semántico", "La variable '" + ctx.id + "' ya esta definida.", 0, 0));
            return defaultVoid;
        }

        ValueWrapper moldeVal = obtenerVariable(ctx.tipo);
        if (!(moldeVal instanceof StructBlueprint molde)) {
            erroresSemanticos.add(new GoliteError("Semántico", "El tipo de struct '" + ctx.tipo + "' no esta definido.", 0, 0));
            return defaultVoid;
        }

        com.olc1.ast.stm.StructDefNode.Context def = molde.definicion();
        java.util.HashMap<String, ValueWrapper> propiedades = new java.util.HashMap<>();

        if (ctx.expresion instanceof com.olc1.ast.exp.NamedStructLiteralNode literal) {
            
            for (java.util.Map.Entry<String, ASTNode> entry : literal.valores.entrySet()) {
                String campo = entry.getKey();
                
                boolean existeCampo = def.atributos.stream().anyMatch(a -> a.id().equals(campo));
                if (!existeCampo) {
                    erroresSemanticos.add(new GoliteError("Semántico", "El struct '" + ctx.tipo + "' no tiene un atributo '" + campo + "'.", 0, 0));
                    continue;
                }
                
                propiedades.put(campo, Visit(entry.getValue()));
            }
        } else {
            ValueWrapper val = Visit(ctx.expresion);
            if (val instanceof StructValue sv && sv.tipoStruct().equals(ctx.tipo)) {
                propiedades = new java.util.HashMap<>(sv.propiedades());
            } else {
                erroresSemanticos.add(new GoliteError("Semántico", "Tipo de asignacion no valida para el struct '" + ctx.tipo + "'.", 0, 0));
                return defaultVoid;
            }
        }

        declararVariable(ctx.id, new StructValue(ctx.tipo, propiedades, 0, 0), 0, 0);
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.stm.EmptySliceDeclNode.Context ctx) {
        // Validamos que la variable no exista ya en el entorno actual
        if (existeEnAmbitoActual(ctx.id)) {
            erroresSemanticos.add(new GoliteError("Semántico", "La variable '" + ctx.id + "' ya esta declarada.", 0, 0));
            return defaultVoid;
        }

        java.util.ArrayList<ValueWrapper> listaVacia = new java.util.ArrayList<>();

        declararVariable(ctx.id, new SliceValue(listaVacia, ctx.tipoBase, 0, 0), 0,0);

        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.exp.NamedStructCreationNode.Context ctx) {
        ValueWrapper moldeVal = obtenerVariable(ctx.id);
        
        if (!(moldeVal instanceof StructBlueprint molde)) {
            erroresSemanticos.add(new GoliteError("Semántico", "El struct '" + ctx.id + "' no esta definido.", 0, 0));
            return defaultVoid;
        }

        com.olc1.ast.stm.StructDefNode.Context def = molde.definicion();
        java.util.HashMap<String, ValueWrapper> propiedades = new java.util.HashMap<>();
        
        for (java.util.Map.Entry<String, ASTNode> entry : ctx.valores.entrySet()) {
            String campo = entry.getKey();
            
            boolean existeCampo = def.atributos.stream().anyMatch(a -> a.id().equals(campo));
            if (!existeCampo) {
                erroresSemanticos.add(new GoliteError("Semántico", "El struct '" + ctx.id + "' no tiene un atributo '" + campo + "'.", 0, 0));
                continue;
            }
            
            propiedades.put(campo, Visit(entry.getValue()));
        }

        return new StructValue(ctx.id, propiedades, 0, 0);
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.exp.MethodCallNode.Context ctx) {
        // 1. Evaluamos al dueño del método (ej. miInstancia)
        ValueWrapper targetObj = Visit(ctx.node.target);
        
        // 2. Reconstruimos el nombre mangleado
        String nombreMangleado = targetObj.getTypeName() + "_" + ctx.node.methodName;
        
        ValueWrapper funcMemoria = obtenerVariable(nombreMangleado);
        if (!(funcMemoria instanceof FunctionValue funcion)) {
            erroresSemanticos.add(new GoliteError("Semántico", "El tipo '" + targetObj.getTypeName() + "' no tiene un metodo '" + ctx.node.methodName + "'.", 0, 0));
            return defaultVoid;
        }

        // 3. Abrimos el entorno
        java.util.Map<String, ValueWrapper> nuevoAmbito = new java.util.HashMap<>();
        ambitos.add(nuevoAmbito);

        // 4. ¡LA CORRECCIÓN PARA EL RECORD!
        var funcCtx = funcion.funcion(); 

        // Inyectamos la instancia como el PRIMER parámetro
        String nombreParamReceiver = funcCtx.parametros.get(0).id();
        declararVariable(nombreParamReceiver, targetObj, 0, 0);

        // 5. Evaluamos e inyectamos el resto de los parámetros enviados
        if (ctx.node.args != null) {
            for (int i = 0; i < ctx.node.args.size(); i++) {
                ValueWrapper argVal = Visit(ctx.node.args.get(i));
                String paramName = funcCtx.parametros.get(i + 1).id();
                declararVariable(paramName, argVal, 0, 0);
            }
        }

        ValueWrapper retorno = defaultVoid;
        try {
            Visit(funcCtx.cuerpo); 
        } catch (ReturnException e) {
            retorno = e.value; 
        }

        ambitos.remove(ambitos.size() - 1);
        
        return retorno;
    }
   
         
}