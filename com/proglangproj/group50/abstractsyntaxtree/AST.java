package com.proglangproj.group50.abstractsyntaxtree;

import java.util.ArrayDeque;
import java.util.Stack;

import com.proglangproj.group50.cse_machine.Beta;
import com.proglangproj.group50.cse_machine.Delta;

/*
 * Abstract Syntax Tree: The nodes use a first-child
 */
public class AST{
  private ASTNode root;
  private ArrayDeque<Pending_Delta_Body> Pending_Delta_Body_Queue;
  private boolean Standardized;
  private Delta Cur_Delta;
  private Delta Root_Delta;
  private int Delta_Index;

  public AST(ASTNode node){
    this.root = node;
  }

  /**
   * Prints the tree nodes in pre-order fashion.
   */
  public void print(){
    Print_In_Preorder(root,"");
  }

  private void Print_In_Preorder(ASTNode node, String Print_Prefix){
    if(node == null) {
      return;
    }

    Print_ASTNode_Details(node, Print_Prefix);
    Print_In_Preorder(node.getChild(),Print_Prefix + ".");
    Print_In_Preorder(node.getSibling(),Print_Prefix);
  }

  private void Print_ASTNode_Details(ASTNode node, String Print_Prefix){
    if(node.getType() == ASTNodeType.IDENTIFIER ||
        node.getType() == ASTNodeType.INTEGER){
      System.out.printf(Print_Prefix+node.getType().getPrintName()+"\n",node.getValue());
    }
    else if(node.getType() == ASTNodeType.STRING)
      System.out.printf(Print_Prefix+node.getType().getPrintName()+"\n",node.getValue());
    else {
      System.out.println(Print_Prefix + node.getType().getPrintName());
    }
  }

  /**
   * Standardize the AST
   */
  public void Standardize(){
    Standardize(root);
    Standardized = true;
  }

  /**
   * Standardize the tree in bottom-up manner
   * @param node node to standardize
   */
  private void Standardize(ASTNode node){
    //standardize children first
    if(node.getChild() != null){
      ASTNode Node_Child = node.getChild();
      while(Node_Child != null){
        Standardize(Node_Child);
        Node_Child = Node_Child.getSibling();
      }
    }

    //all children standardized. now standardize this node
    switch(node.getType()){
      case LET:
        //       LET              GAMMA
        //     /     \           /     \
        //    EQUAL   P   ->   LAMBDA   E
        //   /   \             /    \
        //  X     E           X      P
        ASTNode equalNode = node.getChild();
        if(equalNode.getType() != ASTNodeType.EQUAL)
          throw new StandardizeException("LET/WHERE: left child is not EQUAL"); //for safety
        ASTNode Node_1 = equalNode.getChild().getSibling();
        equalNode.getChild().setSibling(equalNode.getSibling());
        equalNode.setSibling(Node_1);
        equalNode.setType(ASTNodeType.LAMBDA);
        node.setType(ASTNodeType.GAMMA);
        break;
      case WHERE:
        //where will be made as LET node and standardize that
        //       WHERE               LET
        //       /   \             /     \
        //      P    EQUAL   ->  EQUAL   P
        //           /   \       /   \
        //          X     E     X     E
        equalNode = node.getChild().getSibling();
        node.getChild().setSibling(null);
        equalNode.setSibling(node.getChild());
        node.setChild(equalNode);
        node.setType(ASTNodeType.LET);
        Standardize(node);
        break;
      case FCNFORM:
        //       FCN_FORM                EQUAL
        //       /   |   \              /    \
        //      P    V+   E    ->      P     +LAMBDA
        //                                    /     \
        //                                    V     .E
        ASTNode childSibling = node.getChild().getSibling();
        node.getChild().setSibling(constructLambdaChain(childSibling));
        node.setType(ASTNodeType.EQUAL);
        break;
      case AT:
        //         AT              GAMMA
        //       / | \    ->       /    \
        //      E1 N E2          GAMMA   E2
        //                       /    \
        //                      N     E1
        ASTNode Node1 = node.getChild();
        ASTNode Node_2 = Node1.getSibling();
        ASTNode Node_3 = Node_2.getSibling();
        ASTNode gammaNode = new ASTNode();
        gammaNode.setType(ASTNodeType.GAMMA);
        gammaNode.setChild(Node_2);
        Node_2.setSibling(Node1);
        Node1.setSibling(null);
        gammaNode.setSibling(Node_3);
        node.setChild(gammaNode);
        node.setType(ASTNodeType.GAMMA);
        break;
      case WITHIN:
        //           WITHIN                  EQUAL
        //          /      \                /     \
        //        EQUAL   EQUAL    ->      X2     GAMMA
        //       /    \   /    \                  /    \
        //      X1    E1 X2    E2               LAMBDA  E1
        //                                      /    \
        //                                     X1    E2
        if(node.getChild().getType()!=ASTNodeType.EQUAL || node.getChild().getSibling().getType() != ASTNodeType.EQUAL) {
          throw new StandardizeException("WITHIN: one of the children is not EQUAL"); //for safety
        }
        ASTNode Node_4 = node.getChild().getChild();
        Node1 = Node_4.getSibling();
        ASTNode Node_5 = node.getChild().getSibling().getChild();
        Node_3 = Node_5.getSibling();
        ASTNode lambdaNode = new ASTNode();
        lambdaNode.setType(ASTNodeType.LAMBDA);
        Node_4.setSibling(Node_3);
        lambdaNode.setChild(Node_4);
        lambdaNode.setSibling(Node1);
        gammaNode = new ASTNode();
        gammaNode.setType(ASTNodeType.GAMMA);
        gammaNode.setChild(lambdaNode);
        Node_5.setSibling(gammaNode);
        node.setChild(Node_5);
        node.setType(ASTNodeType.EQUAL);
        break;
      case SIMULTDEF:
        //         SIMULTDEF            EQUAL
        //             |               /     \
        //           EQUAL++  ->     COMMA   TAU
        //           /   \             |      |
        //          X     E           X++    E++
        ASTNode commaNode = new ASTNode();
        commaNode.setType(ASTNodeType.COMMA);
        ASTNode tauNode = new ASTNode();
        tauNode.setType(ASTNodeType.TAU);
        ASTNode childNode = node.getChild();
        while(childNode!=null){
          populateCommaAndTauNode(childNode, commaNode, tauNode);
          childNode = childNode.getSibling();
        }
        commaNode.setSibling(tauNode);
        node.setChild(commaNode);
        node.setType(ASTNodeType.EQUAL);
        break;
      case REC:
        //        REC                 EQUAL
        //         |                 /     \
        //       EQUAL     ->       X     GAMMA
        //      /     \                   /    \
        //     X       E                YSTAR  LAMBDA
        //                                     /     \
        //                                    X       E
        childNode = node.getChild();
        if(childNode.getType()!=ASTNodeType.EQUAL)
          throw new StandardizeException("REC: child is not EQUAL"); //safety
        ASTNode x = childNode.getChild();
        lambdaNode = new ASTNode();
        lambdaNode.setType(ASTNodeType.LAMBDA);
        lambdaNode.setChild(x); //x is already attached to e
        ASTNode yStarNode = new ASTNode();
        yStarNode.setType(ASTNodeType.YSTAR);
        yStarNode.setSibling(lambdaNode);
        gammaNode = new ASTNode();
        gammaNode.setType(ASTNodeType.GAMMA);
        gammaNode.setChild(yStarNode);
        ASTNode xWithSiblingGamma = new ASTNode(); //same as x except the sibling is not e but gamma
        xWithSiblingGamma.setChild(x.getChild());
        xWithSiblingGamma.setSibling(gammaNode);
        xWithSiblingGamma.setType(x.getType());
        xWithSiblingGamma.setValue(x.getValue());
        node.setChild(xWithSiblingGamma);
        node.setType(ASTNodeType.EQUAL);
        break;
      case LAMBDA:
        //     LAMBDA        LAMBDA
        //      /   \   ->   /    \
        //     V++   E      V     .E
        childSibling = node.getChild().getSibling();
        node.getChild().setSibling(constructLambdaChain(childSibling));
        break;
      default:
        // Node types we do NOT standardize:
        // CSE Optimization Rule 6 (binops)
        // OR
        // AND
        // PLUS
        // MINUS
        // MULT
        // DIV
        // EXP
        // GR
        // GE
        // LS
        // LE
        // EQ
        // NE
        // CSE Optimization Rule 7 (unops)
        // NOT
        // NEG
        // CSE Optimization Rule 8 (conditionals)
        // CONDITIONAL
        // CSE Optimization Rule 9, 10 (tuples)
        // TAU
        // CSE Optimization Rule 11 (n-ary functions)
        // COMMA
        break;
    }
  }

  private void populateCommaAndTauNode(ASTNode equalNode, ASTNode commaNode, ASTNode tauNode){
    if(equalNode.getType()!=ASTNodeType.EQUAL)
      throw new StandardizeException("SIMULTDEF: one of the children is not EQUAL"); //safety
    ASTNode x = equalNode.getChild();
    ASTNode e = x.getSibling();
    setChild(commaNode, x);
    setChild(tauNode, e);
  }

  /**
   * Either creates a new child of the parent or attaches the child node passed in
   * as the last sibling of the parent's existing children 
   * @param parentNode
   * @param childNode
   */
  private void setChild(ASTNode parentNode, ASTNode childNode){
    if(parentNode.getChild()==null)
      parentNode.setChild(childNode);
    else{
      ASTNode lastSibling = parentNode.getChild();
      while(lastSibling.getSibling()!=null)
        lastSibling = lastSibling.getSibling();
      lastSibling.setSibling(childNode);
    }
    childNode.setSibling(null);
  }

  private ASTNode constructLambdaChain(ASTNode node){
    if(node.getSibling()==null)
      return node;
    
    ASTNode lambdaNode = new ASTNode();
    lambdaNode.setType(ASTNodeType.LAMBDA);
    lambdaNode.setChild(node);
    if(node.getSibling().getSibling()!=null)
      node.setSibling(constructLambdaChain(node.getSibling()));
    return lambdaNode;
  }

  /**
   * Creates delta structures from the standardized tree
   * @return the first delta structure (&delta;0)
   */
  public Delta createDeltas(){
    Pending_Delta_Body_Queue = new ArrayDeque<Pending_Delta_Body>();
    Delta_Index = 0;
    Cur_Delta = createDelta(root);
    processPendingDeltaStack();
    return Root_Delta;
  }

  private Delta createDelta(ASTNode startBodyNode){
    //we'll create this delta's body later
    Pending_Delta_Body pendingDelta = new Pending_Delta_Body();
    pendingDelta.startNode = startBodyNode;
    pendingDelta.body = new Stack<ASTNode>();
    Pending_Delta_Body_Queue.add(pendingDelta);
    
    Delta d = new Delta();
    d.setBody(pendingDelta.body);
    d.setIndex(Delta_Index++);
    Cur_Delta = d;
    
    if(startBodyNode==root)
      Root_Delta = Cur_Delta;
    
    return d;
  }

  private void processPendingDeltaStack(){
    while(!Pending_Delta_Body_Queue.isEmpty()){
      Pending_Delta_Body pendingDeltaBody = Pending_Delta_Body_Queue.pop();
      buildDeltaBody(pendingDeltaBody.startNode, pendingDeltaBody.body);
    }
  }
  
  private void buildDeltaBody(ASTNode node, Stack<ASTNode> body){
    if(node.getType()==ASTNodeType.LAMBDA){ //create a new delta
      Delta d = createDelta(node.getChild().getSibling()); //the new delta's body starts at the right child of the lambda
      if(node.getChild().getType()==ASTNodeType.COMMA){ //the left child of the lambda is the bound variable
        ASTNode commaNode = node.getChild();
        ASTNode childNode = commaNode.getChild();
        while(childNode!=null){
          d.addBoundVars(childNode.getValue());
          childNode = childNode.getSibling();
        }
      }
      else
        d.addBoundVars(node.getChild().getValue());
      body.push(d); //add this new delta to the existing delta's body
      return;
    }
    else if(node.getType()==ASTNodeType.CONDITIONAL){
      //to enable programming order evaluation, traverse the children in reverse order so the condition leads
      // cond -> then else becomes then else Beta cond
      ASTNode conditionNode = node.getChild();
      ASTNode thenNode = conditionNode.getSibling();
      ASTNode elseNode = thenNode.getSibling();
      
      //Add a Beta node.
      Beta betaNode = new Beta();
      
      buildDeltaBody(thenNode, betaNode.getThenBody());
      buildDeltaBody(elseNode, betaNode.getElseBody());
      
      body.push(betaNode);
      
      buildDeltaBody(conditionNode, body);
      
      return;
    }
    
    //preOrder walk
    body.push(node);
    ASTNode childNode = node.getChild();
    while(childNode!=null){
      buildDeltaBody(childNode, body);
      childNode = childNode.getSibling();
    }
  }

  private class Pending_Delta_Body {
    Stack<ASTNode> body;
    ASTNode startNode;
  }

  public boolean isStandardized(){
    return Standardized;
  }
}
