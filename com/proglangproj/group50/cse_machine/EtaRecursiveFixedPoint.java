package com.proglangproj.group50.cse_machine;

import com.proglangproj.group50.abstractsyntaxtree.AbstractSyntaxTreeNode;
import com.proglangproj.group50.abstractsyntaxtree.AbstractSyntaxTreeNodeType;

/**
 * Represents the fixed-point resulting from the application (Y F). We never
 * actually evaluate the fixed-point. The hope is that the program will (in the
 * recursion's base case) choose the option that doesn't have the fixed point (and
 * hence will not lead to our evaluating the fixed point again (what happens when
 * we replace YF with F (YF) i.e., EtaRecursiveFixedPoint with DeltaControlStructure EtaRecursiveFixedPoint)). If the source code creates
 * an infinite recursion, none of these tricks will save us.
 */
public class EtaRecursiveFixedPoint extends AbstractSyntaxTreeNode {
  private DeltaControlStructure delta;
  
  public EtaRecursiveFixedPoint(){
    setTypeOfASTNode(AbstractSyntaxTreeNodeType.ETA);
  }
  
  //used if the program evaluation results in a partial application
  @Override
  public String getValueOfASTNode(){
    return "[eta closure: "+delta.getBoundVars().get(0)+": "+delta.getIndex()+"]";
  }
  
  public EtaRecursiveFixedPoint acceptASTNode(NodeCopier nodeCopier){
    return nodeCopier.copy(this);
  }

  public DeltaControlStructure getDelta(){
    return delta;
  }

  public void setDelta(DeltaControlStructure delta){
    this.delta = delta;
  }
  
}
