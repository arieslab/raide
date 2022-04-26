package org.ufba.raide.java.testsmell.detector.smell;

import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.awt.Container;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.swing.JOptionPane;

import org.ufba.raide.java.refactoring.views.*;
import org.ufba.raide.java.testsmell.AbstractSmell;
import org.ufba.raide.java.testsmell.SmellyElement;
import org.ufba.raide.java.testsmell.TestClass;
import org.ufba.raide.java.testsmell.TestMethod;
import org.ufba.raide.java.testsmell.TestSmellDescription;
import org.ufba.raide.java.testsmell.Util;


public class GeneralFixture extends AbstractSmell {
	
	ArrayList<TestSmellDescription> listTestSmells;
	TestSmellDescription cadaTestSmell;	
	private List<SmellyElement> smellyElementList;
	String className;
	String filePath;
	
	
	List<MethodDeclaration> methodList;
    MethodDeclaration setupMethod;
    List<FieldDeclaration> fieldList;
    List<MethodUsage> setupFields;
    TestMethod testMethod;
    private List<MethodUsage> instaceGeneral;
	
	private boolean flag = false;
    private ArrayList<MethodUsage> instanceIgnored;

    @Override
	public String getSmellName() {
		return "General Fixture";
	}
    
 
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}	

	public GeneralFixture(String name, String path) {
		setClassName(name);
		setFilePath(path);
		smellyElementList = new ArrayList<>();
	}

	/**
	 * Returns true if any of the elements has a smell
	 */
	@Override
	public boolean getHasSmell() {
		return smellyElementList.stream().filter(x -> x.getHasSmell()).count() >= 1;
	}

	@Override
	public long getCountSmell(String name) {
		setClassName(name);
		JOptionPane.showMessageDialog(null, "Nome da classe: " + getClassName());
		return smellyElementList.stream().filter(x -> x.getHasSmell()).count();
	}

	/**
	 * Analyze the test file for test methods for multiple assert statements without
	 * an explanation/message
	 * @return 
	 */
	@Override
    public List<TestSmellDescription> runAnalysis(CompilationUnit testFileCompilationUnit, CompilationUnit productionFileCompilationUnit, String testFileName, String productionFileName) throws FileNotFoundException {
		GeneralFixture.ClassVisitor classVisitor = new GeneralFixture.ClassVisitor();
        classVisitor.visit(testFileCompilationUnit, null); //This call will populate the list of test methods and identify the setup method [visit(ClassOrInterfaceDeclaration n)]

        listTestSmells = new ArrayList<TestSmellDescription>();
		
        //Proceed with general fixture analysis if setup method exists
        if (setupMethod != null) {
            //Get all fields that are initialized in the setup method
            //The following code block will identify the class level variables (i.e. fields) that are initialized in the setup method
            // TODO: There has to be a better way to do this identification/check!
            Optional<BlockStmt> blockStmt = setupMethod.getBody();
            NodeList nodeList = blockStmt.get().getStatements();
            for (int i = 0; i < nodeList.size(); i++) {
                for (int j = 0; j < fieldList.size(); j++) {
                    for (int k = 0; k < fieldList.get(j).getVariables().size(); k++) {
                        if (nodeList.get(i) instanceof ExpressionStmt) {
                            ExpressionStmt expressionStmt = (ExpressionStmt) nodeList.get(i);
                            if (expressionStmt.getExpression() instanceof AssignExpr) {
                                AssignExpr assignExpr = (AssignExpr) expressionStmt.getExpression();
                                if (fieldList.get(j).getVariable(k).getNameAsString().equals(assignExpr.getTarget().toString())) {
                                    setupFields.add(new MethodUsage(setupMethod.getNameAsString(),
                                            assignExpr.getTarget().toString(),
                                            String.valueOf(assignExpr.getRange().get().begin.line)
                                            ));
                                }
                            }
                        }
                    }
                }
            }
        }

        for (MethodDeclaration method : methodList) {
            //This call will visit each test method to identify the list of variables the method contains [visit(MethodDeclaration n)]
            classVisitor.visit(method, null);
        }

        for (MethodUsage method : instaceGeneral) {
            TestMethod testClass = new TestMethod(method.getTestMethodName());
            testClass.setRange(method.getRange());
            testClass.setHasSmell(true);
            smellyElementList.add(testClass);
        }
        return listTestSmells;
    }
	private class ClassVisitor extends VoidVisitorAdapter<Void> {
        private MethodDeclaration methodDeclaration = null;
        private MethodDeclaration currentMethod = null;
        private Set<String> fixtureCount = new HashSet<>();

        @Override
        public void visit(ClassOrInterfaceDeclaration n, Void arg) {
        	methodList = new ArrayList<>();
            fieldList = new ArrayList<>();
            setupFields = new ArrayList<>();
            instaceGeneral = new ArrayList<>();
            
            NodeList<BodyDeclaration<?>> members = n.getMembers();
            for (int i = 0; i < members.size(); i++) {
                if (members.get(i) instanceof MethodDeclaration) {
                    methodDeclaration = (MethodDeclaration) members.get(i);

                    //Get a list of all test methods
                    if (Util.isValidTestMethod(methodDeclaration)) {
                        methodList.add(methodDeclaration);
                    }

                    //Get the setup method
                    if (Util.isValidSetupMethod(methodDeclaration)) {
                        //It should have a body
                        if (methodDeclaration.getBody().isPresent()) {
                            setupMethod = methodDeclaration;
                        }
                    }
                }

                //Get all fields in the class
                if (members.get(i) instanceof FieldDeclaration) {
                    fieldList.add((FieldDeclaration) members.get(i));
                }
            }
        }

        // examine all methods in the test class
        @Override
        public void visit(MethodDeclaration n, Void arg) {
            if (Util.isValidTestMethod(n)) {
                currentMethod = n;

                //call visit(NameExpr) for current method
                super.visit(n, arg);

                // verify against the setup fields and set to the testmehtod
                for (MethodUsage field : setupFields){
                    if (!fixtureCount.contains(field.getProductionMethodName())) {
                        if (!instaceGeneral.contains(field)) {
                            instaceGeneral.add(field);
                            
                            Position position = new Position(Integer. parseInt(field.getRange()), 0);
                            Range rangeAux = new Range(position, position);

                            insertTestSmell(rangeAux, n, field);
                        }
                    }
                }
                fixtureCount = new HashSet();
                currentMethod = null;
            }
        }

        @Override
        public void visit(NameExpr n, Void arg) {
            if (currentMethod != null) {
               for (MethodUsage field : setupFields) {
                    if (field.getProductionMethodName().equals(n.getNameAsString())) {
                        if(!fixtureCount.contains(n.getNameAsString())){
                            fixtureCount.add(n.getNameAsString());
                            return;
                        }
                    }
                }
            }
            super.visit(n, arg);
        }
    }


	/**
	 * Returns the set of analyzed elements (i.e. test methods)
	 */
	@Override
	public List<SmellyElement> getSmellyElements() {
		return smellyElementList;
	}

	
	private String getNameFileWithoutExtension() {
		return className.replace(".java", "");
	}
	
	public void insertTestSmell (Range range, MethodDeclaration testMethod, MethodUsage field) {
		cadaTestSmell = new TestSmellDescription("General Fixture", 
												 "....", 
				 								 getFilePath(), 
				 								 getClassName(),
				 								 field.getTestMethodName() + "()\n",
				 								 range.begin.line + "", 
				 								 range.end.line + "", 
				 								 range.begin.line, 
				 								 range.end.line,
				 								 field.getProductionMethodName());	
		listTestSmells.add(cadaTestSmell);
//		String smellLocation;
//		smellLocation = "Classe " + getClassName() + "\n" + 
//						"Método " + testMethod.getName() + "() \n" + 
//						"Begin " + range.begin.line + "\n" +
//						"End " + range.end.line;
//		System.out.println(smellLocation);
	}
	
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	@Override
	public int contSmell() {
		// TODO Auto-generated method stub
		return 0;
	}
}
