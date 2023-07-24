package com.proglangproj.group50.cse_machine;

import com.proglangproj.group50.cse_machine.EvaluationError;

public class EvaluationError{
  
  public static void printError(int sourceLineNumber, String message){
    System.out.println("Error :"+sourceLineNumber+": "+message);
    System.exit(1);
  }

}
