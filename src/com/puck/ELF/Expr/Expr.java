package com.puck.ELF.Expr;


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

      R visitAssignExpr(Assign expr);
      R visitLogicalExpr(Logical expr);
      R visitVariableExpr(Variable expr);
   };
   public static class Binary extends Expr {
      public Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
      }

   @Override
   public <R> R accept(Visitor<R> visitor) {
      return visitor.visitBexpr((Binary)this);
   }

      public final Expr left;
      public final Token operator;
      public final Expr right;
   }

   
   public static class Grouping extends Expr {
      public Grouping(Expr expression) {
      this.expression = expression;

      }
      
      @Override
      public <R> R accept(Visitor<R> visitor) {
         return visitor.visitGexpr((Grouping)this);
      }

      public final Expr expression;
   }


   public static class Literal extends Expr {
      public Literal(Object value) {
      this.value = value;

      }

      @Override
      public <R> R accept(Visitor<R> visitor) {
         return visitor.visitLexpr((Literal)this);
      }
      public final Object value;
   }


   public static class Unary extends Expr {
      public Unary(Token operator, Expr right) {
         this.operator = operator;
         this.right = right;
      }

      @Override
      public <R> R accept(Visitor<R> visitor) {
         return visitor.visitUexpr((Unary)this);
         // return visitor.visitUnaryExpr(this);
      }

      public final Token operator;
      public final Expr right;
   }

   public static class Assign extends Expr{

      public Assign(Token name, Expr value) {
         this.name = name;
         this.value = value;
      }
   
      @Override
      public  <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
      }
   
      public final Token name;
      public final Expr value;

   }

   public static class Logical extends Expr{
      public Logical(Expr left, Token operator, Expr right) {
         this.left = left;
         this.operator = operator;
         this.right = right;
      }

      @Override
      public  <R> R accept(Visitor<R> visitor) {
         return visitor.visitLogicalExpr(this);
      }

      public final Expr left;
      public final Token operator;
      public final Expr right;
   }

   public static class Variable extends Expr{
      public Variable(Token name) {
         this.name = name;
      }
   
      @Override
      public  <R> R accept(Visitor<R> visitor) {
         return visitor.visitVariableExpr(this);
      }

      public final Token name;
   }

   public abstract<R> R accept(Visitor<R> visitor);
}
