package org.ufba.raide.java.ast.util;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ThisExpression;

public class InstanceOfThisExpression implements ExpressionInstanceChecker {

	public boolean instanceOf(Expression expression) {
		if(expression instanceof ThisExpression)
			return true;
		else
			return false;
	}

}
