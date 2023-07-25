package com.proglangproj.group50.abstractsyntaxtree;

import java.io.Serial;

public class StandardizeException extends RuntimeException{
  @Serial
  private static final long serialVersionUID = 1L;
  
  public StandardizeException(String message){
    super(message);
  }

}
