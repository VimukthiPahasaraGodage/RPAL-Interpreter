package com.proglangproj.group50.cse_machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.proglangproj.group50.abstractsyntaxtree.AbstractSyntaxTreeNode;

/**
 * Class to make copies of nodes on value stack. Used to pass back copies of
 * environment bindings so that later uses of those bindings are not affected
 * by any changes made in any earlier deltas.
 */
public class NodeCopier{
  
  public AbstractSyntaxTreeNode copy(AbstractSyntaxTreeNode astNode){
    AbstractSyntaxTreeNode copy = new AbstractSyntaxTreeNode();
    if(astNode.getChildOfASTNode()!=null)
      copy.setChildOfASTNode(astNode.getChildOfASTNode().acceptASTNode(this));
    if(astNode.getSiblingOfASTNode()!=null)
      copy.setSiblingOfASTNode(astNode.getSiblingOfASTNode().acceptASTNode(this));
    copy.setTypeOfASTNode(astNode.getTypeOfASTNode());
    copy.setValueOfASTNode(astNode.getValueOfASTNode());
    copy.setLineNumberOfSourceFile(astNode.getLineNumberOfSourceFile());
    return copy;
  }
  
  public BetaConditionalEvaluation copy(BetaConditionalEvaluation beta){
    BetaConditionalEvaluation copy = new BetaConditionalEvaluation();
    if(beta.getChildOfASTNode()!=null)
      copy.setChildOfASTNode(beta.getChildOfASTNode().acceptASTNode(this));
    if(beta.getSiblingOfASTNode()!=null)
      copy.setSiblingOfASTNode(beta.getSiblingOfASTNode().acceptASTNode(this));
    copy.setTypeOfASTNode(beta.getTypeOfASTNode());
    copy.setValueOfASTNode(beta.getValueOfASTNode());
    copy.setLineNumberOfSourceFile(beta.getLineNumberOfSourceFile());
    
    Stack<AbstractSyntaxTreeNode> thenBodyCopy = new Stack<AbstractSyntaxTreeNode>();
    for(AbstractSyntaxTreeNode thenBodyElement: beta.getThenBody()){
      thenBodyCopy.add(thenBodyElement.acceptASTNode(this));
    }
    copy.setThenBody(thenBodyCopy);
    
    Stack<AbstractSyntaxTreeNode> elseBodyCopy = new Stack<AbstractSyntaxTreeNode>();
    for(AbstractSyntaxTreeNode elseBodyElement: beta.getElseBody()){
      elseBodyCopy.add(elseBodyElement.acceptASTNode(this));
    }
    copy.setElseBody(elseBodyCopy);
    
    return copy;
  }
  
  public EtaRecursiveFixedPoint copy(EtaRecursiveFixedPoint eta){
    EtaRecursiveFixedPoint copy = new EtaRecursiveFixedPoint();
    if(eta.getChildOfASTNode()!=null)
      copy.setChildOfASTNode(eta.getChildOfASTNode().acceptASTNode(this));
    if(eta.getSiblingOfASTNode()!=null)
      copy.setSiblingOfASTNode(eta.getSiblingOfASTNode().acceptASTNode(this));
    copy.setTypeOfASTNode(eta.getTypeOfASTNode());
    copy.setValueOfASTNode(eta.getValueOfASTNode());
    copy.setLineNumberOfSourceFile(eta.getLineNumberOfSourceFile());
    
    copy.setDelta(eta.getDelta().acceptASTNode(this));
    
    return copy;
  }
  
  public DeltaControlStructure copy(DeltaControlStructure delta){
    DeltaControlStructure copy = new DeltaControlStructure();
    if(delta.getChildOfASTNode()!=null)
      copy.setChildOfASTNode(delta.getChildOfASTNode().acceptASTNode(this));
    if(delta.getSiblingOfASTNode()!=null)
      copy.setSiblingOfASTNode(delta.getSiblingOfASTNode().acceptASTNode(this));
    copy.setTypeOfASTNode(delta.getTypeOfASTNode());
    copy.setValueOfASTNode(delta.getValueOfASTNode());
    copy.setIndex(delta.getIndex());
    copy.setLineNumberOfSourceFile(delta.getLineNumberOfSourceFile());
    
    Stack<AbstractSyntaxTreeNode> bodyCopy = new Stack<AbstractSyntaxTreeNode>();
    for(AbstractSyntaxTreeNode bodyElement: delta.getBody()){
      bodyCopy.add(bodyElement.acceptASTNode(this));
    }
    copy.setBody(bodyCopy);
    
    List<String> boundVarsCopy = new ArrayList<String>();
    boundVarsCopy.addAll(delta.getBoundVars());
    copy.setBoundVars(boundVarsCopy);
    
    copy.setLinkedEnv(delta.getLinkedEnv());
    
    return copy;
  }
  
  public Tuple copy(Tuple tuple){
    Tuple copy = new Tuple();
    if(tuple.getChildOfASTNode()!=null)
      copy.setChildOfASTNode(tuple.getChildOfASTNode().acceptASTNode(this));
    if(tuple.getSiblingOfASTNode()!=null)
      copy.setSiblingOfASTNode(tuple.getSiblingOfASTNode().acceptASTNode(this));
    copy.setTypeOfASTNode(tuple.getTypeOfASTNode());
    copy.setValueOfASTNode(tuple.getValueOfASTNode());
    copy.setLineNumberOfSourceFile(tuple.getLineNumberOfSourceFile());
    return copy;
  }
}
