package com.proglangproj.group50.abstractsyntaxtree;

import com.proglangproj.group50.cse_machine.BetaConditionalEvaluation;
import com.proglangproj.group50.cse_machine.DeltaControlStructure;

import java.util.ArrayDeque;
import java.util.Stack;

/*
 * Abstract Syntax Tree: The nodes use a first-child
 */
public class AbstractSyntaxTree {
    private final AbstractSyntaxTreeNode root;
    private ArrayDeque<PendingDeltaBody> pending_Delta_Body_Queue;
    private boolean Standardized;
    private DeltaControlStructure Cur_Delta;
    private DeltaControlStructure Root_Delta;
    private int Delta_Index;

    public AbstractSyntaxTree(AbstractSyntaxTreeNode node) {
        this.root = node;
    }

    private void PrintASTNodeDetails(AbstractSyntaxTreeNode node, String Print_Prefix) {
        if (node.getTypeOfASTNode() == AbstractSyntaxTreeNodeType.IDENTIFIER ||
                node.getTypeOfASTNode() == AbstractSyntaxTreeNodeType.INTEGER) {
            System.out.printf(Print_Prefix + node.getTypeOfASTNode().getPrintNameOfASTNode() + "\n", node.getValueOfASTNode());
        } else if (node.getTypeOfASTNode() == AbstractSyntaxTreeNodeType.STRING)
            System.out.printf(Print_Prefix + node.getTypeOfASTNode().getPrintNameOfASTNode() + "\n", node.getValueOfASTNode());
        else {
            System.out.println(Print_Prefix + node.getTypeOfASTNode().getPrintNameOfASTNode());
        }
    }

    /**
     * Standardize the AbstractSyntaxTree
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
    private void Standardize(AbstractSyntaxTreeNode node) {
        //standardize children first
        if (node.getChildOfASTNode() != null) {
            AbstractSyntaxTreeNode Node_Child = node.getChildOfASTNode();
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
                AbstractSyntaxTreeNode equalNode = node.getChildOfASTNode();
                if (equalNode.getTypeOfASTNode() != AbstractSyntaxTreeNodeType.EQUAL)
                    throw new RuntimeException("LET/WHERE: left child is not EQUAL"); //for safety
                AbstractSyntaxTreeNode Node_1 = equalNode.getChildOfASTNode().getSiblingOfASTNode();
                equalNode.getChildOfASTNode().setSiblingOfASTNode(equalNode.getSiblingOfASTNode());
                equalNode.setSiblingOfASTNode(Node_1);
                equalNode.setTypeOfASTNode(AbstractSyntaxTreeNodeType.LAMBDA);
                node.setTypeOfASTNode(AbstractSyntaxTreeNodeType.GAMMA);
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
                node.setTypeOfASTNode(AbstractSyntaxTreeNodeType.LET);
                Standardize(node);
                break;
            case FCNFORM:
                //       FCN_FORM                EQUAL
                //       /   |   \              /    \
                //      P    V+   E    ->      P     +LAMBDA
                //                                    /     \
                //                                    V     .E
                AbstractSyntaxTreeNode childSibling = node.getChildOfASTNode().getSiblingOfASTNode();
                node.getChildOfASTNode().setSiblingOfASTNode(constructLambdaChain(childSibling));
                node.setTypeOfASTNode(AbstractSyntaxTreeNodeType.EQUAL);
                break;
            case AT:
                //         AT              GAMMA
                //       / | \    ->       /    \
                //      E1 N E2          GAMMA   E2
                //                       /    \
                //                      N     E1
                AbstractSyntaxTreeNode Node1 = node.getChildOfASTNode();
                AbstractSyntaxTreeNode Node_2 = Node1.getSiblingOfASTNode();
                AbstractSyntaxTreeNode Node_3 = Node_2.getSiblingOfASTNode();
                AbstractSyntaxTreeNode gammaNode = new AbstractSyntaxTreeNode();
                gammaNode.setTypeOfASTNode(AbstractSyntaxTreeNodeType.GAMMA);
                gammaNode.setChildOfASTNode(Node_2);
                Node_2.setSiblingOfASTNode(Node1);
                Node1.setSiblingOfASTNode(null);
                gammaNode.setSiblingOfASTNode(Node_3);
                node.setChildOfASTNode(gammaNode);
                node.setTypeOfASTNode(AbstractSyntaxTreeNodeType.GAMMA);
                break;
            case WITHIN:
                //           WITHIN                  EQUAL
                //          /      \                /     \
                //        EQUAL   EQUAL    ->      X2     GAMMA
                //       /    \   /    \                  /    \
                //      X1    E1 X2    E2               LAMBDA  E1
                //                                      /    \
                //                                     X1    E2
                if (node.getChildOfASTNode().getTypeOfASTNode() != AbstractSyntaxTreeNodeType.EQUAL || node.getChildOfASTNode().getSiblingOfASTNode().getTypeOfASTNode() != AbstractSyntaxTreeNodeType.EQUAL) {
                    throw new RuntimeException("WITHIN: one of the children is not EQUAL"); //for safety
                }
                AbstractSyntaxTreeNode Node_4 = node.getChildOfASTNode().getChildOfASTNode();
                Node1 = Node_4.getSiblingOfASTNode();
                AbstractSyntaxTreeNode Node_5 = node.getChildOfASTNode().getSiblingOfASTNode().getChildOfASTNode();
                Node_3 = Node_5.getSiblingOfASTNode();
                AbstractSyntaxTreeNode lambdaNode = new AbstractSyntaxTreeNode();
                lambdaNode.setTypeOfASTNode(AbstractSyntaxTreeNodeType.LAMBDA);
                Node_4.setSiblingOfASTNode(Node_3);
                lambdaNode.setChildOfASTNode(Node_4);
                lambdaNode.setSiblingOfASTNode(Node1);
                gammaNode = new AbstractSyntaxTreeNode();
                gammaNode.setTypeOfASTNode(AbstractSyntaxTreeNodeType.GAMMA);
                gammaNode.setChildOfASTNode(lambdaNode);
                Node_5.setSiblingOfASTNode(gammaNode);
                node.setChildOfASTNode(Node_5);
                node.setTypeOfASTNode(AbstractSyntaxTreeNodeType.EQUAL);
                break;
            case SIMULTDEF:
                //         SIMULTDEF            EQUAL
                //             |               /     \
                //           EQUAL++  ->     COMMA   TAU
                //           /   \             |      |
                //          X     E           X++    E++
                AbstractSyntaxTreeNode commaNode = new AbstractSyntaxTreeNode();
                commaNode.setTypeOfASTNode(AbstractSyntaxTreeNodeType.COMMA);
                AbstractSyntaxTreeNode tauNode = new AbstractSyntaxTreeNode();
                tauNode.setTypeOfASTNode(AbstractSyntaxTreeNodeType.TAU);
                AbstractSyntaxTreeNode childNode = node.getChildOfASTNode();
                while (childNode != null) {
                    populateCommaAndTauNode(childNode, commaNode, tauNode);
                    childNode = childNode.getSiblingOfASTNode();
                }
                commaNode.setSiblingOfASTNode(tauNode);
                node.setChildOfASTNode(commaNode);
                node.setTypeOfASTNode(AbstractSyntaxTreeNodeType.EQUAL);
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
                if (childNode.getTypeOfASTNode() != AbstractSyntaxTreeNodeType.EQUAL)
                    throw new RuntimeException("REC: child is not EQUAL"); //safety
                AbstractSyntaxTreeNode x = childNode.getChildOfASTNode();
                lambdaNode = new AbstractSyntaxTreeNode();
                lambdaNode.setTypeOfASTNode(AbstractSyntaxTreeNodeType.LAMBDA);
                lambdaNode.setChildOfASTNode(x); //x is already attached to e
                AbstractSyntaxTreeNode yStarNode = new AbstractSyntaxTreeNode();
                yStarNode.setTypeOfASTNode(AbstractSyntaxTreeNodeType.YSTAR);
                yStarNode.setSiblingOfASTNode(lambdaNode);
                gammaNode = new AbstractSyntaxTreeNode();
                gammaNode.setTypeOfASTNode(AbstractSyntaxTreeNodeType.GAMMA);
                gammaNode.setChildOfASTNode(yStarNode);
                AbstractSyntaxTreeNode xWithSiblingGamma = new AbstractSyntaxTreeNode(); //same as x except the sibling is not e but gamma
                xWithSiblingGamma.setChildOfASTNode(x.getChildOfASTNode());
                xWithSiblingGamma.setSiblingOfASTNode(gammaNode);
                xWithSiblingGamma.setTypeOfASTNode(x.getTypeOfASTNode());
                xWithSiblingGamma.setValueOfASTNode(x.getValueOfASTNode());
                node.setChildOfASTNode(xWithSiblingGamma);
                node.setTypeOfASTNode(AbstractSyntaxTreeNodeType.EQUAL);
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

    private void populateCommaAndTauNode(AbstractSyntaxTreeNode equalNode, AbstractSyntaxTreeNode commaNode, AbstractSyntaxTreeNode tauNode) {
        if (equalNode.getTypeOfASTNode() != AbstractSyntaxTreeNodeType.EQUAL)
            throw new RuntimeException("SIMULTDEF: one of the children is not EQUAL"); //safety
        AbstractSyntaxTreeNode x = equalNode.getChildOfASTNode();
        AbstractSyntaxTreeNode e = x.getSiblingOfASTNode();
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
    private void setChild(AbstractSyntaxTreeNode parentNode, AbstractSyntaxTreeNode childNode) {
        if (parentNode.getChildOfASTNode() == null)
            parentNode.setChildOfASTNode(childNode);
        else {
            AbstractSyntaxTreeNode lastSibling = parentNode.getChildOfASTNode();
            while (lastSibling.getSiblingOfASTNode() != null)
                lastSibling = lastSibling.getSiblingOfASTNode();
            lastSibling.setSiblingOfASTNode(childNode);
        }
        childNode.setSiblingOfASTNode(null);
    }

    private AbstractSyntaxTreeNode constructLambdaChain(AbstractSyntaxTreeNode node) {
        if (node.getSiblingOfASTNode() == null)
            return node;

        AbstractSyntaxTreeNode lambdaNode = new AbstractSyntaxTreeNode();
        lambdaNode.setTypeOfASTNode(AbstractSyntaxTreeNodeType.LAMBDA);
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
    public DeltaControlStructure createDeltas() {
        pending_Delta_Body_Queue = new ArrayDeque<PendingDeltaBody>();
        Delta_Index = 0;
        Cur_Delta = createDelta(root);
        processPendingDeltaStack();
        return Root_Delta;
    }

    private DeltaControlStructure createDelta(AbstractSyntaxTreeNode startBodyNode) {
        //we'll create this delta's body later
        PendingDeltaBody pendingDelta = new PendingDeltaBody();
        pendingDelta.startNode = startBodyNode;
        pendingDelta.body = new Stack<AbstractSyntaxTreeNode>();
        pending_Delta_Body_Queue.add(pendingDelta);

        DeltaControlStructure d = new DeltaControlStructure();
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

    private void buildDeltaBody(AbstractSyntaxTreeNode node, Stack<AbstractSyntaxTreeNode> body) {
        if (node.getTypeOfASTNode() == AbstractSyntaxTreeNodeType.LAMBDA) { //create a new delta
            DeltaControlStructure d = createDelta(node.getChildOfASTNode().getSiblingOfASTNode()); //the new delta's body starts at the right child of the lambda
            if (node.getChildOfASTNode().getTypeOfASTNode() == AbstractSyntaxTreeNodeType.COMMA) { //the left child of the lambda is the bound variable
                AbstractSyntaxTreeNode commaNode = node.getChildOfASTNode();
                AbstractSyntaxTreeNode childNode = commaNode.getChildOfASTNode();
                while (childNode != null) {
                    d.addBoundVars(childNode.getValueOfASTNode());
                    childNode = childNode.getSiblingOfASTNode();
                }
            } else
                d.addBoundVars(node.getChildOfASTNode().getValueOfASTNode());
            body.push(d); //add this new delta to the existing delta's body
            return;
        } else if (node.getTypeOfASTNode() == AbstractSyntaxTreeNodeType.CONDITIONAL) {
            //to enable programming order evaluation, traverse the children in reverse order so the condition leads
            // cond -> then else becomes then else BetaConditionalEvaluation cond
            AbstractSyntaxTreeNode conditionNode = node.getChildOfASTNode();
            AbstractSyntaxTreeNode thenNode = conditionNode.getSiblingOfASTNode();
            AbstractSyntaxTreeNode elseNode = thenNode.getSiblingOfASTNode();

            //Add a BetaConditionalEvaluation node.
            BetaConditionalEvaluation betaNode = new BetaConditionalEvaluation();

            buildDeltaBody(thenNode, betaNode.getThenBody());
            buildDeltaBody(elseNode, betaNode.getElseBody());

            body.push(betaNode);

            buildDeltaBody(conditionNode, body);

            return;
        }

        //preOrder walk
        body.push(node);
        AbstractSyntaxTreeNode childNode = node.getChildOfASTNode();
        while (childNode != null) {
            buildDeltaBody(childNode, body);
            childNode = childNode.getSiblingOfASTNode();
        }
    }

    public boolean isASTStandardized() {
        return Standardized;
    }

    private static class PendingDeltaBody {
        Stack<AbstractSyntaxTreeNode> body;
        AbstractSyntaxTreeNode startNode;
    }
}

