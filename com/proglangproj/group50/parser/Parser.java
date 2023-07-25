package com.proglangproj.group50.parser;

import java.util.Stack;

import com.proglangproj.group50.abstractsyntaxtree.AST;
import com.proglangproj.group50.abstractsyntaxtree.ASTNode;
import com.proglangproj.group50.abstractsyntaxtree.ASTNodeType;
import com.proglangproj.group50.lexicalanalyzer.Scanner;
import com.proglangproj.group50.lexicalanalyzer.Token;

/**
 * Here Recursive descent parser, for RPAL's phrase structure grammar.
 * <p>This class is for all heavy parts of this interpreter
 * <ul>
 * <li>This gets the grammar phases of RPAL as input.
 * <li>This builds the AST using the rules in RPAL grammar.
 * </ul>
 * @author dinidu
 */
public class Parser{
  private Scanner s;
  private Token Cur_Token;
  Stack<ASTNode> Stack;

  public Parser(Scanner s){
    this.s = s;
    Stack = new Stack<ASTNode>();
  }
  
  public AST Build_AST(){ // Building AST
    Start_Parse();
    return new AST(Stack.pop());
  }

  public void Start_Parse(){
    Read_Next();
    Proc_E(); //extra Read_Next in Proc_E()
    if(Cur_Token != null) {
      throw new ParseException("Expected EOF.");
    }
  }

  private void Read_Next(){ // loading and reading the next token
    do{
      Cur_Token = s.readNextToken(); //load the next token
    }while(Is_Cur_Token_Type(Scanner.TOKEN_TYPE_DELETE)); // check current token type be DELETE
    if(null != Cur_Token){
      if(Cur_Token.getTokenType() == Scanner.TOKEN_TYPE_IDENTIFIER){ // If token is an 'Identifier'
        Create_Terminal_ASTNode(ASTNodeType.IDENTIFIER, Cur_Token.getTokenValue());
      }
      else if(Cur_Token.getTokenType() == Scanner.TOKEN_TYPE_STRING){ //If token is a 'String'
        Create_Terminal_ASTNode(ASTNodeType.STRING, Cur_Token.getTokenValue());
      }
      else if(Cur_Token.getTokenType() == Scanner.TOKEN_TYPE_INTEGER){ // If token is an 'Integer'
        Create_Terminal_ASTNode(ASTNodeType.INTEGER, Cur_Token.getTokenValue());
      } 

    }
  }
  
  private boolean isCurrentToken(int type, String val){
    if(Cur_Token == null) {
      return false;
    }
    if(Cur_Token.getTokenType()!=type || !Cur_Token.getTokenValue().equals(val)) {
      return false;
    }
    else {
      return true;
    }
  }
  
  private boolean Is_Cur_Token_Type(int type){
    if(Cur_Token == null) {
      return false;
    }
    if(Cur_Token.getTokenType() == type) {
      return true;
    }
    else {
      return false;
    }
  }
  
  /**
   * Building N-ary AST nodes. <p> For example, think stack is in following points
   * <pre>
   * a <- top element
   * b
   * c
   * d
   * ...
   * </pre>
   * Then, after the call Build_NAry_ASTNode(Z, 3), the stack ;
   * <pre>
   * X <- top element
   * d
   * ...
   * </pre>
   * a, b, and c are children of X, and type Z. Or, in the first-child, next-sibling represent:
   * <pre>
   * X
   * |
   * a -> b -> c
   * </pre>
   * @param type type of node for build
   * @param aryness how many children to next node
   */
  private void Build_NAry_ASTNode(ASTNodeType type, int aryness){
    ASTNode node = new ASTNode();
    node.setType(type);
    while(aryness > 0){
      ASTNode child = Stack.pop();// getting the first element of stack
      if(node.getChild() != null) {
        child.setSibling(node.getChild());
      }
      node.setChild(child); // setting up the child
      node.setSource_Line_Num(child.getSource_Line_Num());
      aryness = aryness -1;
    }
    Stack.push(node);
  }

  private void Create_Terminal_ASTNode(ASTNodeType type, String val){
    ASTNode node = new ASTNode();
    node.setVal(val);
    node.setType(type);
    node.setSource_Line_Num(Cur_Token.getLineNumberOfSourceWhereTokenIs());
    Stack.push(node);
  }
  
  /******************************
   * Expressions
   *******************************/
  
  /**
   * <pre>
   * E-> 'let' D 'in' E => 'let'
   *  -> 'fn' Vb+ '.' E => 'lambda'
   *  -> Ew;
   * </pre>
   */
  private void Proc_E(){
    if(isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "let")){ //E -> 'let' D 'in' E => 'let'
      Read_Next();
      procD();
      if(!isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "in"))
        throw new ParseException("E:  'in' expected");
      Read_Next();
      Proc_E(); //extra readNT in procE()
      Build_NAry_ASTNode(ASTNodeType.LET, 2);
    }
    else if(isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "fn")){ //E -> 'fn' Vb+ '.' E => 'lambda'
      int treesToPop = 0;
      
      Read_Next();
      while(Is_Cur_Token_Type(Scanner.TOKEN_TYPE_IDENTIFIER) || Is_Cur_Token_Type(Scanner.TOKEN_TYPE_L_PAREN)){
        procVB(); //extra readNT in procVB()
        treesToPop++;
      }
      
      if(treesToPop==0)
        throw new ParseException("E: at least one 'Vb' expected");
      
      if(!isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, "."))
        throw new ParseException("E: '.' expected");
      
      Read_Next();
      Proc_E(); //extra readNT in procE()
      
      Build_NAry_ASTNode(ASTNodeType.LAMBDA, treesToPop+1); //+1 for the last E
    }
    else //E -> Ew
      procEW();
  }

  /**
   * <pre>
   * Ew -> T 'where' Dr => 'where'
   *    -> T;
   * </pre>
   */
  private void procEW(){
    procT(); //Ew -> T
    //extra readToken done in procT()
    if(isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "where")){ //Ew -> T 'where' Dr => 'where'
      Read_Next();
      procDR(); //extra readToken() in procDR()
      Build_NAry_ASTNode(ASTNodeType.WHERE, 2);
    }
  }
  
  /******************************
   * Tuple Expressions
   *******************************/
  
  /**
   * <pre>
   * T -> Ta ( ',' Ta )+ => 'tau'
   *   -> Ta;
   * </pre>
   */
  private void procT(){
    procTA(); //T -> Ta
    //extra readToken() in procTA()
    int treesToPop = 0;
    while(isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, ",")){ //T -> Ta (',' Ta )+ => 'tau'
      Read_Next();
      procTA(); //extra readToken() done in procTA()
      treesToPop++;
    }
    if(treesToPop > 0) Build_NAry_ASTNode(ASTNodeType.TAU, treesToPop+1);
  }

  /**
   * <pre>
   * Ta -> Ta 'aug' Tc => 'aug'
   *    -> Tc;
   * </pre>
   */
  private void procTA(){
    procTC(); //Ta -> Tc
    //extra readNT done in procTC()
    while(isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "aug")){ //Ta -> Ta 'aug' Tc => 'aug'
      Read_Next();
      procTC(); //extra readNT done in procTC()
      Build_NAry_ASTNode(ASTNodeType.AUG, 2);
    }
  }

  /**
   * <pre>
   * Tc -> B '->' Tc '|' Tc => '->'
   *    -> B;
   * </pre>
   */
  private void procTC(){
    procB(); //Tc -> B
    //extra readNT in procBT()
    if(isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, "->")){ //Tc -> B '->' Tc '|' Tc => '->'
      Read_Next();
      procTC(); //extra readNT done in procTC
      if(!isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, "|"))
        throw new ParseException("TC: '|' expected");
      Read_Next();
      procTC();  //extra readNT done in procTC
      Build_NAry_ASTNode(ASTNodeType.CONDITIONAL, 3);
    }
  }
  
  /******************************
   * Boolean Expressions
   *******************************/
  
  /**
   * <pre>
   * B -> B 'or' Bt => 'or'
   *   -> Bt;
   * </pre>
   */
  private void procB(){
    procBT(); //B -> Bt
    //extra readNT in procBT()
    while(isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "or")){ //B -> B 'or' Bt => 'or'
      Read_Next();
      procBT();
      Build_NAry_ASTNode(ASTNodeType.OR, 2);
    }
  }
  
  /**
   * <pre>
   * Bt -> Bs '&' Bt => '&'
   *    -> Bs;
   * </pre>
   */
  private void procBT(){
    procBS(); //Bt -> Bs;
    //extra readNT in procBS()
    while(isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, "&")){ //Bt -> Bt '&' Bs => '&'
      Read_Next();
      procBS(); //extra readNT in procBS()
      Build_NAry_ASTNode(ASTNodeType.AND, 2);
    }
  }
  
  /**
   * <pre>
   * Bs -> 'not Bp => 'not'
   *    -> Bp;
   * </pre>
   */
  private void procBS(){
    if(isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "not")){ //Bs -> 'not' Bp => 'not'
      Read_Next();
      procBP(); //extra readNT in procBP()
      Build_NAry_ASTNode(ASTNodeType.NOT, 1);
    }
    else
      procBP(); //Bs -> Bp
      //extra readNT in procBP()
  }
  
  /**
   * <pre>
   * Bp -> A ('gr' | '>' ) A => 'gr'
   *    -> A ('ge' | '>=' ) A => 'ge'
   *    -> A ('ls' | '<' ) A => 'ge'
   *    -> A ('le' | '<=' ) A => 'ge'
   *    -> A 'eq' A => 'eq'
   *    -> A 'ne' A => 'ne'
   *    -> A;
   * </pre>
   */
  private void procBP(){
    procA(); //Bp -> A
    if(isCurrentToken(Scanner.TOKEN_TYPE_RESERVED,"gr")||isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR,">")){ //Bp -> A('gr' | '>' ) A => 'gr'
      Read_Next();
      procA(); //extra readNT in procA()
      Build_NAry_ASTNode(ASTNodeType.GR, 2);
    }
    else if(isCurrentToken(Scanner.TOKEN_TYPE_RESERVED,"ge")||isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR,">=")){ //Bp -> A ('ge' | '>=') A => 'ge'
      Read_Next();
      procA(); //extra readNT in procA()
      Build_NAry_ASTNode(ASTNodeType.GE, 2);
    }
    else if(isCurrentToken(Scanner.TOKEN_TYPE_RESERVED,"ls")||isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR,"<")){ //Bp -> A ('ls' | '<' ) A => 'ls'
      Read_Next();
      procA(); //extra readNT in procA()
      Build_NAry_ASTNode(ASTNodeType.LS, 2);
    }
    else if(isCurrentToken(Scanner.TOKEN_TYPE_RESERVED,"le")||isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR,"<=")){ //Bp -> A ('le' | '<=') A => 'le'
      Read_Next();
      procA(); //extra readNT in procA()
      Build_NAry_ASTNode(ASTNodeType.LE, 2);
    }
    else if(isCurrentToken(Scanner.TOKEN_TYPE_RESERVED,"eq")){ //Bp -> A 'eq' A => 'eq'
      Read_Next();
      procA(); //extra readNT in procA()
      Build_NAry_ASTNode(ASTNodeType.EQ, 2);
    }
    else if(isCurrentToken(Scanner.TOKEN_TYPE_RESERVED,"ne")){ //Bp -> A 'ne' A => 'ne'
      Read_Next();
      procA(); //extra readNT in procA()
      Build_NAry_ASTNode(ASTNodeType.NE, 2);
    }
  }
  
  
  /******************************
   * Arithmetic Expressions
   *******************************/
  
  /**
   * <pre>
   * A -> A '+' At => '+'
   *   -> A '-' At => '-'
   *   ->   '+' At
   *   ->   '-' At => 'neg'
   *   -> At;
   * </pre>
   */
  private void procA(){
    if(isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, "+")){ //A -> '+' At
      Read_Next();
      Proc_AT(); //extra readNT in procAT()
    }
    else if(isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, "-")){ //A -> '-' At => 'neg'
      Read_Next();
      Proc_AT(); //extra readNT in procAT()
      Build_NAry_ASTNode(ASTNodeType.NEG, 1);
    }
    else
      Proc_AT(); //extra readNT in procAT()
    
    boolean plus = true;
    while(isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, "+")||isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, "-")){
      if(Cur_Token.getTokenValue().equals("+"))
        plus = true;
      else if(Cur_Token.getTokenValue().equals("-"))
        plus = false;
      Read_Next();
      Proc_AT(); //extra readNT in procAT()
      if(plus) //A -> A '+' At => '+'
        Build_NAry_ASTNode(ASTNodeType.PLUS, 2);
      else //A -> A '-' At => '-'
        Build_NAry_ASTNode(ASTNodeType.MINUS, 2);
    }
  }
  
  /**
   * <pre>
   * At -> At '*' Af => '*'
   *    -> At '/' Af => '/'
   *    -> Af;
   * </pre>
   */
  private void Proc_AT(){
    procAF(); //At -> Af;
    //extra readNT in procAF()
    boolean mult = true;
    while(isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, "*")||isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, "/")){
      if(Cur_Token.getTokenValue().equals("*"))
        mult = true;
      else if(Cur_Token.getTokenValue().equals("/"))
        mult = false;
      Read_Next();
      procAF(); //extra readNT in procAF()
      if(mult) //At -> At '*' Af => '*'
        Build_NAry_ASTNode(ASTNodeType.MULT, 2);
      else //At -> At '/' Af => '/'
        Build_NAry_ASTNode(ASTNodeType.DIV, 2);
    }
  }
  
  /**
   * <pre>
   * Af -> Ap '**' Af => '**'
   *    -> Ap;
   * </pre>
   */
  private void procAF(){
    procAP(); // Af -> Ap;
    //extra readNT in procAP()
    if(isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, "**")){ //Af -> Ap '**' Af => '**'
      Read_Next();
      procAF();
      Build_NAry_ASTNode(ASTNodeType.EXP, 2);
    }
  }
  
  
  /**
   * <pre>
   * Ap -> Ap '@' '&lt;IDENTIFIER&gt;' R => '@'
   *    -> R; 
   * </pre>
   */
  private void procAP(){
    procR(); //Ap -> R;
    //extra readNT in procR()
    while(isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, "@")){ //Ap -> Ap '@' '<IDENTIFIER>' R => '@'
      Read_Next();
      if(!Is_Cur_Token_Type(Scanner.TOKEN_TYPE_IDENTIFIER))
        throw new ParseException("AP: expected Identifier");
      Read_Next();
      procR(); //extra readNT in procR()
      Build_NAry_ASTNode(ASTNodeType.AT, 3);
    }
  }
  
  /******************************
   * Rators and Rands
   *******************************/
  
  /**
   * <pre>
   * R -> R Rn => 'gamma'
   *   -> Rn;
   * </pre>
   */
  private void procR(){
    procRN(); //R -> Rn; NO extra readNT in procRN(). See while loop below for reason.
    Read_Next();
    while(Is_Cur_Token_Type(Scanner.TOKEN_TYPE_INTEGER)||
        Is_Cur_Token_Type(Scanner.TOKEN_TYPE_STRING)||
        Is_Cur_Token_Type(Scanner.TOKEN_TYPE_IDENTIFIER)||
        isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "true")||
        isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "false")||
        isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "nil")||
        isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "dummy")||
        Is_Cur_Token_Type(Scanner.TOKEN_TYPE_L_PAREN)){ //R -> R Rn => 'gamma'
      procRN(); //NO extra readNT in procRN(). This is important because if we do an extra readNT in procRN and currentToken happens to
                //be an INTEGER, IDENTIFIER, or STRING, it will get pushed on the stack. Then, the GAMMA node that we build will have the
                //wrong kids. There are workarounds, e.g. keeping the extra readNT in procRN() and checking here if the last token read
                //(which was read in procRN()) is an INTEGER, IDENTIFIER, or STRING and, if so, to pop it, call buildNAryASTNode, and then
                //push it again. I chose this option because it seems cleaner.
      Build_NAry_ASTNode(ASTNodeType.GAMMA, 2);
      Read_Next();
    }
  }

  /**
   * NOTE: NO extra readNT in procRN. See comments in {@link #procR()} for explanation.
   * <pre>
   * Rn -> '&lt;IDENTIFIER&gt;'
   *    -> '&lt;INTEGER&gt;'
   *    -> '&lt;STRING&gt;'
   *    -> 'true' => 'true'
   *    -> 'false' => 'false'
   *    -> 'nil' => 'nil'
   *    -> '(' E ')'
   *    -> 'dummy' => 'dummy'
   * </pre>
   */
  private void procRN(){
    if(Is_Cur_Token_Type(Scanner.TOKEN_TYPE_IDENTIFIER)|| //R -> '<IDENTIFIER>'
       Is_Cur_Token_Type(Scanner.TOKEN_TYPE_INTEGER)|| //R -> '<INTEGER>'
       Is_Cur_Token_Type(Scanner.TOKEN_TYPE_STRING)){ //R-> '<STRING>'
    }
    else if(isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "true")){ //R -> 'true' => 'true'
      Create_Terminal_ASTNode(ASTNodeType.TRUE, "true");
    }
    else if(isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "false")){ //R -> 'false' => 'false'
      Create_Terminal_ASTNode(ASTNodeType.FALSE, "false");
    } 
    else if(isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "nil")){ //R -> 'nil' => 'nil'
      Create_Terminal_ASTNode(ASTNodeType.NIL, "nil");
    }
    else if(Is_Cur_Token_Type(Scanner.TOKEN_TYPE_L_PAREN)){
      Read_Next();
      Proc_E(); //extra readNT in procE()
      if(!Is_Cur_Token_Type(Scanner.TOKEN_TYPE_R_PAREN))
        throw new ParseException("RN: ')' expected");
    }
    else if(isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "dummy")){ //R -> 'dummy' => 'dummy'
      Create_Terminal_ASTNode(ASTNodeType.DUMMY, "dummy");
    }
  }

  /******************************
   * Definitions
   *******************************/
  
  /**
   * <pre>
   * D -> Da 'within' D => 'within'
   *   -> Da;
   * </pre>
   */
  private void procD(){
    procDA(); //D -> Da
    //extra readToken() in procDA()
    if(isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "within")){ //D -> Da 'within' D => 'within'
      Read_Next();
      procD();
      Build_NAry_ASTNode(ASTNodeType.WITHIN, 2);
    }
  }
  
  /**
   * <pre>
   * Da -> Dr ('and' Dr)+ => 'and'
   *    -> Dr;
   * </pre>
   */
  private void procDA(){
    procDR(); //Da -> Dr
    //extra readToken() in procDR()
    int treesToPop = 0;
    while(isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "and")){ //Da -> Dr ( 'and' Dr )+ => 'and'
      Read_Next();
      procDR(); //extra readToken() in procDR()
      treesToPop++;
    }
    if(treesToPop > 0) Build_NAry_ASTNode(ASTNodeType.SIMULTDEF, treesToPop+1);
  }
  
  /**
   * Dr -> 'rec' Db => 'rec'
   *    -> Db;
   */
  private void procDR(){
    if(isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "rec")){ //Dr -> 'rec' Db => 'rec'
      Read_Next();
      procDB(); //extra readToken() in procDB()
      Build_NAry_ASTNode(ASTNodeType.REC, 1);
    }
    else{ //Dr -> Db
      procDB(); //extra readToken() in procDB()
    }
  }
  
  /**
   * <pre>
   * Db -> Vl '=' E => '='
   *    -> '&lt;IDENTIFIER&gt;' Vb+ '=' E => 'fcn_form'
   *    -> '(' D ')';
   * </pre>
   */
  private void procDB(){
    if(Is_Cur_Token_Type(Scanner.TOKEN_TYPE_L_PAREN)){ //Db -> '(' D ')'
      procD();
      Read_Next();
      if(!Is_Cur_Token_Type(Scanner.TOKEN_TYPE_R_PAREN))
        throw new ParseException("DB: ')' expected");
      Read_Next();
    }
    else if(Is_Cur_Token_Type(Scanner.TOKEN_TYPE_IDENTIFIER)){
      Read_Next();
      if(isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, ",")){ //Db -> Vl '=' E => '='
        Read_Next();
        procVL(); //extra readNT in procVB()
        //VL makes its COMMA nodes for all the tokens EXCEPT the ones
        //we just read above (i.e., the first identifier and the comma after it)
        //Hence, we must pop the top of the tree VL just made and put it under a
        //comma node with the identifier it missed.
        if(!isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, "="))
          throw new ParseException("DB: = expected.");
        Build_NAry_ASTNode(ASTNodeType.COMMA, 2);
        Read_Next();
        Proc_E(); //extra readNT in procE()
        Build_NAry_ASTNode(ASTNodeType.EQUAL, 2);
      }
      else{ //Db -> '<IDENTIFIER>' Vb+ '=' E => 'fcn_form'
        if(isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, "=")){ //Db -> Vl '=' E => '='; if Vl had only one IDENTIFIER (no commas)
          Read_Next();
          Proc_E(); //extra readNT in procE()
          Build_NAry_ASTNode(ASTNodeType.EQUAL, 2);
        }
        else{ //Db -> '<IDENTIFIER>' Vb+ '=' E => 'fcn_form'
          int treesToPop = 0;

          while(Is_Cur_Token_Type(Scanner.TOKEN_TYPE_IDENTIFIER) || Is_Cur_Token_Type(Scanner.TOKEN_TYPE_L_PAREN)){
            procVB(); //extra readNT in procVB()
            treesToPop++;
          }

          if(treesToPop==0)
            throw new ParseException("E: at least one 'Vb' expected");

          if(!isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, "="))
            throw new ParseException("DB: = expected.");

          Read_Next();
          Proc_E(); //extra readNT in procE()

          Build_NAry_ASTNode(ASTNodeType.FCNFORM, treesToPop+2); //+1 for the last E and +1 for the first identifier
        }
      }
    }
  }
  
  /******************************
   * Variables
   *******************************/
  
  /**
   * <pre>
   * Vb -> '&lt;IDENTIFIER&gt;'
   *    -> '(' Vl ')'
   *    -> '(' ')' => '()'
   * </pre>
   */
  private void procVB(){
    if(Is_Cur_Token_Type(Scanner.TOKEN_TYPE_IDENTIFIER)){ //Vb -> '<IDENTIFIER>'
      Read_Next();
    }
    else if(Is_Cur_Token_Type(Scanner.TOKEN_TYPE_L_PAREN)){
      Read_Next();
      if(Is_Cur_Token_Type(Scanner.TOKEN_TYPE_R_PAREN)){ //Vb -> '(' ')' => '()'
        Create_Terminal_ASTNode(ASTNodeType.PAREN, "");
        Read_Next();
      }
      else{ //Vb -> '(' Vl ')'
        procVL(); //extra readNT in procVB()
        if(!Is_Cur_Token_Type(Scanner.TOKEN_TYPE_R_PAREN))
          throw new ParseException("VB: ')' expected");
        Read_Next();
      }
    }
  }

  /**
   * <pre>
   * Vl -> '&lt;IDENTIFIER&gt;' list ',' => ','?;
   * </pre>
   */
  private void procVL(){
    if(!Is_Cur_Token_Type(Scanner.TOKEN_TYPE_IDENTIFIER))
      throw new ParseException("VL: Identifier expected");
    else{
      Read_Next();
      int treesToPop = 0;
      while(isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, ",")){ //Vl -> '<IDENTIFIER>' list ',' => ','?;
        Read_Next();
        if(!Is_Cur_Token_Type(Scanner.TOKEN_TYPE_IDENTIFIER))
          throw new ParseException("VL: Identifier expected");
        Read_Next();
        treesToPop++;
      }
      if(treesToPop > 0) Build_NAry_ASTNode(ASTNodeType.COMMA, treesToPop+1); //+1 for the first identifier
    }
  }

}

