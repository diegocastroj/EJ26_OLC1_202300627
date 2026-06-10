package com.olc1.visitor.interpreter;

import java.util.HashMap;
import java.util.Map;

import com.olc1.ast.ASTNode;
import com.olc1.ast.stm.*;
import com.olc1.ast.exp.*;
import com.olc1.visitor.Visitor;
import com.olc1.visitor.interpreter.values.*;

public class InterpreterVisitor implements Visitor<ValueWrapper> {
    public String output = "";
    private final ValueWrapper defaultVoid = new VoidValue(-1, -1);
    private final Map<String, ValueWrapper> variables = new HashMap<>();

    public ValueWrapper Visit(ASTNode node) {
        return node.accept(this);
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
            default -> throw new RuntimeException("Operacion invalida: " + left.getTypeName() + " + " + right.getTypeName());
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
            case IntValue     l when right instanceof IntValue     r -> new IntValue(l.value() / r.value(), l.line(), l.column());
            case IntValue     l when right instanceof DecimalValue r -> new DecimalValue(l.value() / r.value(), l.line(), l.column());
            case DecimalValue l when right instanceof IntValue     r -> new DecimalValue(l.value() / r.value(), l.line(), l.column());
            case DecimalValue l when right instanceof DecimalValue r -> new DecimalValue(l.value() / r.value(), l.line(), l.column());
            default -> throw new RuntimeException("Operacion invalida: " + left.getTypeName() + " / " + right.getTypeName());
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
        ValueWrapper value = Visit(ctx.expression);
        output += value.toString() + "\n";
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
        ValueWrapper val = variables.get(ctx.name);
        if (val == null) throw new RuntimeException("Variable no definida: " + ctx.name);
        return val;
    }

    @Override
    public ValueWrapper visit(Assign.Context ctx) {
        ValueWrapper val = Visit(ctx.value);
        variables.put(ctx.name, val);
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(IfNode.Context ctx) {
        ValueWrapper cond = Visit(ctx.condition());
        
        if (cond instanceof BoolValue b && b.value()) {
            Visit(ctx.body());
        } else if (ctx.elseBody() != null) {
            // Si la condición fue falsa y existe un bloque else, lo ejecutamos
            Visit(ctx.elseBody());
        }
        
        return defaultVoid;
    }

    @Override
    public ValueWrapper visit(com.olc1.ast.exp.RelationalOp.Context ctx) {
        ValueWrapper left  = Visit(ctx.left());
        ValueWrapper right = Visit(ctx.right());

        // Normalizamos valores numéricos por si vienen mezclados enteros y decimales
        double lVal = (left instanceof IntValue cv) ? cv.value() : (left instanceof DecimalValue cv) ? cv.value() : 0;
        double rVal = (right instanceof IntValue cv) ? cv.value() : (right instanceof DecimalValue cv) ? cv.value() : 0;
        
        boolean res = false;
        switch (ctx.operator()) {
            case "==" -> {
                if (left instanceof StringValue lStr && right instanceof StringValue rStr) {
                    res = lStr.value().equals(rStr.value());
                } else if (left instanceof BoolValue lBool && right instanceof BoolValue rBool) {
                    res = lBool.value() == rBool.value();
                } else {
                    res = lVal == rVal;
                }
            }
            case "!=" -> {
                if (left instanceof StringValue lStr && right instanceof StringValue rStr) {
                    res = !lStr.value().equals(rStr.value());
                } else if (left instanceof BoolValue lBool && right instanceof BoolValue rBool) {
                    res = lBool.value() != rBool.value();
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
        
        // Manejo del operador unario NOT (!)
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
}
