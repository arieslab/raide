package org.ufba.raide.java.ast.decomposition.cfg;

import org.ufba.raide.java.ast.decomposition.AbstractStatement;

public class CFGSynchronizedNode extends CFGBlockNode {
	public CFGSynchronizedNode(AbstractStatement statement) {
		super(statement);
	}
}
