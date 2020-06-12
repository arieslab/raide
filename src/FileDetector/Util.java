package FileDetector;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

public class Util {

    public enum OperationStatus {
        Started,
        Completed
    }

    public static void writeOperationLogEntry(String message, OperationStatus status) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Calendar.getInstance().getTime().toString());
        stringBuilder.append(";");
        stringBuilder.append(message);
        stringBuilder.append(";");
        stringBuilder.append(status.toString());

        System.out.println(stringBuilder.toString());

        FileWriter fileWriter = new FileWriter("Log.txt",true);
        fileWriter.write(stringBuilder.toString());
        fileWriter.write(System.getProperty("line.separator"));
        fileWriter.close();

    }

    public static void writeException(Exception exception, String message) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Calendar.getInstance().getTime().toString());
        stringBuilder.append(";");
        stringBuilder.append(exception);
        stringBuilder.append(";");
        stringBuilder.append(message);


        System.out.println(stringBuilder.toString());

        FileWriter fileWriter = new FileWriter("Error.txt",true);
        fileWriter.write(stringBuilder.toString());
        fileWriter.write(System.getProperty("line.separator"));
        fileWriter.close();
    }

}
