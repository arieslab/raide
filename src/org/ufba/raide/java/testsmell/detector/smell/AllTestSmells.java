package org.ufba.raide.java.testsmell.detector.smell;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
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


public class AllTestSmells extends AbstractSmell {
	
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

	public AllTestSmells(String name, String path) {
		setClassName(name);
		setFilePath(path);
		smellyElementList = new ArrayList<>();
	}

	/**
	 * Checks of 'Assertion Roulette' smell
	 */
	@Override
	public String getSmellName() {
		return "Assertion Roulette";
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
		
		AllTestSmells.ClassVisitor classVisitor;
		classVisitor = new AllTestSmells.ClassVisitor();
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
		private int assertNoMessageCount = 0;
		private int assertCount = 0;
		TestMethod testMethod;

		// examine all methods in the test class
		@Override
		public void visit(MethodDeclaration n, Void arg) {
			
			if (Util.isValidTestMethod(n)) {
				currentMethod = n;
				testMethod = new TestMethod(n.getNameAsString());
				testMethod.setHasSmell(false); // default value is false (i.e. no smell)
				super.visit(n, arg);

				// if there is only 1 assert statement in the method, then a explanation message
				// is not needed
				if (assertCount == 1)
					testMethod.setHasSmell(false);
				else if (assertNoMessageCount >= 1) // if there is more than one assert statement, then all the asserts
													// need to have an explanation message
					testMethod.setHasSmell(true);

				testMethod.addDataItem("AssertCount", String.valueOf(assertNoMessageCount));

				smellyElementList.add(testMethod);

				// reset values for next method
				currentMethod = null;
				assertCount = 0;
				assertNoMessageCount = 0;
			}
		}
		
		public boolean explanationIsEmpty(String str) {
			boolean resultado = false;
			
			char[] ch = str.toCharArray();   
			String strFinal = "";
			
			//Remove todos os espa�os 
			for(int i = 0; i < ch.length; i++ ){    
				if (ch[i] != ' ') {
					strFinal += ch[i]; 
				}	
			}
			if (strFinal.equals("\"\""))
				resultado = true;			
			
			return resultado;
			
		}
		

		// examine the methods being called within the test method
		@Override
		public void visit(MethodCallExpr n, Void arg) {
			
			boolean flag = false;
			super.visit(n, arg);
			if (currentMethod != null) {
				// if the name of a method being called is an assertion and has 3 parameters
				if (n.getNameAsString().startsWith(("assertArrayEquals"))
						|| n.getNameAsString().startsWith(("assertEquals"))
						|| n.getNameAsString().startsWith(("assertNotSame"))
						|| n.getNameAsString().startsWith(("assertSame"))
						|| n.getNameAsString().startsWith(("assertThat"))) {
					assertCount++;
					// assert methods that do not contain a message

					if (n.getArguments().size() < 3 || (explanationIsEmpty(n.getArgument(0).toString()))) {
						assertNoMessageCount++;
						flag = true;
					}
				}
				// if the name of a method being called is an assertion and has 2 parameters
				else if (n.getNameAsString().equals("assertFalse") || n.getNameAsString().equals("assertNotNull")
						|| n.getNameAsString().equals("assertNull") || n.getNameAsString().equals("assertTrue")) {
					assertCount++;
					
					// assert methods that do not contain a message
					if ((n.getArguments().size() < 2) || (explanationIsEmpty(n.getArgument(0).toString())) ) {
						assertNoMessageCount++;
						flag = true;
					}
				}
				//workspace
				// if the name of a method being called is 'fail'
				else if (n.getNameAsString().equals("fail")) {
					assertCount++;
					// fail method does not contain a message
					if (n.getArguments().size() < 1 || (explanationIsEmpty(n.getArgument(0).toString()))) {
						assertNoMessageCount++;
						flag = true;
					}
				}
				if (flag) {
					// JOptionPane.showMessageDialog(null, "O método " +
					// this.testMethod.getElementName() + "() apresenta smell");
					
					cadaTestSmell = new TestSmellDescription("Assertion Roulette", 
															 "Assertion Explanation", 
															 getFilePath(), getClassName(),
															 this.testMethod.getElementName() + "() \n" , 
															 n.getRange().get().begin.line + "", 
															 n.getRange().get().begin.line + "", 
															 n.getRange().get().begin.line, 
															 n.getRange().get().end.line,
															 "");	
					listTestSmells.add(cadaTestSmell);
					String smellLocation;
					smellLocation = "Classe " + getClassName() + "\n" +
							        "Método " + this.testMethod.getElementName() + "() \n" +
							        "Linha " + n.getRange().get().begin.line;
					//JOptionPane.showMessageDialog(null, smellLocation);
					

				}

			}
		}

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
