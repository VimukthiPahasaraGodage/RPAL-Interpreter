package com.proglangproj.group50.cse_machine;

import com.proglangproj.group50.abstractsyntaxtree.ASTNode;
import com.proglangproj.group50.abstractsyntaxtree.ASTNodeType;

public class Tuple extends ASTNode{
  
  public Tuple(){
    setType(ASTNodeType.TUPLE);
  }

  public String get_value(){
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
  
  public Tuple accept_Delta(NodeCopier nodeCopier){
    return nodeCopier.copy(this);
  }
  
}
