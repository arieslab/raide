package org.ufba.raide.java.filedetector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jgit.util.Base64;

public final class RAIDEUtils {

	public static String pathSeparator() {
		if (isWindowsPath()) {
			return "\\";
		} else {
			return "/";
		}
	}

	public static String firstPathSeparator() {
		if (isWindowsPath()) {
			return "";
		} else {
			return "/";
		}
	}

	public static boolean isWindowsPath() {
		return System.getProperty("os.name").toLowerCase().contains("win");
	}

	public static List<Integer> linesAsNumbers(String lines) {
		ArrayList<Integer> numbers = new ArrayList<>();
		if (lines.contains(",")) {
			String[] splitted = lines.split(",");
			for (int i = 0; i < splitted.length; i++) {
				numbers.add(Integer.valueOf(splitted[i].trim()));
			}
		} else {
			numbers.add(Integer.valueOf(lines.trim()));
		}
		return numbers;
	}

	public static String copyMethodContent(String path, List<Integer> begin, List<Integer> end) throws IOException {
		String content = "";
		for (int i = 0; i < begin.size(); i++) {
			content += copyFileContent(path, begin.get(i), end.get(i));
		}
		return content;
	}
	
	public static String copyFileContent(String path, int begin, int end) throws IOException {
		int currentLine = 1;
		String content = "";
		for (String line : Files.readAllLines(new File(path).toPath())){
			if (currentLine >= begin && currentLine <= end) {
				content += line + "\n";
			}
			currentLine++;
			
			if (currentLine > end) {
				break;
			}
		}
		return content;
	}

	public static String copyMethod(String path, int begin, int end) throws IOException {
		int lineCount = 1;
		File file = new File(path);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String st;
		String strMethod = "";
		while ((st = reader.readLine()) != null) {
			if (lineCount >= begin && lineCount < end) {
				strMethod += st + "\n";
			}
			if (lineCount == end) {
				strMethod += st;
				break;
			}
			lineCount++;
		}
		reader.close();
		return strMethod;
	}
	
	public static String encode(String content) {
		return Base64.encodeBytes(content.getBytes());
	}
	
	public static String decode(String content) {
		return new String(Base64.decode(content));
	}
	
	public static String extractOnlySourceDir(String projectPath, String testFilePath) {
		String projectDir = projectPath.substring(projectPath.lastIndexOf("/")).replace("/", "").replace("\\","");
		String filePath = testFilePath.split(projectDir)[1].substring(1);
		return filePath;
	}
	
	public static void main(String[] args) throws IOException {
//		String c = RAIDEUtils.copyMethodContent("C:\\Users\\raila\\Documents\\Workspace\\maven-dependency-plugin_3.3.0\\src\\test\\java\\org\\apache\\maven\\plugins\\dependency\\utils\\TestDependencyUtil.java", new ArrayList<Integer>(Arrays.asList(301,304)), new ArrayList<Integer>(Arrays.asList(301,304)));
//		System.out.println(RAIDEUtils.encode(c));
//		System.out.println(RAIDEUtils.decode(RAIDEUtils.encode(c)));
	}

}
