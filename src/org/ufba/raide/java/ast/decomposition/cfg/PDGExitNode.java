package org.ufba.raide.java.ast.decomposition.cfg;

import java.util.Set;

import org.ufba.raide.java.ast.FieldObject;
import org.ufba.raide.java.ast.VariableDeclarationObject;

public class PDGExitNode extends PDGStatementNode {
	private AbstractVariable returnedVariable;
	
	public PDGExitNode(CFGNode cfgNode, Set<VariableDeclarationObject> variableDeclarationsInMethod,
			Set<FieldObject> fieldsAccessedInMethod) {
		super(cfgNode, variableDeclarationsInMethod, fieldsAccessedInMethod);
		if(cfgNode instanceof CFGExitNode) {
			CFGExitNode exitNode = (CFGExitNode)cfgNode;
			returnedVariable = exitNode.getReturnedVariable();
		}
	}

	public AbstractVariable getReturnedVariable() {
		return returnedVariable;
	}
}
