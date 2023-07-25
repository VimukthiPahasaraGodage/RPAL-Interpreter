package com.proglangproj.group50.abstractsyntaxtree;

import com.proglangproj.group50.cse_machine.Beta;
import com.proglangproj.group50.cse_machine.Delta;

import java.util.ArrayDeque;
import java.util.Stack;

/*
 * Abstract Syntax Tree: The nodes use a first-child
 */
public class AST {
    private final ASTNode root;
    private ArrayDeque<PendingDeltaBody> pending_Delta_Body_Queue;
    private boolean Standardized;
    private Delta Cur_Delta;
    private Delta Root_Delta;
    private int Delta_Index;

    public AST(ASTNode node) {
        this.root = node;
    }

    /**
     * Prints the tree nodes in pre-order fashion.
     */
    public void print() {
        Print_In_Preorder(root, "");
    }

    private void Print_In_Preorder(ASTNode node, String Print_Prefix) {
        if (node == null) {
            return;
        }

        Print_ASTNode_Details(node, Print_Prefix);
        Print_In_Preorder(node.getChildOfASTNode(), Print_Prefix + ".");
        Print_In_Preorder(node.getSiblingOfASTNode(), Print_Prefix);
    }

    private void Print_ASTNode_Details(ASTNode node, String Print_Prefix) {
        if (node.getTypeOfASTNode() == ASTNodeType.IDENTIFIER ||
                node.getTypeOfASTNode() == ASTNodeType.INTEGER) {
            System.out.printf(Print_Prefix + node.getTypeOfASTNode().getPrintNameOfASTNode() + "\n", node.getValueOfASTNode());
        } else if (node.getTypeOfASTNode() == ASTNodeType.STRING)
            System.out.printf(Print_Prefix + node.getTypeOfASTNode().getPrintNameOfASTNode() + "\n", node.getValueOfASTNode());
        else {
            System.out.println(Print_Prefix + node.getTypeOfASTNode().getPrintNameOfASTNode());
        }
    }

    /**
     * Standardize the AST
     */
    public void Standardize() {
        Standardize(root);
        Standardized = true;
    }

    /**
     * Standardize the tree in bottom-up manner
     *
     * @param node node to standardize
     */
    private void Standardize(ASTNode node) {
        //standardize children first
        if (node.getChildOfASTNode() != null) {
            ASTNode Node_Child = node.getChildOfASTNode();
            while (Node_Child != null) {
                Standardize(Node_Child);
                Node_Child = Node_Child.getSiblingOfASTNode();
            }
        }

        //all children standardized. now standardize this node
        switch (node.getTypeOfASTNode()) {
            case LET:
                //       LET              GAMMA
                //     /     \           /     \
                //    EQUAL   P   ->   LAMBDA   E
                //   /   \             /    \
                //  X     E           X      P
                ASTNode equalNode = node.getChildOfASTNode();
                if (equalNode.getTypeOfASTNode() != ASTNodeType.EQUAL)
                    throw new RuntimeException("LET/WHERE: left child is not EQUAL"); //for safety
                ASTNode Node_1 = equalNode.getChildOfASTNode().getSiblingOfASTNode();
                equalNode.getChildOfASTNode().setSiblingOfASTNode(equalNode.getSiblingOfASTNode());
                equalNode.setSiblingOfASTNode(Node_1);
                equalNode.setTypeOfASTNode(ASTNodeType.LAMBDA);
                node.setTypeOfASTNode(ASTNodeType.GAMMA);
                break;
            case WHERE:
                //where will be made as LET node and standardize that
                //       WHERE               LET
                //       /   \             /     \
                //      P    EQUAL   ->  EQUAL   P
                //           /   \       /   \
                //          X     E     X     E
                equalNode = node.getChildOfASTNode().getSiblingOfASTNode();
                node.getChildOfASTNode().setSiblingOfASTNode(null);
                equalNode.setSiblingOfASTNode(node.getChildOfASTNode());
                node.setChildOfASTNode(equalNode);
                node.setTypeOfASTNode(ASTNodeType.LET);
                Standardize(node);
                break;
            case FCNFORM:
                //       FCN_FORM                EQUAL
                //       /   |   \              /    \
                //      P    V+   E    ->      P     +LAMBDA
                //                                    /     \
                //                                    V     .E
                ASTNode childSibling = node.getChildOfASTNode().getSiblingOfASTNode();
                node.getChildOfASTNode().setSiblingOfASTNode(constructLambdaChain(childSibling));
                node.setTypeOfASTNode(ASTNodeType.EQUAL);
                break;
            case AT:
                //         AT              GAMMA
                //       / | \    ->       /    \
                //      E1 N E2          GAMMA   E2
                //                       /    \
                //                      N     E1
                ASTNode Node1 = node.getChildOfASTNode();
                ASTNode Node_2 = Node1.getSiblingOfASTNode();
                ASTNode Node_3 = Node_2.getSiblingOfASTNode();
                ASTNode gammaNode = new ASTNode();
                gammaNode.setTypeOfASTNode(ASTNodeType.GAMMA);
                gammaNode.setChildOfASTNode(Node_2);
                Node_2.setSiblingOfASTNode(Node1);
                Node1.setSiblingOfASTNode(null);
                gammaNode.setSiblingOfASTNode(Node_3);
                node.setChildOfASTNode(gammaNode);
                node.setTypeOfASTNode(ASTNodeType.GAMMA);
                break;
            case WITHIN:
                //           WITHIN                  EQUAL
                //          /      \                /     \
                //        EQUAL   EQUAL    ->      X2     GAMMA
                //       /    \   /    \                  /    \
                //      X1    E1 X2    E2               LAMBDA  E1
                //                                      /    \
                //                                     X1    E2
                if (node.getChildOfASTNode().getTypeOfASTNode() != ASTNodeType.EQUAL || node.getChildOfASTNode().getSiblingOfASTNode().getTypeOfASTNode() != ASTNodeType.EQUAL) {
                    throw new RuntimeException("WITHIN: one of the children is not EQUAL"); //for safety
                }
                ASTNode Node_4 = node.getChildOfASTNode().getChildOfASTNode();
                Node1 = Node_4.getSiblingOfASTNode();
                ASTNode Node_5 = node.getChildOfASTNode().getSiblingOfASTNode().getChildOfASTNode();
                Node_3 = Node_5.getSiblingOfASTNode();
                ASTNode lambdaNode = new ASTNode();
                lambdaNode.setTypeOfASTNode(ASTNodeType.LAMBDA);
                Node_4.setSiblingOfASTNode(Node_3);
                lambdaNode.setChildOfASTNode(Node_4);
                lambdaNode.setSiblingOfASTNode(Node1);
                gammaNode = new ASTNode();
                gammaNode.setTypeOfASTNode(ASTNodeType.GAMMA);
                gammaNode.setChildOfASTNode(lambdaNode);
                Node_5.setSiblingOfASTNode(gammaNode);
                node.setChildOfASTNode(Node_5);
                node.setTypeOfASTNode(ASTNodeType.EQUAL);
                break;
            case SIMULTDEF:
                //         SIMULTDEF            EQUAL
                //             |               /     \
                //           EQUAL++  ->     COMMA   TAU
                //           /   \             |      |
                //          X     E           X++    E++
                ASTNode commaNode = new ASTNode();
                commaNode.setTypeOfASTNode(ASTNodeType.COMMA);
                ASTNode tauNode = new ASTNode();
                tauNode.setTypeOfASTNode(ASTNodeType.TAU);
                ASTNode childNode = node.getChildOfASTNode();
                while (childNode != null) {
                    populateCommaAndTauNode(childNode, commaNode, tauNode);
                    childNode = childNode.getSiblingOfASTNode();
                }
                commaNode.setSiblingOfASTNode(tauNode);
                node.setChildOfASTNode(commaNode);
                node.setTypeOfASTNode(ASTNodeType.EQUAL);
                break;
            case REC:
                //        REC                 EQUAL
                //         |                 /     \
                //       EQUAL     ->       X     GAMMA
                //      /     \                   /    \
                //     X       E                YSTAR  LAMBDA
                //                                     /     \
                //                                    X       E
                childNode = node.getChildOfASTNode();
                if (childNode.getTypeOfASTNode() != ASTNodeType.EQUAL)
                    throw new RuntimeException("REC: child is not EQUAL"); //safety
                ASTNode x = childNode.getChildOfASTNode();
                lambdaNode = new ASTNode();
                lambdaNode.setTypeOfASTNode(ASTNodeType.LAMBDA);
                lambdaNode.setChildOfASTNode(x); //x is already attached to e
                ASTNode yStarNode = new ASTNode();
                yStarNode.setTypeOfASTNode(ASTNodeType.YSTAR);
                yStarNode.setSiblingOfASTNode(lambdaNode);
                gammaNode = new ASTNode();
                gammaNode.setTypeOfASTNode(ASTNodeType.GAMMA);
                gammaNode.setChildOfASTNode(yStarNode);
                ASTNode xWithSiblingGamma = new ASTNode(); //same as x except the sibling is not e but gamma
                xWithSiblingGamma.setChildOfASTNode(x.getChildOfASTNode());
                xWithSiblingGamma.setSiblingOfASTNode(gammaNode);
                xWithSiblingGamma.setTypeOfASTNode(x.getTypeOfASTNode());
                xWithSiblingGamma.setValueOfASTNode(x.getValueOfASTNode());
                node.setChildOfASTNode(xWithSiblingGamma);
                node.setTypeOfASTNode(ASTNodeType.EQUAL);
                break;
            case LAMBDA:
                //     LAMBDA        LAMBDA
                //      /   \   ->   /    \
                //     V++   E      V     .E
                childSibling = node.getChildOfASTNode().getSiblingOfASTNode();
                node.getChildOfASTNode().setSiblingOfASTNode(constructLambdaChain(childSibling));
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

    private void populateCommaAndTauNode(ASTNode equalNode, ASTNode commaNode, ASTNode tauNode) {
        if (equalNode.getTypeOfASTNode() != ASTNodeType.EQUAL)
            throw new RuntimeException("SIMULTDEF: one of the children is not EQUAL"); //safety
        ASTNode x = equalNode.getChildOfASTNode();
        ASTNode e = x.getSiblingOfASTNode();
        setChild(commaNode, x);
        setChild(tauNode, e);
    }

    /**
     * Either creates a new child of the parent or attaches the child node passed in
     * as the last sibling of the parent's existing children
     *
     * @param parentNode
     * @param childNode
     */
    private void setChild(ASTNode parentNode, ASTNode childNode) {
        if (parentNode.getChildOfASTNode() == null)
            parentNode.setChildOfASTNode(childNode);
        else {
            ASTNode lastSibling = parentNode.getChildOfASTNode();
            while (lastSibling.getSiblingOfASTNode() != null)
                lastSibling = lastSibling.getSiblingOfASTNode();
            lastSibling.setSiblingOfASTNode(childNode);
        }
        childNode.setSiblingOfASTNode(null);
    }

    private ASTNode constructLambdaChain(ASTNode node) {
        if (node.getSiblingOfASTNode() == null)
            return node;

        ASTNode lambdaNode = new ASTNode();
        lambdaNode.setTypeOfASTNode(ASTNodeType.LAMBDA);
        lambdaNode.setChildOfASTNode(node);
        if (node.getSiblingOfASTNode().getSiblingOfASTNode() != null)
            node.setSiblingOfASTNode(constructLambdaChain(node.getSiblingOfASTNode()));
        return lambdaNode;
    }

    /**
     * Creates delta structures from the standardized tree
     *
     * @return the first delta structure (&delta;0)
     */
    public Delta createDeltas() {
        pending_Delta_Body_Queue = new ArrayDeque<PendingDeltaBody>();
        Delta_Index = 0;
        Cur_Delta = createDelta(root);
        processPendingDeltaStack();
        return Root_Delta;
    }

    private Delta createDelta(ASTNode startBodyNode) {
        //we'll create this delta's body later
        PendingDeltaBody pendingDelta = new PendingDeltaBody();
        pendingDelta.startNode = startBodyNode;
        pendingDelta.body = new Stack<ASTNode>();
        pending_Delta_Body_Queue.add(pendingDelta);

        Delta d = new Delta();
        d.setBody(pendingDelta.body);
        d.setIndex(Delta_Index++);
        Cur_Delta = d;

        if (startBodyNode == root)
            Root_Delta = Cur_Delta;

        return d;
    }

    private void processPendingDeltaStack() {
        while (!pending_Delta_Body_Queue.isEmpty()) {
            PendingDeltaBody pendingDeltaBody = pending_Delta_Body_Queue.pop();
            buildDeltaBody(pendingDeltaBody.startNode, pendingDeltaBody.body);
        }
    }

    private void buildDeltaBody(ASTNode node, Stack<ASTNode> body) {
        if (node.getTypeOfASTNode() == ASTNodeType.LAMBDA) { //create a new delta
            Delta d = createDelta(node.getChildOfASTNode().getSiblingOfASTNode()); //the new delta's body starts at the right child of the lambda
            if (node.getChildOfASTNode().getTypeOfASTNode() == ASTNodeType.COMMA) { //the left child of the lambda is the bound variable
                ASTNode commaNode = node.getChildOfASTNode();
                ASTNode childNode = commaNode.getChildOfASTNode();
                while (childNode != null) {
                    d.addBoundVars(childNode.getValueOfASTNode());
                    childNode = childNode.getSiblingOfASTNode();
                }
            } else
                d.addBoundVars(node.getChildOfASTNode().getValueOfASTNode());
            body.push(d); //add this new delta to the existing delta's body
            return;
        } else if (node.getTypeOfASTNode() == ASTNodeType.CONDITIONAL) {
            //to enable programming order evaluation, traverse the children in reverse order so the condition leads
            // cond -> then else becomes then else Beta cond
            ASTNode conditionNode = node.getChildOfASTNode();
            ASTNode thenNode = conditionNode.getSiblingOfASTNode();
            ASTNode elseNode = thenNode.getSiblingOfASTNode();

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
        ASTNode childNode = node.getChildOfASTNode();
        while (childNode != null) {
            buildDeltaBody(childNode, body);
            childNode = childNode.getSiblingOfASTNode();
        }
    }

    public boolean isASTStandardized() {
        return Standardized;
    }

    private static class PendingDeltaBody {
        Stack<ASTNode> body;
        ASTNode startNode;
    }
}

