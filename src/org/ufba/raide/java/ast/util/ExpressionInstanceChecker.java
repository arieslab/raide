package org.ufba.raide.java.ast.util;

import org.eclipse.jdt.core.dom.Expression;

public interface ExpressionInstanceChecker {
	public boolean instanceOf(Expression expression);
}
