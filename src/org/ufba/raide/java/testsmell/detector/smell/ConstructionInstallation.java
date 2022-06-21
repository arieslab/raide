package org.ufba.raide.java.testsmell.detector.smell;

import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.awt.Container;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.ufba.raide.java.refactoring.views.*;
import org.ufba.raide.java.testsmell.AbstractSmell;
import org.ufba.raide.java.testsmell.SmellyElement;
import org.ufba.raide.java.testsmell.TestClass;
import org.ufba.raide.java.testsmell.TestMethod;
import org.ufba.raide.java.testsmell.TestSmellDescription;
import org.ufba.raide.java.testsmell.Util;


public class ConstructionInstallation extends AbstractSmell {
	
	ArrayList<TestSmellDescription> listTestSmells;
	TestSmellDescription cadaTestSmell;	
	private List<SmellyElement> smellyElementList;
	private List<MethodUsage> methodConditional;
	
	String className;
	String filePath;
	
	private boolean flag = false;
    private ArrayList<MethodUsage> instanceIgnored;

    @Override
	public String getSmellName() {
		return "Construction Installation";
	}
    
 
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}	

	public ConstructionInstallation(String name, String path) {
		setClassName(name);
		setFilePath(path);
		smellyElementList = new ArrayList<>();
		methodConditional = new ArrayList<>();
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
		
		ConstructionInstallation.ClassVisitor classVisitor;
		classVisitor = new ConstructionInstallation.ClassVisitor();
		classVisitor.visit(testFileCompilationUnit, null);
		
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
		TestClass testClass;
		
        @Override
        public void visit(MethodDeclaration n, Void arg) {

            //JUnit 4
            //check if test method has Ignore annotation
            if (n.getAnnotationByName("Test").isPresent()) {
                if (n.getAnnotationByName("Ignore").isPresent() || flag) {		
                	return;
                }
            }
            if (n.getName().asString().equals(getNameFileWithoutExtension())) {
            	insertTestSmell(n.getRange().get(), n);
            	return;
            }
        }
        
        @Override
        public void visit(ClassOrInterfaceDeclaration n, Void arg) {
            if (n.getAnnotationByName("Ignore").isPresent()) {
                testClass = new TestClass(n.getNameAsString());
                flag = true;
            }
            super.visit(n, arg);
        }
	}
	private String getNameFileWithoutExtension() {
		return className.replace(".java", "");
	}
	
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
												 null);	
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
