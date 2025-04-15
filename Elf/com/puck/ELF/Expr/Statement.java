package com.puck.ELF.Expr;

import java.util.List;

public abstract class Statement{

   public interface Visitor<R> {
      R visitExpressionStatement(Expression statement);
      R visitPrintStatement(Print statement);
   }

   public static class Expression extends Statement {
      public Expression(Expr expression) {
         this.expression = expression;
      }

      public final Expr expression;
   }

   @Override
   <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStatement((Expression) this);
   }

   public static class Print extends Statement {
      public Print(Expr expression) {
         this.expression = expression;
      }

      public final Expr expression;
   }

   @Override
   <R> R accept(Visitor<R> visitor) {
         return visitor.visitPrintStatement((Print)this);
   }

//  abstract<R> R accept(Visitor<R> visitor);
}
