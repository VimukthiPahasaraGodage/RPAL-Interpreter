package com.proglangproj.group50.parser;

import com.proglangproj.group50.abstractsyntaxtree.AbstractSyntaxTree;
import com.proglangproj.group50.abstractsyntaxtree.AbstractSyntaxTreeNode;
import com.proglangproj.group50.abstractsyntaxtree.AbstractSyntaxTreeNodeType;
import com.proglangproj.group50.lexicalanalyzer.Scanner;
import com.proglangproj.group50.lexicalanalyzer.Token;

import java.util.Stack;


 // RPAL's grammar phases are used as input for this.
 // this applies to AbstractSyntaxTree using RPAL grammar rules
public class Parser {
    private final Scanner scannerOfLexicalAnalyzer;
    Stack<AbstractSyntaxTreeNode> Stack;
    private Token Cur_Token;

    public Parser(Scanner scanner) {
        this.scannerOfLexicalAnalyzer = scanner;
        Stack = new Stack<AbstractSyntaxTreeNode>();
    }

    public AbstractSyntaxTree Build_AST() { //This is to build AbstractSyntaxTree
        Start_Parse();
        return new AbstractSyntaxTree(Stack.pop());
    }

    public void Start_Parse() {
        Read_Next();
        ProcessNonTerminal_E(); //This is to extra Read_Next in Proc_E()
        if (Cur_Token != null) {
            throw new RuntimeException("Expected EOF.");
        }
    }

    private void Read_Next() { // This is to load and reading the next token
        do {
            Cur_Token = scannerOfLexicalAnalyzer.readNextToken(); //This is to load the next token
        } while (IsCurrentTokenType(Scanner.TOKEN_TYPE_DELETE)); // This is to check current token type be DELETE
        if (null != Cur_Token) {
            if (Cur_Token.getTokenType() == Scanner.TOKEN_TYPE_IDENTIFIER) {
                Create_Terminal_ASTNode(AbstractSyntaxTreeNodeType.IDENTIFIER, Cur_Token.getTokenValue());
            } else if (Cur_Token.getTokenType() == Scanner.TOKEN_TYPE_STRING) {
                Create_Terminal_ASTNode(AbstractSyntaxTreeNodeType.STRING, Cur_Token.getTokenValue());
            } else if (Cur_Token.getTokenType() == Scanner.TOKEN_TYPE_INTEGER) {
                Create_Terminal_ASTNode(AbstractSyntaxTreeNodeType.INTEGER, Cur_Token.getTokenValue());
            }

        }
    }

    private boolean isCurrentToken(int type, String val) {
        if (Cur_Token == null) {
            return false;
        }
        return Cur_Token.getTokenType() == type && Cur_Token.getTokenValue().equals(val);
    }

    private boolean IsCurrentTokenType(int type) {
        if (Cur_Token == null) {
            return false;
        }
        return Cur_Token.getTokenType() == type;
    }

    private void Build_NAry_ASTNode(AbstractSyntaxTreeNodeType type, int aryness) {
        AbstractSyntaxTreeNode node = new AbstractSyntaxTreeNode();
        node.setTypeOfASTNode(type);
        while (aryness > 0) {
            AbstractSyntaxTreeNode child = Stack.pop();// getting the first element of stack
            if (node.getChildOfASTNode() != null) {
                child.setSiblingOfASTNode(node.getChildOfASTNode());
            }
            node.setChildOfASTNode(child); // setting up the child
            node.setLineNumberOfSourceFile(child.getLineNumberOfSourceFile());
            aryness = aryness - 1;
        }
        Stack.push(node);
    }

    private void Create_Terminal_ASTNode(AbstractSyntaxTreeNodeType type, String val) {
        AbstractSyntaxTreeNode node = new AbstractSyntaxTreeNode();
        node.setValueOfASTNode(val);
        node.setTypeOfASTNode(type);
        node.setLineNumberOfSourceFile(Cur_Token.getLineNumberOfSourceWhereTokenIs());
        Stack.push(node);
    }


     // Expressions

     // <pre>
     // E-> 'let' D 'in' E => 'let'
     //  -> 'fn' Vb+ '.' E => 'lambda'
     //  -> Ew;
     // </pre>

    private void ProcessNonTerminal_E() {
        if (isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "let")) { //E -> 'let' D 'in' E => 'let'
            Read_Next();
            processNonTerminal_D();
            if (!isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "in"))
                throw new RuntimeException("E:  'in' expected");
            Read_Next();
            ProcessNonTerminal_E(); //extra readNT in procE()
            Build_NAry_ASTNode(AbstractSyntaxTreeNodeType.LET, 2);
        } else if (isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "fn")) { //E -> 'fn' Vb+ '.' E => 'lambda'
            int treesToPop = 0;

            Read_Next();
            while (IsCurrentTokenType(Scanner.TOKEN_TYPE_IDENTIFIER) || IsCurrentTokenType(Scanner.TOKEN_TYPE_L_PAREN)) {
                processNonTerminal_Vb(); //extra readNT in procVB()
                treesToPop++;
            }

            if (treesToPop == 0)
                throw new RuntimeException("E: at least one 'Vb' expected");

            if (!isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, "."))
                throw new RuntimeException("E: '.' expected");

            Read_Next();
            ProcessNonTerminal_E(); //this is to extra readNT in procE()

            Build_NAry_ASTNode(AbstractSyntaxTreeNodeType.LAMBDA, treesToPop + 1); //+1 for the last E
        } else //E -> Ew
            processNonTerminal_Ew();
    }

     // <pre>
     // Ew -> T 'where' Dr => 'where'
     //    -> T;
     // </pre>
    private void processNonTerminal_Ew() {
        processNonTerminal_T(); //Ew -> T
        //this is to extra readToken done in procT()
        if (isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "where")) { //Ew -> T 'where' Dr => 'where'
            Read_Next();
            processNonTerminal_Dr(); //this is to extra readToken() in procDR()
            Build_NAry_ASTNode(AbstractSyntaxTreeNodeType.WHERE, 2);
        }
    }


     // Tuple Expressions



     // <pre>
     // T -> Ta ( ',' Ta )+ => 'tau'
     //   -> Ta;
     // </pre>
    private void processNonTerminal_T() {
        processNonTerminal_Ta(); //T -> Ta
        //this is to extra readToken() in procTA()
        int treesToPop = 0;
        while (isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, ",")) { //T -> Ta (',' Ta )+ => 'tau'
            Read_Next();
            processNonTerminal_Ta(); //this is to extra readToken() done in procTA()
            treesToPop++;
        }
        if (treesToPop > 0) Build_NAry_ASTNode(AbstractSyntaxTreeNodeType.TAU, treesToPop + 1);
    }


     // <pre>
     // Ta -> Ta 'aug' Tc => 'aug'
     //    -> Tc;
     // </pre>

    private void processNonTerminal_Ta() {
        processNonTerminal_Tc(); //Ta -> Tc
        //this is to extra readNT done in procTC()
        while (isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "aug")) { //Ta -> Ta 'aug' Tc => 'aug'
            Read_Next();
            processNonTerminal_Tc(); //this is to extra readNT done in procTC()
            Build_NAry_ASTNode(AbstractSyntaxTreeNodeType.AUG, 2);
        }
    }

     // <pre>
     // Tc -> B '->' Tc '|' Tc => '->'
     //   -> B;
     // </pre>

    private void processNonTerminal_Tc() {
        processNonTerminal_B(); //Tc -> B
        //This is to extra readNT in procBT()
        if (isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, "->")) { //Tc -> B '->' Tc '|' Tc => '->'
            Read_Next();
            processNonTerminal_Tc(); //This is to extra readNT done in procTC
            if (!isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, "|"))
                throw new RuntimeException("TC: '|' expected");
            Read_Next();
            processNonTerminal_Tc();  //This is to extra readNT done in procTC
            Build_NAry_ASTNode(AbstractSyntaxTreeNodeType.CONDITIONAL, 3);
        }
    }

     // Boolean Expressions



     // <pre>
     // B -> B 'or' Bt => 'or'
     //  -> Bt;
     // </pre>
    private void processNonTerminal_B() {
        processNonTerminal_Bt(); //B -> Bt
        //this is to extra readNT in procBT()
        while (isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "or")) { //B -> B 'or' Bt => 'or'
            Read_Next();
            processNonTerminal_Bt();
            Build_NAry_ASTNode(AbstractSyntaxTreeNodeType.OR, 2);
        }
    }

     // <pre>
     // Bt -> Bs '&' Bt => '&'
     //   -> Bs;
     // </pre>
    private void processNonTerminal_Bt() {
        processNonTerminal_Bs(); //Bt -> Bs;
        //extra readNT in procBS()
        while (isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, "&")) { //Bt -> Bt '&' Bs => '&'
            Read_Next();
            processNonTerminal_Bs(); //extra readNT in procBS()
            Build_NAry_ASTNode(AbstractSyntaxTreeNodeType.AND, 2);
        }
    }

     // <pre>
     // Bs -> 'not Bp => 'not'
     //   -> Bp;
     // </pre>
    private void processNonTerminal_Bs() {
        if (isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "not")) { //Bs -> 'not' Bp => 'not'
            Read_Next();
            processNonTerminal_Bp(); //This is to extra readNT in procBP()
            Build_NAry_ASTNode(AbstractSyntaxTreeNodeType.NOT, 1);
        } else
            processNonTerminal_Bp(); //Bs -> Bp
        //This is to extra readNT in procBP()
    }

     // <pre>
     // Bp -> A ('gr' | '>' ) A => 'gr'
     //    -> A ('ge' | '>=' ) A => 'ge'
     //    -> A ('ls' | '<' ) A => 'ge'
     //    -> A ('le' | '<=' ) A => 'ge'
     //    -> A 'eq' A => 'eq'
     //    -> A 'ne' A => 'ne'
     //    -> A;
     // </pre>
    private void processNonTerminal_Bp() {
        processNonTerminal_A(); //Bp -> A
        if (isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "gr") || isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, ">")) { //Bp -> A('gr' | '>' ) A => 'gr'
            Read_Next();
            processNonTerminal_A(); //This is to extra readNT in procA()
            Build_NAry_ASTNode(AbstractSyntaxTreeNodeType.GR, 2);
        } else if (isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "ge") || isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, ">=")) { //Bp -> A ('ge' | '>=') A => 'ge'
            Read_Next();
            processNonTerminal_A(); //This is to extra readNT in procA()
            Build_NAry_ASTNode(AbstractSyntaxTreeNodeType.GE, 2);
        } else if (isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "ls") || isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, "<")) { //Bp -> A ('ls' | '<' ) A => 'ls'
            Read_Next();
            processNonTerminal_A(); //This is to extra readNT in procA()
            Build_NAry_ASTNode(AbstractSyntaxTreeNodeType.LS, 2);
        } else if (isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "le") || isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, "<=")) { //Bp -> A ('le' | '<=') A => 'le'
            Read_Next();
            processNonTerminal_A(); //This is to extra readNT in procA()
            Build_NAry_ASTNode(AbstractSyntaxTreeNodeType.LE, 2);
        } else if (isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "eq")) { //Bp -> A 'eq' A => 'eq'
            Read_Next();
            processNonTerminal_A(); //This is to extra readNT in procA()
            Build_NAry_ASTNode(AbstractSyntaxTreeNodeType.EQ, 2);
        } else if (isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "ne")) { //Bp -> A 'ne' A => 'ne'
            Read_Next();
            processNonTerminal_A(); //This is to extra readNT in procA()
            Build_NAry_ASTNode(AbstractSyntaxTreeNodeType.NE, 2);
        }
    }


     //Arithmetic Expressions


     // <pre>
     // A -> A '+' At => '+'
     //   -> A '-' At => '-'
     //   ->   '+' At
     //   ->   '-' At => 'neg'
     //   -> At;
     // </pre>
    private void processNonTerminal_A() {
        if (isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, "+")) { //A -> '+' At
            Read_Next();
            ProcessNonTerminal_At(); //This is to extra readNT in procAT()
        } else if (isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, "-")) { //A -> '-' At => 'neg'
            Read_Next();
            ProcessNonTerminal_At(); //This is to extra readNT in procAT()
            Build_NAry_ASTNode(AbstractSyntaxTreeNodeType.NEG, 1);
        } else
            ProcessNonTerminal_At(); //This is to extra readNT in procAT()

        boolean plus = true;
        while (isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, "+") || isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, "-")) {
            if (Cur_Token.getTokenValue().equals("+"))
                plus = true;
            else if (Cur_Token.getTokenValue().equals("-"))
                plus = false;
            Read_Next();
            ProcessNonTerminal_At(); //This is to extra readNT in procAT()
            if (plus) //A -> A '+' At => '+'
                Build_NAry_ASTNode(AbstractSyntaxTreeNodeType.PLUS, 2);
            else //A -> A '-' At => '-'
                Build_NAry_ASTNode(AbstractSyntaxTreeNodeType.MINUS, 2);
        }
    }

     // <pre>
     // At -> At '*' Af => '*'
     //    -> At '/' Af => '/'
     //    -> Af;
     // </pre>
    private void ProcessNonTerminal_At() {
        processNonTerminal_Af(); //At -> Af;
        //This is to extra readNT in procAF()
        boolean mult = true;
        while (isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, "*") || isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, "/")) {
            if (Cur_Token.getTokenValue().equals("*"))
                mult = true;
            else if (Cur_Token.getTokenValue().equals("/"))
                mult = false;
            Read_Next();
            processNonTerminal_Af(); //extra readNT in procAF()
            if (mult) //At -> At '*' Af => '*'
                Build_NAry_ASTNode(AbstractSyntaxTreeNodeType.MULT, 2);
            else //At -> At '/' Af => '/'
                Build_NAry_ASTNode(AbstractSyntaxTreeNodeType.DIV, 2);
        }
    }

     // <pre>
     // Af -> Ap '**' Af => '**'
     //   -> Ap;
     // </pre>
    private void processNonTerminal_Af() {
        processNonTerminal_Ap(); // Af -> Ap;
        //This is to extra readNT in procAP()
        if (isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, "**")) { //Af -> Ap '**' Af => '**'
            Read_Next();
            processNonTerminal_Af();
            Build_NAry_ASTNode(AbstractSyntaxTreeNodeType.EXP, 2);
        }
    }


     // <pre>
     // Ap -> Ap '@' '&lt;IDENTIFIER&gt;' R => '@'
     //    -> R;
     // </pre>
    private void processNonTerminal_Ap() {
        processNonTerminal_R(); //Ap -> R;
        //This is to extra readNT in procR()
        while (isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, "@")) { //Ap -> Ap '@' '<IDENTIFIER>' R => '@'
            Read_Next();
            if (!IsCurrentTokenType(Scanner.TOKEN_TYPE_IDENTIFIER))
                throw new RuntimeException("AP: expected Identifier");
            Read_Next();
            processNonTerminal_R(); //This is to extra readNT in procR()
            Build_NAry_ASTNode(AbstractSyntaxTreeNodeType.AT, 3);
        }
    }

     // Rators and Rands



     // <pre>
     // R -> R Rn => 'gamma'
     //  -> Rn;
     // </pre>
    private void processNonTerminal_R() {
        processNonTerminal_Rn();
        Read_Next();
        while (IsCurrentTokenType(Scanner.TOKEN_TYPE_INTEGER) ||
                IsCurrentTokenType(Scanner.TOKEN_TYPE_STRING) ||
                IsCurrentTokenType(Scanner.TOKEN_TYPE_IDENTIFIER) ||
                isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "true") ||
                isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "false") ||
                isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "nil") ||
                isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "dummy") ||
                IsCurrentTokenType(Scanner.TOKEN_TYPE_L_PAREN)) { //R -> R Rn => 'gamma'
            processNonTerminal_Rn();
            Build_NAry_ASTNode(AbstractSyntaxTreeNodeType.GAMMA, 2);
            Read_Next();
        }
    }

    private void processNonTerminal_Rn() {
        if (IsCurrentTokenType(Scanner.TOKEN_TYPE_IDENTIFIER) || //R -> '<IDENTIFIER>'
                IsCurrentTokenType(Scanner.TOKEN_TYPE_INTEGER) || //R -> '<INTEGER>'
                IsCurrentTokenType(Scanner.TOKEN_TYPE_STRING)) { //R-> '<STRING>'
        } else if (isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "true")) { //R -> 'true' => 'true'
            Create_Terminal_ASTNode(AbstractSyntaxTreeNodeType.TRUE, "true");
        } else if (isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "false")) { //R -> 'false' => 'false'
            Create_Terminal_ASTNode(AbstractSyntaxTreeNodeType.FALSE, "false");
        } else if (isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "nil")) { //R -> 'nil' => 'nil'
            Create_Terminal_ASTNode(AbstractSyntaxTreeNodeType.NIL, "nil");
        } else if (IsCurrentTokenType(Scanner.TOKEN_TYPE_L_PAREN)) {
            Read_Next();
            ProcessNonTerminal_E(); //extra readNT in procE()
            if (!IsCurrentTokenType(Scanner.TOKEN_TYPE_R_PAREN))
                throw new RuntimeException("RN: ')' expected");
        } else if (isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "dummy")) { //R -> 'dummy' => 'dummy'
            Create_Terminal_ASTNode(AbstractSyntaxTreeNodeType.DUMMY, "dummy");
        }
    }

     // Definitions


     // <pre>
     // D -> Da 'within' D => 'within'
     //   -> Da;
     // </pre>
    private void processNonTerminal_D() {
        processNonTerminal_Da(); //D -> Da
        //This is to extra readToken() in procDA()
        if (isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "within")) { //D -> Da 'within' D => 'within'
            Read_Next();
            processNonTerminal_D();
            Build_NAry_ASTNode(AbstractSyntaxTreeNodeType.WITHIN, 2);
        }
    }

     // <pre>
     // Da -> Dr ('and' Dr)+ => 'and'
     //    -> Dr;
     // </pre>
    private void processNonTerminal_Da() {
        processNonTerminal_Dr(); //Da -> Dr
        //This is to extra readToken() in procDR()
        int treesToPop = 0;
        while (isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "and")) { //Da -> Dr ( 'and' Dr )+ => 'and'
            Read_Next();
            processNonTerminal_Dr(); //This is to extra readToken() in procDR()
            treesToPop++;
        }
        if (treesToPop > 0) Build_NAry_ASTNode(AbstractSyntaxTreeNodeType.SIMULTDEF, treesToPop + 1);
    }

     // Dr -> 'rec' Db => 'rec'
     //    -> Db;
    private void processNonTerminal_Dr() {
        if (isCurrentToken(Scanner.TOKEN_TYPE_RESERVED, "rec")) { //Dr -> 'rec' Db => 'rec'
            Read_Next();
            processNonTerminal_Db(); //This is to extra readToken() in procDB()
            Build_NAry_ASTNode(AbstractSyntaxTreeNodeType.REC, 1);
        } else { //Dr -> Db
            processNonTerminal_Db(); //This is to extra readToken() in procDB()
        }
    }

     // <pre>
     // Db -> Vl '=' E => '='
     //    -> '&lt;IDENTIFIER&gt;' Vb+ '=' E => 'fcn_form'
     //    -> '(' D ')';
     // </pre>

    private void processNonTerminal_Db() {
        if (IsCurrentTokenType(Scanner.TOKEN_TYPE_L_PAREN)) {
            processNonTerminal_D();
            Read_Next();
            if (!IsCurrentTokenType(Scanner.TOKEN_TYPE_R_PAREN))
                throw new RuntimeException("DB: ')' expected");
            Read_Next();
        } else if (IsCurrentTokenType(Scanner.TOKEN_TYPE_IDENTIFIER)) {
            Read_Next();
            if (isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, ",")) {
                Read_Next();
                processNonTerminal_Vl(); //This is to extra readNT in procVB()
                if (!isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, "="))
                    throw new RuntimeException("DB: = expected.");
                Build_NAry_ASTNode(AbstractSyntaxTreeNodeType.COMMA, 2);
                Read_Next();
                ProcessNonTerminal_E(); //This is to extra readNT in procE()
                Build_NAry_ASTNode(AbstractSyntaxTreeNodeType.EQUAL, 2);
            } else {
                if (isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, "=")) {
                    Read_Next();
                    ProcessNonTerminal_E(); //This is to extra readNT in procE()
                    Build_NAry_ASTNode(AbstractSyntaxTreeNodeType.EQUAL, 2);
                } else {
                    int treesToPop = 0;

                    while (IsCurrentTokenType(Scanner.TOKEN_TYPE_IDENTIFIER) || IsCurrentTokenType(Scanner.TOKEN_TYPE_L_PAREN)) {
                        processNonTerminal_Vb(); //extra readNT in procVB()
                        treesToPop++;
                    }

                    if (treesToPop == 0)
                        throw new RuntimeException("E: at least one 'Vb' expected");

                    if (!isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, "="))
                        throw new RuntimeException("DB: = expected.");

                    Read_Next();
                    ProcessNonTerminal_E(); //extra readNT in procE()

                    Build_NAry_ASTNode(AbstractSyntaxTreeNodeType.FCNFORM, treesToPop + 2); //+1 for the last E and +1 for the first identifier
                }
            }
        }
    }

     // Variables

     // <pre>
     // Vb -> '&lt;IDENTIFIER&gt;'
     //    -> '(' Vl ')'
     //    -> '(' ')' => '()'
     // </pre>
    private void processNonTerminal_Vb() {
        if (IsCurrentTokenType(Scanner.TOKEN_TYPE_IDENTIFIER)) { //Vb -> '<IDENTIFIER>'
            Read_Next();
        } else if (IsCurrentTokenType(Scanner.TOKEN_TYPE_L_PAREN)) {
            Read_Next();
            if (IsCurrentTokenType(Scanner.TOKEN_TYPE_R_PAREN)) { //Vb -> '(' ')' => '()'
                Create_Terminal_ASTNode(AbstractSyntaxTreeNodeType.PAREN, "");
                Read_Next();
            } else { //Vb -> '(' Vl ')'
                processNonTerminal_Vl(); //extra readNT in procVB()
                if (!IsCurrentTokenType(Scanner.TOKEN_TYPE_R_PAREN))
                    throw new RuntimeException("VB: ')' expected");
                Read_Next();
            }
        }
    }

     // <pre>
     // Vl -> '&lt;IDENTIFIER&gt;' list ',' => ','?;
     // </pre>

     private void processNonTerminal_Vl() {
        if (!IsCurrentTokenType(Scanner.TOKEN_TYPE_IDENTIFIER))
            throw new RuntimeException("VL: Identifier expected");
        else {
            Read_Next();
            int treesToPop = 0;
            while (isCurrentToken(Scanner.TOKEN_TYPE_OPERATOR, ",")) { //Vl -> '<IDENTIFIER>' list ',' => ','?;
                Read_Next();
                if (!IsCurrentTokenType(Scanner.TOKEN_TYPE_IDENTIFIER))
                    throw new RuntimeException("VL: Identifier expected");
                Read_Next();
                treesToPop++;
            }
            if (treesToPop > 0)
                Build_NAry_ASTNode(AbstractSyntaxTreeNodeType.COMMA, treesToPop + 1); //+1 for the first identifier
        }
    }

}

