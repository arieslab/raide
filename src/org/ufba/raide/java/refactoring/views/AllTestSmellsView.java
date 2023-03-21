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
import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.revwalk.RevCommit;
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
	public String srcProject;

	public String getSrcProject() {
		return srcProject;
	}

	public void setSrcProject(String srcProject) {
		this.srcProject = srcProject;
	}

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
			testSmellsTypes.add(ConditionalTestLogicView.getMessageDialogTitle());
			testSmellsTypes.add(ConstructionInstallationView.getMessageDialogTitle());
			testSmellsTypes.add(DefaultTestView.getMessageDialogTitle());
			testSmellsTypes.add(DuplicateAssertView.getMessageDialogTitle());
			testSmellsTypes.add(EagerTestView.getMessageDialogTitle());
			testSmellsTypes.add(EmptyTestView.getMessageDialogTitle());
			testSmellsTypes.add(ExceptionCatchingThrowingView.getMessageDialogTitle());
			testSmellsTypes.add(GeneralFixtureView.getMessageDialogTitle());
			testSmellsTypes.add(IgnoredTestView.getMessageDialogTitle());
			testSmellsTypes.add(LazyTestView.getMessageDialogTitle());
			testSmellsTypes.add(MagicNumberTestView.getMessageDialogTitle());
			testSmellsTypes.add(MysteryGuestView.getMessageDialogTitle());
			testSmellsTypes.add(PrintStatementView.getMessageDialogTitle());
			testSmellsTypes.add(RedundantAssertionView.getMessageDialogTitle());
			testSmellsTypes.add(ResourceOptimismView.getMessageDialogTitle());
			testSmellsTypes.add(SensitiveEqualityView.getMessageDialogTitle());
			testSmellsTypes.add(SleepyTestView.getMessageDialogTitle());
			testSmellsTypes.add(UnknownTestView.getMessageDialogTitle());
			testSmellsTypes.add(VerboseTestView.getMessageDialogTitle());

			List<TestFile> projectTestFiles = getProjectTestFiles();
			File results = new File(resultsCsvFile);
			FileOutputStream fos = new FileOutputStream(results);
			String csvHeader = "Test Smell;Test Method;File Path;Begin;End;Commit Begin;Commit End;Source\n";
			fos.write(csvHeader.getBytes());

//			GitHelper gitHelper = new GitHelper("C:\\Users\\raila\\Documents\\Workspace\\maven-dependency-plugin".replace("\\", "/"));
			GitHelper gitHelper = new GitHelper(getSrcProject());
			
			for (String testSmellType : testSmellsTypes) {
				List<TestSmellDescription> testSmells = detectTestSmellByType(testSmellType, projectTestFiles);
				for (TestSmellDescription smellDetected : testSmells) {
					String log = smellDetected.getTestSmellType() + ";" + smellDetected.getFilePath() + ";"
							+ smellDetected.getMethodName().replaceAll("\n", "").trim() + ";"
							+ smellDetected.getLinePositionBegin() + ";" + smellDetected.getLinePositionEnd() + ";";

					try {

						String gitResult = gitHelper.getCommitHashes(smellDetected.getFilePath(),
								smellDetected.getLinePositionBegin(), smellDetected.getLinePositionEnd());
						log += gitResult + ";";
						
						String source = RAIDEUtils.copyMethodContent(smellDetected.getFilePath(), 
								RAIDEUtils.linesAsNumbers(smellDetected.getLinePositionBegin()), 
								RAIDEUtils.linesAsNumbers(smellDetected.getLinePositionEnd()));
						
						log += RAIDEUtils.encode(source) + "\n";
						//log += gitResult + "\n";
					} catch (GitAPIException e) {
						e.printStackTrace();
					}
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
					setSrcProject(diretorioPacote.replace("/src/test/java", ""));
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

		ArrayList<TestSmellDescription> testSmellsDetected = new ArrayList<TestSmellDescription>();

		for (TestFile file : testFiles) {
			System.out.println("[" + testSmellType + "] Processing: " + file.getTestFilePath());
			TestSmellDetector testSmellDetector = TestSmellDetector.createTestSmellDetector(testSmellType);
			testSmellDetector.detectSmells(file, testSmellType);
			testSmellsDetected.addAll(testSmellDetector.getLista());
			testSmellDetector.setLista(new ArrayList<>());
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

	class GitHelper {

		private Git git;
		private BlameResult blameResult;
		private String projectPath;

		public GitHelper(String projectPath) throws IOException {
			this.projectPath = projectPath;
			this.git = Git.open(new File(projectPath));
		}

		public String getCommitHashes(String testFilePath, String beginLine, String endLine) throws GitAPIException {
			String projectDir = projectPath.substring(projectPath.lastIndexOf("/"));
			String filePath = testFilePath.replace("\\", "/").split(projectDir)[1].substring(1);

			System.out.println("RUNNING GIT FOR " + filePath);

			BlameCommand blameCommand = new BlameCommand(git.getRepository()).setFilePath(filePath);
			this.blameResult = blameCommand.call();

			String[] beginLines = { beginLine };
			String[] endLines = { endLine };

			if (beginLine.contains(",")) {
				beginLines = beginLine.trim().split(",");
			}

			if (endLine.contains(",")) {
				endLines = endLine.trim().split(",");
			}

			String result = getCommitFromLines(beginLines) + ";" + getCommitFromLines(endLines);
			return result;
		}

		private String getCommitFromLines(String[] lines) {
			String commitHashes = "";
			for (int i = 0; i < lines.length; i++) {
				RevCommit commit = blameResult.getSourceCommit(Integer.valueOf(lines[i].trim()) - 1);
				String hash = commit.getName();
				if (!commitHashes.isEmpty()) {
					commitHashes += "," + hash;
				} else {
					commitHashes += hash;
				}
			}
			return commitHashes;
		}

	}
}