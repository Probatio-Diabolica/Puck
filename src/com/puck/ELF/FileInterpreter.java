package com.puck.ELF;

import java.util.ArrayList;
import java.util.List;

import com.puck.ELF.Expr.Expr;
import com.puck.ELF.Expr.Expr.Binary;
import com.puck.ELF.Expr.Expr.Grouping;
import com.puck.ELF.Expr.Expr.Literal;
import com.puck.ELF.Expr.Expr.Unary;
import com.puck.ELF.Expr.Statement;
import com.puck.ELF.Expr.Statement.Expression;
import com.puck.ELF.Expr.Statement.Loop;
import com.puck.ELF.Expr.Statement.Print;
import com.puck.ELF.Expr.Statement.Var;

public class FileInterpreter implements Expr.Visitor<Object>, Statement.Visitor<Void> {
    final PuckEnv globals = new PuckEnv(); //global scope
    private PuckEnv environment = globals; //local scope

    FileInterpreter(){
        globals.define("clock", new CallableFunction() {
            @Override
            public int ParamLength() 
            { 
                return 0; 
            }

            @Override
            public Object call(FileInterpreter interpreter, List<Object> args){
                return (double)System.currentTimeMillis()/1000.0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double)System.currentTimeMillis() / 1000.0;
            }
            
            @Override
            public String toString() 
            { 
                return "<native fn>"; 
            }
            
        });
    }

    void interpret(List<Statement> statements, boolean print) {
        try {
            for (Statement statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            ELF.runtimeError(error);
        }
    }

    private void execute(Statement stmt) {
        stmt.accept(this);
    }
    void executeBlock(List<Statement> statements,
                      PuckEnv environment) {
        PuckEnv previous = this.environment;
        try {
            this.environment = environment;

            for (Statement statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitBlockStmt(Statement.Block stmt) {
        executeBlock(stmt.statements, new PuckEnv(environment));
        return null;
    }

    private String ConvertToString(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }


    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }

   

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator,
                                    Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;

        throw new RuntimeError(operator, "Operands must be numbers.");
    }


    @Override
    public Void visitIfStmt(Statement.Conditionals stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }



    @Override
    public Void visitWhileStmt(Loop stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Void visitExpressionStatement(Expression expression) {
        evaluate(expression.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Statement.Function stmt) {
        ElfFunction function = new ElfFunction(stmt);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitPrintStatement(Print printable) {
        Object value = evaluate(printable.expression);
        System.out.println(ConvertToString(value));
        return null;
    }

    @Override
    public Void visitVarStmt(Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.name.lexem, value);
        return null;
    }

    @Override
    public Object visitBexpr(Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case MINUS:
                checkNumberOperands(expr.operator, left, right);

                return (double)left - (double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }

                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }

                throw new RuntimeError(expr.operator,
                        "Operands must be two numbers or two strings.");
            case BANG_EQUAL: return !isEqual(left, right);
            case EQUAL_EQUAL: return isEqual(left, right);
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double)left / (double)right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
        }

        // Unreachable.
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
    public Object visitUexpr(Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
        }

        // Unreachable.
        return null;
    }

        public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> args = new ArrayList<>();
        for(Expr argument : expr.arguments()){
            args.add(evaluate(argument));
        }

        //in order to avoid cases like "do the addition function ()".
        if(!(callee instanceof CallableFunction)){
            throw new RuntimeError(expr.paren, "Attempted to call a non-callable value. " +
        "Only functions and classes can be called like '()'. " +
        "Got: " + ConvertToString(callee) + " instead.");
        }

        CallableFunction func = (CallableFunction)callee;
        
        if(args.size() != func.ParamLength()){
            throw new RuntimeError(expr.paren, String.format(
                "Function '%s' expected %d argument%s but got %d.",
                func, 
                func.ParamLength(), 
                func.ParamLength() == 1 ? "" : "s", 
                args.size()
            )
            );
        }
        
        return func.call(this,args);
    }
}


