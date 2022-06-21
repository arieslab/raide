package org.ufba.raide.java.testsmell.detector.smell;

import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
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
import org.ufba.raide.java.testsmell.TestMethod;
import org.ufba.raide.java.testsmell.TestSmellDescription;
import org.ufba.raide.java.testsmell.Util;

/**
 * "Guess what's wrong?" This smell comes from having a number of assertions in
 * a test method that have no explanation. If one of the assertions fails, you
 * do not know which one it is. A. van Deursen, L. Moonen, A. Bergh, G. Kok,
 * â€œRefactoring Test Codeâ€�, Technical Report, CWI, 2001.
 */


public class ConditionalTestLogic extends AbstractSmell {
	
	ArrayList<TestSmellDescription> listTestSmells;
	TestSmellDescription cadaTestSmell;	
	private List<SmellyElement> smellyElementList;
	private List<MethodUsage> methodConditional;
	
	String className;
	String filePath;
	

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}	

	public ConditionalTestLogic(String name, String path) {
		setClassName(name);
		setFilePath(path);
		smellyElementList = new ArrayList<>();
		methodConditional = new ArrayList<>();
	}
	@Override
	public String getSmellName() {
		return "Conditional Test Logic";
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
		
		ConditionalTestLogic.ClassVisitor classVisitor;
		classVisitor = new ConditionalTestLogic.ClassVisitor();
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
		private MethodDeclaration currentMethod = null;
		private int conditionCount, ifCount, switchCount, forCount, foreachCount, whileCount, doCount = 0;
		TestMethod testMethod;

		@Override
		public void visit(MethodDeclaration n, Void arg) {
			
			if (Util.isValidTestMethod(n)) {
				currentMethod = n;
				testMethod = new TestMethod(n.getNameAsString());
				testMethod.setHasSmell(false); 
				super.visit(n, arg);
				
				testMethod.setHasSmell(true);

				currentMethod = null;
                conditionCount = 0;
                ifCount = 0;
                switchCount = 0;
                forCount = 0;
                foreachCount = 0;
                whileCount = 0;
                doCount = 0;
			}
		}
	

		@Override
		public void visit(IfStmt n, Void arg) {
			super.visit(n, arg);
			if (currentMethod != null) {
			    ifCount++;
			    methodConditional.add(new MethodUsage(currentMethod.getNameAsString(), "", n.getRange().get().begin.line + "-" + n.getRange().get().end.line));
			    insertTestSmell(n.getRange().get(), this.testMethod);
			}
		}
		
		@Override
		public void visit(SwitchStmt n, Void arg) {
		    super.visit(n, arg);
		    if (currentMethod != null) {
		        switchCount++;
		        methodConditional.add(new MethodUsage(currentMethod.getNameAsString(), "",n.getRange().get().begin.line + "-" + n.getRange().get().end.line));
		        insertTestSmell(n.getRange().get(), this.testMethod);
		    }
		}
		
		@Override
		public void visit(ConditionalExpr n, Void arg) {
		    super.visit(n, arg);
		    if (currentMethod != null) {
		        conditionCount++;
		        methodConditional.add(new MethodUsage(currentMethod.getNameAsString(), "",n.getRange().get().begin.line + "-" + n.getRange().get().end.line));
		        insertTestSmell(n.getRange().get(), this.testMethod);
		    }
		}
		
		@Override
		public void visit(ForStmt n, Void arg) {
		    super.visit(n, arg);
		    if (currentMethod != null) {
		        forCount++;
		        methodConditional.add(new MethodUsage(currentMethod.getNameAsString(), "", n.getRange().get().begin.line + "-" + n.getRange().get().end.line));
		        insertTestSmell(n.getRange().get(), this.testMethod);
		    }
		}
		
		@Override
		public void visit(ForeachStmt n, Void arg) {
		    super.visit(n, arg);
		    if (currentMethod != null) {
		        foreachCount++;
				methodConditional.add(new MethodUsage(currentMethod.getNameAsString(), "", n.getRange().get().begin.line + "-" + n.getRange().get().end.line));
				insertTestSmell(n.getRange().get(), this.testMethod);
		    }
		}
		
		@Override
		public void visit(WhileStmt n, Void arg) {
		    super.visit(n, arg);
		    if (currentMethod != null) {
		        whileCount++;
		        methodConditional.add(new MethodUsage(currentMethod.getNameAsString(), "", n.getRange().get().begin.line + "-" + n.getRange().get().end.line));
		        insertTestSmell(n.getRange().get(), this.testMethod);
		    }
		}
		
		@Override
		public void visit(DoStmt n, Void arg) {
		    super.visit(n, arg);
		    if (currentMethod != null) {
		        doCount++;
		        methodConditional.add(new MethodUsage(currentMethod.getNameAsString(), "", n.getRange().get().begin.line + "-" + n.getRange().get().end.line));
		        insertTestSmell(n.getRange().get(), this.testMethod);
		    }
		}
		

	}
	public void insertTestSmell (Range range, TestMethod testMethod) {
		cadaTestSmell = new TestSmellDescription("Conditional Test Logic", 
												 "Assertion Explanation", 
				 								 getFilePath(), 
				 								 getClassName(),
				 								 testMethod.getElementName() + "() \n" ,
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
//						"M�todo " + testMethod.getElementName() + "() \n" + 
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
