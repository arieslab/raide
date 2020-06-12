package org.ufba.raide.java.filedetector;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.ufba.raide.java.entity.ClassEntity;


public class TestFileDetectorMain {
    public static void detect(String src) throws IOException {
        final String rootDirectory = src;
        TestFileDetector testFileDetector = TestFileDetector.createTestFileDetector();
        ResultsWriterFileDetector resultsWriter = ResultsWriterFileDetector.createResultsWriter(src);
        ClassEntity classEntity;

        //recursively identify all 'java' files in the specified directory
        Util.writeOperationLogEntry("Identify all 'java' test files", Util.OperationStatus.Started);
        FileWalker fw = new FileWalker();
        List<Path> files = fw.getJavaTestFiles(rootDirectory, true);
        Util.writeOperationLogEntry("Identify all 'java' test files", Util.OperationStatus.Completed);

        //foreach of the identified 'java' files, obtain details about the methods that they contain
        Util.writeOperationLogEntry("Obtain method details", Util.OperationStatus.Started);
        for (Path file : files) {
            try {
                classEntity = testFileDetector.runAnalysis(file);
                resultsWriter.outputToCSV(classEntity);
            } catch (Exception e) {
                Util.writeException(e, "File: " + file.toAbsolutePath().toString());
            }
        }
        Util.writeOperationLogEntry("Obtain method details", Util.OperationStatus.Completed);
        resultsWriter.closeOutputFiles();    	
    }
}
