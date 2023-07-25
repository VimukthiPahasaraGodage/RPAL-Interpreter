package com.proglangproj.group50.lexicalanalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class Scanner{

  /*
   * Declaration of constant integer values for each possible token type
   */
  public static final int TOKEN_TYPE_IDENTIFIER = 0;
  public static final int TOKEN_TYPE_INTEGER = 1;
  public static final int TOKEN_TYPE_STRING = 2;
  public static final int TOKEN_TYPE_OPERATOR = 3;
  public static final int TOKEN_TYPE_DELETE = 4;
  public static final int TOKEN_TYPE_L_PAREN = 5;
  public static final int TOKEN_TYPE_R_PAREN = 6;
  public static final int TOKEN_TYPE_SEMICOLON = 7;
  public static final int TOKEN_TYPE_COMMA = 8;
  public static final int TOKEN_TYPE_RESERVED = 9;
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


  /*
   * Declaration of
   */
  private static final String letterRegexString = "a-zA-Z";
  private static final String digitRegexString = "\\d";
  private static final String spaceRegexString = "[\\s\\t\\n]";
  private static final String punctuationRegexString = "();,";
  private static final String opSymbolRegexString = "+-/~:=|!#%_{}\"*<>.&$^\\[\\]?@";
  private static final String opSymbolToEscapeString = "([*<>.&$^?])";

  public static final Pattern LetterPattern = Pattern.compile("["+letterRegexString+"]");

  public static final Pattern IdentifierPattern = Pattern.compile("["+letterRegexString+digitRegexString+"_]");

  public static final Pattern DigitPattern = Pattern.compile(digitRegexString);

  public static final Pattern PunctuationPattern = Pattern.compile("["+punctuationRegexString+"]");

  public static final String opSymbolRegex = "[" + escapeMetaChars(opSymbolRegexString, opSymbolToEscapeString) + "]";
  public static final Pattern OpSymbolPattern = Pattern.compile(opSymbolRegex);

  public static final Pattern StringPattern = Pattern.compile("[ \\t\\n\\\\"+punctuationRegexString+letterRegexString+digitRegexString+escapeMetaChars(opSymbolRegexString, opSymbolToEscapeString) +"]");

  public static final Pattern SpacePattern = Pattern.compile(spaceRegexString);

  public static final Pattern CommentPattern = Pattern.compile("[ \\t\\'\\\\ \\r"+punctuationRegexString+letterRegexString+digitRegexString+escapeMetaChars(opSymbolRegexString, opSymbolToEscapeString)+"]"); //the \\r is for Windows LF; not really required since we're targeting *nix systems

  private static String escapeMetaChars(String inputString, String charsToEscape){
    return inputString.replaceAll(charsToEscape,"\\\\\\\\$1");
  }

  private BufferedReader buffer;
  private String extraCharRead;
  private final List<String> reservedIdentifiers = Arrays.asList(new String[]{"let","in","within","fn","where","aug","or",
                                                                              "not","gr","ge","ls","le","eq","ne","true",
                                                                              "false","nil","dummy","rec","and"});
  private int sourceLineNumber;
  
  public Scanner(String inputFile) throws IOException{
    sourceLineNumber = 1;
    buffer = new BufferedReader(new InputStreamReader(new FileInputStream(new File(inputFile))));
  }
  
  /**
   * Returns next token from input file
   * @return null if the file has ended
   */
  public Token readNextToken(){
    Token nextToken = null;
    String nextChar;
    if(extraCharRead!=null){
      nextChar = extraCharRead;
      extraCharRead = null;
    } else
      nextChar = getNextCharacterFromSource();
    if(nextChar!=null)
      nextToken = buildToken(nextChar);
    return nextToken;
  }

  private String getNextCharacterFromSource(){
    String nextChar = null;
    try{
      int c = buffer.read();
      if(c!=-1){
        nextChar = Character.toString((char)c);
        if(nextChar.equals("\n")) sourceLineNumber++;
      } else
          buffer.close();
    }catch(IOException e){
    }
    return nextChar;
  }

  /**
   * Builds next token from input
   * @param currentChar character currently being processed 
   * @return token that was built
   */
  private Token buildToken(String currentChar){
    Token nextToken = null;
    if(LetterPattern.matcher(currentChar).matches()){
      nextToken = buildIdentifierToken(currentChar);
    }
    else if(DigitPattern.matcher(currentChar).matches()){
      nextToken = buildIntegerToken(currentChar);
    }
    else if(OpSymbolPattern.matcher(currentChar).matches()){ //comment tokens are also entered from here
      nextToken = buildOperatorToken(currentChar);
    }
    else if(currentChar.equals("\'")){
      nextToken = buildStringToken(currentChar);
    }
    else if(SpacePattern.matcher(currentChar).matches()){
      nextToken = buildSpaceToken(currentChar);
    }
    else if(PunctuationPattern.matcher(currentChar).matches()){
      nextToken = buildPunctuationPattern(currentChar);
    }
    return nextToken;
  }

  /**
   * Builds Identifier token.
   * Identifier -> Letter (Letter | Digit | '_')*
   * @param currentChar character currently being processed 
   * @return token that was built
   */
  private Token buildIdentifierToken(String currentChar){
    Token identifierToken = new Token();
    identifierToken.setType(TOKEN_TYPE_IDENTIFIER);
    identifierToken.setSourceLineNumber(sourceLineNumber);
    StringBuilder sBuilder = new StringBuilder(currentChar);
    
    String nextChar = getNextCharacterFromSource();
    while(nextChar!=null){ //null indicates the file ended
      if(IdentifierPattern.matcher(nextChar).matches()){
        sBuilder.append(nextChar);
        nextChar = getNextCharacterFromSource();
      }
      else{
        extraCharRead = nextChar;
        break;
      }
    }
    
    String value = sBuilder.toString();
    if(reservedIdentifiers.contains(value))
      identifierToken.setType(TOKEN_TYPE_RESERVED);
    
    identifierToken.setValue(value);
    return identifierToken;
  }

  /**
   * Builds integer token.
   * Integer -> Digit+
   * @param currentChar character currently being processed 
   * @return token that was built
   */
  private Token buildIntegerToken(String currentChar){
    Token integerToken = new Token();
    integerToken.setType(TOKEN_TYPE_INTEGER);
    integerToken.setSourceLineNumber(sourceLineNumber);
    StringBuilder sBuilder = new StringBuilder(currentChar);
    
    String nextChar = getNextCharacterFromSource();
    while(nextChar!=null){ //null indicates the file ended
      if(DigitPattern.matcher(nextChar).matches()){
        sBuilder.append(nextChar);
        nextChar = getNextCharacterFromSource();
      }
      else{
        extraCharRead = nextChar;
        break;
      }
    }
    
    integerToken.setValue(sBuilder.toString());
    return integerToken;
  }

  /**
   * Builds operator token.
   * Operator -> Operator_symbol+
   * @param currentChar character currently being processed 
   * @return token that was built
   */
  private Token buildOperatorToken(String currentChar){
    Token opSymbolToken = new Token();
    opSymbolToken.setType(TOKEN_TYPE_OPERATOR);
    opSymbolToken.setSourceLineNumber(sourceLineNumber);
    StringBuilder sBuilder = new StringBuilder(currentChar);
    
    String nextChar = getNextCharacterFromSource();
    
    if(currentChar.equals("/") && nextChar.equals("/"))
      return buildCommentToken(currentChar+nextChar);
    
    while(nextChar!=null){ //null indicates the file ended
      if(OpSymbolPattern.matcher(nextChar).matches()){
        sBuilder.append(nextChar);
        nextChar = getNextCharacterFromSource();
      }
      else{
        extraCharRead = nextChar;
        break;
      }
    }
    
    opSymbolToken.setValue(sBuilder.toString());
    return opSymbolToken;
  }

  /**
   * Builds string token.
   * String -> '''' ('\' 't' | '\' 'n' | '\' '\' | '\' '''' |'(' | ')' | ';' | ',' |'' |Letter | Digit | Operator_symbol )* ''''
   * @param currentChar character currently being processed 
   * @return token that was built
   */
  private Token buildStringToken(String currentChar){
    Token stringToken = new Token();
    stringToken.setType(TOKEN_TYPE_STRING);
    stringToken.setSourceLineNumber(sourceLineNumber);
    StringBuilder sBuilder = new StringBuilder("");
    
    String nextChar = getNextCharacterFromSource();
    while(nextChar!=null){ //null indicates the file ended
      if(nextChar.equals("\'")){ //we just used up the last char we read, hence no need to set extraCharRead
        //sBuilder.append(nextChar);
        stringToken.setValue(sBuilder.toString());
        return stringToken;
      }
      else if(StringPattern.matcher(nextChar).matches()){ //match Letter | Digit | Operator_symbol
        sBuilder.append(nextChar);
        nextChar = getNextCharacterFromSource();
      }
    }
    
    return null;
  }
  
  private Token buildSpaceToken(String currentChar){
    Token deleteToken = new Token();
    deleteToken.setType(TOKEN_TYPE_DELETE);
    deleteToken.setSourceLineNumber(sourceLineNumber);
    StringBuilder sBuilder = new StringBuilder(currentChar);
    
    String nextChar = getNextCharacterFromSource();
    while(nextChar!=null){ //null indicates the file ended
      if(SpacePattern.matcher(nextChar).matches()){
        sBuilder.append(nextChar);
        nextChar = getNextCharacterFromSource();
      }
      else{
        extraCharRead = nextChar;
        break;
      }
    }
    
    deleteToken.setValue(sBuilder.toString());
    return deleteToken;
  }
  
  private Token buildCommentToken(String currentChar){
    Token commentToken = new Token();
    commentToken.setType(TOKEN_TYPE_DELETE);
    commentToken.setSourceLineNumber(sourceLineNumber);
    StringBuilder sBuilder = new StringBuilder(currentChar);
    
    String nextChar = getNextCharacterFromSource();
    while(nextChar!=null){ //null indicates the file ended
      if(CommentPattern.matcher(nextChar).matches()){
        sBuilder.append(nextChar);
        nextChar = getNextCharacterFromSource();
      }
      else if(nextChar.equals("\n"))
        break;
    }
    
    commentToken.setValue(sBuilder.toString());
    return commentToken;
  }

  private Token buildPunctuationPattern(String currentChar){
    Token punctuationToken = new Token();
    punctuationToken.setSourceLineNumber(sourceLineNumber);
    punctuationToken.setValue(currentChar);
    switch (currentChar) {
      case "(" -> punctuationToken.setType(TOKEN_TYPE_L_PAREN);
      case ")" -> punctuationToken.setType(TOKEN_TYPE_R_PAREN);
      case ";" -> punctuationToken.setType(TOKEN_TYPE_SEMICOLON);
      case "," -> punctuationToken.setType(TOKEN_TYPE_COMMA);
    }
    return punctuationToken;
  }
}

