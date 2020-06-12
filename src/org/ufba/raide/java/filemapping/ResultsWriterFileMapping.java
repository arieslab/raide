package org.ufba.raide.java.filemapping;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.List;

import org.ufba.raide.java.filedetector.RAIDEUtils;
import org.ufba.raide.java.filedetector.TrataStringCaminhoTeste;

import com.opencsv.CSVWriter;


/**
 * This class is utilized to write output to a CSV file
 */
public class ResultsWriterFileMapping {

	private static String src;
    private FileWriter writer;
    /**
     * Factory method that provides a new instance of the ResultsWriter
     * @return new ResultsWriter instance
     * @throws IOException
     */
    public static ResultsWriterFileMapping createResultsWriter(String src) throws IOException {
    	setSrc(src);
        return new ResultsWriterFileMapping();
    }

    /**
     * Creates the file into which output it to be written into. Results from each file will be stored in a new file
     * @throws IOException
     */
    private ResultsWriterFileMapping() throws IOException {
    	String nameFile = new TrataStringCaminhoTeste().getFILE_MAPPING();
        File file = new File(getSrc()+ RAIDEUtils.pathSeparator() + nameFile);
        file.delete();
        writer = new FileWriter(getSrc()+ RAIDEUtils.pathSeparator() + nameFile,false);
    }

    /**
     * Writes column names into the CSV file
     * @param columnNames the column names
     * @throws IOException
     */
    public void writeColumnName(List<String> columnNames) throws IOException {
        writeOutput(columnNames);
    }

    /**
     * Writes column values into the CSV file
     * @param columnValues the column values
     * @throws IOException
     */
    public void writeLine(List<String> columnValues) throws IOException {
        writeOutput(columnValues);
    }

    /**
     * Appends the input values into the CSV file
     * @param dataValues the data that needs to be written into the file
     * @throws IOException
     */
    private void writeOutput(List<String> dataValues)throws IOException {
    	File file = new File(getSrc()+ RAIDEUtils.pathSeparator() + new TrataStringCaminhoTeste().getFILE_MAPPING());
        writer = new FileWriter(file,true);

        for (int i=0; i<dataValues.size(); i++) {
            writer.append(String.valueOf(dataValues.get(i)));

            if(i!=dataValues.size()-1)
                writer.append(",");
            else
                writer.append(System.lineSeparator());

        }
        writer.flush();
        writer.close();
    }
    public String getSrc() {
		return src;
	}

	public static void setSrc(String dir) {
		src = dir;
	}
}