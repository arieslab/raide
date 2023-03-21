package org.ufba.raide.java.testsmell.detector.smell;

import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

import org.ufba.raide.java.refactoring.views.*;
import org.ufba.raide.java.testsmell.AbstractSmell;
import org.ufba.raide.java.testsmell.SmellyElement;
import org.ufba.raide.java.testsmell.TestClass;
import org.ufba.raide.java.testsmell.TestMethod;
import org.ufba.raide.java.testsmell.TestSmellDescription;
import org.ufba.raide.java.testsmell.Util;


public class LazyTest extends AbstractSmell {
	
	ArrayList<TestSmellDescription> listTestSmells;
	TestSmellDescription cadaTestSmell;	
	private List<SmellyElement> smellyElementList;
	
	String className;
	String filePath;
	
	
	private static final String TEST_FILE = "Test";
    private static final String PRODUCTION_FILE = "Production";
    private String productionClassName;
    private List<MethodDeclaration> productionMethods;
    private List<ConstructorDeclaration> constructorMethods;
    private ArrayList<MethodUsage> instanceLazy;
    private HashMap<String,ArrayList<String>> calledMethodsLine = new HashMap<>();
    private HashMap<String,ArrayList<String>> calledMethodsName = new HashMap<>();

    @Override
	public String getSmellName() {
		return "Lazy Test";
	}
    
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}	

	public LazyTest(String name, String path) {
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
		
		productionMethods = new ArrayList<>();
        constructorMethods = new ArrayList<>();
        instanceLazy = new ArrayList<>();
        
        LazyTest.ClassVisitor classVisitor;
		classVisitor = new LazyTest.ClassVisitor("");
		classVisitor.visit(testFileCompilationUnit, null);
		
//		if (productionFileCompilationUnit == null)
//            throw new FileNotFoundException();
//
//        classVisitor = new LazyTest.ClassVisitor(PRODUCTION_FILE);
//        classVisitor.visit(productionFileCompilationUnit, null);
//
//        classVisitor = new LazyTest.ClassVisitor(TEST_FILE);
//        classVisitor.visit(testFileCompilationUnit, null);
//
//        for (MethodUsage method : instanceEager) {
//            TestMethod testClass = new TestMethod(method.getTestMethodName());
//            testClass.setRange(method.getRange());
//            testClass.setHasSmell(true);
//            smellyElementList.add(testClass);
//        }
		if (productionFileCompilationUnit == null)
            throw new FileNotFoundException();

        classVisitor = new LazyTest.ClassVisitor(PRODUCTION_FILE);
        classVisitor.visit(productionFileCompilationUnit, null);

        classVisitor = new LazyTest.ClassVisitor(TEST_FILE);
        classVisitor.visit(testFileCompilationUnit, null);

        for (MethodUsage method : instanceLazy) {
            TestMethod testClass = new TestMethod(method.getTestMethodName());
            testClass.setRange(method.getRange());
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
        TestMethod testMethod;
        private List<String> productionVariables = new ArrayList<>();
        private HashMap<String,ArrayList<String>> calledMethods = new HashMap<>();
        private String fileType;

//        public void EagerTest() {
//            
//        }
        
        public ClassVisitor(String type) {
            fileType = type;
        }
        

        @Override
        public void visit(ClassOrInterfaceDeclaration n, Void arg) {
            if (Objects.equals(fileType, PRODUCTION_FILE)) {
                productionClassName = n.getNameAsString();
            }
            super.visit(n, arg);
            ArrayList<String> resultado = new ArrayList<>();

            calledMethodsLine.forEach( (key, value ) -> { // forEach() também é novidade Java 8
                if(calledMethodsLine.get(key).size() > 1){
                    List<String> names = calledMethodsName.get(key).stream().distinct().collect(Collectors.toList());
                    if(names.size()>1) {
                        List<String> lines = calledMethodsLine.get(key).stream().distinct().collect(Collectors.toList());
                        
                        	
                        MethodUsage method_Usage = new MethodUsage(names.toString()
                                .replace("[", "").replace("]", ""),
                                "", lines.toString()
                                .replace("[", "").replace("]", ""));
                        instanceLazy.add(method_Usage);
                        
//                        insertTestSmell(n.getRange().get(), this.testMethod);
                        String auxLines = lineListToString(lines);
                        insertTestSmell(n.getRange().get(), this.testMethod, key.toString(), auxLines);
                    }
                }
            } );
        }

        @Override
        public void visit(EnumDeclaration n, Void arg) {
            if (Objects.equals(fileType, PRODUCTION_FILE)) {
                productionClassName = n.getNameAsString();
            }
            super.visit(n, arg);
        }

        /**
         * The purpose of this method is to 'visit' all test methods.
         */
        @Override
        public void visit(MethodDeclaration n, Void arg) {
        	// ensure that this method is only executed for the test file
            if (Objects.equals(fileType, TEST_FILE)) {
                if (Util.isValidTestMethod(n)) {
                    currentMethod = n;
                    testMethod = new TestMethod(currentMethod.getNameAsString());
                    testMethod.setHasSmell(false); //default value is false (i.e. no smell)
                    super.visit(n, arg);

                    //reset values for next method
                    currentMethod = null;
                    productionVariables = new ArrayList<>();
                }
            } else { //collect a list of all public/protected members of the production class
                for (Modifier modifier : n.getModifiers()) {
                    if (modifier.name().toLowerCase().equals("public") || modifier.name().toLowerCase().equals("protected")) {
                        productionMethods.add(n);
                    }
                }
            }
        }
        /**
         * The purpose of this method is to identify the production class methods that are called from the test method
         * When the parser encounters a method call:
         * 1) the method is contained in the productionMethods list
         * or
         * 2) the code will check the 'scope' of the called method
         * A match is made if the scope is either:
         * equal to the name of the production class (as in the case of a static method) or
         * if the scope is a variable that has been declared to be of type of the production class (i.e. contained in the 'productionVariables' list).
         */
        @Override
        public void visit(MethodCallExpr n, Void arg) {
            super.visit(n, arg);
            if (currentMethod != null) {
                if (productionMethods.stream().anyMatch(i -> i.getNameAsString().equals(n.getNameAsString()) &&
                        i.getParameters().size() == n.getArguments().size())) {
                    //calledProductionMethods.add(new MethodUsage(currentMethod.getNameAsString(), n.getNameAsString(),n.getRange().get().begin.line + "-" + n.getRange().get().end.line));
                    String valor = n.getNameAsString();
                    ArrayList<String> lines = new ArrayList<>();
                    ArrayList<String> names = new ArrayList<>();
                    lines.add(String.valueOf(n.getRange().get().begin.line));
                    if(!names.contains(currentMethod.getNameAsString())) {
                        names.add(currentMethod.getNameAsString());
                    }
                    if(!calledMethodsLine.containsKey(valor)) {
                        calledMethodsLine.put(valor, lines);
                        calledMethodsName.put(valor,names);
                    }
                    else {
                        lines.addAll(calledMethodsLine.get(valor));
                        names.addAll(calledMethodsName.get(valor));
                        calledMethodsLine.computeIfPresent(valor, (k, v) -> v = lines);
                        calledMethodsName.computeIfPresent(valor, (k, v) -> v = names);
                    }
                } else {
                    if (n.getScope().isPresent()) {
                        if (n.getScope().get() instanceof NameExpr) {
                            //checks if the scope of the method being called is either of production class (e.g. static method)
                            //or
                            ///if the scope matches a variable which, in turn, is of type of the production class
                            if (((NameExpr) n.getScope().get()).getNameAsString().equals(productionClassName) ||
                                    productionVariables.contains(((NameExpr) n.getScope().get()).getNameAsString())) {
                                //calledProductionMethods.add(new MethodUsage(currentMethod.getNameAsString(), n.getNameAsString(), n.getRange().get().begin.line + "-" + n.getRange().get().end.line));
                                String valor = n.getNameAsString();
                                ArrayList<String> lines = new ArrayList<>();
                                ArrayList<String> names = new ArrayList<>();
                                lines.add(String.valueOf(n.getRange().get().begin.line));
                                names.add(currentMethod.getNameAsString());
                                if(!names.contains(currentMethod.getNameAsString())) {
                                    names.add(currentMethod.getNameAsString());
                                }
                                if(!calledMethodsLine.containsKey(valor)) {
                                    calledMethodsLine.put(valor, lines);
                                    calledMethodsName.put(valor,names);
                                }
                                else {
                                    lines.addAll(calledMethodsLine.get(valor));
                                    names.addAll(calledMethodsName.get(valor));
                                    calledMethodsLine.computeIfPresent(valor, (k, v) -> v = lines);
                                    calledMethodsName.computeIfPresent(valor, (k, v) -> v = names);
                                }
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void visit(ObjectCreationExpr n, Void arg) {
            super.visit(n, arg);
            if (currentMethod != null) {
                for (int i = 0; i < constructorMethods.size(); i++) {
                    if (constructorMethods.get(i).getName().asString().equals(n.getType().toString())) {
                        String valor = constructorMethods.get(i).getName().asString();
                        ArrayList<String> lines = new ArrayList<>();
                        ArrayList<String> names = new ArrayList<>();
                        lines.add(String.valueOf(n.getRange().get().begin.line));
                        names.add(currentMethod.getNameAsString());
                        if(!names.contains(currentMethod.getNameAsString())) {
                            names.add(currentMethod.getNameAsString());
                        }
                        if(!calledMethodsLine.containsKey(valor)) {
                            calledMethodsLine.put(valor, lines);
                            calledMethodsName.put(valor,names);
                        }
                        else {
                            lines.addAll(calledMethodsLine.get(valor));
                            names.addAll(calledMethodsName.get(valor));
                            calledMethodsLine.computeIfPresent(valor, (k, v) -> v = lines);
                            calledMethodsName.computeIfPresent(valor, (k, v) -> v = names);
                        }
                    }
                }
            }
        }

        @Override
        public void visit(ConstructorDeclaration n, Void arg){
            constructorMethods.add(n);
        }

        @Override
        public void visit(VariableDeclarator n, Void arg) {
            if (Objects.equals(fileType, TEST_FILE)) {
                if (productionClassName.equals(n.getType().asString())) {
                    productionVariables.add(n.getNameAsString());
                }
            }
            super.visit(n, arg);
        }
       
    }
	public void insertTestSmell (Range range, TestMethod testMethod, String key, String linhasAgrupadas) {
		cadaTestSmell = new TestSmellDescription(getSmellName(), 
												 "....", 
				 								 getFilePath(), 
				 								 getClassName(),
				 								 key + "() \n" ,
				 								 linhasAgrupadas + "", 
				 								 range.end.line + "", 
				 								 range.begin.line, 
				 								 range.end.line,
												 "",
												 null,
												 null);	
		listTestSmells.add(cadaTestSmell);
		String smellLocation;
		smellLocation = "Classe " + getClassName() + "\n" + 
						"Método de produção " + key + "() \n" + 
						"Lines " + linhasAgrupadas + "\n" +
						calledMethodsName;
		System.out.println(smellLocation);
	}
	public String lineListToString(List<String> linesList) {
		List<Integer> listNumber = linesList.stream().map(Integer::parseInt).collect(Collectors.toList());
		
		String resultLines = "";
		Collections.sort(listNumber);
		int countLines = listNumber.size();
		int i = 1;
		
		for (Integer l : listNumber) { 
			resultLines += l.toString();
			if (i != countLines)
				resultLines += ", ";
			i++;
		}
		
		return resultLines;
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
