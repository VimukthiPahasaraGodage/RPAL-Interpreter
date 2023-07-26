package com.proglangproj.group50.abstractsyntaxtree;

import com.proglangproj.group50.cse_machine.NodeCopier;

public class AbstractSyntaxTreeNode {
    private AbstractSyntaxTreeNodeType typeOfASTNode;
    private String valueOfASTNode;
    private AbstractSyntaxTreeNode childOfASTNode;
    private AbstractSyntaxTreeNode siblingOfASTNode;
    private int lineNumberOfSourceFile;

    public AbstractSyntaxTreeNodeType getTypeOfASTNode() {
        return typeOfASTNode;
    }

    public void setTypeOfASTNode(AbstractSyntaxTreeNodeType typeOfASTNode) {
        this.typeOfASTNode = typeOfASTNode;
    }

    public AbstractSyntaxTreeNode getChildOfASTNode() {
        return childOfASTNode;
    }

    public void setChildOfASTNode(AbstractSyntaxTreeNode childOfASTNode) {
        this.childOfASTNode = childOfASTNode;
    }

    public AbstractSyntaxTreeNode getSiblingOfASTNode() {
        return siblingOfASTNode;
    }

    public void setSiblingOfASTNode(AbstractSyntaxTreeNode siblingOfASTNode) {
        this.siblingOfASTNode = siblingOfASTNode;
    }

    public String getValueOfASTNode() {
        return valueOfASTNode;
    }

    public void setValueOfASTNode(String valueOfASTNode) {
        this.valueOfASTNode = valueOfASTNode;
    }

    public AbstractSyntaxTreeNode acceptASTNode(NodeCopier nodeCopier) {
        return nodeCopier.copy(this);
    }

    public int getLineNumberOfSourceFile() {
        return lineNumberOfSourceFile;
    }

    public void setLineNumberOfSourceFile(int lineNumberOfSourceFile) {
        this.lineNumberOfSourceFile = lineNumberOfSourceFile;
    }
}
