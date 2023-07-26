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
    if(!ast.Is_Standardized())
      throw new RuntimeException("AST has NOT been standardized!"); //should never happen
    rootDelta = ast.Create_Deltas();
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
      switch(node.getType()){
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
    switch(rator.getType()){
      case PLUS:
      case MINUS:
      case MULT:
      case DIV:
      case EXP:
      case LS:
      case LE:
      case GR:
      case GE:
        binaryArithmeticOp(rator.getType());
        return true;
      case EQ:
      case NE:
        binaryLogicalEqNeOp(rator.getType());
        return true;
      case OR:
      case AND:
        binaryLogicalOrAndOp(rator.getType());
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
    if(rand1.getType()!=ASTNodeType.INTEGER || rand2.getType()!=ASTNodeType.INTEGER)
      EvaluationError.printError(rand1.getSource_Line_Num(), "Expected two integers; was given \""+rand1.getVal()+"\", \""+rand2.getVal()+"\"");

    ASTNode result = new ASTNode();
    result.setType(ASTNodeType.INTEGER);

    switch(type){
      case PLUS:
        result.setVal(Integer.toString(Integer.parseInt(rand1.getVal())+Integer.parseInt(rand2.getVal())));
        break;
      case MINUS:
        result.setVal(Integer.toString(Integer.parseInt(rand1.getVal())-Integer.parseInt(rand2.getVal())));
        break;
      case MULT:
        result.setVal(Integer.toString(Integer.parseInt(rand1.getVal())*Integer.parseInt(rand2.getVal())));
        break;
      case DIV:
        result.setVal(Integer.toString(Integer.parseInt(rand1.getVal())/Integer.parseInt(rand2.getVal())));
        break;
      case EXP:
        result.setVal(Integer.toString((int)Math.pow(Integer.parseInt(rand1.getVal()), Integer.parseInt(rand2.getVal()))));
        break;
      case LS:
        if(Integer.parseInt(rand1.getVal())<Integer.parseInt(rand2.getVal()))
          pushTrueNode();
        else
          pushFalseNode();
        return;
      case LE:
        if(Integer.parseInt(rand1.getVal())<=Integer.parseInt(rand2.getVal()))
          pushTrueNode();
        else
          pushFalseNode();
        return;
      case GR:
        if(Integer.parseInt(rand1.getVal())>Integer.parseInt(rand2.getVal()))
          pushTrueNode();
        else
          pushFalseNode();
        return;
      case GE:
        if(Integer.parseInt(rand1.getVal())>=Integer.parseInt(rand2.getVal()))
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

    if(rand1.getType()==ASTNodeType.TRUE || rand1.getType()==ASTNodeType.FALSE){
      if(rand2.getType()!=ASTNodeType.TRUE && rand2.getType()!=ASTNodeType.FALSE)
        EvaluationError.printError(rand1.getSource_Line_Num(), "Cannot compare dissimilar types; was given \""+rand1.getVal()+"\", \""+rand2.getVal()+"\"");
      compareTruthValues(rand1, rand2, type);
      return;
    }

    if(rand1.getType()!=rand2.getType())
      EvaluationError.printError(rand1.getSource_Line_Num(), "Cannot compare dissimilar types; was given \""+rand1.getVal()+"\", \""+rand2.getVal()+"\"");

    if(rand1.getType()==ASTNodeType.STRING)
      compareStrings(rand1, rand2, type);
    else if(rand1.getType()==ASTNodeType.INTEGER)
      compareIntegers(rand1, rand2, type);
    else
      EvaluationError.printError(rand1.getSource_Line_Num(), "Don't know how to " + type + " \""+rand1.getVal()+"\", \""+rand2.getVal()+"\"");

  }

  private void compareTruthValues(ASTNode rand1, ASTNode rand2, ASTNodeType type){
    if(rand1.getType()==rand2.getType())
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
    if(rand1.getVal().equals(rand2.getVal()))
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
    if(Integer.parseInt(rand1.getVal())==Integer.parseInt(rand2.getVal()))
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

    if((rand1.getType()==ASTNodeType.TRUE || rand1.getType()==ASTNodeType.FALSE) &&
        (rand2.getType()==ASTNodeType.TRUE || rand2.getType()==ASTNodeType.FALSE)){
      orAndTruthValues(rand1, rand2, type);
      return;
    }

    EvaluationError.printError(rand1.getSource_Line_Num(), "Don't know how to " + type + " \""+rand1.getVal()+"\", \""+rand2.getVal()+"\"");
  }

  private void orAndTruthValues(ASTNode rand1, ASTNode rand2, ASTNodeType type){
    if(type==ASTNodeType.OR){
      if(rand1.getType()==ASTNodeType.TRUE || rand2.getType()==ASTNodeType.TRUE)
        pushTrueNode();
      else
        pushFalseNode();
    }
    else{
      if(rand1.getType()==ASTNodeType.TRUE && rand2.getType()==ASTNodeType.TRUE)
        pushTrueNode();
      else
        pushFalseNode();
    }
  }

  private void augTuples(){
    ASTNode rand1 = valueStack.pop();
    ASTNode rand2 = valueStack.pop();

    if(rand1.getType()!=ASTNodeType.TUPLE)
      EvaluationError.printError(rand1.getSource_Line_Num(), "Cannot augment a non-tuple \""+rand1.getVal()+"\"");

    ASTNode childNode = rand1.getChild();
    if(childNode==null)
      rand1.setChild(rand2);
    else{
      while(childNode.getSibling()!=null)
        childNode = childNode.getSibling();
      childNode.setSibling(rand2);
    }
    rand2.setSibling(null);

    valueStack.push(rand1);
  }

  // RULE 7
  private boolean applyUnaryOperation(ASTNode rator){
    switch(rator.getType()){
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
    if(rand.getType()!=ASTNodeType.TRUE && rand.getType()!=ASTNodeType.FALSE)
      EvaluationError.printError(rand.getSource_Line_Num(), "Expecting a truthvalue; was given \""+rand.getVal()+"\"");

    if(rand.getType()==ASTNodeType.TRUE)
      pushFalseNode();
    else
      pushTrueNode();
  }

  private void neg(){
    ASTNode rand = valueStack.pop();
    if(rand.getType()!=ASTNodeType.INTEGER)
      EvaluationError.printError(rand.getSource_Line_Num(), "Expecting a truthvalue; was given \""+rand.getVal()+"\"");

    ASTNode result = new ASTNode();
    result.setType(ASTNodeType.INTEGER);
    result.setVal(Integer.toString(-1*Integer.parseInt(rand.getVal())));
    valueStack.push(result);
  }

  //RULE 3
  private void applyGamma(Delta currentDelta, ASTNode node, Environment currentEnv, Stack<ASTNode> currentControlStack){
    ASTNode rator = valueStack.pop();
    ASTNode rand = valueStack.pop();

    if(rator.getType()==ASTNodeType.DELTA){
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
        if(rand.getType()!=ASTNodeType.TUPLE)
          EvaluationError.printError(rand.getSource_Line_Num(), "Expected a tuple; was given \""+rand.getVal()+"\"");
        
        for(int i = 0; i < nextDelta.getBoundVars().size(); i++){
          newEnv.addMapping(nextDelta.getBoundVars().get(i), getNthTupleChild((Tuple)rand, i+1)); //+ 1 coz tuple indexing starts at 1
        }
      }
      
      processControlStack(nextDelta, newEnv);
      return;
    }
    else if(rator.getType()==ASTNodeType.YSTAR){
      //RULE 12
      if(rand.getType()!=ASTNodeType.DELTA)
        EvaluationError.printError(rand.getSource_Line_Num(), "Expected a Delta; was given \""+rand.getVal()+"\"");
      
      Eta etaNode = new Eta();
      etaNode.setDelta((Delta)rand);
      valueStack.push(etaNode);
      return;
    }
    else if(rator.getType()==ASTNodeType.ETA){
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
    else if(rator.getType()==ASTNodeType.TUPLE){
      tupleSelection((Tuple)rator, rand);
      return;
    }
    else if(evaluateReservedIdentifiers(rator, rand, currentControlStack))
      return;
    else
      EvaluationError.printError(rator.getSource_Line_Num(), "Don't know how to evaluate \""+rator.getVal()+"\"");
  }

  private boolean evaluateReservedIdentifiers(ASTNode rator, ASTNode rand, Stack<ASTNode> currentControlStack){
    switch(rator.getVal()){
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
        if(rand.getType()==ASTNodeType.TRUE||rand.getType()==ASTNodeType.FALSE)
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
    if(rand.getType()==type)
      pushTrueNode();
    else
      pushFalseNode();
  }

  private void pushTrueNode(){
    ASTNode trueNode = new ASTNode();
    trueNode.setType(ASTNodeType.TRUE);
    trueNode.setVal("true");
    valueStack.push(trueNode);
  }
  
  private void pushFalseNode(){
    ASTNode falseNode = new ASTNode();
    falseNode.setType(ASTNodeType.FALSE);
    falseNode.setVal("false");
    valueStack.push(falseNode);
  }

  private void pushDummyNode(){
    ASTNode falseNode = new ASTNode();
    falseNode.setType(ASTNodeType.DUMMY);
    valueStack.push(falseNode);
  }

  private void stem(ASTNode rand){
    if(rand.getType()!=ASTNodeType.STRING)
      EvaluationError.printError(rand.getSource_Line_Num(), "Expected a string; was given \""+rand.getVal()+"\"");
    
    if(rand.getVal().isEmpty())
      rand.setVal("");
    else
      rand.setVal(rand.getVal().substring(0,1));
    
    valueStack.push(rand);
  }

  private void stern(ASTNode rand){
    if(rand.getType()!=ASTNodeType.STRING)
      EvaluationError.printError(rand.getSource_Line_Num(), "Expected a string; was given \""+rand.getVal()+"\"");
    
    if(rand.getVal().isEmpty() || rand.getVal().length()==1)
      rand.setVal("");
    else
      rand.setVal(rand.getVal().substring(1));
    
    valueStack.push(rand);
  }

  private void conc(ASTNode rand1, Stack<ASTNode> currentControlStack){
    currentControlStack.pop();
    ASTNode rand2 = valueStack.pop();
    if(rand1.getType()!=ASTNodeType.STRING || rand2.getType()!=ASTNodeType.STRING)
      EvaluationError.printError(rand1.getSource_Line_Num(), "Expected two strings; was given \""+rand1.getVal()+"\", \""+rand2.getVal()+"\"");

    ASTNode result = new ASTNode();
    result.setType(ASTNodeType.STRING);
    result.setVal(rand1.getVal()+rand2.getVal());
    
    valueStack.push(result);
  }

  private void itos(ASTNode rand){
    if(rand.getType()!=ASTNodeType.INTEGER)
      EvaluationError.printError(rand.getSource_Line_Num(), "Expected an integer; was given \""+rand.getVal()+"\"");
    
    rand.setType(ASTNodeType.STRING); //all values are stored internally as strings, so nothing else to do
    valueStack.push(rand);
  }

  private void order(ASTNode rand){
    if(rand.getType()!=ASTNodeType.TUPLE)
      EvaluationError.printError(rand.getSource_Line_Num(), "Expected a tuple; was given \""+rand.getVal()+"\"");

    ASTNode result = new ASTNode();
    result.setType(ASTNodeType.INTEGER);
    result.setVal(Integer.toString(getNumChildren(rand)));
    
    valueStack.push(result);
  }

  private void isNullTuple(ASTNode rand){
    if(rand.getType()!=ASTNodeType.TUPLE)
      EvaluationError.printError(rand.getSource_Line_Num(), "Expected a tuple; was given \""+rand.getVal()+"\"");

    if(getNumChildren(rand)==0)
      pushTrueNode();
    else
      pushFalseNode();
  }

  // RULE 10
  private void tupleSelection(Tuple rator, ASTNode rand){
    if(rand.getType()!=ASTNodeType.INTEGER)
      EvaluationError.printError(rand.getSource_Line_Num(), "Non-integer tuple selection with \""+rand.getVal()+"\"");

    ASTNode result = getNthTupleChild(rator, Integer.parseInt(rand.getVal()));
    if(result==null)
      EvaluationError.printError(rand.getSource_Line_Num(), "Tuple selection index "+rand.getVal()+" out of bounds");

    valueStack.push(result);
  }

  /**
   * Get the nth element of the tuple. Note that n starts from 1 and NOT 0.
   * @param tupleNode
   * @param n n starts from 1 and NOT 0.
   * @return
   */
  private ASTNode getNthTupleChild(Tuple tupleNode, int n){
    ASTNode childNode = tupleNode.getChild();
    for(int i=1;i<n;++i){ //tuple selection index starts at 1
      if(childNode==null)
        break;
      childNode = childNode.getSibling();
    }
    return childNode;
  }

  private void handleIdentifiers(ASTNode node, Environment currentEnv){
    if(currentEnv.lookup(node.getVal())!=null) // RULE 1
      valueStack.push(currentEnv.lookup(node.getVal()));
    else if(isReservedIdentifier(node.getVal()))
      valueStack.push(node);
    else
      EvaluationError.printError(node.getSource_Line_Num(), "Undeclared identifier \""+node.getVal()+"\"");
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
        childNode.setSibling(tempNode);
      }
      else{
        tempNode.setSibling(valueStack.pop());
        tempNode = tempNode.getSibling();
      }
    }
    tempNode.setSibling(null);
    tupleNode.setChild(childNode);
    valueStack.push(tupleNode);
  }

  // RULE 8
  private void handleBeta(Beta node, Stack<ASTNode> currentControlStack){
    ASTNode conditionResultNode = valueStack.pop();

    if(conditionResultNode.getType()!=ASTNodeType.TRUE && conditionResultNode.getType()!=ASTNodeType.FALSE)
      EvaluationError.printError(conditionResultNode.getSource_Line_Num(), "Expecting a truthvalue; found \""+conditionResultNode.getVal()+"\"");

    if(conditionResultNode.getType()==ASTNodeType.TRUE)
      currentControlStack.addAll(node.getThenNode());
    else
      currentControlStack.addAll(node.getElseNode());
  }

  private int getNumChildren(ASTNode node){
    int numChildren = 0;
    ASTNode childNode = node.getChild();
    while(childNode!=null){
      numChildren++;
      childNode = childNode.getSibling();
    }
    return numChildren;
  }
  
  private void printNodeValue(ASTNode rand){
    String evaluationResult = rand.getVal();
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
