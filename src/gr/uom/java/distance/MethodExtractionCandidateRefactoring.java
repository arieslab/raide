package gr.uom.java.distance;

import gr.uom.java.ast.ClassObject;
import gr.uom.java.ast.FieldInstructionObject;
import gr.uom.java.ast.MethodInvocationObject;
import gr.uom.java.ast.MethodObject;
import gr.uom.java.ast.TypeObject;
import gr.uom.java.ast.decomposition.cfg.PlainVariable;
import gr.uom.java.ast.visualization.AssertionRouletteVisualizationData;
import gr.uom.java.ast.visualization.FeatureEnvyVisualizationData;
import gr.uom.java.raide.refactoring.views.DuplicateAssertView;
import gr.uom.*;
import testsmell.AbstractSmell;
import testsmell.ResultsWriter;
import testsmell.TestFile;
import testsmell.TestSmellDetector;

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

public class MethodExtractionCandidateRefactoring extends CandidateRefactoring implements Comparable<MethodExtractionCandidateRefactoring> {
    private MySystem system;
	private MyClass sourceClass;
    private MyClass targetClass;
    private MyMethod sourceMethod;
    private Map<MethodInvocation, MethodDeclaration> additionalMethodsToBeMoved;
    private String movedMethodName;
    private AssertionRouletteVisualizationData visualizationData;
    private Integer userRate;
	private String lineNumber;
	private int beginMethod;
	private int endMethod;
    
    public String getLineNumber() {
		return lineNumber;
	}
	public void setLineNumber(String lineNumber) {
		this.lineNumber = lineNumber;
	}


	public MethodExtractionCandidateRefactoring(MySystem system, MyClass sourceClass, MyClass targetClass, MyMethod sourceMethod, int beginMethod, int endMethod) {

        this.system = system;
    	this.sourceClass = sourceClass;
        this.targetClass = targetClass;
        this.sourceMethod = sourceMethod;
        this.additionalMethodsToBeMoved = new LinkedHashMap<MethodInvocation, MethodDeclaration>();
        this.movedMethodName = sourceMethod.getMethodName();
        this.lineNumber = lineNumber;
        this.beginMethod = beginMethod;
        this.endMethod = endMethod;
       
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
    public MethodExtractionCandidateRefactoring(MySystem system, MyClass sourceClass, MyClass targetClass, MyMethod sourceMethod, String lineNumber, int beginMethod, int endMethod) {
    	
        this.system = system;
    	this.sourceClass = sourceClass;
        this.targetClass = targetClass;
        this.sourceMethod = sourceMethod;
        this.additionalMethodsToBeMoved = new LinkedHashMap<MethodInvocation, MethodDeclaration>();
        this.movedMethodName = sourceMethod.getMethodName();
        this.lineNumber = lineNumber;
        this.beginMethod = beginMethod;
        this.endMethod = endMethod;
        this.visualizationData = new AssertionRouletteVisualizationData(new ClassObject(), new MethodObject(null), new ClassObject());
    }
    private List<MyTestSmells> callAssertionRoulette() throws IOException{
    	List<MyTestSmells> listaTestSmells = new ArrayList<MyTestSmells>();
    	listaTestSmells = new ArrayList<MyTestSmells>();
    	
    	MyTestSmells meuTestSmell = null;
		TestSmellDetector testSmellDetector = TestSmellDetector.createTestSmellDetector(DuplicateAssertView.getMessageDialogTitle());
        BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader("C:\\Users\\raila\\OneDrive\\Documentos\\Workspace\\JDeodorant\\commons-text-master\\commons-text_csv.csv"));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
        String str;

        String[] lineItem;
        TestFile testFile;
        List<TestFile> testFiles = new ArrayList<>();
        while ((str = in.readLine()) != null) {
            lineItem = str.split(",");
            if(lineItem.length ==2){
                testFile = new TestFile(lineItem[0], lineItem[1], "");
            }
            else{
                testFile = new TestFile(lineItem[0], lineItem[1], lineItem[2]);
            }
            testFiles.add(testFile);
        }
        ResultsWriter resultsWriter = ResultsWriter.createResultsWriter();
        List<String> columnNames;
        List<String> columnValues;

        columnNames = testSmellDetector.getTestSmellNames();
        columnNames.add(0, "App");
        columnNames.add(1, "Version");
        columnNames.add(2, "TestFilePath");
        columnNames.add(3, "ProductionFilePath");
        columnNames.add(4, "RelativeTestFilePath");
        columnNames.add(5, "RelativeProductionFilePath");

        resultsWriter.writeColumnName(columnNames);
        TestFile tempFile;
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date;
        for (TestFile file : testFiles) {
            date = new Date();
            System.out.println(dateFormat.format(date) + " Processing: "+file.getTestFilePath());
            System.out.println("Processing: "+file.getTestFilePath());

            //detect smells
            tempFile = testSmellDetector.detectSmells(file, DuplicateAssertView.getMessageDialogTitle());

            //write output
            columnValues = new ArrayList<>();
            columnValues.add(file.getApp());
            columnValues.add(file.getTagName());
            columnValues.add(file.getTestFilePath());
            columnValues.add(file.getProductionFilePath());
            columnValues.add(file.getRelativeTestFilePath());
            columnValues.add(file.getRelativeProductionFilePath());
            for (AbstractSmell smell : tempFile.getTestSmells()) {
                try {
                    
                	JOptionPane.showMessageDialog(null, file.toString());
                	columnValues.add(String.valueOf(smell.getCountSmell(file.toString())));
                }
                catch (NullPointerException e){
                    columnValues.add("");
                }
            }
            resultsWriter.writeLine(columnValues);
        }

        System.out.println("end");
        
		return listaTestSmells;
	}
    //joda

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
    		return true;
    	}
    	else {
    		return false;
    	}
    }

    private boolean isTargetClassAnEnum() {
    	if(targetClass.getClassObject().isEnum()) {
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
    		return false;
    	}
    }

    private boolean oneToManyRelationshipWithTargetClass() {
    	if(sourceMethod.getMethodObject().oneToManyRelationshipWithTargetClass(system.getAssociationsOfClass(sourceClass.getClassObject()), targetClass.getClassObject())) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }

    private boolean overridesMethod() {
    	if(sourceMethod.getMethodObject().overridesMethod()) {
    		return true;
    	}
    	else
    		return false;
    }

    private boolean containsFieldAssignment() {
    	if(!sourceMethod.getMethodObject().getDefinedFieldsThroughThisReference().isEmpty()) {
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
    		return true;
    	}
    	else
    		return false;
    }

    private boolean isSynchronized() {
    	if(sourceMethod.getMethodObject().isSynchronized()) {
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
		return movedMethodName;
	}

	public void setMovedMethodName(String movedMethodName) {
		this.movedMethodName = movedMethodName;
	}

	public String toString() {
        return getSourceEntity() + "->" + getTarget();
    }
	public String getSourceEntity() {
		StringBuilder sb = new StringBuilder();
        sb.append(sourceMethod.getClassOrigin()).append("::");
        sb.append(movedMethodName);
        List<String> parameterList = sourceMethod.getParameterList();
        sb.append("(");
        if(!parameterList.isEmpty()) {
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
        sb.append(movedMethodName);
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

	public int compareTo(MethodExtractionCandidateRefactoring other) {
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
		return beginMethod;
	}
	@Override
	public int getEndMethod() {
		// TODO Auto-generated method stub
		return endMethod;
	}
	@Override
	public String getMethod() {
		//return sourceMethod.toString();
		return sourceMethod.getMethodName();
	}
	
	
}
