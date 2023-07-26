package com.proglangproj.group50.abstractsyntaxtree;

public enum AbstractSyntaxTreeNodeType {
    /**
     * Expressions
     */
    LET("let"),
    LAMBDA("lambda"),
    WHERE("where"),

    /**
     * Tuple expressions
     */
    TAU("tau"),
    AUG("aug"),
    CONDITIONAL("->"),

    /**
     * General
     */
    IDENTIFIER("<ID:%s>"),
    STRING("<STR:'%s'>"),
    INTEGER("<INT:%s>"),

    /**
     * Arithmetic Expressions
     */
    PLUS("+"),
    MINUS("-"),
    NEG("neg"),
    MULT("*"),
    DIV("/"),
    EXP("**"),
    AT("@"),

    /**
     * Rators and Rands
     */
    GAMMA("gamma"),
    TRUE("<true>"),
    FALSE("<false>"),
    NIL("<nil>"),
    DUMMY("<dummy>"),

    /**
     * Definitions
     */
    WITHIN("within"),
    SIMULTDEF("and"),
    REC("rec"),
    EQUAL("="),
    FCNFORM("function_form"),

    /**
     * Variables
     */
    PAREN("<()>"),
    COMMA(","),

    /**
     * Boolean Expressions
     */
    OR("or"),
    AND("&"),
    NOT("not"),
    GR("gr"),
    GE("ge"),
    LS("ls"),
    LE("le"),
    EQ("eq"),
    NE("ne"),

    /**
     * YSTAR - rec
     */
    YSTAR("<Y*>"),

    /**
     * For program evaluation
     */
    BETA(""),
    DELTA(""),
    ETA(""),
    TUPLE("");

    private final String printNameOfASTNode; //used for printing AbstractSyntaxTree representation

    AbstractSyntaxTreeNodeType(String name) {
        printNameOfASTNode = name;
    }

    public String getPrintNameOfASTNode() {
        return printNameOfASTNode;
    }
}
