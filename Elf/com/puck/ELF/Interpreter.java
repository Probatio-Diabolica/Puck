package com.puck.ELF;


import com.puck.ELF.Expr.Statement;
import com.puck.ELF.Expr.Statement.Block;
import com.puck.ELF.Expr.Statement.Expression;
import com.puck.ELF.Expr.Statement.Loop;
import com.puck.ELF.Expr.Statement.Print;

import java.util.List;

import com.puck.ELF.Expr.Expr;
import com.puck.ELF.Expr.Expr.Assign;
import com.puck.ELF.Expr.Expr.Binary;
import com.puck.ELF.Expr.Expr.Grouping;
import com.puck.ELF.Expr.Expr.Literal;
import com.puck.ELF.Expr.Expr.Logical;
import com.puck.ELF.Expr.Expr.Unary;
import com.puck.ELF.Expr.Expr.Variable;

public class Interpreter implements Expr.Visitor<Object>,Statement.Visitor<Object>{
    private PuckEnv env = new PuckEnv();


    public void interpret(Expr expression){
        try{
            Object value = evaluate(expression);
            System.out.println(ConvertToString(value));
        }catch(RuntimeError error)
        {
            ELF.runtimeError(error);
        }
    }
    
    public void interpret(List<Statement> statements, boolean print)
    {
        try 
        {
            for (Statement statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            ELF.runtimeError(error);
        }
    }

    public void executeBlock(List<Statement> statements, PuckEnv environment){
        PuckEnv previous = this.env;
        try {
            this.env = environment;

            for (Statement statement : statements) {
                execute(statement);
            }
        } finally {
            this.env = previous;
        }
    }
    
    @Override
    public Object visitAssignExpr(Assign expr) {
        return null;
    }

    @Override
    public Object visitBlockStmt(Block statement) {
        executeBlock(statement.statements, new PuckEnv(env));
        return null;
    }


    @Override
    public Object visitBexpr(Binary expr) { //binary expressions eg a + b , where a & b are vars
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch(expr.operator.type){
            ////Comparasion ops
            case GREATER: 
                checkNumberOperand(expr.operator, right);
                return (double) left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperand(expr.operator, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperand(expr.operator, right);
                return (double) left < (double)right;
            case LESS_EQUAL:
                checkNumberOperand(expr.operator, right);
                return (double) left <= (double) right;
            case BANG_EQUAL:
                checkNumberOperand(expr.operator, right);
                return !isEqual(left,right);
            case EQUAL_EQUAL:
                checkNumberOperand(expr.operator, right);
                return isEqual(left,right);
            ////Arithematic ops
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return (double)left - (double)right;
            case PLUS:
                checkNumberOperand(expr.operator, right);
                if(left instanceof Double && right instanceof Double){
                    return (double)left + (double)right;
                }
                if(left instanceof String && right instanceof String){
                    return (String) left + (String) right;
                }
                throw new RuntimeError(expr.operator, "operands must be of same type");
            case SLASH:
                checkNumberOperand(expr.operator, right);
                return (double)left / (double) right;
            case STAR:
                checkNumberOperand(expr.operator, right);
                return (double)left * (double) right;
            default:
                break;
        }

        return null;
    }

    @Override
    public Object visitExpressionStatement(Expression statement) {
        evaluate(statement.expression);
        return null;
    }


    @Override
    public Object visitPrintStatement(Print statement) {
        Object value = evaluate(statement.expression);
        System.out.println(ConvertToString(value));
        return null;        
    }

    @Override
    public Object visitGexpr(Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLexpr(Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Logical expr) {
        return null;
    }

    @Override
    public Object visitUexpr(Unary expr) {
        Object right = evaluate(expr.right);
        switch(expr.operator.type){
            case BANG:
                return !isTruthy(right); //This is peak naming, Don't laugh
            case MINUS:
                return -(double)right;
        }        
        return null;
    }

    private void checkNumberOperand(Token operator, Object operand){
        if(operand instanceof Double) return;
        throw new RuntimeError(operator,"Operand must  must be a number.");
    }
    private boolean isTruthy(Object object) {
        if(object == null) return false;
        if(object instanceof Boolean) return (boolean) object;
        return true;
    }

    private Object evaluate(Expr expr){
        return expr.accept(this);
    }

    private String ConvertToString(Object object) {
        if(object == null) return "nil";

        if(object instanceof Double){
            String text = ((Double)object).toString();
            if(text.endsWith(".0")){
                text = text.substring(0, text.length()-2);
            }
            return text;
        }
        return object.toString();
    }

    private boolean isEqual(Object left, Object right) {
        if(left==null && right == null) return true;
        if(left==null) return false;
        return left.equals(right);
    }

    private void execute(Statement stmt){
        stmt.accept(this);
    }

    @Override
    public Object visitVariableExpr(Variable expr) {
        return env.get(expr.name);
    }


    @Override
    public Object visitIfStmt(Statement.Conditionals conditionalStatement) {
        if (isTruthy(evaluate(conditionalStatement.condition))) {
            execute(conditionalStatement.thenBranch);
        } else if (conditionalStatement.elseBranch != null) {
            execute(conditionalStatement.elseBranch);
        }
        return null;
    }

    @Override
    public Object visitVarStmt(Statement.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        env.define(stmt.name.lexem, value);
        return null;
    }

    @Override
    public Object visitWhileStmt(Loop stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }

}
