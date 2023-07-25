package com.proglangproj.group50.cse_machine;

import java.util.Stack;
import com.proglangproj.group50.abstractsyntaxtree.AST;
import com.proglangproj.group50.abstractsyntaxtree.ASTNode;
import com.proglangproj.group50.abstractsyntaxtree.ASTNodeType;

public class CSEMachine{

  public String evaluationResult;

  private Stack<ASTNode> valueStack;
  private Delta rootDelta;

  public CSEMachine(AST ast){
    if(!ast.isASTStandardized())
      throw new RuntimeException("AST has NOT been standardized!"); //should never happen
    rootDelta = ast.createDeltas();
    rootDelta.setLinkedEnv(new Environment()); //primitive environment
    valueStack = new Stack<ASTNode>();
  }

  public void evaluateProgram(){
    processControlStack(rootDelta, rootDelta.getLinkedEnv());
  }

  private void processControlStack(Delta currentDelta, Environment currentEnv){
    //create a new control stack and add all of the delta's body to it so that the delta's body isn't
    //modified whenever the control stack is popped in all the functions below
    Stack<ASTNode> controlStack = new Stack<ASTNode>();
    controlStack.addAll(currentDelta.getBody());
    
    while(!controlStack.isEmpty())
      processCurrentNode(currentDelta, currentEnv, controlStack);
  }

  private void processCurrentNode(Delta currentDelta, Environment currentEnv, Stack<ASTNode> currentControlStack){
    ASTNode node = currentControlStack.pop();
    if(applyBinaryOperation(node))
      return;
    else if(applyUnaryOperation(node))
      return;
    else{
      switch(node.getTypeOfASTNode()){
        case IDENTIFIER:
          handleIdentifiers(node, currentEnv);
          break;
        case NIL:
        case TAU:
          createTuple(node);
          break;
        case BETA:
          handleBeta((Beta)node, currentControlStack);
          break;
        case GAMMA:
          applyGamma(currentDelta, node, currentEnv, currentControlStack);
          break;
        case DELTA:
          ((Delta)node).setLinkedEnv(currentEnv); //RULE 2
          valueStack.push(node);
          break;
        default:
          // Although we use ASTNodes, a CSEM will only ever see a subset of all possible ASTNodeTypes.
          // These are the types that are NOT standardized away into lambdas and gammas. E.g. types
          // such as LET, WHERE, WITHIN, SIMULTDEF etc will NEVER be encountered by the CSEM
          valueStack.push(node);
          break;
      }
    }
  }

  // RULE 6
  private boolean applyBinaryOperation(ASTNode rator){
    switch(rator.getTypeOfASTNode()){
      case PLUS:
      case MINUS:
      case MULT:
      case DIV:
      case EXP:
      case LS:
      case LE:
      case GR:
      case GE:
        binaryArithmeticOp(rator.getTypeOfASTNode());
        return true;
      case EQ:
      case NE:
        binaryLogicalEqNeOp(rator.getTypeOfASTNode());
        return true;
      case OR:
      case AND:
        binaryLogicalOrAndOp(rator.getTypeOfASTNode());
        return true;
      case AUG:
        augTuples();
        return true;
      default:
        return false;
    }
  }

  private void binaryArithmeticOp(ASTNodeType type){
    ASTNode rand1 = valueStack.pop();
    ASTNode rand2 = valueStack.pop();
    if(rand1.getTypeOfASTNode()!=ASTNodeType.INTEGER || rand2.getTypeOfASTNode()!=ASTNodeType.INTEGER)
      EvaluationError.printError(rand1.getLineNumberOfSourceFile(), "Expected two integers; was given \""+rand1.getValueOfASTNode()+"\", \""+rand2.getValueOfASTNode()+"\"");

    ASTNode result = new ASTNode();
    result.setTypeOfASTNode(ASTNodeType.INTEGER);

    switch(type){
      case PLUS:
        result.setValueOfASTNode(Integer.toString(Integer.parseInt(rand1.getValueOfASTNode())+Integer.parseInt(rand2.getValueOfASTNode())));
        break;
      case MINUS:
        result.setValueOfASTNode(Integer.toString(Integer.parseInt(rand1.getValueOfASTNode())-Integer.parseInt(rand2.getValueOfASTNode())));
        break;
      case MULT:
        result.setValueOfASTNode(Integer.toString(Integer.parseInt(rand1.getValueOfASTNode())*Integer.parseInt(rand2.getValueOfASTNode())));
        break;
      case DIV:
        result.setValueOfASTNode(Integer.toString(Integer.parseInt(rand1.getValueOfASTNode())/Integer.parseInt(rand2.getValueOfASTNode())));
        break;
      case EXP:
        result.setValueOfASTNode(Integer.toString((int)Math.pow(Integer.parseInt(rand1.getValueOfASTNode()), Integer.parseInt(rand2.getValueOfASTNode()))));
        break;
      case LS:
        if(Integer.parseInt(rand1.getValueOfASTNode())<Integer.parseInt(rand2.getValueOfASTNode()))
          pushTrueNode();
        else
          pushFalseNode();
        return;
      case LE:
        if(Integer.parseInt(rand1.getValueOfASTNode())<=Integer.parseInt(rand2.getValueOfASTNode()))
          pushTrueNode();
        else
          pushFalseNode();
        return;
      case GR:
        if(Integer.parseInt(rand1.getValueOfASTNode())>Integer.parseInt(rand2.getValueOfASTNode()))
          pushTrueNode();
        else
          pushFalseNode();
        return;
      case GE:
        if(Integer.parseInt(rand1.getValueOfASTNode())>=Integer.parseInt(rand2.getValueOfASTNode()))
          pushTrueNode();
        else
          pushFalseNode();
        return;
      default:
        break;
    }
    valueStack.push(result);
  }

  private void binaryLogicalEqNeOp(ASTNodeType type){
    ASTNode rand1 = valueStack.pop();
    ASTNode rand2 = valueStack.pop();

    if(rand1.getTypeOfASTNode()==ASTNodeType.TRUE || rand1.getTypeOfASTNode()==ASTNodeType.FALSE){
      if(rand2.getTypeOfASTNode()!=ASTNodeType.TRUE && rand2.getTypeOfASTNode()!=ASTNodeType.FALSE)
        EvaluationError.printError(rand1.getLineNumberOfSourceFile(), "Cannot compare dissimilar types; was given \""+rand1.getValueOfASTNode()+"\", \""+rand2.getValueOfASTNode()+"\"");
      compareTruthValues(rand1, rand2, type);
      return;
    }

    if(rand1.getTypeOfASTNode()!=rand2.getTypeOfASTNode())
      EvaluationError.printError(rand1.getLineNumberOfSourceFile(), "Cannot compare dissimilar types; was given \""+rand1.getValueOfASTNode()+"\", \""+rand2.getValueOfASTNode()+"\"");

    if(rand1.getTypeOfASTNode()==ASTNodeType.STRING)
      compareStrings(rand1, rand2, type);
    else if(rand1.getTypeOfASTNode()==ASTNodeType.INTEGER)
      compareIntegers(rand1, rand2, type);
    else
      EvaluationError.printError(rand1.getLineNumberOfSourceFile(), "Don't know how to " + type + " \""+rand1.getValueOfASTNode()+"\", \""+rand2.getValueOfASTNode()+"\"");

  }

  private void compareTruthValues(ASTNode rand1, ASTNode rand2, ASTNodeType type){
    if(rand1.getTypeOfASTNode()==rand2.getTypeOfASTNode())
      if(type==ASTNodeType.EQ)
        pushTrueNode();
      else
        pushFalseNode();
    else
      if(type==ASTNodeType.EQ)
        pushFalseNode();
      else
        pushTrueNode();
  }

  private void compareStrings(ASTNode rand1, ASTNode rand2, ASTNodeType type){
    if(rand1.getValueOfASTNode().equals(rand2.getValueOfASTNode()))
      if(type==ASTNodeType.EQ)
        pushTrueNode();
      else
        pushFalseNode();
    else
      if(type==ASTNodeType.EQ)
        pushFalseNode();
      else
        pushTrueNode();
  }

  private void compareIntegers(ASTNode rand1, ASTNode rand2, ASTNodeType type){
    if(Integer.parseInt(rand1.getValueOfASTNode())==Integer.parseInt(rand2.getValueOfASTNode()))
      if(type==ASTNodeType.EQ)
        pushTrueNode();
      else
        pushFalseNode();
    else
      if(type==ASTNodeType.EQ)
        pushFalseNode();
      else
        pushTrueNode();
  }

  private void binaryLogicalOrAndOp(ASTNodeType type){
    ASTNode rand1 = valueStack.pop();
    ASTNode rand2 = valueStack.pop();

    if((rand1.getTypeOfASTNode()==ASTNodeType.TRUE || rand1.getTypeOfASTNode()==ASTNodeType.FALSE) &&
        (rand2.getTypeOfASTNode()==ASTNodeType.TRUE || rand2.getTypeOfASTNode()==ASTNodeType.FALSE)){
      orAndTruthValues(rand1, rand2, type);
      return;
    }

    EvaluationError.printError(rand1.getLineNumberOfSourceFile(), "Don't know how to " + type + " \""+rand1.getValueOfASTNode()+"\", \""+rand2.getValueOfASTNode()+"\"");
  }

  private void orAndTruthValues(ASTNode rand1, ASTNode rand2, ASTNodeType type){
    if(type==ASTNodeType.OR){
      if(rand1.getTypeOfASTNode()==ASTNodeType.TRUE || rand2.getTypeOfASTNode()==ASTNodeType.TRUE)
        pushTrueNode();
      else
        pushFalseNode();
    }
    else{
      if(rand1.getTypeOfASTNode()==ASTNodeType.TRUE && rand2.getTypeOfASTNode()==ASTNodeType.TRUE)
        pushTrueNode();
      else
        pushFalseNode();
    }
  }

  private void augTuples(){
    ASTNode rand1 = valueStack.pop();
    ASTNode rand2 = valueStack.pop();

    if(rand1.getTypeOfASTNode()!=ASTNodeType.TUPLE)
      EvaluationError.printError(rand1.getLineNumberOfSourceFile(), "Cannot augment a non-tuple \""+rand1.getValueOfASTNode()+"\"");

    ASTNode childNode = rand1.getChildOfASTNode();
    if(childNode==null)
      rand1.setChildOfASTNode(rand2);
    else{
      while(childNode.getSiblingOfASTNode()!=null)
        childNode = childNode.getSiblingOfASTNode();
      childNode.setSiblingOfASTNode(rand2);
    }
    rand2.setSiblingOfASTNode(null);

    valueStack.push(rand1);
  }

  // RULE 7
  private boolean applyUnaryOperation(ASTNode rator){
    switch(rator.getTypeOfASTNode()){
      case NOT:
        not();
        return true;
      case NEG:
        neg();
        return true;
      default:
        return false;
    }
  }

  private void not(){
    ASTNode rand = valueStack.pop();
    if(rand.getTypeOfASTNode()!=ASTNodeType.TRUE && rand.getTypeOfASTNode()!=ASTNodeType.FALSE)
      EvaluationError.printError(rand.getLineNumberOfSourceFile(), "Expecting a truthvalue; was given \""+rand.getValueOfASTNode()+"\"");

    if(rand.getTypeOfASTNode()==ASTNodeType.TRUE)
      pushFalseNode();
    else
      pushTrueNode();
  }

  private void neg(){
    ASTNode rand = valueStack.pop();
    if(rand.getTypeOfASTNode()!=ASTNodeType.INTEGER)
      EvaluationError.printError(rand.getLineNumberOfSourceFile(), "Expecting a truthvalue; was given \""+rand.getValueOfASTNode()+"\"");

    ASTNode result = new ASTNode();
    result.setTypeOfASTNode(ASTNodeType.INTEGER);
    result.setValueOfASTNode(Integer.toString(-1*Integer.parseInt(rand.getValueOfASTNode())));
    valueStack.push(result);
  }

  //RULE 3
  private void applyGamma(Delta currentDelta, ASTNode node, Environment currentEnv, Stack<ASTNode> currentControlStack){
    ASTNode rator = valueStack.pop();
    ASTNode rand = valueStack.pop();

    if(rator.getTypeOfASTNode()==ASTNodeType.DELTA){
      Delta nextDelta = (Delta) rator;
      
      //Delta has a link to the environment in effect when it is pushed on to the value stack (search
      //for 'RULE 2' in this file to see where it's done)
      //We construct a new environment here that will contain all the bindings (single or multiple)
      //required by this Delta. This new environment will link back to the environment carried by the Delta.
      Environment newEnv = new Environment();
      newEnv.setParent(nextDelta.getLinkedEnv());
      
      //RULE 4
      if(nextDelta.getBoundVars().size()==1){
        newEnv.addMapping(nextDelta.getBoundVars().get(0), rand);
      }
      //RULE 11
      else{
        if(rand.getTypeOfASTNode()!=ASTNodeType.TUPLE)
          EvaluationError.printError(rand.getLineNumberOfSourceFile(), "Expected a tuple; was given \""+rand.getValueOfASTNode()+"\"");
        
        for(int i = 0; i < nextDelta.getBoundVars().size(); i++){
          newEnv.addMapping(nextDelta.getBoundVars().get(i), getNthTupleChild((Tuple)rand, i+1)); //+ 1 coz tuple indexing starts at 1
        }
      }
      
      processControlStack(nextDelta, newEnv);
      return;
    }
    else if(rator.getTypeOfASTNode()==ASTNodeType.YSTAR){
      //RULE 12
      if(rand.getTypeOfASTNode()!=ASTNodeType.DELTA)
        EvaluationError.printError(rand.getLineNumberOfSourceFile(), "Expected a Delta; was given \""+rand.getValueOfASTNode()+"\"");
      
      Eta etaNode = new Eta();
      etaNode.setDelta((Delta)rand);
      valueStack.push(etaNode);
      return;
    }
    else if(rator.getTypeOfASTNode()==ASTNodeType.ETA){
      //RULE 13
      //push back the rand, the eta and then the delta it contains
      valueStack.push(rand);
      valueStack.push(rator);
      valueStack.push(((Eta)rator).getDelta());
      //push back two gammas (one for the eta and one for the delta)
      currentControlStack.push(node);
      currentControlStack.push(node);
      return;
    }
    else if(rator.getTypeOfASTNode()==ASTNodeType.TUPLE){
      tupleSelection((Tuple)rator, rand);
      return;
    }
    else if(evaluateReservedIdentifiers(rator, rand, currentControlStack))
      return;
    else
      EvaluationError.printError(rator.getLineNumberOfSourceFile(), "Don't know how to evaluate \""+rator.getValueOfASTNode()+"\"");
  }

  private boolean evaluateReservedIdentifiers(ASTNode rator, ASTNode rand, Stack<ASTNode> currentControlStack){
    switch(rator.getValueOfASTNode()){
      case "Isinteger":
        checkTypeAndPushTrueOrFalse(rand, ASTNodeType.INTEGER);
        return true;
      case "Isstring":
        checkTypeAndPushTrueOrFalse(rand, ASTNodeType.STRING);
        return true;
      case "Isdummy":
        checkTypeAndPushTrueOrFalse(rand, ASTNodeType.DUMMY);
        return true;
      case "Isfunction":
        checkTypeAndPushTrueOrFalse(rand, ASTNodeType.DELTA);
        return true;
      case "Istuple":
        checkTypeAndPushTrueOrFalse(rand, ASTNodeType.TUPLE);
        return true;
      case "Istruthvalue":
        if(rand.getTypeOfASTNode()==ASTNodeType.TRUE||rand.getTypeOfASTNode()==ASTNodeType.FALSE)
          pushTrueNode();
        else
          pushFalseNode();
        return true;
      case "Stem":
        stem(rand);
        return true;
      case "Stern":
        stern(rand);
        return true;
      case "Conc":
      case "conc": //typos
        conc(rand, currentControlStack);
        return true;
      case "Print":
      case "print": //typos
        printNodeValue(rand);
        pushDummyNode();
        return true;
      case "ItoS":
        itos(rand);
        return true;
      case "Order":
        order(rand);
        return true;
      case "Null":
        isNullTuple(rand);
        return true;
      default:
        return false;
    }
  }

  private void checkTypeAndPushTrueOrFalse(ASTNode rand, ASTNodeType type){
    if(rand.getTypeOfASTNode()==type)
      pushTrueNode();
    else
      pushFalseNode();
  }

  private void pushTrueNode(){
    ASTNode trueNode = new ASTNode();
    trueNode.setTypeOfASTNode(ASTNodeType.TRUE);
    trueNode.setValueOfASTNode("true");
    valueStack.push(trueNode);
  }
  
  private void pushFalseNode(){
    ASTNode falseNode = new ASTNode();
    falseNode.setTypeOfASTNode(ASTNodeType.FALSE);
    falseNode.setValueOfASTNode("false");
    valueStack.push(falseNode);
  }

  private void pushDummyNode(){
    ASTNode falseNode = new ASTNode();
    falseNode.setTypeOfASTNode(ASTNodeType.DUMMY);
    valueStack.push(falseNode);
  }

  private void stem(ASTNode rand){
    if(rand.getTypeOfASTNode()!=ASTNodeType.STRING)
      EvaluationError.printError(rand.getLineNumberOfSourceFile(), "Expected a string; was given \""+rand.getValueOfASTNode()+"\"");
    
    if(rand.getValueOfASTNode().isEmpty())
      rand.setValueOfASTNode("");
    else
      rand.setValueOfASTNode(rand.getValueOfASTNode().substring(0,1));
    
    valueStack.push(rand);
  }

  private void stern(ASTNode rand){
    if(rand.getTypeOfASTNode()!=ASTNodeType.STRING)
      EvaluationError.printError(rand.getLineNumberOfSourceFile(), "Expected a string; was given \""+rand.getValueOfASTNode()+"\"");
    
    if(rand.getValueOfASTNode().isEmpty() || rand.getValueOfASTNode().length()==1)
      rand.setValueOfASTNode("");
    else
      rand.setValueOfASTNode(rand.getValueOfASTNode().substring(1));
    
    valueStack.push(rand);
  }

  private void conc(ASTNode rand1, Stack<ASTNode> currentControlStack){
    currentControlStack.pop();
    ASTNode rand2 = valueStack.pop();
    if(rand1.getTypeOfASTNode()!=ASTNodeType.STRING || rand2.getTypeOfASTNode()!=ASTNodeType.STRING)
      EvaluationError.printError(rand1.getLineNumberOfSourceFile(), "Expected two strings; was given \""+rand1.getValueOfASTNode()+"\", \""+rand2.getValueOfASTNode()+"\"");

    ASTNode result = new ASTNode();
    result.setTypeOfASTNode(ASTNodeType.STRING);
    result.setValueOfASTNode(rand1.getValueOfASTNode()+rand2.getValueOfASTNode());
    
    valueStack.push(result);
  }

  private void itos(ASTNode rand){
    if(rand.getTypeOfASTNode()!=ASTNodeType.INTEGER)
      EvaluationError.printError(rand.getLineNumberOfSourceFile(), "Expected an integer; was given \""+rand.getValueOfASTNode()+"\"");
    
    rand.setTypeOfASTNode(ASTNodeType.STRING); //all values are stored internally as strings, so nothing else to do
    valueStack.push(rand);
  }

  private void order(ASTNode rand){
    if(rand.getTypeOfASTNode()!=ASTNodeType.TUPLE)
      EvaluationError.printError(rand.getLineNumberOfSourceFile(), "Expected a tuple; was given \""+rand.getValueOfASTNode()+"\"");

    ASTNode result = new ASTNode();
    result.setTypeOfASTNode(ASTNodeType.INTEGER);
    result.setValueOfASTNode(Integer.toString(getNumChildren(rand)));
    
    valueStack.push(result);
  }

  private void isNullTuple(ASTNode rand){
    if(rand.getTypeOfASTNode()!=ASTNodeType.TUPLE)
      EvaluationError.printError(rand.getLineNumberOfSourceFile(), "Expected a tuple; was given \""+rand.getValueOfASTNode()+"\"");

    if(getNumChildren(rand)==0)
      pushTrueNode();
    else
      pushFalseNode();
  }

  // RULE 10
  private void tupleSelection(Tuple rator, ASTNode rand){
    if(rand.getTypeOfASTNode()!=ASTNodeType.INTEGER)
      EvaluationError.printError(rand.getLineNumberOfSourceFile(), "Non-integer tuple selection with \""+rand.getValueOfASTNode()+"\"");

    ASTNode result = getNthTupleChild(rator, Integer.parseInt(rand.getValueOfASTNode()));
    if(result==null)
      EvaluationError.printError(rand.getLineNumberOfSourceFile(), "Tuple selection index "+rand.getValueOfASTNode()+" out of bounds");

    valueStack.push(result);
  }

  /**
   * Get the nth element of the tuple. Note that n starts from 1 and NOT 0.
   * @param tupleNode
   * @param n n starts from 1 and NOT 0.
   * @return
   */
  private ASTNode getNthTupleChild(Tuple tupleNode, int n){
    ASTNode childNode = tupleNode.getChildOfASTNode();
    for(int i=1;i<n;++i){ //tuple selection index starts at 1
      if(childNode==null)
        break;
      childNode = childNode.getSiblingOfASTNode();
    }
    return childNode;
  }

  private void handleIdentifiers(ASTNode node, Environment currentEnv){
    if(currentEnv.lookup(node.getValueOfASTNode())!=null) // RULE 1
      valueStack.push(currentEnv.lookup(node.getValueOfASTNode()));
    else if(isReservedIdentifier(node.getValueOfASTNode()))
      valueStack.push(node);
    else
      EvaluationError.printError(node.getLineNumberOfSourceFile(), "Undeclared identifier \""+node.getValueOfASTNode()+"\"");
  }

  //RULE 9
  private void createTuple(ASTNode node){
    int numChildren = getNumChildren(node);
    Tuple tupleNode = new Tuple();
    if(numChildren==0){
      valueStack.push(tupleNode);
      return;
    }

    ASTNode childNode = null, tempNode = null;
    for(int i=0;i<numChildren;++i){
      if(childNode==null)
        childNode = valueStack.pop();
      else if(tempNode==null){
        tempNode = valueStack.pop();
        childNode.setSiblingOfASTNode(tempNode);
      }
      else{
        tempNode.setSiblingOfASTNode(valueStack.pop());
        tempNode = tempNode.getSiblingOfASTNode();
      }
    }
    tempNode.setSiblingOfASTNode(null);
    tupleNode.setChildOfASTNode(childNode);
    valueStack.push(tupleNode);
  }

  // RULE 8
  private void handleBeta(Beta node, Stack<ASTNode> currentControlStack){
    ASTNode conditionResultNode = valueStack.pop();

    if(conditionResultNode.getTypeOfASTNode()!=ASTNodeType.TRUE && conditionResultNode.getTypeOfASTNode()!=ASTNodeType.FALSE)
      EvaluationError.printError(conditionResultNode.getLineNumberOfSourceFile(), "Expecting a truthvalue; found \""+conditionResultNode.getValueOfASTNode()+"\"");

    if(conditionResultNode.getTypeOfASTNode()==ASTNodeType.TRUE)
      currentControlStack.addAll(node.getThenBody());
    else
      currentControlStack.addAll(node.getElseBody());
  }

  private int getNumChildren(ASTNode node){
    int numChildren = 0;
    ASTNode childNode = node.getChildOfASTNode();
    while(childNode!=null){
      numChildren++;
      childNode = childNode.getSiblingOfASTNode();
    }
    return numChildren;
  }
  
  private void printNodeValue(ASTNode rand){
    String evaluationResult = rand.getValueOfASTNode();
    evaluationResult = evaluationResult.replace("\\t", "\t");
    evaluationResult = evaluationResult.replace("\\n", "\n");
    this.evaluationResult = evaluationResult;
  }

  // Note how this list is different from the one defined in Scanner.java
  private boolean isReservedIdentifier(String value){
    switch(value){
      case "Isinteger":
      case "Isstring":
      case "Istuple":
      case "Isdummy":
      case "Istruthvalue":
      case "Isfunction":
      case "ItoS":
      case "Order":
      case "Conc":
      case "conc": //typos
      case "Stern":
      case "Stem":
      case "Null":
      case "Print":
      case "print": //typos
      case "neg":
        return true;
    }
    return false;
  }

}
