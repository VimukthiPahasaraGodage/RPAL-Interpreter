package com.proglangproj.group50.abstractsyntaxtree;

import java.util.ArrayDeque;
import java.util.Stack;

import com.proglangproj.group50.cse_machine.Beta;
import com.proglangproj.group50.cse_machine.Delta;

/*
 * Abstract Syntax Tree - The nodes use a first-child
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
      System.out.printf(Print_Prefix + node.getType().getPrintName()+"\n",node.getValue());
    }
    else if(node.getType() == ASTNodeType.STRING)
      System.out.printf(Print_Prefix + node.getType().getPrintName()+"\n",node.getValue());
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
        ASTNode Eq_Node = node.getChild();
        if(Eq_Node.getType() != ASTNodeType.EQUAL)
          throw new StandardizeException("LET/WHERE: left child is not EQUAL"); //for safety
        ASTNode E = Eq_Node.getChild().getSibling();
        Eq_Node.getChild().setSibling(Eq_Node.getSibling());
        Eq_Node.setSibling(E);
        Eq_Node.setType(ASTNodeType.LAMBDA);
        node.setType(ASTNodeType.GAMMA);
        break;
      case WHERE:
        //where will be made as LET node and standardize that
        //       WHERE               LET
        //       /   \             /     \
        //      P    EQUAL   ->  EQUAL   P
        //           /   \       /   \
        //          X     E     X     E
        Eq_Node = node.getChild().getSibling();
        node.getChild().setSibling(null);
        Eq_Node.setSibling(node.getChild());
        node.setChild(Eq_Node);
        node.setType(ASTNodeType.LET);
        Standardize(node);
        break;
      case FCNFORM:
        //       FCN_FORM                EQUAL
        //       /   |   \              /    \
        //      P    V+   E    ->      P     +LAMBDA
        //                                    /     \
        //                                    V     .E
        ASTNode Child_Sibling = node.getChild().getSibling();
        node.getChild().setSibling(Construct_Lambda_Chain(Child_Sibling));
        node.setType(ASTNodeType.EQUAL);
        break;
      case AT:
        //         AT              GAMMA
        //       / | \    ->       /    \
        //      E1 N E2          GAMMA   E2
        //                       /    \
        //                      N     E1
        ASTNode E1 = node.getChild();
        ASTNode N = E1.getSibling();
        ASTNode E2 = N.getSibling();
        ASTNode Gamma_Node = new ASTNode();
        Gamma_Node.setType(ASTNodeType.GAMMA);
        Gamma_Node.setChild(N);
        N.setSibling(E1);
        E1.setSibling(null);
        Gamma_Node.setSibling(E2);
        node.setChild(Gamma_Node);
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
        ASTNode X1 = node.getChild().getChild();
        E1 = X1.getSibling();
        ASTNode X2 = node.getChild().getSibling().getChild();
        X2 = X2.getSibling();
        ASTNode Lambda_Node = new ASTNode();
        Lambda_Node.setType(ASTNodeType.LAMBDA);
        X1.setSibling(X2);
        Lambda_Node.setChild(X1);
        Lambda_Node.setSibling(E1);
        Gamma_Node = new ASTNode();
        Gamma_Node.setType(ASTNodeType.GAMMA);
        Gamma_Node.setChild(Lambda_Node);
        X2.setSibling(Gamma_Node);
        node.setChild(X2);
        node.setType(ASTNodeType.EQUAL);
        break;
      case SIMULTDEF:
        //         SIMULTDEF            EQUAL
        //             |               /     \
        //           EQUAL++  ->     COMMA   TAU
        //           /   \             |      |
        //          X     E           X++    E++
        ASTNode Comma_Node = new ASTNode();
        Comma_Node.setType(ASTNodeType.COMMA);
        ASTNode tauNode = new ASTNode();
        tauNode.setType(ASTNodeType.TAU);
        ASTNode Child_Node = node.getChild();
        while(Child_Node != null){
          Populate_Comma_Tau_Node(Child_Node, Comma_Node, tauNode);
          Child_Node = Child_Node.getSibling();
        }
        Comma_Node.setSibling(tauNode);
        node.setChild(Comma_Node);
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
        Child_Node = node.getChild();
        if(Child_Node.getType()!=ASTNodeType.EQUAL) {
          throw new StandardizeException("REC: child is not EQUAL"); //for safety
        }
        ASTNode X = Child_Node.getChild();
        Lambda_Node = new ASTNode();
        Lambda_Node.setType(ASTNodeType.LAMBDA);
        Lambda_Node.setChild(X); //X is already attached to E
        ASTNode Y_Star_Node = new ASTNode();
        Y_Star_Node.setType(ASTNodeType.YSTAR);
        Y_Star_Node.setSibling(Lambda_Node);
        Gamma_Node = new ASTNode();
        Gamma_Node.setType(ASTNodeType.GAMMA);
        Gamma_Node.setChild(Y_Star_Node);
        ASTNode X_With_Sibling_Gamma = new ASTNode(); //same as X except the sibling is not E but gamma
        X_With_Sibling_Gamma.setChild(X.getChild());
        X_With_Sibling_Gamma.setSibling(Gamma_Node);
        X_With_Sibling_Gamma.setType(X.getType());
        X_With_Sibling_Gamma.setValue(X.getValue());
        node.setChild(X_With_Sibling_Gamma);
        node.setType(ASTNodeType.EQUAL);
        break;
      case LAMBDA:
        //     LAMBDA        LAMBDA
        //      /   \   ->   /    \
        //     V++   E      V     .E
        Child_Sibling = node.getChild().getSibling();
        node.getChild().setSibling(Construct_Lambda_Chain(Child_Sibling));
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

  private void Populate_Comma_Tau_Node(ASTNode equalNode, ASTNode commaNode, ASTNode tauNode){
    if(equalNode.getType() != ASTNodeType.EQUAL) {
      throw new StandardizeException("SIMULTDEF: one of the children is not EQUAL"); //for safety
    }
    ASTNode X = equalNode.getChild();
    ASTNode E = X.getSibling();
    setChild(commaNode, X);
    setChild(tauNode, E);
  }

  /**
   * Either creates a new child of the parent or attaches the child node passed in
   * as the last sibling of the parent's existing children 
   * @param Parent_Node
   * @param Child_Node
   */
  private void setChild(ASTNode Parent_Node, ASTNode Child_Node){
    if(Parent_Node.getChild() == null)
      Parent_Node.setChild(Child_Node);
    else{
      ASTNode Last_Sibling = Parent_Node.getChild();
      while(Last_Sibling.getSibling()!=null)
        Last_Sibling = Last_Sibling.getSibling();
      Last_Sibling.setSibling(Child_Node);
    }
    Child_Node.setSibling(null);
  }

  private ASTNode Construct_Lambda_Chain(ASTNode node){
    if(node.getSibling()==null)
      return node;
    
    ASTNode lambdaNode = new ASTNode();
    lambdaNode.setType(ASTNodeType.LAMBDA);
    lambdaNode.setChild(node);
    if(node.getSibling().getSibling()!=null)
      node.setSibling(Construct_Lambda_Chain(node.getSibling()));
    return lambdaNode;
  }

  /**
   * Creates delta structures from the standardized tree
   * @return the first delta structure (&delta;0)
   */
  public Delta Create_Deltas(){
    Pending_Delta_Body_Queue = new ArrayDeque<Pending_Delta_Body>();
    Delta_Index = 0;
    Cur_Delta = createDelta(root);
    Process_Pending_Delta_Stack();
    return Root_Delta;
  }

  private Delta createDelta(ASTNode Start_Body_Node){
    //we'll create this delta's body later
    Pending_Delta_Body Pend_Delta = new Pending_Delta_Body();
    Pend_Delta.startNode = Start_Body_Node;
    Pend_Delta.body = new Stack<ASTNode>();
    Pending_Delta_Body_Queue.add(Pend_Delta);
    
    Delta delta_1 = new Delta();
    delta_1.setBody(Pend_Delta.body);
    delta_1.setIndex(Delta_Index++);
    Cur_Delta = delta_1;
    
    if(Start_Body_Node == root)
      Root_Delta = Cur_Delta;
    
    return delta_1;
  }

  private void Process_Pending_Delta_Stack(){
    while(!Pending_Delta_Body_Queue.isEmpty()){
      Pending_Delta_Body pendingDeltaBody = Pending_Delta_Body_Queue.pop();
      Build_Delta_Body(pendingDeltaBody.startNode, pendingDeltaBody.body);
    }
  }
  
  private void Build_Delta_Body(ASTNode node, Stack<ASTNode> body){
    if(node.getType()==ASTNodeType.LAMBDA){
      Delta delta_temp = createDelta(node.getChild().getSibling()); //new delta's body starts at the right child of the lambda
      if(node.getChild().getType() == ASTNodeType.COMMA){ //the left child of the lambda is the bound variable
        ASTNode commaNode = node.getChild();
        ASTNode Child_Node = commaNode.getChild();
        while(Child_Node != null){
          delta_temp.addBoundVars(Child_Node.getValue());
          Child_Node = Child_Node.getSibling();
        }
      }
      else {
        delta_temp.addBoundVars(node.getChild().getValue());
      }
      body.push(delta_temp); //add this new delta to the existing delta's body
      return;
    }
    else if(node.getType() == ASTNodeType.CONDITIONAL){
      //to enable programming order evaluation, traverse the children in reverse order so the condition leads
      // cond -> then else becomes then else Beta cond
      ASTNode Condition_Node = node.getChild();
      ASTNode Then_Node = Condition_Node.getSibling();
      ASTNode Else_Node = Then_Node.getSibling();
      
      //Add a Beta node.
      Beta Beta_Node = new Beta();
      
      Build_Delta_Body(Then_Node, Beta_Node.getThenBody());
      Build_Delta_Body(Else_Node, Beta_Node.getElseBody());
      
      body.push(Beta_Node);
      
      Build_Delta_Body(Condition_Node, body);
      
      return;
    }
    
    //preOrder traversal in tree
    body.push(node);
    ASTNode Child_Node = node.getChild();
    while(Child_Node != null){
      Build_Delta_Body(Child_Node, body);
      Child_Node = Child_Node.getSibling();
    }
  }

  private class Pending_Delta_Body {
    Stack<ASTNode> body;
    ASTNode startNode;
  }

  public boolean Is_Standardized(){ // checking if standardized
    return Standardized;
  }
}
