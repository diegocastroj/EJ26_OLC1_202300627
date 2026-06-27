package com.olc1.visitor;

import com.olc1.ast.exp.*;
import com.olc1.ast.stm.*;

public interface Visitor<T> {
    T visit(Integers.Context ctx);
    T visit(Decimal.Context ctx);
    T visit(Paren.Context ctx);
    T visit(Add.Context ctx);
    T visit(Sub.Context ctx);
    T visit(Mul.Context ctx);
    T visit(Div.Context ctx);
    T visit(Negate.Context ctx);
    T visit(BoolLiteral.Context ctx);
    T visit(StringLiteral.Context ctx);
    T visit(VarRef.Context ctx);
    T visit(Imprimir.Context ctx);
    T visit(Assign.Context ctx);
    T visit(IfNode.Context ctx);
    T visit(Statments.Context ctx);
    T visit(com.olc1.ast.exp.RelationalOp.Context ctx);
    T visit(com.olc1.ast.exp.LogicalOp.Context ctx);
    T visit(com.olc1.ast.stm.ForNode.Context ctx);
    T visit(com.olc1.ast.stm.BreakNode node);
    T visit(com.olc1.ast.stm.ContinueNode node);
    T visit(com.olc1.ast.exp.StrConvNode.Context ctx);
    T visit(com.olc1.ast.exp.ReflectNode.Context ctx);
    T visit(com.olc1.ast.exp.Mod.Context ctx);
    T visit(com.olc1.ast.exp.NullLiteral.Context ctx);
    T visit(com.olc1.ast.stm.ShortDeclaration.Context ctx);
    T visit(com.olc1.ast.stm.VarDeclaration.Context ctx);
    T visit(com.olc1.ast.stm.BlockNode.Context ctx);
    T visit(com.olc1.ast.stm.EmptyNode node);
    T visit(com.olc1.ast.exp.RuneLiteral.Context ctx);
    T visit(com.olc1.ast.stm.SwitchNode.Context ctx);
    T visit(com.olc1.ast.stm.CaseNode.Context ctx);
    T visit(com.olc1.ast.stm.ForRangeNode.Context ctx);
    T visit(com.olc1.ast.exp.SliceNode.Context ctx);
    T visit(com.olc1.ast.exp.LenNode.Context ctx);
    T visit(com.olc1.ast.exp.AccessNode.Context ctx);
    T visit(com.olc1.ast.exp.AppendNode.Context ctx);
    T visit(com.olc1.ast.exp.SlicesIndexNode.Context ctx);
    T visit(com.olc1.ast.exp.StringsJoinNode.Context ctx);
    T visit(com.olc1.ast.stm.ReturnNode.Context ctx);
    T visit(com.olc1.ast.stm.SliceModifyNode.Context ctx);
    T visit(com.olc1.ast.stm.FuncDeclNode.Context ctx);
    T visit(com.olc1.ast.exp.FuncCallNode.Context ctx);
    
}