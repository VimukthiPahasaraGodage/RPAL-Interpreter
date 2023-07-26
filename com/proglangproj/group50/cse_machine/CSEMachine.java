package com.proglangproj.group50.cse_machine;

import com.proglangproj.group50.abstractsyntaxtree.AbstractSyntaxTree;
import com.proglangproj.group50.abstractsyntaxtree.AbstractSyntaxTreeNode;
import com.proglangproj.group50.abstractsyntaxtree.AbstractSyntaxTreeNodeType;

import java.util.Stack;

public class CSEMachine {

    private final Stack<AbstractSyntaxTreeNode> valueStack;
    private final DeltaControlStructure rootDelta;
    public String evaluationResult;

    public CSEMachine(AbstractSyntaxTree ast) {
        if (!ast.isASTStandardized())
            throw new RuntimeException("AbstractSyntaxTree has NOT been standardized!");
        rootDelta = ast.createDeltas();
        rootDelta.setLinkedEnv(new Environment()); //primitive environment
        valueStack = new Stack<AbstractSyntaxTreeNode>();
    }

    private void printEvaluationErrorToStdOut(int sourceLineNumber, String message) {
        System.out.println("Error :" + sourceLineNumber + ": " + message);
        System.exit(1);
    }

    public void evaluateRPALProgram() {
        processControlStructures(rootDelta, rootDelta.getLinkedEnv());
    }

    private void processControlStructures(DeltaControlStructure currentDelta, Environment currentEnv) {
        //create a new control stack and add all of the delta's body to it so that the delta's body isn't
        //modified whenever the control stack is popped in all the functions below
        Stack<AbstractSyntaxTreeNode> controlStack = new Stack<AbstractSyntaxTreeNode>();
        controlStack.addAll(currentDelta.getBody());

        while (!controlStack.isEmpty())
            processCurrentNodeOfControlStructure(currentDelta, currentEnv, controlStack);
    }

    private void processCurrentNodeOfControlStructure(DeltaControlStructure currentDelta, Environment currentEnv, Stack<AbstractSyntaxTreeNode> currentControlStack) {
        AbstractSyntaxTreeNode node = currentControlStack.pop();
        if (!applyBinaryOperation(node) && !applyUnaryOperation(node)) {
            switch (node.getTypeOfASTNode()) {
                case IDENTIFIER -> handleIdentifiers(node, currentEnv);
                case NIL, TAU -> createTuple(node);
                case BETA -> handleBeta((BetaConditionalEvaluation) node, currentControlStack);
                case GAMMA -> applyGamma(currentDelta, node, currentEnv, currentControlStack);
                case DELTA -> {
                    ((DeltaControlStructure) node).setLinkedEnv(currentEnv); //RULE 2
                    valueStack.push(node);
                }
                default ->
                    // Although we use ASTNodes, a CSEM will only ever see a subset of all possible ASTNodeTypes.
                    // These are the types that are NOT standardized away into lambdas and gammas. E.g. types
                    // such as LET, WHERE, WITHIN, SIMULTDEF etc will NEVER be encountered by the CSEM
                        valueStack.push(node);
            }
        }
    }

    // RULE 6
    private boolean applyBinaryOperation(AbstractSyntaxTreeNode rator) {
        switch (rator.getTypeOfASTNode()) {
            case PLUS, MINUS, MULT, DIV, EXP, LS, LE, GR, GE -> {
                binaryArithmeticOperation(rator.getTypeOfASTNode());
                return true;
            }
            case EQ, NE -> {
                binaryLogicalEqualNotEqualOperation(rator.getTypeOfASTNode());
                return true;
            }
            case OR, AND -> {
                binaryLogicalOrAndOperations(rator.getTypeOfASTNode());
                return true;
            }
            case AUG -> {
                augTuples();
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private void binaryArithmeticOperation(AbstractSyntaxTreeNodeType type) {
        AbstractSyntaxTreeNode rand1 = valueStack.pop();
        AbstractSyntaxTreeNode rand2 = valueStack.pop();
        if (rand1.getTypeOfASTNode() != AbstractSyntaxTreeNodeType.INTEGER || rand2.getTypeOfASTNode() != AbstractSyntaxTreeNodeType.INTEGER)
            printEvaluationErrorToStdOut(rand1.getLineNumberOfSourceFile(), "Expected two integers; was given \"" + rand1.getValueOfASTNode() + "\", \"" + rand2.getValueOfASTNode() + "\"");

        AbstractSyntaxTreeNode result = new AbstractSyntaxTreeNode();
        result.setTypeOfASTNode(AbstractSyntaxTreeNodeType.INTEGER);

        switch (type) {
            case PLUS ->
                    result.setValueOfASTNode(Integer.toString(Integer.parseInt(rand1.getValueOfASTNode()) + Integer.parseInt(rand2.getValueOfASTNode())));
            case MINUS ->
                    result.setValueOfASTNode(Integer.toString(Integer.parseInt(rand1.getValueOfASTNode()) - Integer.parseInt(rand2.getValueOfASTNode())));
            case MULT ->
                    result.setValueOfASTNode(Integer.toString(Integer.parseInt(rand1.getValueOfASTNode()) * Integer.parseInt(rand2.getValueOfASTNode())));
            case DIV ->
                    result.setValueOfASTNode(Integer.toString(Integer.parseInt(rand1.getValueOfASTNode()) / Integer.parseInt(rand2.getValueOfASTNode())));
            case EXP ->
                    result.setValueOfASTNode(Integer.toString((int) Math.pow(Integer.parseInt(rand1.getValueOfASTNode()), Integer.parseInt(rand2.getValueOfASTNode()))));
            case LS -> {
                if (Integer.parseInt(rand1.getValueOfASTNode()) < Integer.parseInt(rand2.getValueOfASTNode()))
                    pushTrueNode();
                else
                    pushFalseNode();
                return;
            }
            case LE -> {
                if (Integer.parseInt(rand1.getValueOfASTNode()) <= Integer.parseInt(rand2.getValueOfASTNode()))
                    pushTrueNode();
                else
                    pushFalseNode();
                return;
            }
            case GR -> {
                if (Integer.parseInt(rand1.getValueOfASTNode()) > Integer.parseInt(rand2.getValueOfASTNode()))
                    pushTrueNode();
                else
                    pushFalseNode();
                return;
            }
            case GE -> {
                if (Integer.parseInt(rand1.getValueOfASTNode()) >= Integer.parseInt(rand2.getValueOfASTNode()))
                    pushTrueNode();
                else
                    pushFalseNode();
                return;
            }
            default -> {
            }
        }
        valueStack.push(result);
    }

    private void binaryLogicalEqualNotEqualOperation(AbstractSyntaxTreeNodeType type) {
        AbstractSyntaxTreeNode rand1 = valueStack.pop();
        AbstractSyntaxTreeNode rand2 = valueStack.pop();

        if (rand1.getTypeOfASTNode() == AbstractSyntaxTreeNodeType.TRUE || rand1.getTypeOfASTNode() == AbstractSyntaxTreeNodeType.FALSE) {
            if (rand2.getTypeOfASTNode() != AbstractSyntaxTreeNodeType.TRUE && rand2.getTypeOfASTNode() != AbstractSyntaxTreeNodeType.FALSE)
                printEvaluationErrorToStdOut(rand1.getLineNumberOfSourceFile(), "Cannot compare dissimilar types; was given \"" + rand1.getValueOfASTNode() + "\", \"" + rand2.getValueOfASTNode() + "\"");
            compareTruthValues(rand1, rand2, type);
            return;
        }

        if (rand1.getTypeOfASTNode() != rand2.getTypeOfASTNode())
            printEvaluationErrorToStdOut(rand1.getLineNumberOfSourceFile(), "Cannot compare dissimilar types; was given \"" + rand1.getValueOfASTNode() + "\", \"" + rand2.getValueOfASTNode() + "\"");

        if (rand1.getTypeOfASTNode() == AbstractSyntaxTreeNodeType.STRING)
            compareStrings(rand1, rand2, type);
        else if (rand1.getTypeOfASTNode() == AbstractSyntaxTreeNodeType.INTEGER)
            compareIntegers(rand1, rand2, type);
        else
            printEvaluationErrorToStdOut(rand1.getLineNumberOfSourceFile(), "Don't know how to " + type + " \"" + rand1.getValueOfASTNode() + "\", \"" + rand2.getValueOfASTNode() + "\"");

    }

    private void compareTruthValues(AbstractSyntaxTreeNode rand1, AbstractSyntaxTreeNode rand2, AbstractSyntaxTreeNodeType type) {
        if (rand1.getTypeOfASTNode() == rand2.getTypeOfASTNode())
            if (type == AbstractSyntaxTreeNodeType.EQ)
                pushTrueNode();
            else
                pushFalseNode();
        else if (type == AbstractSyntaxTreeNodeType.EQ)
            pushFalseNode();
        else
            pushTrueNode();
    }

    private void compareStrings(AbstractSyntaxTreeNode rand1, AbstractSyntaxTreeNode rand2, AbstractSyntaxTreeNodeType type) {
        if (rand1.getValueOfASTNode().equals(rand2.getValueOfASTNode()))
            if (type == AbstractSyntaxTreeNodeType.EQ)
                pushTrueNode();
            else
                pushFalseNode();
        else if (type == AbstractSyntaxTreeNodeType.EQ)
            pushFalseNode();
        else
            pushTrueNode();
    }

    private void compareIntegers(AbstractSyntaxTreeNode rand1, AbstractSyntaxTreeNode rand2, AbstractSyntaxTreeNodeType type) {
        if (Integer.parseInt(rand1.getValueOfASTNode()) == Integer.parseInt(rand2.getValueOfASTNode()))
            if (type == AbstractSyntaxTreeNodeType.EQ)
                pushTrueNode();
            else
                pushFalseNode();
        else if (type == AbstractSyntaxTreeNodeType.EQ)
            pushFalseNode();
        else
            pushTrueNode();
    }

    private void binaryLogicalOrAndOperations(AbstractSyntaxTreeNodeType type) {
        AbstractSyntaxTreeNode rand1 = valueStack.pop();
        AbstractSyntaxTreeNode rand2 = valueStack.pop();

        if ((rand1.getTypeOfASTNode() == AbstractSyntaxTreeNodeType.TRUE || rand1.getTypeOfASTNode() == AbstractSyntaxTreeNodeType.FALSE) &&
                (rand2.getTypeOfASTNode() == AbstractSyntaxTreeNodeType.TRUE || rand2.getTypeOfASTNode() == AbstractSyntaxTreeNodeType.FALSE)) {
            orAndTruthValues(rand1, rand2, type);
            return;
        }

        printEvaluationErrorToStdOut(rand1.getLineNumberOfSourceFile(), "Don't know how to " + type + " \"" + rand1.getValueOfASTNode() + "\", \"" + rand2.getValueOfASTNode() + "\"");
    }

    private void orAndTruthValues(AbstractSyntaxTreeNode rand1, AbstractSyntaxTreeNode rand2, AbstractSyntaxTreeNodeType type) {
        if (type == AbstractSyntaxTreeNodeType.OR) {
            if (rand1.getTypeOfASTNode() == AbstractSyntaxTreeNodeType.TRUE || rand2.getTypeOfASTNode() == AbstractSyntaxTreeNodeType.TRUE)
                pushTrueNode();
            else
                pushFalseNode();
        } else {
            if (rand1.getTypeOfASTNode() == AbstractSyntaxTreeNodeType.TRUE && rand2.getTypeOfASTNode() == AbstractSyntaxTreeNodeType.TRUE)
                pushTrueNode();
            else
                pushFalseNode();
        }
    }

    private void augTuples() {
        AbstractSyntaxTreeNode rand1 = valueStack.pop();
        AbstractSyntaxTreeNode rand2 = valueStack.pop();

        if (rand1.getTypeOfASTNode() != AbstractSyntaxTreeNodeType.TUPLE)
            printEvaluationErrorToStdOut(rand1.getLineNumberOfSourceFile(), "Cannot augment a non-tuple \"" + rand1.getValueOfASTNode() + "\"");

        AbstractSyntaxTreeNode childNode = rand1.getChildOfASTNode();
        if (childNode == null)
            rand1.setChildOfASTNode(rand2);
        else {
            while (childNode.getSiblingOfASTNode() != null)
                childNode = childNode.getSiblingOfASTNode();
            childNode.setSiblingOfASTNode(rand2);
        }
        rand2.setSiblingOfASTNode(null);

        valueStack.push(rand1);
    }

    // RULE 7
    private boolean applyUnaryOperation(AbstractSyntaxTreeNode rator) {
        switch (rator.getTypeOfASTNode()) {
            case NOT -> {
                not();
                return true;
            }
            case NEG -> {
                neg();
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private void not() {
        AbstractSyntaxTreeNode rand = valueStack.pop();
        if (rand.getTypeOfASTNode() != AbstractSyntaxTreeNodeType.TRUE && rand.getTypeOfASTNode() != AbstractSyntaxTreeNodeType.FALSE)
            printEvaluationErrorToStdOut(rand.getLineNumberOfSourceFile(), "Expecting a truthvalue; was given \"" + rand.getValueOfASTNode() + "\"");

        if (rand.getTypeOfASTNode() == AbstractSyntaxTreeNodeType.TRUE)
            pushFalseNode();
        else
            pushTrueNode();
    }

    private void neg() {
        AbstractSyntaxTreeNode rand = valueStack.pop();
        if (rand.getTypeOfASTNode() != AbstractSyntaxTreeNodeType.INTEGER)
            printEvaluationErrorToStdOut(rand.getLineNumberOfSourceFile(), "Expecting a truthvalue; was given \"" + rand.getValueOfASTNode() + "\"");

        AbstractSyntaxTreeNode result = new AbstractSyntaxTreeNode();
        result.setTypeOfASTNode(AbstractSyntaxTreeNodeType.INTEGER);
        result.setValueOfASTNode(Integer.toString(-1 * Integer.parseInt(rand.getValueOfASTNode())));
        valueStack.push(result);
    }

    //RULE 3
    private void applyGamma(DeltaControlStructure currentDelta, AbstractSyntaxTreeNode node, Environment currentEnv, Stack<AbstractSyntaxTreeNode> currentControlStack) {
        AbstractSyntaxTreeNode rator = valueStack.pop();
        AbstractSyntaxTreeNode rand = valueStack.pop();

        if (rator.getTypeOfASTNode() == AbstractSyntaxTreeNodeType.DELTA) {
            DeltaControlStructure nextDelta = (DeltaControlStructure) rator;

            //DeltaControlStructure has a link to the environment in effect when it is pushed on to the value stack (search
            //for 'RULE 2' in this file to see where it's done)
            //We construct a new environment here that will contain all the bindings (single or multiple)
            //required by this DeltaControlStructure. This new environment will link back to the environment carried by the DeltaControlStructure.
            Environment newEnv = new Environment();
            newEnv.setParent(nextDelta.getLinkedEnv());

            //RULE 4
            if (nextDelta.getBoundVars().size() == 1) {
                newEnv.addMapping(nextDelta.getBoundVars().get(0), rand);
            }
            //RULE 11
            else {
                if (rand.getTypeOfASTNode() != AbstractSyntaxTreeNodeType.TUPLE)
                    printEvaluationErrorToStdOut(rand.getLineNumberOfSourceFile(), "Expected a tuple; was given \"" + rand.getValueOfASTNode() + "\"");

                for (int i = 0; i < nextDelta.getBoundVars().size(); i++) {
                    newEnv.addMapping(nextDelta.getBoundVars().get(i), getNthTupleChild((Tuple) rand, i + 1)); //+ 1 coz tuple indexing starts at 1
                }
            }

            processControlStructures(nextDelta, newEnv);
        } else if (rator.getTypeOfASTNode() == AbstractSyntaxTreeNodeType.YSTAR) {
            //RULE 12
            if (rand.getTypeOfASTNode() != AbstractSyntaxTreeNodeType.DELTA)
                printEvaluationErrorToStdOut(rand.getLineNumberOfSourceFile(), "Expected a DeltaControlStructure; was given \"" + rand.getValueOfASTNode() + "\"");

            EtaRecursiveFixedPoint etaNode = new EtaRecursiveFixedPoint();
            etaNode.setDelta((DeltaControlStructure) rand);
            valueStack.push(etaNode);
        } else if (rator.getTypeOfASTNode() == AbstractSyntaxTreeNodeType.ETA) {
            //RULE 13
            //push back the rand, the eta and then the delta it contains
            valueStack.push(rand);
            valueStack.push(rator);
            valueStack.push(((EtaRecursiveFixedPoint) rator).getDelta());
            //push back two gammas (one for the eta and one for the delta)
            currentControlStack.push(node);
            currentControlStack.push(node);
        } else if (rator.getTypeOfASTNode() == AbstractSyntaxTreeNodeType.TUPLE) {
            tupleSelection((Tuple) rator, rand);
        } else if (!evaluatePredefinedFunctionsOfRPAL(rator, rand, currentControlStack))
            printEvaluationErrorToStdOut(rator.getLineNumberOfSourceFile(), "Don't know how to evaluate \"" + rator.getValueOfASTNode() + "\"");
    }

    private boolean evaluatePredefinedFunctionsOfRPAL(AbstractSyntaxTreeNode rator, AbstractSyntaxTreeNode rand, Stack<AbstractSyntaxTreeNode> currentControlStack) {
        switch (rator.getValueOfASTNode()) {
            case "Isinteger" -> {
                checkTypeAndPushTrueOrFalse(rand, AbstractSyntaxTreeNodeType.INTEGER);
                return true;
            }
            case "Isstring" -> {
                checkTypeAndPushTrueOrFalse(rand, AbstractSyntaxTreeNodeType.STRING);
                return true;
            }
            case "Isdummy" -> {
                checkTypeAndPushTrueOrFalse(rand, AbstractSyntaxTreeNodeType.DUMMY);
                return true;
            }
            case "Isfunction" -> {
                checkTypeAndPushTrueOrFalse(rand, AbstractSyntaxTreeNodeType.DELTA);
                return true;
            }
            case "Istuple" -> {
                checkTypeAndPushTrueOrFalse(rand, AbstractSyntaxTreeNodeType.TUPLE);
                return true;
            }
            case "Istruthvalue" -> {
                if (rand.getTypeOfASTNode() == AbstractSyntaxTreeNodeType.TRUE || rand.getTypeOfASTNode() == AbstractSyntaxTreeNodeType.FALSE)
                    pushTrueNode();
                else
                    pushFalseNode();
                return true;
            }
            case "Stem" -> {
                stem(rand);
                return true;
            }
            case "Stern" -> {
                stern(rand);
                return true;
            }
            case "Conc", "conc" -> { //typos
                conc(rand, currentControlStack);
                return true;
            }
            case "Print", "print" -> { //typos
                printNodeValue(rand);
                pushDummyNode();
                return true;
            }
            case "ItoS" -> {
                itos(rand);
                return true;
            }
            case "Order" -> {
                order(rand);
                return true;
            }
            case "Null" -> {
                isNullTuple(rand);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private void checkTypeAndPushTrueOrFalse(AbstractSyntaxTreeNode rand, AbstractSyntaxTreeNodeType type) {
        if (rand.getTypeOfASTNode() == type)
            pushTrueNode();
        else
            pushFalseNode();
    }

    private void pushTrueNode() {
        AbstractSyntaxTreeNode trueNode = new AbstractSyntaxTreeNode();
        trueNode.setTypeOfASTNode(AbstractSyntaxTreeNodeType.TRUE);
        trueNode.setValueOfASTNode("true");
        valueStack.push(trueNode);
    }

    private void pushFalseNode() {
        AbstractSyntaxTreeNode falseNode = new AbstractSyntaxTreeNode();
        falseNode.setTypeOfASTNode(AbstractSyntaxTreeNodeType.FALSE);
        falseNode.setValueOfASTNode("false");
        valueStack.push(falseNode);
    }

    private void pushDummyNode() {
        AbstractSyntaxTreeNode falseNode = new AbstractSyntaxTreeNode();
        falseNode.setTypeOfASTNode(AbstractSyntaxTreeNodeType.DUMMY);
        valueStack.push(falseNode);
    }

    private void stem(AbstractSyntaxTreeNode rand) {
        if (rand.getTypeOfASTNode() != AbstractSyntaxTreeNodeType.STRING)
            printEvaluationErrorToStdOut(rand.getLineNumberOfSourceFile(), "Expected a string; was given \"" + rand.getValueOfASTNode() + "\"");

        if (rand.getValueOfASTNode().isEmpty())
            rand.setValueOfASTNode("");
        else
            rand.setValueOfASTNode(rand.getValueOfASTNode().substring(0, 1));

        valueStack.push(rand);
    }

    private void stern(AbstractSyntaxTreeNode rand) {
        if (rand.getTypeOfASTNode() != AbstractSyntaxTreeNodeType.STRING)
            printEvaluationErrorToStdOut(rand.getLineNumberOfSourceFile(), "Expected a string; was given \"" + rand.getValueOfASTNode() + "\"");

        if (rand.getValueOfASTNode().isEmpty() || rand.getValueOfASTNode().length() == 1)
            rand.setValueOfASTNode("");
        else
            rand.setValueOfASTNode(rand.getValueOfASTNode().substring(1));

        valueStack.push(rand);
    }

    private void conc(AbstractSyntaxTreeNode rand1, Stack<AbstractSyntaxTreeNode> currentControlStack) {
        currentControlStack.pop();
        AbstractSyntaxTreeNode rand2 = valueStack.pop();
        if (rand1.getTypeOfASTNode() != AbstractSyntaxTreeNodeType.STRING || rand2.getTypeOfASTNode() != AbstractSyntaxTreeNodeType.STRING)
            printEvaluationErrorToStdOut(rand1.getLineNumberOfSourceFile(), "Expected two strings; was given \"" + rand1.getValueOfASTNode() + "\", \"" + rand2.getValueOfASTNode() + "\"");

        AbstractSyntaxTreeNode result = new AbstractSyntaxTreeNode();
        result.setTypeOfASTNode(AbstractSyntaxTreeNodeType.STRING);
        result.setValueOfASTNode(rand1.getValueOfASTNode() + rand2.getValueOfASTNode());

        valueStack.push(result);
    }

    private void itos(AbstractSyntaxTreeNode rand) {
        if (rand.getTypeOfASTNode() != AbstractSyntaxTreeNodeType.INTEGER)
            printEvaluationErrorToStdOut(rand.getLineNumberOfSourceFile(), "Expected an integer; was given \"" + rand.getValueOfASTNode() + "\"");

        rand.setTypeOfASTNode(AbstractSyntaxTreeNodeType.STRING); //all values are stored internally as strings, so nothing else to do
        valueStack.push(rand);
    }

    private void order(AbstractSyntaxTreeNode rand) {
        if (rand.getTypeOfASTNode() != AbstractSyntaxTreeNodeType.TUPLE)
            printEvaluationErrorToStdOut(rand.getLineNumberOfSourceFile(), "Expected a tuple; was given \"" + rand.getValueOfASTNode() + "\"");

        AbstractSyntaxTreeNode result = new AbstractSyntaxTreeNode();
        result.setTypeOfASTNode(AbstractSyntaxTreeNodeType.INTEGER);
        result.setValueOfASTNode(Integer.toString(getNumChildren(rand)));

        valueStack.push(result);
    }

    private void isNullTuple(AbstractSyntaxTreeNode rand) {
        if (rand.getTypeOfASTNode() != AbstractSyntaxTreeNodeType.TUPLE)
            printEvaluationErrorToStdOut(rand.getLineNumberOfSourceFile(), "Expected a tuple; was given \"" + rand.getValueOfASTNode() + "\"");

        if (getNumChildren(rand) == 0)
            pushTrueNode();
        else
            pushFalseNode();
    }

    // RULE 10
    private void tupleSelection(Tuple rator, AbstractSyntaxTreeNode rand) {
        if (rand.getTypeOfASTNode() != AbstractSyntaxTreeNodeType.INTEGER)
            printEvaluationErrorToStdOut(rand.getLineNumberOfSourceFile(), "Non-integer tuple selection with \"" + rand.getValueOfASTNode() + "\"");

        AbstractSyntaxTreeNode result = getNthTupleChild(rator, Integer.parseInt(rand.getValueOfASTNode()));
        if (result == null)
            printEvaluationErrorToStdOut(rand.getLineNumberOfSourceFile(), "Tuple selection index " + rand.getValueOfASTNode() + " out of bounds");

        valueStack.push(result);
    }

    /**
     * Get the nth element of the tuple. Note that n starts from 1 and NOT 0.
     *
     * @param tupleNode
     * @param n         n starts from 1 and NOT 0.
     * @return
     */
    private AbstractSyntaxTreeNode getNthTupleChild(Tuple tupleNode, int n) {
        AbstractSyntaxTreeNode childNode = tupleNode.getChildOfASTNode();
        for (int i = 1; i < n; ++i) { //tuple selection index starts at 1
            if (childNode == null)
                break;
            childNode = childNode.getSiblingOfASTNode();
        }
        return childNode;
    }

    private void handleIdentifiers(AbstractSyntaxTreeNode node, Environment currentEnv) {
        if (currentEnv.lookup(node.getValueOfASTNode()) != null) // RULE 1
            valueStack.push(currentEnv.lookup(node.getValueOfASTNode()));
        else if (isReservedIdentifier(node.getValueOfASTNode()))
            valueStack.push(node);
        else
            printEvaluationErrorToStdOut(node.getLineNumberOfSourceFile(), "Undeclared identifier \"" + node.getValueOfASTNode() + "\"");
    }

    //RULE 9
    private void createTuple(AbstractSyntaxTreeNode node) {
        int numChildren = getNumChildren(node);
        Tuple tupleNode = new Tuple();
        if (numChildren == 0) {
            valueStack.push(tupleNode);
            return;
        }

        AbstractSyntaxTreeNode childNode = null, tempNode = null;
        for (int i = 0; i < numChildren; ++i) {
            if (childNode == null)
                childNode = valueStack.pop();
            else if (tempNode == null) {
                tempNode = valueStack.pop();
                childNode.setSiblingOfASTNode(tempNode);
            } else {
                tempNode.setSiblingOfASTNode(valueStack.pop());
                tempNode = tempNode.getSiblingOfASTNode();
            }
        }
        tempNode.setSiblingOfASTNode(null);
        tupleNode.setChildOfASTNode(childNode);
        valueStack.push(tupleNode);
    }

    // RULE 8
    private void handleBeta(BetaConditionalEvaluation node, Stack<AbstractSyntaxTreeNode> currentControlStack) {
        AbstractSyntaxTreeNode conditionResultNode = valueStack.pop();

        if (conditionResultNode.getTypeOfASTNode() != AbstractSyntaxTreeNodeType.TRUE && conditionResultNode.getTypeOfASTNode() != AbstractSyntaxTreeNodeType.FALSE)
            printEvaluationErrorToStdOut(conditionResultNode.getLineNumberOfSourceFile(), "Expecting a truthvalue; found \"" + conditionResultNode.getValueOfASTNode() + "\"");

        if (conditionResultNode.getTypeOfASTNode() == AbstractSyntaxTreeNodeType.TRUE)
            currentControlStack.addAll(node.getThenBody());
        else
            currentControlStack.addAll(node.getElseBody());
    }

    private int getNumChildren(AbstractSyntaxTreeNode node) {
        int numChildren = 0;
        AbstractSyntaxTreeNode childNode = node.getChildOfASTNode();
        while (childNode != null) {
            numChildren++;
            childNode = childNode.getSiblingOfASTNode();
        }
        return numChildren;
    }

    private void printNodeValue(AbstractSyntaxTreeNode rand) {
        String evaluationResult = rand.getValueOfASTNode();
        evaluationResult = evaluationResult.replace("\\t", "\t");
        evaluationResult = evaluationResult.replace("\\n", "\n");
        this.evaluationResult = evaluationResult;
    }

    // Note how this list is different from the one defined in Scanner.java
    private boolean isReservedIdentifier(String value) {
        return switch (value) { //typos
            //typos
            case "Isinteger", "Isstring", "Istuple", "Isdummy", "Istruthvalue", "Isfunction", "ItoS", "Order", "Conc", "conc", "Stern", "Stem", "Null", "Print", "print", "neg" ->
                    true;
            default -> false;
        };
    }

}
