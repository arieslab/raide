package org.ufba.raide.java.distance;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.Position;
import org.ufba.raide.java.ast.ClassObject;
import org.ufba.raide.java.ast.FieldInstructionObject;
import org.ufba.raide.java.ast.MethodInvocationObject;
import org.ufba.raide.java.ast.MethodObject;
import org.ufba.raide.java.ast.TypeObject;
import org.ufba.raide.java.ast.decomposition.cfg.PlainVariable;
import org.ufba.raide.java.ast.visualization.AssertionRouletteVisualizationData;
import org.ufba.raide.java.ast.visualization.FeatureEnvyVisualizationData;
import org.ufba.raide.java.refactoring.views.DuplicateAssertView;
import org.ufba.raide.java.testsmell.AbstractSmell;
import org.ufba.raide.java.testsmell.ResultsWriter;
import org.ufba.raide.java.testsmell.TestFile;
import org.ufba.raide.java.testsmell.TestSmellDetector;

import com.github.javaparser.Range;

public class RemoveMethodCandidateRefactoring extends CandidateRefactoring implements Comparable<RemoveMethodCandidateRefactoring> {
    private MySystem system;
	private MyClass sourceClass;
    private MyClass targetClass;
    private MyMethod sourceMethod;
    private Map<MethodInvocation, MethodDeclaration> additionalMethodsToBeMoved;
    private String methodName;
    private AssertionRouletteVisualizationData visualizationData;
    private Integer userRate;
    private String lineNumber;
    private Position position;
    private String field;
    private Range range;
    
    public String getLineNumber() {
		return lineNumber;
	}
	public void setLineNumber(String lineNumber) {
		this.lineNumber = lineNumber;
	}
	
	@Override
	public Position getPosition() {
		return position;
	}
	@Override
	public void setPosition(int line, int column) {
		// TODO Auto-generated method stub
		
	}

    public RemoveMethodCandidateRefactoring(MySystem system, MyClass sourceClass, MyClass targetClass, MyMethod sourceMethod, Position targetPosition) {
  
        this.system = system;
    	this.sourceClass = sourceClass;
        this.targetClass = targetClass;
        this.sourceMethod = sourceMethod;
        this.additionalMethodsToBeMoved = new LinkedHashMap<MethodInvocation, MethodDeclaration>();
        this.methodName = sourceMethod.getMethodName();
        this.position = targetPosition;
        
        List<MethodInvocationObject> methodInvocations = sourceMethod.getMethodObject().getMethodInvocations();
        for(MethodInvocationObject methodInvocation : methodInvocations) {
        	if(methodInvocation.getOriginClassName().equals(sourceClass.getClassObject().getName()) &&
        			!sourceClass.getClassObject().containsMethodInvocation(methodInvocation, sourceMethod.getMethodObject()) &&
        			!system.getSystemObject().containsMethodInvocation(methodInvocation, sourceClass.getClassObject())) {
        		MethodObject invokedMethod = sourceClass.getClassObject().getMethod(methodInvocation);
        		boolean systemMemberAccessed = false;
        		for(MethodInvocationObject methodInvocationObject : invokedMethod.getMethodInvocations()) {
        			if(system.getSystemObject().getClassObject(methodInvocationObject.getOriginClassName()) != null) {
        				systemMemberAccessed = true;
        				break;
        			}
        		}
        		if(!systemMemberAccessed) {
        			for(FieldInstructionObject fieldInstructionObject : invokedMethod.getFieldInstructions()) {
        				if(system.getSystemObject().getClassObject(fieldInstructionObject.getOwnerClass()) != null) {
        					systemMemberAccessed = true;
        					break;
        				}
        			}
        		}
        		if(!systemMemberAccessed && !additionalMethodsToBeMoved.containsKey(methodInvocation.getMethodInvocation()))
        			additionalMethodsToBeMoved.put(methodInvocation.getMethodInvocation(), invokedMethod.getMethodDeclaration());
        	}
        }
        this.visualizationData = new AssertionRouletteVisualizationData(sourceClass.getClassObject(),
				sourceMethod.getMethodObject(), targetClass.getClassObject());
    }
    public RemoveMethodCandidateRefactoring(MySystem system, MyClass sourceClass, MyClass targetClass, MyMethod sourceMethod, String lineNumber, Position targetPosition, Range range) {
    	
        this.system = system;
    	this.sourceClass = sourceClass;
        this.targetClass = targetClass;
        this.sourceMethod = sourceMethod;
        this.additionalMethodsToBeMoved = new LinkedHashMap<MethodInvocation, MethodDeclaration>();
        this.methodName = sourceMethod.getMethodName();
        this.lineNumber = lineNumber;
        this.visualizationData = new AssertionRouletteVisualizationData(new ClassObject(), new MethodObject(null), new ClassObject());
        this.position = targetPosition;
        this.range = range;
        
    }

    public boolean isApplicable() {
    	if(!isSynchronized() && !containsSuperMethodInvocation() && !overridesMethod() && !containsFieldAssignment() && !isTargetClassAnInterface() &&
    			validTargetObject() && !oneToManyRelationshipWithTargetClass() && !containsAssignmentToTargetClassVariable() &&
    			!containsMethodCallWithThisExpressionAsArgument() && !isTargetClassAnEnum() && !isSourceClassATestClass() && !targetClassContainsMethodWithSourceMethodSignature() &&
    			!containsNullCheckForTargetObject())
    		return true;
    	else
    		return false;
    }

    public boolean leaveDelegate() {
		return system.getSystemObject().containsMethodInvocation(getSourceMethod().getMethodObject().generateMethodInvocation(), getSourceClass().getClassObject()) ||
		system.getSystemObject().containsSuperMethodInvocation(getSourceMethod().getMethodObject().generateSuperMethodInvocation());
    }

    private boolean targetClassContainsMethodWithSourceMethodSignature() {
    	MethodObject sourceMethod = this.sourceMethod.getMethodObject();
    	for(MethodObject targetMethod : targetClass.getClassObject().getMethodList()) {
    		if(targetMethod.getName().equals(sourceMethod.getName()) &&
    				targetMethod.getReturnType().equals(sourceMethod.getReturnType())) {
    			if(targetMethod.getParameterTypeList().equals(sourceMethod.getParameterTypeList())) {
    				return true;
    			}
    			else {
    				List<TypeObject> sourceParameterTypeListWithoutTargetType = new ArrayList<TypeObject>();
    				for(TypeObject type : sourceMethod.getParameterTypeList()) {
    					if(!type.getClassType().equals(targetClass.getName())) {
    						sourceParameterTypeListWithoutTargetType.add(type);
    					}
    				}
    				if(targetMethod.getParameterTypeList().equals(sourceParameterTypeListWithoutTargetType)) {
    					return true;
    				}
    			}
    		}
    	}
    	return false;
    }

    private boolean isSourceClassATestClass() {
    	return sourceClass.getClassObject().containsMethodWithTestAnnotation() || sourceClass.getClassObject().extendsTestCase();
    }

    public void setSourceClass(MyClass sourceClass) {
		this.sourceClass = sourceClass;
	}
	private boolean isTargetClassAnInterface() {
    	if(targetClass.getClassObject().isInterface()) {
    		//System.out.println(this.toString() + "\tTarget class is an interface");
    		return true;
    	}
    	else {
    		return false;
    	}
    }

    private boolean isTargetClassAnEnum() {
    	if(targetClass.getClassObject().isEnum()) {
    		//System.out.println(this.toString() + "\tTarget class is an enum");
    		return true;
    	}
    	else {
    		return false;
    	}
    }

    private boolean validTargetObject() {
    	if(sourceMethod.getMethodObject().validTargetObject(sourceClass.getClassObject(), targetClass.getClassObject())) {
    		return true;
    	}
    	else {
    		//System.out.println(this.toString() + "\tdoes not contain a valid target object");
    		return false;
    	}
    }

    private boolean oneToManyRelationshipWithTargetClass() {
    	if(sourceMethod.getMethodObject().oneToManyRelationshipWithTargetClass(system.getAssociationsOfClass(sourceClass.getClassObject()), targetClass.getClassObject())) {
    		//System.out.println(this.toString() + "\thas one-to-many relationship with target class");
    		return true;
    	}
    	else {
    		return false;
    	}
    }

    private boolean overridesMethod() {
    	if(sourceMethod.getMethodObject().overridesMethod()) {
    		//System.out.println(this.toString() + "\toverrides method of superclass");
    		return true;
    	}
    	else
    		return false;
    }

    private boolean containsFieldAssignment() {
    	if(!sourceMethod.getMethodObject().getDefinedFieldsThroughThisReference().isEmpty()) {
    		//System.out.println(this.toString() + "\tcontains field assignment");
    		return true;
    	}
    	else
    		return false;
    }
    
    private boolean containsAssignmentToTargetClassVariable() {
    	Set<PlainVariable> definedVariables = sourceMethod.getMethodObject().getDefinedLocalVariables();
    	for(PlainVariable variable : definedVariables) {
    		if(variable.isParameter() && variable.getVariableType().equals(targetClass.getName()))
    			return true;
    	}
    	return false;
    }

    private boolean containsSuperMethodInvocation() {
    	if(sourceMethod.getMethodObject().containsSuperMethodInvocation() || sourceMethod.getMethodObject().containsSuperFieldAccess()) {
    		//System.out.println(this.toString() + "\tcontains super method invocation");
    		return true;
    	}
    	else
    		return false;
    }

    private boolean isSynchronized() {
    	if(sourceMethod.getMethodObject().isSynchronized()) {
    		//System.out.println(this.toString() + "\tis synchronized");
    		return true;
    	}
    	else
    		return false;
    }

    private boolean containsMethodCallWithThisExpressionAsArgument() {
    	if(sourceMethod.getMethodObject().containsMethodCallWithThisExpressionAsArgument()) {
    		return true;
    	}
    	else
    		return false;
    }

    private boolean containsNullCheckForTargetObject() {
    	if(sourceMethod.getMethodObject().containsNullCheckForTargetObject(targetClass.getClassObject())) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }

    public TypeDeclaration getSourceClassTypeDeclaration() {
        return (TypeDeclaration)sourceClass.getClassObject().getAbstractTypeDeclaration();
    }

    public TypeDeclaration getTargetClassTypeDeclaration() {
        return (TypeDeclaration)targetClass.getClassObject().getAbstractTypeDeclaration();
    }

	public IFile getSourceIFile() {
		return sourceClass.getClassObject().getIFile();
	}

	public IFile getTargetIFile() {
		return targetClass.getClassObject().getIFile();
	}
    public MethodDeclaration getSourceMethodDeclaration() {
        return sourceMethod.getMethodObject().getMethodDeclaration();
    }

    public MyClass getSourceClass() {
    	return sourceClass;
    }

    public MyClass getTargetClass() {
    	return targetClass;
    }

    public MyMethod getSourceMethod() {
    	return sourceMethod;
    }

    public Map<MethodInvocation, MethodDeclaration> getAdditionalMethodsToBeMoved() {
    	return additionalMethodsToBeMoved;
    }

    public String getMovedMethodName() {
		return methodName;
	}

	public void setMovedMethodName(String movedMethodName) {
		this.methodName = movedMethodName;
	}

	public String toString() {
        return getSourceEntity() + "->" + getTarget();
    }
	public String getSourceEntity() {
		StringBuilder sb = new StringBuilder();
        sb.append(sourceMethod.getClassOrigin()).append("::");
        sb.append(methodName);
        List<String> parameterList = sourceMethod.getParameterList();
        sb.append("(");
       
        if(parameterList!=null && !parameterList.isEmpty()) {
            for(int i=0; i<parameterList.size()-1; i++)
                sb.append(parameterList.get(i)).append(", ");
            sb.append(parameterList.get(parameterList.size()-1));
        }
        sb.append(")");
        if(sourceMethod.getReturnType() != null)
            sb.append(":").append(sourceMethod.getReturnType());
        return sb.toString();
	}
	

	public String getSourceEntity2() {
		StringBuilder sb = new StringBuilder();
        sb.append(methodName);
        List<String> parameterList = sourceMethod.getParameterList();        
        if(sourceMethod.getReturnType() != null)
            sb.append(":").append(sourceMethod.getReturnType());
        return sb.toString();
	}


	public String getSource() {
		return sourceClass.getName();
	}

	public String getTarget() {
		return targetClass.getName();
	}

	public Set<String> getEntitySet() {
		return sourceMethod.getEntitySet();
	}

	public List<Position> getPositions() {
		ArrayList<Position> positions = new ArrayList<Position>();
		Position position = new Position(getSourceMethodDeclaration().getStartPosition(), getSourceMethodDeclaration().getLength());
		positions.add(position);
		return positions;
	}

	public String getAnnotationText() {
		return visualizationData.toString();
	}

	public AssertionRouletteVisualizationData getFeatureEnvyVisualizationData() {
		return visualizationData;
	}

	public int getNumberOfDistinctEnviedElements() {
		int counter = 0;
		for(String entity : getEntitySet()) {
			String[] tokens = entity.split("::");
			String classOrigin = tokens[0];
			if(classOrigin.equals(targetClass.getName()))
				counter++;
		}
		return counter;
	}

	public Integer getUserRate() {
		return userRate;
	}

	public void setUserRate(Integer userRate) {
		this.userRate = userRate;
	}
	@Override
	public Range getRange() {
		// TODO Auto-generated method stub
		return range;
	}

	public int compareTo(RemoveMethodCandidateRefactoring other) {
		int thisSourceClassDependencies = this.getDistinctSourceDependencies();
		int otherSourceClassDependencies = other.getDistinctSourceDependencies();
		if(thisSourceClassDependencies != otherSourceClassDependencies) {
			return Integer.compare(thisSourceClassDependencies, otherSourceClassDependencies);
		}
		else {
			int thisTargetClassDependencies = this.getDistinctTargetDependencies();
			int otherTargetClassDependencies = other.getDistinctTargetDependencies();
			if(thisTargetClassDependencies != otherTargetClassDependencies) {
				return -Integer.compare(thisTargetClassDependencies, otherTargetClassDependencies);
			}
			else {
				return this.sourceClass.getName().compareTo(other.sourceClass.getName());
			}
		}
	}

	public int getDistinctSourceDependencies() {
		return getFeatureEnvyVisualizationData().getDistinctSourceDependencies();
	}

	public int getDistinctTargetDependencies() {
		return getFeatureEnvyVisualizationData().getDistinctTargetDependencies();
	}
	@Override
	public int getBeginMethod() {
		// TODO Auto-generated method stub
		return (Integer) null;
	}

	@Override
	public int getEndMethod() {
		// TODO Auto-generated method stub
		return (Integer) null;
	}
	@Override
	public String getMethod() {
		return methodName;
	}
	@Override
	public String getField() {
		return "";
	}
	
	
	
	
	
}
