package com.proglangproj.group50.cse_machine;

import com.proglangproj.group50.abstractsyntaxtree.AbstractSyntaxTreeNode;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private Environment parent;
    private final Map<String, AbstractSyntaxTreeNode> nameValueMap;

    public Environment() {
        nameValueMap = new HashMap<String, AbstractSyntaxTreeNode>();
    }

    public Environment getParent() {
        return parent;
    }

    public void setParent(Environment parent) {
        this.parent = parent;
    }

    /**
     * Tries to find the binding of the given key in the mappings of this Environment's
     * inheritance hierarchy, starting with the Environment this method is invoked on.
     *
     * @param key key the mapping of which to find
     * @return AbstractSyntaxTreeNode that corresponds to the mapping of the key passed in as an argument
     * or null if no mapping was found
     */
    public AbstractSyntaxTreeNode lookup(String key) {
        AbstractSyntaxTreeNode retValue = null;
        Map<String, AbstractSyntaxTreeNode> map = nameValueMap;

        retValue = map.get(key);

        if (retValue != null)
            return retValue.acceptASTNode(new CopierOfNodes());

        if (parent != null)
            return parent.lookup(key);
        else
            return null;
    }

    public void addMapping(String key, AbstractSyntaxTreeNode value) {
        nameValueMap.put(key, value);
    }
}
