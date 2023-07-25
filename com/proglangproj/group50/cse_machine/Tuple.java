package com.proglangproj.group50.cse_machine;

import com.proglangproj.group50.abstractsyntaxtree.ASTNode;
import com.proglangproj.group50.abstractsyntaxtree.ASTNodeType;

public class Tuple extends ASTNode{
  
  public Tuple(){
    setType(ASTNodeType.TUPLE);
  }
  
  @Override
  public String getVal(){
    ASTNode childNode = getChild();
    if(childNode==null)
      return "nil";
    
    String printValue = "(";
    while(childNode.getSibling()!=null){
      printValue += childNode.getVal() + ", ";
      childNode = childNode.getSibling();
    }
    printValue += childNode.getVal() + ")";
    return printValue;
  }
  
  public Tuple Accept(NodeCopier Node_Copier){
    return Node_Copier.copy(this);
  }
  
}
