package com.proglangproj.group50.abstractsyntaxtree;

import com.proglangproj.group50.cse_machine.NodeCopier;

/**
 * AST node. Uses  first-child, next-sibling representation.
 *
 */
public class ASTNode{
  private int Source_Line_Num;
  private ASTNodeType type;
   private ASTNode Child;
  private ASTNode Sibling;
  private String val;

  // Getters and Setters
  public String getName(){
    return type.name();
  }

  public int getSource_Line_Num(){
    return Source_Line_Num;
  }

  public void setSource_Line_Num(int Source_Line_Num){
    this.Source_Line_Num = Source_Line_Num;
  }

  public ASTNodeType getType(){
    return type;
  }

  public void setType(ASTNodeType type){
    this.type = type;
  }

  public ASTNode getChild(){
    return Child;
  }

  public void setChild(ASTNode child){
    this.Child = child;
  }

  public ASTNode getSibling(){
    return Sibling;
  }

  public void setSibling(ASTNode sibling){
    this.Sibling = sibling;
  }

  public String getVal(){
    return val;
  }

  public void setVal(String val){
    this.val = val;
  }

  public ASTNode Accept(NodeCopier Node_Copier){
    return Node_Copier.copy(this);
  }

}
