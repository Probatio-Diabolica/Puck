package com.puck.ELF.Expr;

import com.puck.ELF.Expr.Expr.Assign;
import com.puck.ELF.Expr.Expr.Logical;
import com.puck.ELF.Expr.Expr.Variable;

public class AstPrinter  implements Expr.Visitor<String>{
    public String print(Expr expr){
        return expr.accept(this);
    }

    @Override
    public String visitBexpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexem,expr.left,expr.right);
    }

    @Override
    public String visitGexpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLexpr(Expr.Literal expr) {
        if(expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUexpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexem,expr.right);
    }

    private String parenthesize(String name, Expr... exprs){
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        
        for(Expr expr: exprs)
        {
            builder.append(" ");
            builder.append(expr.accept(this));
        } 
        
        builder.append(")");

        return builder.toString();
    }

    @Override
    public String visitAssignExpr(Assign expr) {
        return "";
    }

    @Override
    public String visitLogicalExpr(Logical expr) {
        return "";
    }

    @Override
    public String visitVariableExpr(Variable expr) {
        return "";
    }

}
