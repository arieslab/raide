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


public class VerboseTest extends AbstractSmell {
	
	ArrayList<TestSmellDescription> listTestSmells;
	TestSmellDescription cadaTestSmell;	
	private List<SmellyElement> smellyElementList;
	
	String className;
	String filePath;
	
	private boolean flag = false;
    private ArrayList<MethodUsage> instanceAbstract;
    CompilationUnit testFileCompilationUnit = null;
    public static int MAX_STATEMENTS = 30;

    @Override
	public String getSmellName() {
		return "Verbose Test";
	}
    
 
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}	

	public VerboseTest(String name, String path) {
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
	public List<TestSmellDescription> runAnalysis(CompilationUnit testFileCompilationUnit, 
												  CompilationUnit productionFileCompilationUnit,
												  String testFileName, 
												  String productionFileName) throws FileNotFoundException {
		
		listTestSmells = new ArrayList<TestSmellDescription>();
		instanceAbstract = new ArrayList<> (  );
		VerboseTest.ClassVisitor classVisitor;
		classVisitor = new VerboseTest.ClassVisitor();
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
		private MethodDeclaration currentMethod = null;
        private int verboseCount = 0;
		
		// examine all methods in the test class
        @Override
        public void visit(MethodDeclaration n, Void arg) {
            if (Util.isValidTestMethod(n)) {
                currentMethod = n;

                //method should not be abstract
                if (!currentMethod.isAbstract()) {
                    if (currentMethod.getBody().isPresent()) {
                        //get the total number of statements contained in the method
                        int inicio = currentMethod.getBody().get().getBegin().get().line;
                        int fim = currentMethod.getBody().get().getEnd().get().line;
                        if ((fim-inicio) >= MAX_STATEMENTS) {
                            verboseCount++;
                            instanceAbstract.add ( new MethodUsage (n.getNameAsString(), "",n.getRange().get().begin.line + "-" + n.getRange().get().end.line));
                            insertTestSmell(n.getRange().get(), this.currentMethod);
                        }
                    }
                }

                //reset values for next method
                currentMethod = null;
                verboseCount = 0;
            }
        }
	}
	public void insertTestSmell (Range range, MethodDeclaration testMethod) {
		cadaTestSmell = new TestSmellDescription("Verbose Test", 
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
