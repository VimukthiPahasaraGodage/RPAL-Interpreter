package com.proglangproj.group50.cse_machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.proglangproj.group50.abstractsyntaxtree.AbstractSyntaxTreeNode;
import com.proglangproj.group50.abstractsyntaxtree.AbstractSyntaxTreeNodeType;

/**
 * Represents a lambda closure.
 */
public class DeltaControlStructure extends AbstractSyntaxTreeNode {
  private List<String> boundVars;
  private Environment linkedEnv; //environment in effect when this DeltaControlStructure was pushed on to the value stack
  private Stack<AbstractSyntaxTreeNode> body;
  private int index;
  
  public DeltaControlStructure(){
    setTypeOfASTNode(AbstractSyntaxTreeNodeType.DELTA);
    boundVars = new ArrayList<String>();
  }
  
  public DeltaControlStructure acceptASTNode(NodeCopier nodeCopier){
    return nodeCopier.copy(this);
  }
  
  //used if the program evaluation results in a partial application
  @Override
  public String getValueOfASTNode(){
    return "[lambda closure: "+boundVars.get(0)+": "+index+"]";
  }

  public List<String> getBoundVars(){
    return boundVars;
  }
  
  public void addBoundVars(String boundVar){
    boundVars.add(boundVar);
  }
  
  public void setBoundVars(List<String> boundVars){
    this.boundVars = boundVars;
  }
  
  public Stack<AbstractSyntaxTreeNode> getBody(){
    return body;
  }
  
  public void setBody(Stack<AbstractSyntaxTreeNode> body){
    this.body = body;
  }
  
  public int getIndex(){
    return index;
  }

  public void setIndex(int index){
    this.index = index;
  }

  public Environment getLinkedEnv(){
    return linkedEnv;
  }

  public void setLinkedEnv(Environment linkedEnv){
    this.linkedEnv = linkedEnv;
  }
}
