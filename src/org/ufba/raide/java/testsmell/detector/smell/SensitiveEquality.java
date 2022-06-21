package org.ufba.raide.java.testsmell.detector.smell;

import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import com.github.javaparser.ast.expr.AnnotationExpr;
import java.awt.Container;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

import org.ufba.raide.java.refactoring.views.*;
import org.ufba.raide.java.testsmell.AbstractSmell;
import org.ufba.raide.java.testsmell.SmellyElement;
import org.ufba.raide.java.testsmell.TestMethod;
import org.ufba.raide.java.testsmell.TestSmellDescription;
import org.ufba.raide.java.testsmell.Util;

/**
 * "Guess what's wrong?" This smell comes from having a number of assertions in
 * a test method that have no explanation. If one of the assertions fails, you
 * do not know which one it is. A. van Deursen, L. Moonen, A. Bergh, G. Kok,
 * â€œRefactoring Test Codeâ€�, Technical Report, CWI, 2001.
 */


public class SensitiveEquality extends AbstractSmell {
	
	ArrayList<TestSmellDescription> listTestSmells;
	TestSmellDescription cadaTestSmell;	
	private List<SmellyElement> smellyElementList;
	private ArrayList<MethodUsage> methodSensitiveEquality;
	
	String className;
	String filePath;
	private ArrayList<MethodUsage> instances;
	private List<MethodUsage> methodPrints;
	
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}	

	public SensitiveEquality(String name, String path) {
		setClassName(name);
		setFilePath(path);
		smellyElementList = new ArrayList<>();
	}
	@Override
	public String getSmellName() {
		return "Sensitive Equality";
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
	public List<TestSmellDescription> runAnalysis(CompilationUnit testFileCompilationUnit, 
												  CompilationUnit productionFileCompilationUnit,
												  String testFileName, 
												  String productionFileName) throws FileNotFoundException {
		listTestSmells = new ArrayList<TestSmellDescription>();
		instances = new ArrayList<>();
		methodSensitiveEquality = new ArrayList<MethodUsage>();
		
		
		SensitiveEquality.ClassVisitor classVisitor;
		classVisitor = new SensitiveEquality.ClassVisitor();
		classVisitor.visit(testFileCompilationUnit, null);
		
		
		for (MethodUsage method : instances) {
            TestMethod testClass = new TestMethod(method.getTestMethodName());
            testClass.setRange(method.getRange());
//            testClass.addDataItem("begin", method.getLine());
//            testClass.addDataItem("end", method.getLine()); // [Remover]
            testClass.setHasSmell(true);
            smellyElementList.add(testClass);
        }
		
		return listTestSmells;
	}


	/**
	 * Returns the set of analyzed elements (i.e. test methods)
	 */
	@Override
	public List<SmellyElement> getSmellyElements() {
		return smellyElementList;
	}

	private class ClassVisitor extends VoidVisitorAdapter<Void> {
		
		private MethodDeclaration currentMethod = null;
		private int sensitiveCount = 0;

		@Override
        public void visit(MethodDeclaration n, Void arg) {
            if (Util.isValidTestMethod(n)) {
                currentMethod = n;
                super.visit(n, arg);

                //reset values for next method
                currentMethod = null;
                sensitiveCount = 0;
            }
        }

        // examine the methods being called within the test method
        @Override
        public void visit(MethodCallExpr n, Void arg) {
            super.visit(n, arg);
            if (currentMethod != null) {
                // if the name of a method being called start with 'assert'
                if (n.getNameAsString().startsWith(("assert"))) {
                    // assert methods that contain toString
                    for (Expression argument : n.getArguments()) {
                        if (argument.toString().contains("toString")) {
                            sensitiveCount++;
                            methodSensitiveEquality.add(new MethodUsage(currentMethod.getNameAsString(), "",n.getRange().get().begin.line + "-" + n.getRange().get().end.line));
                            insertTestSmell(n.getRange().get(), currentMethod);
                        }
                    }
                }
                // if the name of a method being called is 'fail' \/ added validation to jUnit3 fail cases
                else if (n.getNameAsString().equals("fail") || n.getNameAsString().equals("failNotEquals") ||
                		 n.getNameAsString().equals("failSame") || n.getNameAsString().equals("failNotSame"))  {
                    // fail methods that contain toString
                    for (Expression argument : n.getArguments()) {
                        if (argument.toString().contains("toString")) {
                            sensitiveCount++;
                            methodSensitiveEquality.add(new MethodUsage(currentMethod.getNameAsString(), "",n.getRange().get().begin.line + "-" + n.getRange().get().end.line));
                            insertTestSmell(n.getRange().get(), currentMethod);
                        }
                    }
                }
            }
        }
        
    }

//		insertTestSmell(n.getRange().get(), this.testMethod);

	public void insertTestSmell (Range range, MethodDeclaration testMethod) {
		cadaTestSmell = new TestSmellDescription("Ignored Test", 
				 "....", 
				 getFilePath(), 
				 getClassName(),
				 testMethod.getName() + "() \n" ,
				 range.begin.line + "", 
				 range.end.line + "", 
				 range.begin.line, 
				 range.end.line,
				 "",
				 null,
				 null);	
		listTestSmells.add(cadaTestSmell);
		
		String smellLocation;
		smellLocation = "Classe " + getClassName() + "\n" + 
		"M�todo " + testMethod.getName() + "() \n" + 
		"Begin " + range.begin.line + "\n" +
		"End " + range.end.line;
		System.out.println(smellLocation);
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
