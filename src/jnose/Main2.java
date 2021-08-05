package jnose;
import java.io.IOException;
import java.util.List;

import br.ufba.jnose.core.Config;
import br.ufba.jnose.core.JNoseCore;
import br.ufba.jnose.dto.TestClass;
import br.ufba.jnose.dto.TestSmell;
public class Main2 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Testandooo");
		processarJNose();
	}
	
	 private static String processarJNose(){
		 String directoryPath = "/home/daniele/Documentos/tests/IC_projects/jnose-tests/";
	        System.out.println("Processando JNose: ");

	        String pathTestSmellsFile = null;

	        Config conf = new Config() {
	            public Boolean assertionRoulette() {
	                return true;
	            }
	            public Boolean conditionalTestLogic() {
	                return true;
	            }
	            public Boolean constructorInitialization() {
	                return true;
	            }
	            public Boolean defaultTest() {
	                return true;
	            }
	            public Boolean dependentTest() {
	                return true;
	            }
	            public Boolean duplicateAssert() {
	                return true;
	            }
	            public Boolean eagerTest() {
	                return true;
	            }
	            public Boolean emptyTest() {
	                return true;
	            }
	            public Boolean exceptionCatchingThrowing() {
	                return true;
	            }
	            public Boolean generalFixture() {
	                return true;
	            }
	            public Boolean mysteryGuest() {
	                return true;
	            }
	            public Boolean printStatement() {
	                return true;
	            }
	            public Boolean redundantAssertion() {
	                return true;
	            }
	            public Boolean sensitiveEquality() {
	                return true;
	            }
	            public Boolean verboseTest() {
	                return true;
	            }
	            public Boolean sleepyTest() {
	                return true;
	            }
	            public Boolean lazyTest() {
	                return true;
	            }
	            public Boolean unknownTest() {
	                return true;
	            }
	            public Boolean ignoredTest() {
	                return true;
	            }
	            public Boolean resourceOptimism() {
	                return true;
	            }
	            public Boolean magicNumberTest() {
	                return true;
	            }
	            public Integer maxStatements() {
	                return 30;
	            }
	        };

	        JNoseCore jNoseCore = new JNoseCore(conf);
	        List<TestClass> lista = null;
	        try {
	            lista = jNoseCore.getFilesTest(directoryPath);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }

	        for(TestClass testClass : lista){
	            System.out.println(testClass.getPathFile() + " - " + testClass.getProductionFile() + " - " + testClass.getJunitVersion());

	            if(testClass.getListTestSmell().size() > 0){
	                System.out.println("Foram encontrados " + testClass.getListTestSmell().size() + " TestsSmells.");
//	                pathTestSmellsFile = salvarTestSmellsFile(repository,testClass);
	                for (TestSmell testSmell : testClass.getListTestSmell()){
	                    System.out.println(testSmell.getName() + " - " + testSmell.getMethod() + " - " + testSmell.getRange());
	                }
//	                System.out.println(testClass.getLineSumTestSmells());
	            }else{
	                System.out.println("Não foi encontrados TestSmells no Projeto: ");
	            }

	            System.out.println(testClass.getLineSumTestSmells());
	        }
	        

	        return pathTestSmellsFile;
	    }	


}
