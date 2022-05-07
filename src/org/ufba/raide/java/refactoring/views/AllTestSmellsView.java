package org.ufba.raide.java.refactoring.views;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.ufba.raide.java.filedetector.RAIDEUtils;
import org.ufba.raide.java.filedetector.TestFileDetectorMain;
import org.ufba.raide.java.filedetector.TrataStringCaminhoTeste;
import org.ufba.raide.java.filemapping.FileMappingMain;
import org.ufba.raide.java.testsmell.TestFile;
import org.ufba.raide.java.testsmell.TestSmellDescription;
import org.ufba.raide.java.testsmell.TestSmellDetector;

public class AllTestSmellsView extends ViewPart {

	private static final String MESSAGE_DIALOG_TITLE = "All Test Smells";

	private IJavaProject project;
	private ISelection currentSelection;
	private String resultsCsvFile;

	private boolean isAnalysisRunning;

	public static String getMessageDialogTitle() {
		return MESSAGE_DIALOG_TITLE;
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		runTestSmellsAnalysis();
	}

	private void runTestSmellsAnalysis() {
		if (isTestSelectedOnProject()) {
			isAnalysisRunning = true;

			Display display = PlatformUI.getWorkbench().getDisplay();
			FileDialog dialog = new FileDialog(display.getActiveShell(), SWT.SAVE);
			dialog.setFilterNames(new String[] { "All Files (*.*)" });
			dialog.setFilterExtensions(new String[] { "*.csv" });

			dialog.setFilterPath(System.getProperty("home"));
			dialog.setFileName("raide_test_smells_analysis_" + (new Date()).getTime() + ".csv");
			resultsCsvFile = dialog.open();

			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					final File csvFile = new File(resultsCsvFile);
					if (csvFile.exists() || new File(csvFile.getParent()).exists()) {
						startTestSmellsAnalysis();
					}
					isAnalysisRunning = false;
				}
			});
		}
	}

	private void startTestSmellsAnalysis() {
		try {
			final List<String> testSmellsTypes = new ArrayList<>();
			testSmellsTypes.add(AssertionRouletteView.getMessageDialogTitle());
			testSmellsTypes.add(DuplicateAssertView.getMessageDialogTitle());
			testSmellsTypes.add(ConditionalTestLogicView.getMessageDialogTitle());

			List<TestFile> projectTestFiles = getProjectTestFiles();
			File results = new File(resultsCsvFile);
			FileOutputStream fos = new FileOutputStream(results);
			for (String testSmellType : testSmellsTypes) {
				List<TestSmellDescription> testSmells = detectTestSmellByType(testSmellType, projectTestFiles);
				for (TestSmellDescription smellDetected : testSmells) {
					String log = smellDetected.getTestSmellType() + ";" + smellDetected.getFilePath() + ";"
							+ smellDetected.getMethodName().replaceAll("\n", "") + ";"
							+ smellDetected.getLinePositionBegin() + "\n";
					fos.write(log.getBytes());
					System.out.println(log);
				}
			}
			fos.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Verify if "src/test" is selected on project
	 */
	private boolean isTestSelectedOnProject() {
		if (currentSelection == null) {
			currentSelection = getSite().getWorkbenchWindow().getSelectionService().getSelection();
		}
		if (currentSelection instanceof IStructuredSelection) {

			IStructuredSelection structuredSelection = (IStructuredSelection) currentSelection;
			Object element = structuredSelection.getFirstElement();

			if (element instanceof IPackageFragmentRoot) {
				// Primeiro verifica se o diretório do pacote
				IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) element;
				IJavaProject javaProject = packageFragmentRoot.getJavaProject();

				String diretorioPacote = packageFragmentRoot.getResource().getLocation().toString();

				// Depois verificados se é um pacote válido
				boolean valido = new TrataStringCaminhoTeste().diretorioTesteValido(diretorioPacote);

				if (valido) {
					project = javaProject;
				}

				return valido;
			}
		}
		return false;
	}

	/**
	 * Get the list of files from the project
	 */
	private List<TestFile> getProjectTestFiles() throws IOException {
		// tratando caminho do arquivo para pegar dinamicamente
		URI myURI = project.getResource().getLocationURI();
		String strURI = new TrataStringCaminhoTeste().removeFileString(myURI.toString());
		String strURIFinal = new TrataStringCaminhoTeste().inverteDuplicaBarraURL(strURI);

		TestFileDetectorMain.detect(RAIDEUtils.firstPathSeparator() + strURIFinal);
		FileMappingMain.detect(RAIDEUtils.firstPathSeparator() + strURIFinal);

		// retorna \\ se for windows, ou / caso não for
		String nameFile = RAIDEUtils.firstPathSeparator() + strURIFinal + RAIDEUtils.pathSeparator()
				+ new TrataStringCaminhoTeste().getFILE_MAPPING();

		String str;
		String[] lineItem;
		TestFile testFile;
		List<TestFile> testFiles = new ArrayList<>();

		try (BufferedReader in = new BufferedReader(new FileReader(nameFile))) {
			while ((str = in.readLine()) != null) {
				lineItem = str.split(",");
				if (lineItem.length == 2) {
					testFile = new TestFile(lineItem[0], lineItem[1], "");
				} else {
					testFile = new TestFile(lineItem[0], lineItem[1], lineItem[2]);
				}
				testFiles.add(testFile);
			}
		}

		return testFiles;
	}

	/**
	 * Run test smells analysis from a type
	 * 
	 * @param testSmellType (Assetion Roulette, Duplicate Assert, ...)
	 * @param testFiles
	 */
	private List<TestSmellDescription> detectTestSmellByType(String testSmellType, List<TestFile> testFiles)
			throws IOException {

		TestSmellDetector testSmellDetector = TestSmellDetector.createTestSmellDetector(testSmellType);
		ArrayList<TestSmellDescription> testSmellsDetected = new ArrayList<TestSmellDescription>();

		for (TestFile file : testFiles) {
			System.out.println("[" + testSmellType + "] Processing: " + file.getTestFilePath());
			testSmellDetector.detectSmells(file, testSmellType);
			testSmellsDetected.addAll(testSmellDetector.getLista());
		}
		return testSmellsDetected;
	}

	public void dispose() {
		super.dispose();
	}

	@Override
	public void setFocus() {
		if (!isAnalysisRunning) {
			runTestSmellsAnalysis();
		}
	}

	public void createPartControl(Composite parent) {
		ISelectionListener selectionListener = new ISelectionListener() {
			public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {
				currentSelection = selection;
			}
		};
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(selectionListener);
		JavaCore.addElementChangedListener(ElementChangedListener.getInstance());
	}
}