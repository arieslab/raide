package org.ufba.raide.java.ast.delegation;

import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;

import org.ufba.raide.java.ast.MethodObject;

public class DelegationPath {
    private List<MethodObject> path;

    public DelegationPath() {
        path = new ArrayList<MethodObject>();
    }

    public void addMethodInvocation(MethodObject mio) {
        path.add(mio);
    }
    
    public int size() {
        return path.size();
    }

    public ListIterator<MethodObject> getPathIterator() {
        return path.listIterator();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<path.size()-1; i++) {
            sb.append(path.get(i)).append("->");
        }
        sb.append(path.get(path.size()-1));
        return sb.toString();
    }
}
