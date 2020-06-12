package FileMapping;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;


import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class MappingDetector {

    TestFile testFile;
    String productionFileName, productionFilePath;
    boolean ignoreFile;

    public MappingDetector() {
        productionFileName = "";
        productionFilePath = "";
        ignoreFile = false;
    }


    public TestFile detectMapping(String testFilePath) throws IOException {

        testFile = new TestFile(testFilePath);

        int index = testFile.getFileName().toLowerCase().lastIndexOf("test");
        if (index == 0) {
            //the name of the test file starts with the name 'test'
            productionFileName = testFile.getFileName().substring(4, testFile.getFileName().length());
        } else {
            //the name of the test file ends with the name 'test'
            productionFileName = testFile.getFileName().substring(0, testFile.getFileName().toLowerCase().lastIndexOf("test")) + ".java";
        }

        Path startDir = Paths.get(testFile.getProjectRootFolder());
        Files.walkFileTree(startDir, new FindJavaTestFilesVisitor());

        if (isFileSyntacticallyValid(productionFilePath))
            testFile.setProductionFilePath(productionFilePath);
        else
            testFile.setProductionFilePath("");

        return testFile;
    }

    /**
     * Determines if the identified production file is syntactically correct by parsing it and generating its AST
     *
     * @param filePath of the production file
     */
    private boolean isFileSyntacticallyValid(String filePath) {
        boolean valid = false;
        ignoreFile = false;

        if (filePath.length() != 0) {
            try {
                FileInputStream fTemp = new FileInputStream(filePath);
                CompilationUnit compilationUnit = JavaParser.parse(fTemp);
                MappingDetector.ClassVisitor classVisitor;
                classVisitor = new MappingDetector.ClassVisitor();
                classVisitor.visit(compilationUnit, null);
                valid = !ignoreFile;
            } catch (Exception error) {
                valid = false;
            }

        }

        return valid;
    }

    public class FindJavaTestFilesVisitor extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult visitFile(Path file,
                                         BasicFileAttributes attrs)
                throws IOException {
            if (file.getFileName().toString().toLowerCase().equals(productionFileName.toLowerCase())) {
                productionFilePath = file.toAbsolutePath().toString();
                return FileVisitResult.TERMINATE;
            }
            return FileVisitResult.CONTINUE;
        }
    }

    /**
     * Visitor class
     */
    private class ClassVisitor extends VoidVisitorAdapter<Void> {

        @Override
        public void visit(ClassOrInterfaceDeclaration n, Void arg) {
            ignoreFile = n.isInterface();
            super.visit(n, arg);
        }

        @Override
        public void visit(AnnotationDeclaration n, Void arg) {
            ignoreFile = true;
            super.visit(n, arg);
        }
    }

}

