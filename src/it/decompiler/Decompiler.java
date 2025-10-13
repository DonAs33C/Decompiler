package it.decompiler;

import java.awt.FlowLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.*;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Decompiler {

	// JD CLI commands
	final static String NEXT_LINE_WINDOWS = "\r\n";
	final static String COMMAND_DECOMPILE = "cmd /c java -jar jd-cli.jar -g OFF ";
	final static String JD_CLI_SOURCE = "lib\\jd-cli.jar";

	private static File folder;
	protected static String stringPathFolder = null;
	static boolean flag = true;

	Decompiler() {}

	public static void main(String[] args) throws IOException, URISyntaxException {
		Scanner input = new Scanner(System.in);
		System.out.println("Enter the path to scan:"); String stringPathFolder = input.nextLine();
		setFolder(new File(stringPathFolder));
		getFileForExtensionName(getFolder(), ".jar");
		deleteFileDotClass(getFolder());
	}

	protected static boolean searchOnMvnRepository(File file) {
		String searchName = file.getName().replace(".jar", "");
		System.out.println(searchName);
		try {
			String tag;

			Document doc = Jsoup.connect("https://mvnrepository.com/search?q=" + searchName).get();
			Element tagTitle = doc.selectFirst("h2");
			tag = tagTitle.text();
			int numberFoundCasted;
			if (tag.contains("Found")) {
				String numberFound = tag.replaceAll("[^0-9]", "");
				numberFoundCasted = Integer.parseInt(numberFound);
				if (numberFoundCasted == 0) {
					System.out.println("There are no results on mvnrepository.com");
					return true;
				} else
					return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	protected static void addListFilesForFolderClass(File folder) throws IOException {
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				// createFolder(file);
				addListFilesForFolderClass(file);
			} else {
				System.out.println("File:	" + file);
				copyFileClass(file, getDestDotFolder("File Class").toString() + File.separator + file.getName());
			}
		}
	}

	protected static void addListFilesForFolderJar(File folder) throws IOException {
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				// createFolder(file);
				addListFilesForFolderJar(file);
			} else {
				System.out.println("File:	" + file);
				copyFileJar(file, getDestDotFolder("File Jar").toString() + File.separator + file.getName());
			}
		}
	}

	protected static void createFolder(File file) throws IOException {
		// if (getDotClass(folderName)) {
		File folderDest = null;
		System.out.println("Entry dir: " + file);
		String folderName = File.separator + file.getName();
		if (flag) {
			folderDest = new File(getDestDotFolder("File Class") + folderName);
			setStringPathFolder(folderDest.getAbsolutePath());
			folderDest.mkdir();
			flag = false;
			// return;
		} else {
			folderDest = new File(getStringPathFolder() + folderName);
			setStringPathFolder(folderDest.getAbsolutePath());
			folderDest.mkdirs();
			flag = true;
		}
		System.out.println("Destination dir: " + folderDest);

//		if (getDotJar(folderName)) {
//			folder = new File(getDestDotFolder("File Jar") + "\\" + folderName);
//			setStringPathFolder(folder.getAbsolutePath());
//			folder.mkdir();
//		}
	}

	private static void getFileForExtensionName(File folder, String extOfFile) throws IOException {
		// File file = new File(folder);
		switch (extOfFile) {
		case ".class":
			// addListFilesForFolderClass(folder);
			// copyFileTxt(folder, getDestDotFolder("File Class").toString() + File.separator);
			// deleteFileDotClass(getDestDotFolder("File Class"));
			searchOnFolderClass(folder);
			break;

		case ".jar":
//			addListFilesForFolderJar(folder);
//			searchOnFolder(getDestDotFolder("File Jar"));
//			copyFileTxt(folder, getDestDotFolder("File Jar").toString() + File.separator);
//			deleteFileDotJar(getDestDotFolder("File Jar"));
//			deleteFileDotClass(getDestDotFolder("File Jar"));
			searchOnFolderJar(folder);
			deleteFileDotJar(folder);
			decompileClass(folder);
			// deleteFileDotClass(folder);
			break;
		}
	}

	protected static void searchOnFolderClass(File folder) throws IOException {
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				searchOnFolder(file);
			} else if (getDotClass(file)) {
				System.out.println("File class:	" + file);
				// writeFileLoggerTxt(file, ".class");
				decompileClass(file.toString());
			}
		}
	}

	protected static void searchOnFolderJar(File folder) throws IOException {
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				searchOnFolderJar(file);
			} else if (getDotJar(file) && searchOnMvnRepository(file)) {
				System.out.println("File jar:	" + file);
				// writeFileLoggerTxt(file, ".class");
				extractJarFile(file);
			}
		}
	}

	protected static void searchOnFolder(File folder) throws IOException {
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				searchOnFolder(file);
			} else {
				System.out.println("File in Jar:	" + file);
				writeFileLoggerTxt(file, ".class");
				decompileClass(file.toString());
			}
		}
	}

	protected static void copyFileTxt(File source, String dest) throws IOException {
		for (File file : source.listFiles()) {
			if (file.isDirectory()) {
				copyFileTxt(file, dest);
				;
			} else if (file.toString().endsWith(".txt")) {
				InputStream is = null;
				OutputStream os = null;
				try {
					is = new FileInputStream(file);
					os = new FileOutputStream(dest + file.getName());
					byte[] buffer = new byte[1024];
					int length;
					while ((length = is.read(buffer)) > 0) {
						os.write(buffer, 0, length);
					}
				} finally {
					is.close();
					os.close();
				}
				deleteFileLoggerTxt(file);
			}
		}
	}

	private static void copyFileJar(File source, String dest) throws IOException {
		if (getDotJar(source) && searchOnMvnRepository(source)) {
			writeFileLoggerTxt(source, ".jar");
			InputStream is = null;
			OutputStream os = null;
			try {
				is = new FileInputStream(source);
				os = new FileOutputStream(dest);
				byte[] buffer = new byte[1024];
				int length;
				while ((length = is.read(buffer)) > 0) {
					os.write(buffer, 0, length);
				}
			} finally {
				is.close();
				os.close();
			}
			extractJarFile(source.getName());
		}
	}

	private static void copyFileClass(File source, String dest) throws IOException {
		if (getDotClass(source)) {
			writeFileLoggerTxt(source, ".class");
			InputStream is = null;
			OutputStream os = null;
			try {
				is = new FileInputStream(source);
				os = new FileOutputStream(dest);
				byte[] buffer = new byte[1024];
				int length;
				while ((length = is.read(buffer)) > 0) {
					os.write(buffer, 0, length);
				}
			} finally {
				is.close();
				os.close();
			}
			decompileClass(dest);
		}
	}

	protected static void deleteFileDotJar(File file) {
		for (File fileJar : file.listFiles()) {
			if (fileJar.toString().endsWith(".jar")) {
				fileJar.delete();
				System.out.println("File removed: " + fileJar);
			}
		}
	}

	protected static void deleteFileDotClass(File file) throws IOException {
		for (File fileClass : file.listFiles()) {
			if (fileClass.isDirectory()) {
				deleteFileDotClass(fileClass);
			} else if (getDotClass(fileClass)) {
				//writeFileLoggerTxt(fileClass, ".class");
				fileClass.delete();
				System.out.println("File removed: " + fileClass);
			}
		}
	}

	protected static void deleteFileLoggerTxt(File file) {
		if (file.toString().endsWith(".txt")) {
			file.delete();
			System.out.println("File removed: " + file);
		}
	}

	private static void extensionForLoggerTxt(File file) throws IOException {
		if (getDotJar(file)) {
			writeFileLoggerTxt(file, ".jar");
		}
		if (getDotClass(file)) {
			writeFileLoggerTxt(file, ".class");
		}
	}

	protected static void writeFileLoggerTxt(File file, String extension) throws IOException {
		String fileTxt = file.toString().replace(extension, ".txt");
		FileOutputStream fos = new FileOutputStream(fileTxt);
		fos.write(getAbsolutePathOfFile(file).getBytes());
		fos.write(NEXT_LINE_WINDOWS.getBytes());
		fos.write(NEXT_LINE_WINDOWS.getBytes());
		fos.write(getDateOfLastModifiedFile(file).getBytes());
		fos.flush();
		fos.close();
		System.out.println("File logger wrote.");
	}

	protected static void extractJarFile(File fileJar) throws IOException {
		File folderForJar = new File(fileJar.toString().replace(".jar", ""));
		folderForJar.mkdir();
		Runtime.getRuntime().exec("cmd /c jar -xf " + '"' + fileJar + '"', null, folderForJar);
	}

	protected static void extractJarFile(String fileJar) throws IOException {
		String fileName = fileJar.replace(".jar", "");
		File folderForJar = new File(getDestDotFolder("File Jar") + File.separator + fileName);
		folderForJar.mkdir();
		Runtime.getRuntime().exec("cmd /c jar -xf " + '"' + getDestDotFolder("File Jar") + File.separator + fileJar + '"', null,
				folderForJar);
	}

	protected static void decompileClass(File file) throws IOException {
		for (File fileClass : file.listFiles()) {
			if (fileClass.isDirectory()) {
				decompileClass(fileClass);
			} else if (getDotClass(fileClass)) {
				//System.out.println("file class: " + fileClass);
				String fileJava = fileClass.toString().replace(".class", ".java");
				String commandForJdCli = '"' + fileClass.toString() + '"' + " > " + '"' + fileJava + '"';
				Process process = Runtime.getRuntime()
						.exec("cmd /c java -jar " + JD_CLI_SOURCE + " -g OFF " + commandForJdCli);
				try {
					if (process.waitFor() == 0) {
						System.out.println("Class decompilation " + fileClass.getName() + " completed.");
						process.destroy();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected static void decompileClass(String file) throws IOException {
		if (file.endsWith(".class")) {
			String fileJava = file.toString().replace(".class", ".java");
			String commandForJdCli = '"' + file + '"' + " > " + '"' + fileJava + '"';
			Runtime.getRuntime().exec("cmd /c java -jar " + JD_CLI_SOURCE + " -g OFF " + commandForJdCli);
		}
	}

	private static boolean getDotClass(File file) {
		if (file.toString().endsWith(".class")) {
			return true;
		}
		return false;
	}

	private static boolean getDotJar(File file) {
		if (file.toString().endsWith(".jar")) {
			return true;
		}
		return false;
	}

	private static String getAbsolutePathOfFile(File listOfFile) {
		return "File found in path:" + NEXT_LINE_WINDOWS + listOfFile.getAbsolutePath();
	}

	private static String getDateOfLastModifiedFile(File listOfFile) {
		return "Last modified of file:" + NEXT_LINE_WINDOWS + new Date(listOfFile.lastModified());
	}

	private static File getDestDotFolder(String destFolder){
		String username = System.getProperty("user.name");
		// Path of destination folder
		return new File("C:\\Users\\" + username + "\\Desktop\\" + destFolder);
	}

	protected static File getFolder() {
		return folder;
	}

	protected static void setFolder(File folder) {
		Decompiler.folder = folder;
	}

	protected static String getStringPathFolder() {
		return stringPathFolder;
	}

	protected static void setStringPathFolder(String stringPathFolder) {
		Decompiler.stringPathFolder = stringPathFolder;
	}

}