package com.proglangproj.group50.cse_machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.proglangproj.group50.abstractsyntaxtree.ASTNode;

// Class to make copies of nodes on value stack

public class NodeCopier{
  
  public ASTNode copy(ASTNode astNode){
    ASTNode copy = new ASTNode();
    if(astNode.getChild()!=null)
      copy.setChild(astNode.getChild().Accept(this));
    if(astNode.getSibling()!=null)
      copy.setSibling(astNode.getSibling().Accept(this));
    copy.setType(astNode.getType());
    copy.setVal(astNode.getVal());
    copy.setSource_Line_Num(astNode.getSource_Line_Num());
    return copy;
  }
  
  public Beta copy(Beta beta){
    Beta copy = new Beta();
    if(beta.getChild()!=null)
      copy.setChild(beta.getChild().Accept(this));
    if(beta.getSibling()!=null)
      copy.setSibling(beta.getSibling().Accept(this));
    copy.setType(beta.getType());
    copy.setVal(beta.getVal());
    copy.setSource_Line_Num(beta.getSource_Line_Num ());
    
    Stack<ASTNode> thenNodeCopy = new Stack<ASTNode>();
    for(ASTNode thenBodyElement: beta.getThenNode()){
      thenNodeCopy.add(thenBodyElement.Accept(this));
    }
    copy.setThenNode(thenNodeCopy);
    
    Stack<ASTNode> elseNodeCopy = new Stack<ASTNode>();
    for(ASTNode elseBodyElement: beta.getElseNode()){
      elseNodeCopy.add(elseBodyElement.Accept(this));
    }
    copy.setElseNode(elseNodeCopy);
    
    return copy;
  }
  
  public Eta copy(Eta eta){
    Eta copy = new Eta();
    if(eta.getChild()!=null)
      copy.setChild(eta.getChild().Accept(this));
    if(eta.getSibling()!=null)
      copy.setSibling(eta.getSibling().Accept(this));
    copy.setType(eta.getType());
    copy.setVal(eta.get_value());
    copy.setSource_Line_Num(eta.getSource_Line_Num());
    
    copy.setDelta(eta.getDelta().accept_Delta(this));
    
    return copy;
  }
  
  public Delta copy(Delta delta){
    Delta copy = new Delta();
    if(delta.getChild()!=null)
      copy.setChild(delta.getChild().Accept(this));
    if(delta.getSibling()!=null)
      copy.setSibling(delta.getSibling().Accept(this));
    copy.setType(delta.getType());
    copy.setVal(delta.get_value());
    copy.setElement(delta.getElement());
    copy.setSource_Line_Num(delta.getSource_Line_Num());
    
    Stack<ASTNode> bodyCopy = new Stack<ASTNode>();
    for(ASTNode bodyElement: delta.getBody()){
      bodyCopy.add(bodyElement.Accept(this));
    }
    copy.setBody(bodyCopy);
    
    List<String> boundVarsCopy = new ArrayList<String>();
    boundVarsCopy.addAll(delta.getBound_var_list());
    copy.setBound_var_list(boundVarsCopy);
    
    copy.setLinked_Environment(delta.getLinked_Environment());
    
    return copy;
  }
  
  public Tuple copy(Tuple tuple){
    Tuple copy = new Tuple();
    if(tuple.getChild()!=null)
      copy.setChild(tuple.getChild().Accept(this));
    if(tuple.getSibling()!=null)
      copy.setSibling(tuple.getSibling().Accept(this));
    copy.setType(tuple.getType());
    copy.setVal(tuple.get_value());
    copy.setSource_Line_Num(tuple.getSource_Line_Num());
    return copy;
  }
}