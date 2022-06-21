package org.ufba.raide.java.testsmell;

import org.eclipse.jface.text.Position;

import com.github.javaparser.Range;

public class TestSmellDescription {
	
	String testSmellType;
	String testSmellRefactoring;
	String filePath;
	String className;
	String methodName;
	String linePositionBegin;
	String linePositionEnd;
	int beginMethod;
	int endMethod;
	String field;
	Range range;
//	Position positionBegin;
//	Position positionEnd;
	
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
	public TestSmellDescription(String testSmellType, String testSmellRefactoring, String filePath, 
								String className, String methodName, String linePositionBegin,  
								String linePositionEnd, int beginMethod, int endMethod, String field,
								Range range) {
		
		this.testSmellType = testSmellType;
		this.testSmellRefactoring = testSmellRefactoring;
		this.filePath = filePath;
		this.className = className;
		this.methodName = methodName;
		this.linePositionBegin = linePositionBegin;
		this.linePositionEnd = linePositionEnd;		
		this.beginMethod = beginMethod;
		this.endMethod = endMethod;
		this.field = field;
		this.range = range;
	}
	public TestSmellDescription() {
		new TestSmellDescription("", "", "", "", "", "", "", 0, 0, "", null);		
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
