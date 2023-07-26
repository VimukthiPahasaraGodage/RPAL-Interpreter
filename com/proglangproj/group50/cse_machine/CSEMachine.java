package com.proglangproj.group50.cse_machine;

import java.util.Stack;
import com.proglangproj.group50.abstractsyntaxtree.AST;
import com.proglangproj.group50.abstractsyntaxtree.ASTNode;
import com.proglangproj.group50.abstractsyntaxtree.ASTNodeType;

public class CSEMachine{

  public String evaluation_result;

  private Stack<ASTNode> value_stack;
  private Delta root_delta;

  public CSEMachine(AST ast){
    if(!ast.Is_Standardized())
      throw new RuntimeException("AST has NOT been standardized!");
    root_delta = ast.Create_Deltas();
    root_delta.setLinked_Environment(new Environment());
    value_stack = new Stack<ASTNode>();
  }

  public void evaluate_Program(){
    process_ControlStack(root_delta, root_delta.getLinked_Environment());
  }

  private void process_ControlStack(Delta currentDelta, Environment currentEnv){
    Stack<ASTNode> controlStack = new Stack<ASTNode>();
    controlStack.addAll(currentDelta.getBody());
    
    while(!controlStack.isEmpty())
      process_CurrentNode(currentDelta, currentEnv, controlStack);
  }

  private void process_CurrentNode(Delta currentDelta, Environment currentEnv, Stack<ASTNode> currentControlStack){
    ASTNode node = currentControlStack.pop();
    if(apply_BinaryOperation(node))
      return;
    else if(applyUnaryOperation(node))
      return;
    else{
      switch(node.getType()){
        case IDENTIFIER:
          handle_Identifiers(node, currentEnv);
          break;
        case NIL:
        case TAU:
          createTuple(node);
          break;
        case BETA:
          handle_Beta((Beta)node, currentControlStack);
          break;
        case GAMMA:
          applyGamma(currentDelta, node, currentEnv, currentControlStack);
          break;
        case DELTA:
          ((Delta)node).setLinked_Environment(currentEnv); //RULE 2
          value_stack.push(node);
          break;
        default:
          value_stack.push(node);
          break;
      }
    }
  }

  // RULE 6
  private boolean apply_BinaryOperation(ASTNode rator){
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
        binary_ArithmeticOp(rator.getType());
        return true;
      case EQ:
      case NE:
        binary_LogicalEqNeOp(rator.getType());
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

  private void binary_ArithmeticOp(ASTNodeType type){
    ASTNode rand_a = value_stack.pop();
    ASTNode rand_b = value_stack.pop();
    if(rand_a.getType()!=ASTNodeType.INTEGER || rand_b.getType()!=ASTNodeType.INTEGER)
      EvaluationError.print_error(rand_a.getSource_Line_Num(), "Expected two integers; was given \""+rand_a.getVal()+"\", \""+rand_b.getVal()+"\"");

    ASTNode result = new ASTNode();
    result.setType(ASTNodeType.INTEGER);

    switch(type){
      case PLUS:
        result.setVal(Integer.toString(Integer.parseInt(rand_a.getVal())+Integer.parseInt(rand_b.getVal())));
        break;
      case MINUS:
        result.setVal(Integer.toString(Integer.parseInt(rand_a.getVal())-Integer.parseInt(rand_b.getVal())));
        break;
      case MULT:
        result.setVal(Integer.toString(Integer.parseInt(rand_a.getVal())*Integer.parseInt(rand_b.getVal())));
        break;
      case DIV:
        result.setVal(Integer.toString(Integer.parseInt(rand_a.getVal())/Integer.parseInt(rand_b.getVal())));
        break;
      case EXP:
        result.setVal(Integer.toString((int)Math.pow(Integer.parseInt(rand_a.getVal()), Integer.parseInt(rand_b.getVal()))));
        break;
      case LS:
        if(Integer.parseInt(rand_a.getVal())<Integer.parseInt(rand_b.getVal()))
          pushTrueNode();
        else
          pushFalseNode();
        return;
      case LE:
        if(Integer.parseInt(rand_a.getVal())<=Integer.parseInt(rand_b.getVal()))
          pushTrueNode();
        else
          pushFalseNode();
        return;
      case GR:
        if(Integer.parseInt(rand_a.getVal())>Integer.parseInt(rand_b.getVal()))
          pushTrueNode();
        else
          pushFalseNode();
        return;
      case GE:
        if(Integer.parseInt(rand_a.getVal())>=Integer.parseInt(rand_b.getVal()))
          pushTrueNode();
        else
          pushFalseNode();
        return;
      default:
        break;
    }
    value_stack.push(result);
  }

  private void binary_LogicalEqNeOp(ASTNodeType type){
    ASTNode rand_a = value_stack.pop();
    ASTNode rand_b = value_stack.pop();

    if(rand_a.getType()==ASTNodeType.TRUE || rand_a.getType()==ASTNodeType.FALSE){
      if(rand_b.getType()!=ASTNodeType.TRUE && rand_b.getType()!=ASTNodeType.FALSE)
        EvaluationError.print_error(rand_a.getSource_Line_Num(), "Can't compare dissimilar types; was given \""+rand_a.getVal()+"\", \""+rand_b.getVal()+"\"");
      compare_TruthValues(rand_a, rand_b, type);
      return;
    }

    if(rand_a.getType()!=rand_b.getType())
      EvaluationError.print_error(rand_a.getSource_Line_Num(), "Can't compare dissimilar types; was given \""+rand_a.getVal()+"\", \""+rand_b.getVal()+"\"");

    if(rand_a.getType()==ASTNodeType.STRING)
      compare_Strings(rand_a, rand_b, type);
    else if(rand_a.getType()==ASTNodeType.INTEGER)
      compare_Integers(rand_a, rand_b, type);
    else
      EvaluationError.print_error(rand_a.getSource_Line_Num(), "Don't know how to " + type + " \""+rand_a.getVal()+"\", \""+rand_b.getVal()+"\"");

  }

  private void compare_TruthValues(ASTNode rand_a, ASTNode rand_b, ASTNodeType type){
    if(rand_a.getType()==rand_b.getType())
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

  private void compare_Strings(ASTNode rand_a, ASTNode rand_b, ASTNodeType type){
    if(rand_a.getVal().equals(rand_b.getVal()))
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

  private void compare_Integers(ASTNode rand_a, ASTNode rand_b, ASTNodeType type){
    if(Integer.parseInt(rand_a.getVal())==Integer.parseInt(rand_b.getVal()))
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
    ASTNode rand_a = value_stack.pop();
    ASTNode rand_b = value_stack.pop();

    if((rand_a.getType()==ASTNodeType.TRUE || rand_a.getType()==ASTNodeType.FALSE) &&
        (rand_b.getType()==ASTNodeType.TRUE || rand_b.getType()==ASTNodeType.FALSE)){
      orAndTruthValues(rand_a, rand_b, type);
      return;
    }

    EvaluationError.print_error(rand_a.getSource_Line_Num(), "Don't know how to " + type + " \""+rand_a.getVal()+"\", \""+rand_b.getVal()+"\"");
  }

  private void orAndTruthValues(ASTNode rand_a, ASTNode rand_b, ASTNodeType type){
    if(type==ASTNodeType.OR){
      if(rand_a.getType()==ASTNodeType.TRUE || rand_b.getType()==ASTNodeType.TRUE)
        pushTrueNode();
      else
        pushFalseNode();
    }
    else{
      if(rand_a.getType()==ASTNodeType.TRUE && rand_b.getType()==ASTNodeType.TRUE)
        pushTrueNode();
      else
        pushFalseNode();
    }
  }

  private void augTuples(){
    ASTNode rand_a = value_stack.pop();
    ASTNode rand_b = value_stack.pop();

    if(rand_a.getType()!=ASTNodeType.TUPLE)
      EvaluationError.print_error(rand_a.getSource_Line_Num(), "Cannot augment a non-tuple \""+rand_a.getVal()+"\"");

    ASTNode childNode = rand_a.getChild();
    if(childNode==null)
      rand_a.setChild(rand_b);
    else{
      while(childNode.getSibling()!=null)
        childNode = childNode.getSibling();
      childNode.setSibling(rand_b);
    }
    rand_b.setSibling(null);

    value_stack.push(rand_a);
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
    ASTNode rand = value_stack.pop();
    if(rand.getType()!=ASTNodeType.TRUE && rand.getType()!=ASTNodeType.FALSE)
      EvaluationError.print_error(rand.getSource_Line_Num(), "Expecting a truthvalue; was given \""+rand.getVal()+"\"");

    if(rand.getType()==ASTNodeType.TRUE)
      pushFalseNode();
    else
      pushTrueNode();
  }

  private void neg(){
    ASTNode rand = value_stack.pop();
    if(rand.getType()!=ASTNodeType.INTEGER)
      EvaluationError.print_error(rand.getSource_Line_Num(), "Expecting a truthvalue; was given \""+rand.getVal()+"\"");

    ASTNode result = new ASTNode();
    result.setType(ASTNodeType.INTEGER);
    result.setVal(Integer.toString(-1*Integer.parseInt(rand.getVal())));
    value_stack.push(result);
  }

  //RULE 3
  private void applyGamma(Delta currentDelta, ASTNode node, Environment currentEnv, Stack<ASTNode> currentControlStack){
    ASTNode rator_a = value_stack.pop();
    ASTNode rand_a = value_stack.pop();

    if(rator_a.getType()==ASTNodeType.DELTA){
      Delta nextDelta = (Delta) rator_a;
      
      //Delta has a link to the environment in effect when it is pushed on to the value stack (search
      //for 'RULE 2' in this file to see where it's done)
      //We construct a new environment here that will contain all the bindings (single or multiple)
      //required by this Delta. This new environment will link back to the environment carried by the Delta.
      Environment newEnv = new Environment();
      newEnv.setParent_Environment(nextDelta.getLinked_Environment());
      
      //RULE 4
      if(nextDelta.getBound_var_list().size()==1){
        newEnv.add_Map(nextDelta.getBound_var_list().get(0), rand_a);
      }
      //RULE 11
      else{
        if(rand_a.getType()!=ASTNodeType.TUPLE)
          EvaluationError.print_error(rand_a.getSource_Line_Num(), "Expected a tuple; was given \""+rand_a.getVal()+"\"");
        
        for(int i = 0; i < nextDelta.getBound_var_list().size(); i++){
          newEnv.add_Map(nextDelta.getBound_var_list().get(i), get_NthTuple_Child((Tuple)rand_a, i+1)); //+ 1 coz tuple indexing starts at 1
        }
      }
      
      process_ControlStack(nextDelta, newEnv);
      return;
    }
    else if(rator_a.getType()==ASTNodeType.YSTAR){
      //RULE 12
      if(rand_a.getType()!=ASTNodeType.DELTA)
        EvaluationError.print_error(rand_a.getSource_Line_Num(), "Expected a Delta; was given \""+rand_a.getVal()+"\"");
      
      Eta etaNode = new Eta();
      etaNode.setDelta((Delta)rand_a);
      value_stack.push(etaNode);
      return;
    }
    else if(rator_a.getType()==ASTNodeType.ETA){
      //RULE 13
      //push back the rand, the eta and then the delta it contains
      value_stack.push(rand_a);
      value_stack.push(rator_a);
      value_stack.push(((Eta)rator_a).getDelta());
      //push back two gammas (one for the eta and one for the delta)
      currentControlStack.push(node);
      currentControlStack.push(node);
      return;
    }
    else if(rator_a.getType()==ASTNodeType.TUPLE){
      tuple_Selection((Tuple)rator_a, rand_a);
      return;
    }
    else if(evaluate_ReservedIdentifiers(rator_a, rand_a, currentControlStack))
      return;
    else
      EvaluationError.print_error(rator_a.getSource_Line_Num(), "Don't know how to evaluate \""+rator_a.getVal()+"\"");
  }

  private boolean evaluate_ReservedIdentifiers(ASTNode rator, ASTNode rand, Stack<ASTNode> currentControlStack){
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
    value_stack.push(trueNode);
  }
  
  private void pushFalseNode(){
    ASTNode falseNode = new ASTNode();
    falseNode.setType(ASTNodeType.FALSE);
    falseNode.setVal("false");
    value_stack.push(falseNode);
  }

  private void pushDummyNode(){
    ASTNode falseNode = new ASTNode();
    falseNode.setType(ASTNodeType.DUMMY);
    value_stack.push(falseNode);
  }

  private void stem(ASTNode rand){
    if(rand.getType()!=ASTNodeType.STRING)
      EvaluationError.print_error(rand.getSource_Line_Num(), "Expected a string; was given \""+rand.getVal()+"\"");
    
    if(rand.getVal().isEmpty())
      rand.setVal("");
    else
      rand.setVal(rand.getVal().substring(0,1));
    
    value_stack.push(rand);
  }

  private void stern(ASTNode rand){
    if(rand.getType()!=ASTNodeType.STRING)
      EvaluationError.print_error(rand.getSource_Line_Num(), "Expected a string; was given \""+rand.getVal()+"\"");
    
    if(rand.getVal().isEmpty() || rand.getVal().length()==1)
      rand.setVal("");
    else
      rand.setVal(rand.getVal().substring(1));
    
    value_stack.push(rand);
  }

  private void conc(ASTNode rand_a, Stack<ASTNode> currentControlStack){
    currentControlStack.pop();
    ASTNode rand_b = value_stack.pop();
    if(rand_a.getType()!=ASTNodeType.STRING || rand_b.getType()!=ASTNodeType.STRING)
      EvaluationError.print_error(rand_a.getSource_Line_Num(), "Expected two strings; was given \""+rand_a.getVal()+"\", \""+rand_b.getVal()+"\"");

    ASTNode result = new ASTNode();
    result.setType(ASTNodeType.STRING);
    result.setVal(rand_a.getVal()+rand_b.getVal());
    
    value_stack.push(result);
  }

  private void itos(ASTNode rand){
    if(rand.getType()!=ASTNodeType.INTEGER)
      EvaluationError.print_error(rand.getSource_Line_Num(), "Expected an integer; was given \""+rand.getVal()+"\"");
    
    rand.setType(ASTNodeType.STRING); //all values are stored internally as strings, so nothing else to do
    value_stack.push(rand);
  }

  private void order(ASTNode rand){
    if(rand.getType()!=ASTNodeType.TUPLE)
      EvaluationError.print_error(rand.getSource_Line_Num(), "Expected a tuple; was given \""+rand.getVal()+"\"");

    ASTNode result = new ASTNode();
    result.setType(ASTNodeType.INTEGER);
    result.setVal(Integer.toString(get_NumChildren(rand)));
    
    value_stack.push(result);
  }

  private void isNullTuple(ASTNode rand){
    if(rand.getType()!=ASTNodeType.TUPLE)
      EvaluationError.print_error(rand.getSource_Line_Num(), "Expected a tuple; was given \""+rand.getVal()+"\"");

    if(get_NumChildren(rand)==0)
      pushTrueNode();
    else
      pushFalseNode();
  }

  // RULE 10
  private void tuple_Selection(Tuple rator, ASTNode rand){
    if(rand.getType()!=ASTNodeType.INTEGER)
      EvaluationError.print_error(rand.getSource_Line_Num(), "Non-integer tuple selection with \""+rand.getVal()+"\"");

    ASTNode result = get_NthTuple_Child(rator, Integer.parseInt(rand.getVal()));
    if(result==null)
      EvaluationError.print_error(rand.getSource_Line_Num(), "Tuple selection index "+rand.getVal()+" out of bounds");

    value_stack.push(result);
  }

  private ASTNode get_NthTuple_Child(Tuple tupleNode, int n){
    ASTNode childNode = tupleNode.getChild();
    for(int i=1;i<n;++i){ //tuple selection index starts at 1
      if(childNode==null)
        break;
      childNode = childNode.getSibling();
    }
    return childNode;
  }

  private void handle_Identifiers(ASTNode node, Environment currentEnv){
    if(currentEnv.lookup_parent(node.getVal())!=null) // RULE 1
      value_stack.push(currentEnv.lookup_parent(node.getVal()));
    else if(isReservedIdentifier(node.getVal()))
      value_stack.push(node);
    else
      EvaluationError.print_error(node.getSource_Line_Num(), "Undeclared identifier \""+node.getVal()+"\"");
  }

  //RULE 9
  private void createTuple(ASTNode node){
    int numofChildren = get_NumChildren(node);
    Tuple tupleNode = new Tuple();
    if(numofChildren==0){
      value_stack.push(tupleNode);
      return;
    }

    ASTNode childNode = null, tempNode = null;
    for(int i=0;i<numofChildren;++i){
      if(childNode==null)
        childNode = value_stack.pop();
      else if(tempNode==null){
        tempNode = value_stack.pop();
        childNode.setSibling(tempNode);
      }
      else{
        tempNode.setSibling(value_stack.pop());
        tempNode = tempNode.getSibling();
      }
    }
    tempNode.setSibling(null);
    tupleNode.setChild(childNode);
    value_stack.push(tupleNode);
  }

  // RULE 8
  private void handle_Beta(Beta node, Stack<ASTNode> currentControlStack){
    ASTNode conditionResultNode = value_stack.pop();

    if(conditionResultNode.getType()!=ASTNodeType.TRUE && conditionResultNode.getType()!=ASTNodeType.FALSE)
      EvaluationError.print_error(conditionResultNode.getSource_Line_Num(), "Expecting a truthvalue; found \""+conditionResultNode.getVal()+"\"");

    if(conditionResultNode.getType()==ASTNodeType.TRUE)
      currentControlStack.addAll(node.getThenNode());
    else
      currentControlStack.addAll(node.getElseNode());
  }

  private int get_NumChildren(ASTNode node){
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
    this.evaluation_result = evaluationResult;
  }

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
      case "conc":
      case "Stern":
      case "Stem":
      case "Null":
      case "Print":
      case "print":
      case "neg":
        return true;
    }
    return false;
  }

}