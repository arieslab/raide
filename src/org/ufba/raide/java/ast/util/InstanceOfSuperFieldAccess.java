package org.ufba.raide.java.ast.util;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SuperFieldAccess;

public class InstanceOfSuperFieldAccess implements ExpressionInstanceChecker {

	public boolean instanceOf(Expression expression) {
		if(expression instanceof SuperFieldAccess)
			return true;
		else
			return false;
	}

}
