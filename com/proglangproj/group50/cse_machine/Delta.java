package com.proglangproj.group50.cse_machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.proglangproj.group50.abstractsyntaxtree.ASTNode;
import com.proglangproj.group50.abstractsyntaxtree.ASTNodeType;

/**
 * Represents a lambda closure.
 * @author Raj
 */
public class Delta extends ASTNode{
  private List<String> boundVars;
  private Environment linkedEnv; //environment in effect when this Delta was pushed on to the value stack
  private Stack<ASTNode> body;
  private int index;
  
  public Delta(){
    setType(ASTNodeType.DELTA);
    boundVars = new ArrayList<String>();
  }
  
  public Delta Accept(NodeCopier Node_Copier){
    return Node_Copier.copy(this);
  }
  
  //used if the program evaluation results in a partial application
  @Override
  public String getVal(){
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
  
  public Stack<ASTNode> getBody(){
    return body;
  }
  
  public void setBody(Stack<ASTNode> body){
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
