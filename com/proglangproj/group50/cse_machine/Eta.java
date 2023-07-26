package com.proglangproj.group50.cse_machine;

import com.proglangproj.group50.abstractsyntaxtree.ASTNode;
import com.proglangproj.group50.abstractsyntaxtree.ASTNodeType;

// Represents the fixed-point resulting from the application (Y F)

public class Eta extends ASTNode{
  private Delta delta;
  
  public Eta(){
    setType(ASTNodeType.ETA);
  }
  public String get_value(){
    return "[eta closure: "+delta.getBound_var_list().get(0)+": "+delta.getElement()+"]";
  }
  
  public Eta accept_Delta(NodeCopier nodeCopier){
    return nodeCopier.copy(this);
  }

  public Delta getDelta(){
    return delta;
  }

  public void setDelta(Delta delta){
    this.delta = delta;
  }
  
}
