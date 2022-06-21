package org.ufba.raide.java.testsmell.detector.smell;

import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

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


public class RedundantAssertion extends AbstractSmell {
	
	ArrayList<TestSmellDescription> listTestSmells;
	TestSmellDescription cadaTestSmell;	
	private List<SmellyElement> smellyElementList;
	ArrayList<MethodUsage> methodPrints = null;
	
	String className;
	String filePath;
	

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}	

	public RedundantAssertion(String name, String path) {
		setClassName(name);
		setFilePath(path);
		smellyElementList = new ArrayList<>();
	}

	/**
	 * Checks of 'Assertion Roulette' smell
	 */
	@Override
	public String getSmellName() {
		return "Redundant Assertion";
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
		//JOptionPane.showMessageDialog(null, "Nome da classe: " + getClassName());
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
		methodPrints = new ArrayList<MethodUsage>();
		RedundantAssertion.ClassVisitor classVisitor;
		classVisitor = new RedundantAssertion.ClassVisitor();
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
		TestMethod testMethod;
		private int redundantCount = 0;

		// examine all methods in the test class
		@Override
		public void visit(MethodDeclaration n, Void arg) {
			
			if (Util.isValidTestMethod(n)) {
				currentMethod = n;
				testMethod = new TestMethod(n.getNameAsString());
				testMethod.setHasSmell(false); // default value is false (i.e. no smell)
				super.visit(n, arg);
				smellyElementList.add(testMethod);

				// reset values for next method
				currentMethod = null;
				redundantCount = 0;
			}
		}
		
		@Override
        public void visit(MethodCallExpr n, Void arg) {
            String argumentValue = null;

            super.visit(n, arg);
            if (currentMethod != null) {
                switch (n.getNameAsString()) {
                    case "assertTrue":
                    case "assertFalse":
                        if (n.getArguments().size() == 1 && n.getArgument(0) instanceof BooleanLiteralExpr) { // assertTrue(boolean condition) or assertFalse(boolean condition)
                            argumentValue = Boolean.toString(((BooleanLiteralExpr) n.getArgument(0)).getValue());
                        } else if (n.getArguments().size() == 2 && n.getArgument(1) instanceof BooleanLiteralExpr) { // assertTrue(java.lang.String message, boolean condition)  or assertFalse(java.lang.String message, boolean condition)
                            argumentValue = Boolean.toString(((BooleanLiteralExpr) n.getArgument(1)).getValue());
                        }

                        if (argumentValue != null && (argumentValue.toLowerCase().equals("true") || argumentValue.toLowerCase().equals("false"))) {
                            redundantCount++;
                            methodPrints.add(new MethodUsage(currentMethod.getNameAsString(), "",n.getRange().get().begin.line + "-" + n.getRange().get().end.line));
                            insertTestSmell(n.getRange().get(), this.currentMethod);
                        }
                    break;

                    case "assertNotNull":
                    case "assertNull":
                        if (n.getArguments().size() == 1 && n.getArgument(0) instanceof NullLiteralExpr) { // assertNotNull(java.lang.Object object) or assertNull(java.lang.Object object)
                            argumentValue = (((NullLiteralExpr) n.getArgument(0)).toString());
                        } else if (n.getArguments().size() == 2 && n.getArgument(1) instanceof NullLiteralExpr) { // assertNotNull(java.lang.String message, java.lang.Object object) or assertNull(java.lang.String message, java.lang.Object object)
                            argumentValue = (((NullLiteralExpr) n.getArgument(1)).toString());
                        }

                        if (argumentValue != null && (argumentValue.toLowerCase().equals("null"))) {
                            redundantCount++;
                            methodPrints.add(new MethodUsage(currentMethod.getNameAsString(), "",n.getRange().get().begin.line + "-" + n.getRange().get().end.line));
                            insertTestSmell(n.getRange().get(), this.currentMethod);
                        }
                    break;

                    default:
                        if (n.getNameAsString().startsWith("assert")) {
                            if (n.getArguments().size() == 2) { //e.g. assertArrayEquals(byte[] expecteds, byte[] actuals); assertEquals(long expected, long actual);
                                if (n.getArgument(0).equals(n.getArgument(1))) {
                                    redundantCount++;
                                    methodPrints.add(new MethodUsage(currentMethod.getNameAsString(), "",n.getRange().get().begin.line + "-" + n.getRange().get().end.line));
                                    insertTestSmell(n.getRange().get(), this.currentMethod);
                                }
                            }
                            if (n.getArguments().size() == 3) { //e.g. assertArrayEquals(java.lang.String message, byte[] expecteds, byte[] actuals); assertEquals(java.lang.String message, long expected, long actual)
                                if (n.getArgument(1).equals(n.getArgument(2))) {
                                    redundantCount++;
                                    methodPrints.add(new MethodUsage(currentMethod.getNameAsString(), "",n.getRange().get().begin.line + "-" + n.getRange().get().end.line));
                                    insertTestSmell(n.getRange().get(), this.currentMethod);
                                }
                            }
                        }
                    break;
                }
            }
        }
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
