package com.proglangproj.group50.cse_machine;

import com.proglangproj.group50.abstractsyntaxtree.AbstractSyntaxTreeNode;
import com.proglangproj.group50.abstractsyntaxtree.AbstractSyntaxTreeNodeType;

/**
 * EtaRecursiveFixedPoint represents the result of applying the fixed-point operator (Y F) to a function F.
 * It is important to note that we do not actually evaluate the fixed-point itself. Instead, we rely on the program
 * to make the right choice in the recursion's base case, avoiding the evaluation of the fixed-point again.
 * <p>
 * The purpose of EtaRecursiveFixedPoint is to handle recursive functions efficiently. When a recursive function
 * is encountered in the source code, applying the fixed-point operator creates an opportunity for the program
 * to break out of infinite recursion. By allowing the program to choose the option that avoids the fixed-point,
 * we prevent unnecessary evaluations and potentially infinite loops.
 * <p>
 * However, it's essential to note that if the source code contains an infinite recursion, no tricks like fixed-point
 * operators can prevent the program from encountering infinite evaluation cycles.
 * <p>
 * EtaRecursiveFixedPoint is a critical element in managing recursive scenarios, enabling efficient evaluation of
 * recursive functions while avoiding infinite loops.
 */
public class EtaRecursiveFixedPoint extends AbstractSyntaxTreeNode {
    private DeltaControlStructure delta;

    public EtaRecursiveFixedPoint() {
        setTypeOfASTNode(AbstractSyntaxTreeNodeType.ETA);
    }

    //used if the program evaluation results in a partial application
    @Override
    public String getValueOfASTNode() {
        return "[eta closure: " + delta.getBoundVars().get(0) + ": " + delta.getIndex() + "]";
    }

    public EtaRecursiveFixedPoint acceptASTNode(CopierOfNodes nodeCopier) {
        return nodeCopier.copy(this);
    }

    public DeltaControlStructure getDelta() {
        return delta;
    }

    public void setDelta(DeltaControlStructure delta) {
        this.delta = delta;
    }

}
