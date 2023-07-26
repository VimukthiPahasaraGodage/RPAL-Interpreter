package com.proglangproj.group50.cse_machine;

public class EvaluationError{
  
  public static void print_error(int sourceLineNumber, String message){
    System.out.println("Error :"+sourceLineNumber+": "+message);
    System.exit(1);
  }

}
