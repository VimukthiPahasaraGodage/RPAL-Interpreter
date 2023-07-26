package com.proglangproj.group50.cse_machine;

import com.proglangproj.group50.abstractsyntaxtree.AbstractSyntaxTreeNode;
import com.proglangproj.group50.abstractsyntaxtree.AbstractSyntaxTreeNodeType;

import java.util.Stack;

/**
 * BetaConditionalEvaluation is a control structure used to handle conditionals in RPAL.
 * In the source code, a conditional expression 'cond -> then | else' becomes 'BetaConditionalEvaluation cond' on the control stack.
 * The BetaConditionalEvaluation class contains references to the standardized versions of 'then' and 'else' expressions,
 * which are to be evaluated based on the result of 'cond'.
 * <p>
 * The key feature of BetaConditionalEvaluation is its inversion of program order evaluation. This inversion is essential
 * for handling recursion and avoiding infinite loops. By putting 'cond' before BetaConditionalEvaluation on the control stack,
 * the condition is evaluated first. Then, based on the condition's result, the appropriate branch is chosen for evaluation,
 * either 'then' or 'else'.
 * <p>
 * This inversion breaks the recursion cycle in cases where 'then' or 'else' expressions themselves contain recursive calls.
 * By evaluating the condition first and deferring the evaluation of branches, we ensure that the recursive functions are not
 * infinitely called within the same evaluation cycle.
 * <p>
 * Overall, BetaConditionalEvaluation ensures proper handling of conditionals, allowing for controlled evaluation of branches
 * and avoiding recursive pitfalls.
 */
public class BetaConditionalEvaluation extends AbstractSyntaxTreeNode {
    private Stack<AbstractSyntaxTreeNode> thenBody;
    private Stack<AbstractSyntaxTreeNode> elseBody;

    public BetaConditionalEvaluation() {
        setTypeOfASTNode(AbstractSyntaxTreeNodeType.BETA);
        thenBody = new Stack<AbstractSyntaxTreeNode>();
        elseBody = new Stack<AbstractSyntaxTreeNode>();
    }

    public BetaConditionalEvaluation acceptASTNode(CopierOfNodes nodeCopier) {
        return nodeCopier.copy(this);
    }

    public Stack<AbstractSyntaxTreeNode> getThenBody() {
        return thenBody;
    }

    public void setThenBody(Stack<AbstractSyntaxTreeNode> thenBody) {
        this.thenBody = thenBody;
    }

    public Stack<AbstractSyntaxTreeNode> getElseBody() {
        return elseBody;
    }

    public void setElseBody(Stack<AbstractSyntaxTreeNode> elseBody) {
        this.elseBody = elseBody;
    }

}
