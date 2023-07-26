package com.proglangproj.group50.cse_machine;

import java.util.Stack;

import com.proglangproj.group50.abstractsyntaxtree.ASTNode;
import com.proglangproj.group50.abstractsyntaxtree.ASTNodeType;

public class Beta extends ASTNode{
  private Stack<ASTNode> thenNode;
  private Stack<ASTNode> elseNode;
  
  public Beta(){
    setType(ASTNodeType.BETA);
    thenNode = new Stack<ASTNode>();
    elseNode = new Stack<ASTNode>();
  }
  
  public Beta Accept(NodeCopier Node_Copier){
    return Node_Copier.copy(this);
  }

  public Stack<ASTNode> getThenNode(){
    return thenNode;
  }

  public Stack<ASTNode> getElseNode(){
    return elseNode;
  }

  public void setThenNode(Stack<ASTNode> thenNode){
    this.thenNode = thenNode;
  }

  public void setElseNode(Stack<ASTNode> elseNode){
    this.elseNode = elseNode;
  }
  
}
