package FileMapping;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import FileDetector.RAIDEUtils;
import FileDetector.TrataStringCaminhoTeste;

public class FileMappingMain {

	static List<TestFile> testFiles;

    public static void detect(String src) throws IOException {
        System.out.println("Started!");
        
        String localFile = src + RAIDEUtils.pathSeparator() + new TrataStringCaminhoTeste().getFILE_DETECTOR();
        
        MappingDetector mappingDetector;
        BufferedReader in = new BufferedReader(new FileReader(localFile));
        String str;

        testFiles = new ArrayList<>();

        System.out.println("Reading input.");
        while ((str = in.readLine()) != null) {
            System.out.println("Detecting: "+str);
            mappingDetector = new MappingDetector();
            testFiles.add(mappingDetector.detectMapping(str));
        }

        System.out.println("Saving results. Total lines:"+ testFiles.size());
        ResultsWriterFileMapping resultsWriter = ResultsWriterFileMapping.createResultsWriter(src);
        List<String> columnValues = null;
        String projectName = new TrataStringCaminhoTeste().srcToNameProject(src);

        for (int i = 0; i < testFiles.size(); i++) {
            columnValues = new ArrayList<>();
            
            columnValues.add(0, projectName);
            columnValues.add(1, testFiles.get(i).getFilePath());
            columnValues.add(2, testFiles.get(i).getProductionFilePath());
            
            resultsWriter.writeLine(columnValues);
        }

        System.out.println("Completed!");
    }}