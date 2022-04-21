package org.ufba.raide.java.testsmell.detector.smell;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.FileNotFoundException;
import java.util.*;

import javax.swing.JOptionPane;

import org.ufba.raide.java.testsmell.AbstractSmell;
import org.ufba.raide.java.testsmell.SmellyElement;
import org.ufba.raide.java.testsmell.TestMethod;
import org.ufba.raide.java.testsmell.TestSmellDescription;
import org.ufba.raide.java.testsmell.Util;

public class DuplicateAssert extends AbstractSmell {
	
	ArrayList<TestSmellDescription> listTestSmells = new ArrayList<TestSmellDescription>();
	TestSmellDescription cadaTestSmell;		
	String className;
	String filePath;
    private List<SmellyElement> smellyElementList;
    
    public class EstruturaDA {    	

    	String text;
    	int line;
    	boolean verificado;
    	
		public EstruturaDA(String text,int line) {
			super();
			this.text = text;
			this.line = line;
			this.verificado = false;
		}
		
		public String getText() {
			return text;
		}
		
		public void setText(String text) {
			this.text = text;
		}   
		
		public int getLine() {
			return line;
		}
		
		public void setLine(int line) {
			this.line = line;
		}

		public boolean isVerificado() {
			return verificado;
		}

		public void setVerificado(boolean verificado) {
			this.verificado = verificado;
		}
    	
    }
    
    public DuplicateAssert(String name, String path) {
    	setClassName(name);
		setFilePath(path);
        smellyElementList = new ArrayList<>();
    }

    /**
     * Checks of 'Duplicate Assert' smell
     */
    @Override
    public String getSmellName() {
        return "Duplicate Assert";
    }
    public String getFilePath() {
		return filePath;
	}
    public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

    /**
     * Returns true if any of the elements has a smell
     */
    @Override
    public boolean getHasSmell() {
        return smellyElementList.stream().filter(x -> x.getHasSmell()).count() >= 1;
    }

    /**
     * Analyze the test file for test methods that have multiple assert statements with the same explanation message
     * @return 
     */
    @Override
    public List<TestSmellDescription> runAnalysis(CompilationUnit testFileCompilationUnit, 
    										      CompilationUnit productionFileCompilationUnit, 
    										      String testFileName, 
    										      String productionFileName) throws FileNotFoundException {
    	
    	listTestSmells = new ArrayList<TestSmellDescription>();
    	
    	DuplicateAssert.ClassVisitor classVisitor;
        classVisitor = new DuplicateAssert.ClassVisitor();
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
        List<String> assertMessage = new ArrayList<>();
        List<EstruturaDA> assertMethod = new ArrayList<>();

        // examine all methods in the test class
        @Override
        public void visit(MethodDeclaration n, Void arg) {
            if (Util.isValidTestMethod(n)) {
                currentMethod = n;
                testMethod = new TestMethod(n.getNameAsString());
                testMethod.setHasSmell(false); //default value is false (i.e. no smell)
                super.visit(n, arg);
                
                Set<String> set1 = new HashSet<String>(assertMessage);
                if (set1.size() < assertMessage.size()) {
                    testMethod.setHasSmell(true);
                }

                List<EstruturaDA> teste = assertMethod;                         
               
                for (int i = 0; i < teste.size() ; i++ ) {  
                	if (!teste.get(i).isVerificado()) {
                		String lines = "";
	                	boolean hasSmell = false;
	                	for (int j = i + 1; j < teste.size() ; j++ ) {
	                		//S� compara com outros asserts, caso o objeto ainda n�o tenha sido identificado como outro DA
	                		if ((!teste.get(j).isVerificado()) && (teste.get(i).text.equals(teste.get(j).text))) {
	                			if (!hasSmell) {
	                				lines +=  teste.get(i).line;
	                    			teste.get(i).setVerificado(true); 
	                			}
	                			lines += ", " + teste.get(j).line; 
	                			teste.get(j).setVerificado(true);  
	                			hasSmell = true;	                			
	                		}
	                	}
	                	if (hasSmell) {
		                	cadaTestSmell = new TestSmellDescription("Duplicate Assert", "Remove Line", 
		                            getFilePath(), getClassName(), this.testMethod.getElementName() + 
		                           "() \n" , lines + "", n.getRange().get().begin.line, n.getRange().get().end.line);	
		                	listTestSmells.add(cadaTestSmell);
	                	}     
                	}
                }               
                smellyElementList.add(testMethod);

                //reset values for next method
                currentMethod = null;
                assertMessage = new ArrayList<>();
                assertMethod = new ArrayList<>();
            }
        }
        

        // examine the methods being called within the test method
        @Override
        public void visit(MethodCallExpr n, Void arg) {
            super.visit(n, arg);
            if (currentMethod != null) {
                // if the name of a method being called start with 'assert'
                // if the name of a method being called is an assertion and has 3 parameters
                if (n.getNameAsString().startsWith(("assertArrayEquals")) ||
                        n.getNameAsString().startsWith(("assertEquals")) ||
                        n.getNameAsString().startsWith(("assertNotSame")) ||
                        n.getNameAsString().startsWith(("assertSame")) ||
                        n.getNameAsString().startsWith(("assertThat"))) {
                    assertMethod.add(new EstruturaDA(n.toString(), n.getRange().get().begin.line));
                    // assert method contains a message
                    if (n.getArguments().size() == 3) {
                        assertMessage.add(n.getArgument(0).toString());
                    }

                }
                // if the name of a method being called is an assertion and has 2 parameters
                else if (n.getNameAsString().equals("assertFalse") ||
                        n.getNameAsString().equals("assertNotNull") ||
                        n.getNameAsString().equals("assertNull") ||
                        n.getNameAsString().equals("assertTrue")) {
                	assertMethod.add(new EstruturaDA(n.toString(), n.getRange().get().begin.line));
                    // assert method contains a message
                    if (n.getArguments().size() == 2) {
                        assertMessage.add(n.getArgument(0).toString());
                    }
                }
                // if the name of a method being called is 'fail'
                else if (n.getNameAsString().equals("fail")) {
                	assertMethod.add(new EstruturaDA(n.toString(), n.getRange().get().begin.line));
                    // fail method contains a message
                    if (n.getArguments().size() == 1) {
                        assertMessage.add(n.getArgument(0).toString());
                    }
                }
            }
        }

    }


	@Override
	public int contSmell() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getCountSmell(String string) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setClassName(String string) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getClassName() {
		// TODO Auto-generated method stub
		return null;
	}
}
