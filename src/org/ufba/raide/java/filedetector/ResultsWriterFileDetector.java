package org.ufba.raide.java.filedetector;
import com.github.javaparser.ast.expr.ThisExpr;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.ufba.raide.java.entity.ClassEntity;
import org.ufba.raide.java.entity.MethodEntity;
import org.ufba.raide.java.filedetector.TrataStringCaminhoTeste;

public class ResultsWriterFileDetector {
	private static String src;


    private CSVWriter classCSVWriter, methodCSVWriter, debtCSVWriter;

    public static ResultsWriterFileDetector createResultsWriter(String src) throws IOException {
    	setSrc(src);
        return new ResultsWriterFileDetector();
    }

    private ResultsWriterFileDetector() throws IOException { 
    	String nameFile = new TrataStringCaminhoTeste().getFILE_DETECTOR();
    	
        File file = new File(getSrc()+ RAIDEUtils.pathSeparator() + nameFile);
        file.delete();
        classCSVWriter = new CSVWriter(new FileWriter(file), ',');
        //createClassFile();
    }
    
    private void createClassFile() throws IOException {
        List<String[]> fileLines = new ArrayList<String[]>();
        String[] columnNames = {
                "App",
                "FilePath",
        };
        fileLines.add(columnNames);
        classCSVWriter.writeAll(fileLines, false);
        classCSVWriter.flush();
    }

    public void outputToCSV(ClassEntity classEntity) throws IOException {
        outputClassDetails(classEntity);
    }

    public void closeOutputFiles() throws IOException {
        classCSVWriter.close();
    }

    private void outputClassDetails(ClassEntity classEntity) throws IOException {
        List<String[]> fileLines = new ArrayList<String[]>();
        String[] dataLine;

        dataLine = new String[1];
        dataLine[0] = classEntity.getFilePath();

        fileLines.add(dataLine);

        classCSVWriter.writeAll(fileLines, false);
        classCSVWriter.flush();
    }
    public String getSrc() {
		return src;
	}

	public static void setSrc(String dir) {
		src = dir;
	}
}
