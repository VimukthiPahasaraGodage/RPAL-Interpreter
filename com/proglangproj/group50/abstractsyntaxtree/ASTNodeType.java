package com.proglangproj.group50.abstractsyntaxtree;

/**
 * Type of AST node. As specified in RPAL grammar.

 */
public enum ASTNodeType{
  //General
  IDENTIFIER("<ID:%s>"),
  STRING("<STR:'%s'>"),
  INTEGER("<INT:%s>"),
  
  //Expressions
  LET("let"),
  LAMBDA("lambda"),
  WHERE("where"),
  //Boolean Expressions
  OR("or"),
  AND("&"),
  NOT("not"),
  GR("gr"),
  GE("ge"),
  LS("ls"),
  LE("le"),
  EQ("eq"),
  NE("ne"),

  //Tuple expressions
  TAU("tau"),
  AUG("aug"),
  CONDITIONAL("->"),

  
  //Arithmetic Expressions
  PLUS("+"),
  MINUS("-"),
  NEG("neg"),
  MULT("*"),
  DIV("/"),
  EXP("**"),
  AT("@"),

  //Definitions
  WITHIN("within"),
  SIMULTDEF("and"),
  REC("rec"),
  EQUAL("="),
  FCNFORM("function_form"),

  //Rators and Rands
  GAMMA("gamma"),
  TRUE("<true>"),
  FALSE("<false>"),
  NIL("<nil>"),
  DUMMY("<dummy>"),

  //Variables
  PAREN("<()>"),
  COMMA(","),
  
  //Post-standardize
  YSTAR("<Y*>"),
  
  //not in AST or ST, For program evaluation only
  BETA(""),
  DELTA(""),
  ETA(""),
  TUPLE("");
  
  private String printName; //use to  print AST
  
  private ASTNodeType(String name){
    printName = name;
  }

  public String getPrintName(){
    return printName;
  }
}
