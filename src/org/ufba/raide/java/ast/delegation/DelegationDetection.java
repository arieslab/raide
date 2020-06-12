package org.ufba.raide.java.ast.delegation;

import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;

import org.ufba.raide.java.ast.ClassObject;
import org.ufba.raide.java.ast.MethodObject;
import org.ufba.raide.java.ast.SystemObject;

public class DelegationDetection {
    private List<DelegationPath> delegationPathList;

    public DelegationDetection(SystemObject systemObject) {
        delegationPathList = new ArrayList<DelegationPath>();
        ListIterator<ClassObject> classIt = systemObject.getClassListIterator();
        while(classIt.hasNext()) {
            ClassObject classObject = classIt.next();
            ListIterator<MethodObject> methodIt = classObject.getMethodIterator();
            while(methodIt.hasNext()) {
                MethodObject methodObject = methodIt.next();
                DelegationTree tree = new DelegationTree(systemObject,methodObject);
                if(tree.getDepth() >= 1) {
                    delegationPathList.addAll(tree.getDelegationPathList());
                }
            }
        }
    }

    public List<DelegationPath> getAllDelegationPaths() {
        return this.delegationPathList;
    }

    //size must be >= 2
    public List<DelegationPath> getDelegationPathsOfSize(int size) {
        List<DelegationPath> depthDelegationPathList = new ArrayList<DelegationPath>();
        for(DelegationPath path : delegationPathList) {
            if(path.size() == size)
                depthDelegationPathList.add(path);
        }
        return depthDelegationPathList;
    }
}
