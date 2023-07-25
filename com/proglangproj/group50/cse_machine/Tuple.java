package com.proglangproj.group50.cse_machine;

import com.proglangproj.group50.abstractsyntaxtree.ASTNode;
import com.proglangproj.group50.abstractsyntaxtree.ASTNodeType;

public class Tuple extends ASTNode{
  
  public Tuple(){
    setTypeOfASTNode(ASTNodeType.TUPLE);
  }
  
  @Override
  public String getValueOfASTNode(){
    ASTNode childNode = getChildOfASTNode();
    if(childNode==null)
      return "nil";
    
    String printValue = "(";
    while(childNode.getSiblingOfASTNode()!=null){
      printValue += childNode.getValueOfASTNode() + ", ";
      childNode = childNode.getSiblingOfASTNode();
    }
    printValue += childNode.getValueOfASTNode() + ")";
    return printValue;
  }
  
  public Tuple acceptASTNode(NodeCopier nodeCopier){
    return nodeCopier.copy(this);
  }
  
}
