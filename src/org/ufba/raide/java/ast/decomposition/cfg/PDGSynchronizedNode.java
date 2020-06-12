package org.ufba.raide.java.ast.decomposition.cfg;

import java.util.Set;

import org.ufba.raide.java.ast.FieldObject;
import org.ufba.raide.java.ast.VariableDeclarationObject;

public class PDGSynchronizedNode extends PDGBlockNode {
	public PDGSynchronizedNode(CFGSynchronizedNode cfgSynchronizedNode, Set<VariableDeclarationObject> variableDeclarationsInMethod,
			Set<FieldObject> fieldsAccessedInMethod) {
		super(cfgSynchronizedNode, variableDeclarationsInMethod, fieldsAccessedInMethod);
		this.controlParent = cfgSynchronizedNode.getControlParent();
		determineDefinedAndUsedVariables();
	}
}
