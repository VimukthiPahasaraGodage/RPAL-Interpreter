package com.proglangproj.group50.cse_machine;

import java.util.Stack;

import com.proglangproj.group50.abstractsyntaxtree.AbstractSyntaxTreeNode;
import com.proglangproj.group50.abstractsyntaxtree.AbstractSyntaxTreeNodeType;

/**
 * Used to evaluate conditionals.
 * 'cond -> then | else' in source becomes 'BetaConditionalEvaluation cond' on the control stack where
 * BetaConditionalEvaluation.thenBody = standardized version of then
 * BetaConditionalEvaluation.elseBody = standardized version of else
 * 
 * This inversion is key to implementing a program order evaluation
 * (critical for recursion where putting the then and else nodes above the Conditional
 * node on the control stack will cause infinite recursion if the then and else
 * nodes call the recursive function themselves). Putting the cond node before BetaConditionalEvaluation (and, since
 * BetaConditionalEvaluation contains the then and else nodes, effectively before the then and else nodes), allows
 * evaluating the cond first and then (in the base case) choosing the non-recursive option. This
 * allows breaking out of infinite recursion.
 * @author Raj
 */
public class BetaConditionalEvaluation extends AbstractSyntaxTreeNode {
  private Stack<AbstractSyntaxTreeNode> thenBody;
  private Stack<AbstractSyntaxTreeNode> elseBody;
  
  public BetaConditionalEvaluation(){
    setTypeOfASTNode(AbstractSyntaxTreeNodeType.BETA);
    thenBody = new Stack<AbstractSyntaxTreeNode>();
    elseBody = new Stack<AbstractSyntaxTreeNode>();
  }
  
  public BetaConditionalEvaluation acceptASTNode(NodeCopier nodeCopier){
    return nodeCopier.copy(this);
  }

  public Stack<AbstractSyntaxTreeNode> getThenBody(){
    return thenBody;
  }

  public Stack<AbstractSyntaxTreeNode> getElseBody(){
    return elseBody;
  }

  public void setThenBody(Stack<AbstractSyntaxTreeNode> thenBody){
    this.thenBody = thenBody;
  }

  public void setElseBody(Stack<AbstractSyntaxTreeNode> elseBody){
    this.elseBody = elseBody;
  }
  
}
