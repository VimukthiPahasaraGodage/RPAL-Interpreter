package com.proglangproj.group50.cse_machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.proglangproj.group50.abstractsyntaxtree.ASTNode;
import com.proglangproj.group50.abstractsyntaxtree.ASTNodeType;

// Represents a lambda closure
public class Delta extends ASTNode{
  private List<String> bound_var_list;
  private Environment linked_Environment;
  private Stack<ASTNode> body;
  private int element;
  
  public Delta(){
    setType(ASTNodeType.DELTA);
    bound_var_list = new ArrayList<String>();
  }
  
  public Delta Accept(NodeCopier Node_Copier){
    return Node_Copier.copy(this);
  }
  
  //used if the program evaluation results in a partial application
  @Override
  public String getVal(){
    return "[lambda closure: "+ bound_var_list.get(0)+": "+ element +"]";
  }

  public List<String> getBound_var_list(){
    return bound_var_list;
  }
  
  public void addBoundVars(String boundVar){
    bound_var_list.add(boundVar);
  }
  
  public void setBound_var_list(List<String> bound_var_list){
    this.bound_var_list = bound_var_list;
  }
  
  public Stack<ASTNode> getBody(){
    return body;
  }
  
  public void setBody(Stack<ASTNode> body){
    this.body = body;
  }
  
  public int getElement(){
    return element;
  }

  public void setElement(int element){
    this.element = element;
  }

  public Environment getLinked_Environment(){
    return linked_Environment;
  }

  public void setLinked_Environment(Environment linked_Environment){
    this.linked_Environment = linked_Environment;
  }
}
