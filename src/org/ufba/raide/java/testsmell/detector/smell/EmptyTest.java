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

import org.eclipse.jface.text.Position;
import org.ufba.raide.java.refactoring.views.*;
import org.ufba.raide.java.testsmell.AbstractSmell;
import org.ufba.raide.java.testsmell.SmellyElement;
import org.ufba.raide.java.testsmell.TestClass;
import org.ufba.raide.java.testsmell.TestMethod;
import org.ufba.raide.java.testsmell.TestSmellDescription;
import org.ufba.raide.java.testsmell.Util;

/**
 * "Guess what's wrong?" This smell comes from having a number of assertions in
 * a test method that have no explanation. If one of the assertions fails, you
 * do not know which one it is. A. van Deursen, L. Moonen, A. Bergh, G. Kok,
 * â€œRefactoring Test Codeâ€�, Technical Report, CWI, 2001.
 */


public class EmptyTest extends AbstractSmell {
	
	private ArrayList<MethodUsage> instanceEmpty;
	
	private boolean flag = false;
	
	ArrayList<TestSmellDescription> listTestSmells;
	TestSmellDescription cadaTestSmell;	
	private List<SmellyElement> smellyElementList;
	
	String className;
	String filePath;
	

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}	

	public EmptyTest(String name, String path) {
		setClassName(name);
		setFilePath(path);
		smellyElementList = new ArrayList<>();
	}
	@Override
	public String getSmellName() {
		return "Empty Test";
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
		
		EmptyTest.ClassVisitor classVisitor;
		classVisitor = new EmptyTest.ClassVisitor();
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
			
			if (Util.isValidTestMethod(n)) {
				//method should not be abstract
                if (!n.isAbstract()) {
                    if (n.getBody().isPresent()) {
                        //get the total number of statements contained in the method
                        if (n.getBody().get().getStatements().size() == 0) {
//                            instanceEmpty.add(new MethodUsage(n.getNameAsString(),"",n.getRange().get().begin.line + "-" + n.getRange().get().end.line));
                        	insertTestSmell(n);
                            return;
                        }
                    }
                }
			}
		}

	}
	public void insertTestSmell (MethodDeclaration testMethod) {
		cadaTestSmell = new TestSmellDescription("Empty Test", 
												 "Remove Method", 
				 								 getFilePath(), 
				 								 getClassName(),
				 								 testMethod.getName() + "() \n" ,
				 								 testMethod.getRange().get().begin.line + "", 
				 								 testMethod.getRange().get().end.line + "", 
				 								 testMethod.getRange().get().begin.line, 
				 								 testMethod.getRange().get().end.line,
												 "",
												 testMethod.getRange().get());	
		listTestSmells.add(cadaTestSmell);
		String smellLocation;
		smellLocation = "Classe " + getClassName() + "\n" + 
						"M�todo " + testMethod.getName() + "() \n" + 
						"Begin " + testMethod.getRange().get().begin.line + "\n" +
						"End " + testMethod.getRange().get().end.line;
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
