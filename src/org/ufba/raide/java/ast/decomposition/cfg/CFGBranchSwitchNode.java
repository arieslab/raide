package org.ufba.raide.java.ast.decomposition.cfg;

import org.ufba.raide.java.ast.decomposition.AbstractStatement;

public class CFGBranchSwitchNode extends CFGBranchConditionalNode {

	public CFGBranchSwitchNode(AbstractStatement statement) {
		super(statement);
	}
}
