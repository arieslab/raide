package org.ufba.raide.java.filedetector;

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

}
