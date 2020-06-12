package org.ufba.raide.java.ast.util;

import org.eclipse.jdt.core.dom.Expression;

public class InstanceOfExpression implements ExpressionInstanceChecker {

	public boolean instanceOf(Expression expression) {
		return true;
	}

}
