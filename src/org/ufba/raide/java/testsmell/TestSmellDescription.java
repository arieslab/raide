package org.ufba.raide.java.testsmell;

import org.eclipse.jface.text.Position;

import com.github.javaparser.Range;
import com.github.javaparser.ast.body.MethodDeclaration;

public class TestSmellDescription {
	
	String testSmellType;
	String testSmellRefactoring;
	String filePath;
	String className;
	String methodName;
	// Remove this snippet in the next refactorings
	// Begin
	String linePositionBegin;
	String linePositionEnd;
	int beginMethod;
	int endMethod;
	//End
	String field;
	Range range;
	MethodDeclaration methodTest;
	
	// Remove this snippet in the next refactorings
	// Begin
	public int getBeginMethod() {
		return beginMethod;
	}
	public void setBeginMethod(int beginMethod) {
		this.beginMethod = beginMethod;
	}
	public int getEndMethod() {
		return endMethod;
	}
	public void setEndMethod(int endMethod) {
		this.endMethod = endMethod;
	}
	//End
	public TestSmellDescription(String testSmellType, String testSmellRefactoring, 
								String filePath, String className, String methodName, 
								// Remove this snippet in the next refactorings
								// Begin
								String linePositionBegin, String linePositionEnd, int beginMethod, int endMethod, 
								//End
								String field, Range range, MethodDeclaration methodTest) {
		
		this.testSmellType = testSmellType;
		this.testSmellRefactoring = testSmellRefactoring;
		this.filePath = filePath;
		this.className = className;
		this.methodName = methodName;
		// Remove this snippet in the next refactorings
		// Begin
		this.linePositionBegin = linePositionBegin;
		this.linePositionEnd = linePositionEnd;		
		this.beginMethod = beginMethod;
		this.endMethod = endMethod;
		//End
		this.field = field;
		this.range = range;
		this.methodTest = methodTest;
	}
	public TestSmellDescription() {
		// Remove this snippet in the next refactorings
		// Begin
		new TestSmellDescription("", "", "", "", "", "", "", 0, 0, "", null, null);
		//End
		//new TestSmellDescription("", "", "", "", "", "", null);	
	}	
	
	
	public String getTestSmellType() {
		return testSmellType;
	}
	public void setTestSmellType(String testSmellType) {
		this.testSmellType = testSmellType;
	}
	public String getTestSmellRefactoring() {
		return testSmellRefactoring;
	}
	public void setTestSmellRefactoring(String testSmellRefactoring) {
		this.testSmellRefactoring = testSmellRefactoring;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	
	// Remove this snippet in the next refactorings
	// Begin
	public String getLinePositionBegin() {
		return linePositionBegin;
	}
	public void setLinePositionBegin(String linePositionBegin) {
		this.linePositionBegin = linePositionBegin;
	}
	public String getLinePositionEnd() {
		return linePositionEnd;
	}
	public void setLinePositionEnd(String linePositionEnd) {
		this.linePositionEnd = linePositionEnd;
	}
	//End
	public Range getRange() {
		return range;
	}
	public void setRange(Range range) {
		this.range = range;
	}
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
}
