package org.ufba.raide.java.testsmell.detector.smell;

import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.Expression;
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


public class ResourceOptimism extends AbstractSmell {
	
	ArrayList<TestSmellDescription> listTestSmells;
	TestSmellDescription cadaTestSmell;	
	private List<SmellyElement> smellyElementList;
	private ArrayList<MethodUsage> instanceResource;
	
	String className;
	String filePath;
	private ArrayList<MethodUsage> instances;
	
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}	

	public ResourceOptimism(String name, String path) {
		setClassName(name);
		setFilePath(path);
		smellyElementList = new ArrayList<>();
	}
	@Override
	public String getSmellName() {
		return "Mystery Guest";
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
		instanceResource = new ArrayList<> ();
		listTestSmells = new ArrayList<TestSmellDescription>();
		instances = new ArrayList<>();
		ResourceOptimism.ClassVisitor classVisitor;
		classVisitor = new ResourceOptimism.ClassVisitor();
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
        private int resourceOptimismCount = 0;
        private boolean hasSmell = false;
        private List<String> methodVariables = new ArrayList<>();
        private List<String> classVariables = new ArrayList<>();
		

		@Override
        public void visit(MethodDeclaration n, Void arg) {
            if (Util.isValidTestMethod(n) || Util.isValidSetupMethod(n)) {
                currentMethod = n;
                super.visit(n, arg);

                if(methodVariables.size() >= 1 || hasSmell==true){
                    instanceResource.add(new MethodUsage (n.getNameAsString ( ), "",n.getRange().get().begin.line + "-" + n.getRange().get().end.line));
            		insertTestSmell(n.getRange().get(), currentMethod);
                }

                //reset values for next method
                currentMethod = null;
                resourceOptimismCount = 0;
                hasSmell = false;
                methodVariables = new ArrayList<>();
            }
        }

        @Override
        public void visit(VariableDeclarationExpr n, Void arg) {
            if (currentMethod != null) {
                for (VariableDeclarator variableDeclarator : n.getVariables()) {
                    if (variableDeclarator.getType().equals("File")) {
                        methodVariables.add(variableDeclarator.getNameAsString());
                    }
                }
            }
            super.visit(n, arg);
        }

        @Override
        public void visit(ObjectCreationExpr n, Void arg) {
            if (currentMethod != null) {
                if (n.getParentNode().isPresent()) {
                    if (!(n.getParentNode().get() instanceof VariableDeclarator)) { // VariableDeclarator is handled in the override method
                        if (n.getType().asString().equals("File")) {
                            hasSmell = true;
                        }
                    }
                }
            }
            super.visit(n, arg);
        }

        @Override
        public void visit(VariableDeclarator n, Void arg) {
            if (currentMethod != null) {
                if (n.getType().asString().equals("File")) {
                    methodVariables.add(n.getNameAsString());
                }
            } else {
                if (n.getType().asString().equals("File")) {
                    classVariables.add(n.getNameAsString());
                }
            }
            super.visit(n, arg);
        }

        @Override
        public void visit(FieldDeclaration n, Void arg) {
            for (VariableDeclarator variableDeclarator : n.getVariables()) {
                if (variableDeclarator.getType().equals("File")) {
                    classVariables.add(variableDeclarator.getNameAsString());
                }
            }
            super.visit(n, arg);
        }


        @Override
        public void visit(MethodCallExpr n, Void arg) {
            super.visit(n, arg);
            if (currentMethod != null) {
                if (n.getNameAsString().equals("exists") ||
                        n.getNameAsString().equals("isFile") ||
                        n.getNameAsString().equals("notExists")) {
                    if (n.getScope().isPresent()) {
                        if(n.getScope().get() instanceof NameExpr) {
                            if (methodVariables.contains(((NameExpr) n.getScope().get()).getNameAsString())) {
                                methodVariables.remove(((NameExpr) n.getScope().get()).getNameAsString());
                            }
                        }
                    }
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
		
//		String smellLocation;
//		smellLocation = "Classe " + getClassName() + "\n" + 
//		"M�todo " + testMethod.getName() + "() \n" + 
//		"Begin " + range.begin.line + "\n" +
//		"End " + range.end.line;
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
