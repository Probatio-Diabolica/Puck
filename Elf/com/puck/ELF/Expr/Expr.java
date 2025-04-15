package com.puck.ELF.Expr;

import java.util.List;

import com.puck.ELF.Token;

public abstract class Expr{
   public interface Visitor<R> {
      // R visitBexpr(B expr);
      // R visitGexpr(G expr);
      // R visitLexpr(L expr);
      // R visitUexpr(U expr);
      R visitBexpr(Binary expr);
      R visitGexpr(Grouping expr);
      R visitLexpr(Literal expr);
      R visitUexpr(Unary expr);
   };
   public static class Binary extends Expr {
      public Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
      }

      public final Expr left;
      public final Token operator;
      public final Expr right;
   }

   @Override
   public <R> R accept(Visitor<R> visitor) {
      return visitor.visitBexpr((Binary)this);
   }
   public static class Grouping extends Expr {
      public Grouping(Expr expression) {
      this.expression = expression;

      }

      public final Expr expression;
   }

   @Override
   <R> R accept(Visitor<R> visitor) {
      return visitor.visitGexpr((Grouping)this);
   }
   public static class Literal extends Expr {
      public Literal(Object value) {
      this.value = value;

      }

      public final Object value;
   }

   @Override
   <R> R accept(Visitor<R> visitor) {
      return visitor.visitLexpr((Literal)this);
    }
   public static class Unary extends Expr {
      Unary(Token operator, Expr right) {
         this.operator = operator;
         this.right = right;
      }

      public final Token operator;
      public final Expr right;
   }

   @Override
   <R> R accept(Visitor<R> visitor) {
      return visitor.visitUexpr((Unary)this);
      // return visitor.visitUnaryExpr(this);
   }

   abstract<R> R accept(Visitor<R> visitor);
}
