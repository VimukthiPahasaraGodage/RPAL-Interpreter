package com.proglangproj.group50.lexicalanalyzer;

public class Token{
  private int type;
  private String value;
  private int sourceLineNumber;
  
  public int getType(){
    return type;
  }
  
  public void setType(int type){
    this.type = type;
  }
  
  public String getValue(){
    return value;
  }
  
  public void setValue(String value){
    this.value = value;
  }

  public int getSourceLineNumber(){
    return sourceLineNumber;
  }

  public void setSourceLineNumber(int sourceLineNumber){
    this.sourceLineNumber = sourceLineNumber;
  }
}
