package com.proglangproj.group50.cse_machine;

import com.proglangproj.group50.abstractsyntaxtree.AbstractSyntaxTreeNode;
import com.proglangproj.group50.abstractsyntaxtree.AbstractSyntaxTreeNodeType;

public class Tuple extends AbstractSyntaxTreeNode {

    public Tuple() {
        setTypeOfASTNode(AbstractSyntaxTreeNodeType.TUPLE);
    }

    @Override
    public String getValueOfASTNode() {
        AbstractSyntaxTreeNode childNode = getChildOfASTNode();
        if (childNode == null)
            return "nil";

        String printValue = "(";
        while (childNode.getSiblingOfASTNode() != null) {
            printValue += childNode.getValueOfASTNode() + ", ";
            childNode = childNode.getSiblingOfASTNode();
        }
        printValue += childNode.getValueOfASTNode() + ")";
        return printValue;
    }

    public Tuple acceptASTNode(CopierOfNodes nodeCopier) {
        return nodeCopier.copy(this);
    }

}
