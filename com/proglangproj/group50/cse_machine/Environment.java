package com.proglangproj.group50.cse_machine;

import java.util.HashMap;
import java.util.Map;

import com.proglangproj.group50.abstractsyntaxtree.ASTNode;

// Tries to find the binding of the given key in the mappings of this Environment's

public class Environment{
  private Environment parent_Environment;
  private Map<String, ASTNode> name_value_map;
  
  public Environment(){
    name_value_map = new HashMap<String, ASTNode>();
  }

  public Environment getParent_Environment(){
    return parent_Environment;
  }

  public void setParent_Environment(Environment parent_Environment){
    this.parent_Environment = parent_Environment;
  }

  public ASTNode lookup_parent(String key){
    ASTNode retValue = null;
    Map<String, ASTNode> map = name_value_map;
    
    retValue = map.get(key);
    
    if(retValue!=null)
      return retValue.Accept(new NodeCopier());
    
    if(parent_Environment !=null)
      return parent_Environment.lookup_parent(key);
    else
      return null;
  }
  
  public void add_Map(String key, ASTNode value){
    name_value_map.put(key, value);
  }
}
