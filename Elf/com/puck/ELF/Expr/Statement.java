package com.puck.ELF.Expr;

import java.util.List;

import com.puck.ELF.Token;

public abstract class Statement{

   public interface Visitor<R> {
      R visitExpressionStatement(Expression expression);
      R visitPrintStatement(Print printable);

      R visitBlockStmt(Block statement);
      R visitIfStmt(Conditionals conditionalStatement);
      R visitVarStmt(Var stmt);
      R visitWhileStmt(Loop stmt);
   }

   public static class Expression extends Statement {
      public Expression(Expr expression) {
         this.expression = expression;
      }

      @Override
      public <R> R accept(Visitor<R> visitor) {
         return visitor.visitExpressionStatement((Expression) this);
      }

      public final Expr expression;
   }



   public static class Print extends Statement {
      public Print(Expr expression) {
         this.expression = expression;
      }

      @Override
      public <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStatement((Print)this);
      }

      public final Expr expression;
   }

public static class Block extends Statement{
   public Block(List<Statement> statements) {
      this.statements = statements;
   }

   @Override
   public  <R> R accept(Visitor<R> visitor) {
      return visitor.visitBlockStmt(this);
   }

   public final List<Statement> statements;
}
public static class Conditionals extends Statement{
   public Conditionals(Expr condition, Statement thenBranch, Statement elseBranch) {
      this.condition = condition;
      this.thenBranch = thenBranch;
      this.elseBranch = elseBranch;
   }

   @Override
   public  <R> R accept(Visitor<R> visitor) {
      return visitor.visitIfStmt(this);
   }

   public final Expr condition;
   public final Statement thenBranch;
   public final Statement elseBranch;
}
public static class Loop extends Statement{
   public Loop(Expr condition, Statement body) {
      this.condition = condition;
      this.body = body;
    }

    @Override
  public  <R> R accept(Visitor<R> visitor) {
      return visitor.visitWhileStmt(this);
    }

   public final Expr condition;
   public final Statement body;
}
public static class Var extends Statement{
   public Var(Token name, Expr initializer) {
      this.name = name;
      this.initializer = initializer;
   }

   @Override
   public  <R> R accept(Visitor<R> visitor) {
      return visitor.visitVarStmt(this);
   }

   public final Token name;
   public final Expr initializer;
}

   public abstract<R> R accept(Visitor<R> visitor);
}
