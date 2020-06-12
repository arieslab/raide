package org.ufba.raide.java.distance;

import org.ufba.raide.java.ast.decomposition.AbstractStatement;

public class MyStatement extends MyAbstractStatement {

	public MyStatement(AbstractStatement statement) {
		super(statement);
	}

	public MyStatement(MyMethodInvocation methodInvocation) {
		super(methodInvocation);
	}
}
