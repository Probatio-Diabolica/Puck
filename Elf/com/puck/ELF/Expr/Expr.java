package com.puck.ELF.Expr;

import java.util.List;

import com.puck.ELF.Token;

abstract class Expr{
   interface Visitor<R> {
      R visitBexpr(B expr);
      R visitGexpr(G expr);
      R visitLexpr(L expr);
      R visitUexpr(U expr);
   };
 static class Binary extends Expr {
    Binary(Expr left, Token operator, Expr right) {
    this.left = left;
    this.operator = operator;
    this.right = right;

    }

    final Expr left;
    final Token operator;
    final Expr right;
    }

   @Override
   <R> R accept(Visitor<R> visitor) {
      return visitor.visit BinaryExpr(this);
   }
   static class Grouping extends Expr {
      Grouping(Expr expression) {
      this.expression = expression;

      }

      final Expr expression;
      }

   @Override
   <R> R accept(Visitor<R> visitor) {
      return visitor.visit GroupingExpr(this);
   }
   static class Literal extends Expr {
      Literal(Object value) {
      this.value = value;

      }

      final Object value;
   }

   @Override
   <R> R accept(Visitor<R> visitor) {
      return visitor.visit LiteralExpr(this);
    }
   static class Unary extends Expr {
      Unary(Token operator, Expr right) {
         this.operator = operator;
         this.right = right;
      }

      final Token operator;
      final Expr right;
   }

   @Override
   <R> R accept(Visitor<R> visitor) {
      return visitor.visit UnaryExpr(this);
   }

   abstract<R> R accept(Visitor<R> visitor);
}
