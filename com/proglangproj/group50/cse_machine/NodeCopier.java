package com.proglangproj.group50.cse_machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.proglangproj.group50.abstractsyntaxtree.ASTNode;

/**
 * Class to make copies of nodes on value stack. Used to pass back copies of
 * environment bindings so that later uses of those bindings are not affected
 * by any changes made in any earlier deltas.
 * 
 * <p>Uses the Visitor pattern to avoid instanceOf code smell.
 * 
 * @author Raj
 */
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
    copy.setSource_Line_Num(beta.getSource_Line_Num());
    
    Stack<ASTNode> thenBodyCopy = new Stack<ASTNode>();
    for(ASTNode thenBodyElement: beta.getThenNode()){
      thenBodyCopy.add(thenBodyElement.Accept(this));
    }
    copy.setThenNode(thenBodyCopy);
    
    Stack<ASTNode> elseBodyCopy = new Stack<ASTNode>();
    for(ASTNode elseBodyElement: beta.getElseNode()){
      elseBodyCopy.add(elseBodyElement.Accept(this));
    }
    copy.setElseNode(elseBodyCopy);
    
    return copy;
  }
  
  public Eta copy(Eta eta){
    Eta copy = new Eta();
    if(eta.getChild()!=null)
      copy.setChild(eta.getChild().Accept(this));
    if(eta.getSibling()!=null)
      copy.setSibling(eta.getSibling().Accept(this));
    copy.setType(eta.getType());
    copy.setVal(eta.getVal());
    copy.setSource_Line_Num(eta.getSource_Line_Num());
    
    copy.setDelta(eta.getDelta().Accept(this));
    
    return copy;
  }
  
  public Delta copy(Delta delta){
    Delta copy = new Delta();
    if(delta.getChild()!=null)
      copy.setChild(delta.getChild().Accept(this));
    if(delta.getSibling()!=null)
      copy.setSibling(delta.getSibling().Accept(this));
    copy.setType(delta.getType());
    copy.setVal(delta.getVal());
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
    copy.setVal(tuple.getVal());
    copy.setSource_Line_Num(tuple.getSource_Line_Num());
    return copy;
  }
}
