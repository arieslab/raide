package org.ufba.raide.java.refactoring.views;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.swing.JOptionPane;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.*;
import org.eclipse.ui.progress.IProgressService;
import org.apache.commons.lang3.text.StrTokenizer;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.internal.runtime.LocalizationUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.ufba.raide.Activator;
import org.ufba.raide.java.ast.ASTReader;
import org.ufba.raide.java.ast.ClassObject;
import org.ufba.raide.java.ast.CompilationErrorDetectedException;
import org.ufba.raide.java.ast.CompilationUnitCache;
import org.ufba.raide.java.ast.SystemObject;
import org.ufba.raide.java.clone.parsers.CloneInstance;
import org.ufba.raide.java.distance.AddExplanationCandidateRefactoring;
import org.ufba.raide.java.distance.CandidateRefactoring;
import org.ufba.raide.java.distance.DistanceMatrix;
import org.ufba.raide.java.distance.MethodExtractionCandidateRefactoring;
import org.ufba.raide.java.distance.MyClass;
import org.ufba.raide.java.distance.MyMethod;
import org.ufba.raide.java.distance.MySystem;
import org.ufba.raide.java.filedetector.RAIDEUtils;
import org.ufba.raide.java.filedetector.ResultsWriterFileDetector;
import org.ufba.raide.java.filedetector.TestFileDetectorMain;
import org.ufba.raide.java.filedetector.TrataStringCaminhoTeste;
import org.ufba.raide.java.filemapping.FileMappingMain;
import org.ufba.raide.java.preferences.PreferenceConstants;
import org.ufba.raide.java.refactoring.manipulators.ASTSlice;
import org.ufba.raide.java.refactoring.manipulators.MoveMethodRefactoring;
import org.ufba.raide.java.refactoring.views.AssertionRouletteView.AssertionParenteseLine;
import org.ufba.raide.java.refactoring.views.CodeSmellPackageExplorer.CodeSmellType;
import org.ufba.raide.java.testsmell.AbstractSmell;
import org.ufba.raide.java.testsmell.ResultsWriter;
import org.ufba.raide.java.testsmell.TestFile;
import org.ufba.raide.java.testsmell.TestSmellDescription;
import org.ufba.raide.java.testsmell.TestSmellDetector;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.core.builder.SourceFile;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;

public class DuplicateAssertView extends ViewPart {
	
	private static final String MESSAGE_DIALOG_TITLE = "Duplicate Assert";
	private TableViewer tableViewer;
	private TreeViewer treeViewer;
	private Action identifyBadSmellsAction;
	private Action doubleClickAction;
	private Action applyRefactoringAction;
	private Action packageExplorerAction;
	private IJavaProject selectedProject;
	private IJavaProject activeProject;
	private IPackageFragmentRoot selectedPackageFragmentRoot;
	private IPackageFragment selectedPackageFragment;
	private ICompilationUnit selectedCompilationUnit;
	private IType selectedType;
	private CandidateRefactoring[] candidateRefactoringTable;
	private IJavaProject project;
	private static int numMaximoDuplicate = 0;

    public static String getMessageDialogTitle() {
		return MESSAGE_DIALOG_TITLE;
	}

	private List<TestSmellDescription> testSmells;
    
    private String getNumberString(String myText) {
    	int tam = myText.length();
    	char[] vetor = myText.toCharArray();   
    	String novaString = "";
    	
    	for (int i = 0; i < tam; i++ ) {    		
    		if (Character.isDigit(vetor[i]) || (vetor[i] == ',') || (vetor[i] == ' '))
    			novaString += vetor[i]; 	
    	}
    	
    	return novaString;
    }
    
    private String getTextString(String myText) {
    	int tam = myText.length();
    	char[] vetor = myText.toCharArray();   
    	String novaString = "";
    	
    	for (int i = 0; i < tam; i++ ) {    		
    		if (Character.isLetter(vetor[i]))
    			novaString += vetor[i]; 	
    	}
    	
    	return novaString;
    }

	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			if(candidateRefactoringTable!=null) {
				return candidateRefactoringTable;
			}
			else {
				return new CandidateRefactoring[] {};
			}
		}
	}
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			CandidateRefactoring entry = (CandidateRefactoring)obj;
			switch(index){
				case 0:
					if(entry instanceof MethodExtractionCandidateRefactoring)
						return "Duplicate Assert";
				case 1:
					return getTextString(entry.getSourceEntity2()) + "( )";
				case 2:
					return getNumberString(entry.getSourceEntity2());
				case 3:
					return entry.getSourceClass().getFilePath();
				case 4:
					return "Method Extraction";
				default:
					return "";
			}

		}
		public Image getColumnImage(Object obj, int index) {
			Image image = null;
			return image;
		}
		public Image getImage(Object obj) {
			return null;
		}
	}
	class NameSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object obj1, Object obj2) {
			return 1;
		}
	}

	private ISelectionListener selectionListener = new ISelectionListener() {
		public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection structuredSelection = (IStructuredSelection)selection;
				Object element = structuredSelection.getFirstElement();
				IJavaProject javaProject = null;
				if(element instanceof IJavaProject) {
					javaProject = (IJavaProject)element;
					selectedPackageFragmentRoot = null;
					selectedPackageFragment = null;
					selectedCompilationUnit = null;
					selectedType = null;
				}
				else if(element instanceof IPackageFragmentRoot) {
					IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot)element;
					javaProject = packageFragmentRoot.getJavaProject();
					selectedPackageFragmentRoot = packageFragmentRoot;
					selectedPackageFragment = null;
					selectedCompilationUnit = null;
					selectedType = null;
				}
				else if(element instanceof IPackageFragment) {
					IPackageFragment packageFragment = (IPackageFragment)element;
					javaProject = packageFragment.getJavaProject();
					selectedPackageFragment = packageFragment;
					selectedPackageFragmentRoot = null;
					selectedCompilationUnit = null;
					selectedType = null;
				}
				else if(element instanceof ICompilationUnit) {
					ICompilationUnit compilationUnit = (ICompilationUnit)element;
					javaProject = compilationUnit.getJavaProject();
					selectedCompilationUnit = compilationUnit;
					selectedPackageFragmentRoot = null;
					selectedPackageFragment = null;
					selectedType = null;
				}
				else if(element instanceof IType) {
					IType type = (IType)element;
					javaProject = type.getJavaProject();
					selectedType = type;
					selectedPackageFragmentRoot = null;
					selectedPackageFragment = null;
					selectedCompilationUnit = null;
				}
				if(javaProject != null && element instanceof IPackageFragmentRoot) {
					setProject(javaProject);
					//Primeiro verifica se o diret�rio do pacote 
					IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot)element;
					String diretorioPacote = packageFragmentRoot.getResource().getLocation().toString();

					//Depois verificados se � um pacote v�lido
					boolean valido = new TrataStringCaminhoTeste().diretorioTesteValido(diretorioPacote);
					
					//Se for v�lido, ativamos a op��o
					identifyBadSmellsAction.setEnabled(valido);						
				}
			}
		}
	};

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		/* Ordem de apresenta��o:
		 * 1� Coluna: TestSmell
		 * 2� Coluna: Source Method
		 * 3� Coluna: Linha
		 * 4� Coluna: Refactoring Type		 * 
		 * 5� Coluna: Caminho do arquivo
		 * */
		tableViewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		tableViewer.setContentProvider(new ViewContentProvider());
		tableViewer.setLabelProvider(new ViewLabelProvider());
		tableViewer.setSorter(new NameSorter());
		tableViewer.setInput(getViewSite());
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(15, true));
		layout.addColumnData(new ColumnWeightData(25, true));
		layout.addColumnData(new ColumnWeightData(8, true));
		layout.addColumnData(new ColumnWeightData(15, true));
		layout.addColumnData(new ColumnWeightData(20, true));
		tableViewer.getTable().setLayout(layout);
		tableViewer.getTable().setLinesVisible(true);
		tableViewer.getTable().setHeaderVisible(true);
		TableColumn column0 = new TableColumn(tableViewer.getTable(),SWT.LEFT);
		column0.setText("Test Smell");
		column0.setResizable(true);
		column0.pack();
		TableColumn column1 = new TableColumn(tableViewer.getTable(),SWT.LEFT);
		column1.setText("Source Method");
		column1.setResizable(true);
		column1.pack();
		TableColumn column2 = new TableColumn(tableViewer.getTable(),SWT.LEFT);
		column2.setText("Lines");
		column2.setResizable(true);
		column2.pack();
		TableColumn column3 = new TableColumn(tableViewer.getTable(),SWT.LEFT);
		column3.setText("File Path");
		column3.setResizable(true);
		column3.pack();
		TableColumn column4 = new TableColumn(tableViewer.getTable(),SWT.LEFT);
		column4.setText("Refactoring Type");
		column4.setResizable(true);
		column4.pack();
		
		
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				tableViewer.getTable().setMenu(null);
				IStructuredSelection selection = (IStructuredSelection)tableViewer.getSelection();
				if(selection instanceof IStructuredSelection) {
					IStructuredSelection structuredSelection = (IStructuredSelection)selection;
					Object[] selectedItems = structuredSelection.toArray();
					if(selection.getFirstElement() instanceof AddExplanationCandidateRefactoring && selectedItems.length == 1) {
						AddExplanationCandidateRefactoring candidateRefactoring = (AddExplanationCandidateRefactoring)selection.getFirstElement();
						tableViewer.getTable().setMenu(getRightClickMenu(tableViewer, candidateRefactoring));
					}
				}
			}
		});

		tableViewer.setColumnProperties(new String[] {"type", "source", "target", "ep", "rate"});
		tableViewer.setCellEditors(new CellEditor[] {
				new TextCellEditor(), new TextCellEditor(), new TextCellEditor(), new TextCellEditor(),
				new MyComboBoxCellEditor(tableViewer.getTable(), new String[] {"0", "1"}, SWT.READ_ONLY)
		});

		tableViewer.setCellModifier(new ICellModifier() {
			public boolean canModify(Object element, String property) {
				return property.equals("rate");
			}

			public Object getValue(Object element, String property) {
				if(element instanceof AddExplanationCandidateRefactoring) {
					AddExplanationCandidateRefactoring candidate = (AddExplanationCandidateRefactoring)element;
					if(candidate.getUserRate() != null)
						return candidate.getUserRate();
					else
						return 0;
				}
				return 0;
			}

			public void modify(Object element, String property, Object value) {
				TableItem item = (TableItem)element;
				Object data = item.getData();
				if(data instanceof AddExplanationCandidateRefactoring) {
					AddExplanationCandidateRefactoring candidate = (AddExplanationCandidateRefactoring)data;
					candidate.setUserRate((Integer)value);
					IPreferenceStore store = Activator.getDefault().getPreferenceStore();
					boolean allowUsageReporting = store.getBoolean(PreferenceConstants.P_ENABLE_USAGE_REPORTING);
					if(allowUsageReporting) {
						Table table = tableViewer.getTable();
						int rankingPosition = -1;
						for(int i=0; i<table.getItemCount(); i++) {
							TableItem tableItem = table.getItem(i);
							if(tableItem.equals(item)) {
								rankingPosition = i;
								break;
							}
						}
						try {
							boolean allowSourceCodeReporting = store.getBoolean(PreferenceConstants.P_ENABLE_SOURCE_CODE_REPORTING);
							String declaringClass = candidate.getSourceClassTypeDeclaration().resolveBinding().getQualifiedName();
							String methodName = candidate.getSourceMethodDeclaration().resolveBinding().toString();
							String sourceMethodName = declaringClass + "::" + methodName;
							String content = URLEncoder.encode("project_name", "UTF-8") + "=" + URLEncoder.encode(activeProject.getElementName(), "UTF-8");
							content += "&" + URLEncoder.encode("source_method_name", "UTF-8") + "=" + URLEncoder.encode(sourceMethodName, "UTF-8");
							content += "&" + URLEncoder.encode("target_class_name", "UTF-8") + "=" + URLEncoder.encode(candidate.getTarget(), "UTF-8");
							content += "&" + URLEncoder.encode("ranking_position", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(rankingPosition), "UTF-8");
							content += "&" + URLEncoder.encode("total_opportunities", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(table.getItemCount()), "UTF-8");
							content += "&" + URLEncoder.encode("EP", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(0.0), "UTF-8");
							content += "&" + URLEncoder.encode("envied_elements", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(candidate.getNumberOfDistinctEnviedElements()), "UTF-8");
							if(allowSourceCodeReporting)
								content += "&" + URLEncoder.encode("source_method_code", "UTF-8") + "=" + URLEncoder.encode(candidate.getSourceMethodDeclaration().toString(), "UTF-8");
							content += "&" + URLEncoder.encode("rating", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(candidate.getUserRate()), "UTF-8");
							content += "&" + URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(System.getProperty("user.name"), "UTF-8");
							content += "&" + URLEncoder.encode("tb", "UTF-8") + "=" + URLEncoder.encode("0", "UTF-8");
							URL url = new URL(Activator.RANK_URL);
							URLConnection urlConn = url.openConnection();
							urlConn.setDoInput(true);
							urlConn.setDoOutput(true);
							urlConn.setUseCaches(false);
							urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
							DataOutputStream printout = new DataOutputStream(urlConn.getOutputStream());
							printout.writeBytes(content);
							printout.flush();
							printout.close();
							DataInputStream input = new DataInputStream(urlConn.getInputStream());
							input.close();
						} catch (IOException ioe) {
							ioe.printStackTrace();
						}
					}
					tableViewer.update(data, null);
				}
			}
		});

		makeActions();
		hookDoubleClickAction();
		contributeToActionBars();
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(selectionListener);
		JavaCore.addElementChangedListener(ElementChangedListener.getInstance());
		

		JFaceResources.getFontRegistry().put(MyToolTip.HEADER_FONT, JFaceResources.getFontRegistry().getBold(JFaceResources.getDefaultFont().getFontData()[0].getName()).getFontData());
		MyToolTip toolTip = new MyToolTip(tableViewer.getControl());
		toolTip.setShift(new Point(-5, -5));
		toolTip.setHideOnMouseDown(false);
		toolTip.activate();
	}

	private Menu getRightClickMenu(TableViewer tableViewer, final AddExplanationCandidateRefactoring candidateRefactoring) {
		Menu popupMenu = new Menu(tableViewer.getControl());
		MenuItem textualDiffMenuItem = new MenuItem(popupMenu, SWT.NONE);
		textualDiffMenuItem.setText("Visualize Code Smell");
		textualDiffMenuItem.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent arg) {
				CodeSmellVisualizationDataSingleton.setData(candidateRefactoring.getFeatureEnvyVisualizationData());
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IViewPart viewPart = page.findView(CodeSmellVisualization.ID);
				if(viewPart != null)
					page.hideView(viewPart);
				try {
					page.showView(CodeSmellVisualization.ID);
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
			public void widgetDefaultSelected(SelectionEvent arg) {}
		});
		popupMenu.setVisible(false);
		return popupMenu;
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(identifyBadSmellsAction);
		manager.add(applyRefactoringAction);
	}
	private void callDuplicateAssert() throws IOException{
		TestSmellDetector testSmellDetector = TestSmellDetector.createTestSmellDetector(getMessageDialogTitle());
        BufferedReader in = null;
        
        //tratando caminho do arquivo para pegar dinamicamente
        URI myURI = project.getResource().getLocationURI();
		String strURI = new TrataStringCaminhoTeste().removeFileString(myURI.toString());
		String strURIFinal = new TrataStringCaminhoTeste().inverteDuplicaBarraURL(strURI);        
        
        /* ********************    N�AAO APAGAR ******************
         * TestFileDetectorMain.detect(strURIFinal);
         * FileMappingMain.detect(strURIFinal);
         * */   
		
		TestFileDetectorMain.detect(RAIDEUtils.firstPathSeparator() + strURIFinal);
        FileMappingMain.detect(RAIDEUtils.firstPathSeparator() + strURIFinal);		
        
        // retorna \\ se for windows, ou / caso não for
        String nameFile = RAIDEUtils.firstPathSeparator() + strURIFinal + 
        		RAIDEUtils.pathSeparator() + new TrataStringCaminhoTeste().getFILE_MAPPING();
        
        //String nameFile = strURIFinal+ RAIDEUtils.pathSeparator() + new TrataStringCaminhoTeste().getFILE_MAPPING();
                
		try {			
			in = new BufferedReader(new FileReader(nameFile));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
        String str;
        

        String[] lineItem;
        TestFile testFile;
        List<TestFile> testFiles = new ArrayList<>();
        while ((str = in.readLine()) != null) {
            lineItem = str.split(",");
            if(lineItem.length ==2){
                testFile = new TestFile(lineItem[0], lineItem[1], "");
            }
            else{
                testFile = new TestFile(lineItem[0], lineItem[1], lineItem[2]);
            }
            testFiles.add(testFile);
        }
       
        for (TestFile file : testFiles) {
            System.out.println("Processing: "+file.getTestFilePath());
            testSmellDetector.detectSmells(file, getMessageDialogTitle());
            testSmells = testSmellDetector.getLista();
        }
        System.out.println("end");
		
	}
	
	private ArrayList<Integer> extraiNumeroLinhas(String todasLinhas){
		ArrayList<Integer> num = new ArrayList<Integer>();
		char[] stringToCharArray = todasLinhas.toCharArray();
		String aux = "";
		
		int inicio = 0;
		int fim = -1;
		for (int i = 0; i < todasLinhas.length(); i++) {
			if (stringToCharArray[i] == ',') {
				inicio = fim + 1;
				fim = i - 1;
				aux = "";
				for (int j = inicio; j <= fim; j++) {
					aux += stringToCharArray[j];
				}
				num.add(Integer.valueOf(aux.toString()));
				fim = i + 1;
			}
		}
		aux = "";
		for (int j = fim + 1; j < todasLinhas.length(); j++) {
			aux += stringToCharArray[j];
		}
		num.add(Integer.valueOf(aux.toString()));

		return num;
	}
	

	private void makeActions() {
		identifyBadSmellsAction = new Action() {
			public void run() {
				
				boolean wasAlreadyOpen = false;
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IViewPart viewPart = page.findView(CodeSmellPackageExplorer.ID);
				if(viewPart != null) {
					page.hideView(viewPart);
					wasAlreadyOpen = true;
				}
		
				activeProject = getProject();
				CompilationUnitCache.getInstance().clearCache();
				candidateRefactoringTable = getTable();
					
				tableViewer.setContentProvider(new ViewContentProvider());
				packageExplorerAction.setEnabled(true);
				if(wasAlreadyOpen)
					openPackageExplorerViewPart();
				if (candidateRefactoringTable == null || candidateRefactoringTable.length == 0 ) {
					JOptionPane.showMessageDialog(null, "Duplicate Asserts not found.");				
				}
			}
		};
		identifyBadSmellsAction.setToolTipText("Identify Test Smells");
		identifyBadSmellsAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		identifyBadSmellsAction.setEnabled(false);

		packageExplorerAction = new Action(){
			public void run() {
				
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IViewPart viewPart = page.findView(CodeSmellPackageExplorer.ID);
				if(viewPart == null/* || !CodeSmellPackageExplorer.CODE_SMELL_TYPE.equals(CodeSmellType.FEATURE_ENVY)*/)
					openPackageExplorerViewPart();
			}
		};
		packageExplorerAction.setToolTipText("Code Smell Package Explorer");
		packageExplorerAction.setImageDescriptor(Activator.getImageDescriptor("/icons/" + "compass.png"));
		packageExplorerAction.setEnabled(false);

		doubleClickAction = new Action() {
			public void run() {
				IStructuredSelection selection = (IStructuredSelection)tableViewer.getSelection();
				CandidateRefactoring candidate = (CandidateRefactoring)selection.getFirstElement();
				
				String filePath = candidate.getSourceClass().getFilePath();
				File file = new File(filePath);
				URI location = file.toURI();

				IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(location);					
				IFile sourceFile = files[0];
				
				try {
				
					IJavaElement sourceJavaElement = JavaCore.create(sourceFile);
					ITextEditor sourceEditor = (ITextEditor)JavaUI.openInEditor(sourceJavaElement);
					ArrayList<Position> positions = new ArrayList<Position>();
					
					//int num = 0;

					ArrayList<Integer> num = extraiNumeroLinhas(candidate.getLineNumber());
							 
					//num = Integer.valueOf(candidate.getLineNumber());
					try {
						//positions.add(getPositioAssertion(filePath, num));
						for (int i = 0; i < num.size(); i ++) {
							positions.add(getPositioAssertion(filePath, num.get(i)));
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					AnnotationModel annotationModel = (AnnotationModel)sourceEditor.getDocumentProvider().getAnnotationModel(sourceEditor.getEditorInput());
					Iterator<Annotation> annotationIterator = annotationModel.getAnnotationIterator();
					while(annotationIterator.hasNext()) {
						Annotation currentAnnotation = annotationIterator.next();
						if(currentAnnotation.getType().equals(SliceAnnotation.EXTRACTION)) {
							annotationModel.removeAnnotation(currentAnnotation);
						}
					}
					String texto = "Duplicate Assert occurs when a test method tests for the same condition multiple times within the same test method. If the test method needs to test the same condition using different values, a new test method should be created. ";
					for(Position position : positions) {
						SliceAnnotation annotation = new SliceAnnotation(SliceAnnotation.EXTRACTION, texto);
						annotationModel.addAnnotation(annotation, position);
					}
					Position firstPosition = positions.get(0);
					Position lastPosition = positions.get(positions.size()-1);
					int offset = firstPosition.getOffset();
					int length = lastPosition.getOffset() + lastPosition.getLength() - firstPosition.getOffset();
					sourceEditor.setHighlightRange(offset, length, true);
				} catch (PartInitException e) {
					e.printStackTrace();
				} catch (JavaModelException e) {
					e.printStackTrace();
				} finally {
					applyRefactoringAction.setEnabled(true);
				}
			}
		};
		
		applyRefactoringAction = new Action() {
			
			public void run() {
				numMaximoDuplicate = 0;
				IStructuredSelection selection = (IStructuredSelection)tableViewer.getSelection();
				CandidateRefactoring candidate = (CandidateRefactoring)selection.getFirstElement();
				
				String filePath = candidate.getSourceClass().getFilePath();
				File file = new File(filePath);
				URI location = file.toURI();

				IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(location);					
				IFile sourceFile = files[0];
				
				int linhaInicialMetodo = candidate.getBeginMethod();
				int linhaFinalMetodo = candidate.getEndMethod();
				int tamanhoVetor = linhaFinalMetodo - linhaInicialMetodo + 1;
				int vetorLinhas[] = new int[tamanhoVetor];
				String nomeMetodo = getTextString(candidate.getSourceEntity2()) + "( )";
				List<TestSmellDescription> lista = extraiSmellsMetodo(nomeMetodo);
				vetorLinhas = mapeiaAsserDuplicados(tamanhoVetor, lista, linhaInicialMetodo);
				//MapearLinahs
				try {					 
					String newMethods = copyMethod(filePath, linhaInicialMetodo, linhaFinalMetodo);
					String oldMethod = newMethods;
					String oldMethodCommented = "\t/* \n" + oldMethod + "*/ \n \n" + "\t/* Refactored method by RAIDE */\n";
	
					oldMethod = oldMethodCommented + eliminaDuplicaoes(oldMethod, vetorLinhas);
					//oldMethod = eliminaDuplicaoes(oldMethod, vetorLinhas);
					newMethods = createNewMethods(newMethods, vetorLinhas, nomeMetodo);
					
					ArrayList<Position> positions = new ArrayList<Position>();
					try {
						positions.add(getPositioMethod(filePath, linhaInicialMetodo, linhaFinalMetodo));
					} catch (IOException e) {
						e.printStackTrace();
					}
					//*********************************
					IJavaElement sourceJavaElement = JavaCore.create(sourceFile);
					ITextEditor sourceEditor = (ITextEditor)JavaUI.openInEditor(sourceJavaElement);
					
					AnnotationModel annotationModel = (AnnotationModel)sourceEditor.getDocumentProvider().getAnnotationModel(sourceEditor.getEditorInput());
					Iterator<Annotation> annotationIterator = annotationModel.getAnnotationIterator();
					while(annotationIterator.hasNext()) {
						Annotation currentAnnotation = annotationIterator.next();
						if(currentAnnotation.getType().equals(SliceAnnotation.EXTRACTION)) {
							annotationModel.removeAnnotation(currentAnnotation);
						}
					}
					String texto = "Duplicate Assert occurs when a test method tests for the same condition multiple times within the same test method. If the test method needs to test the same condition using different values, a new test method should be created. ";
					
					for(Position position : positions) {
						SliceAnnotation annotation = new SliceAnnotation(SliceAnnotation.EXTRACTION, texto);
						annotationModel.addAnnotation(annotation, position);
					}
					Position firstPosition = positions.get(0);
					int offset = firstPosition.getOffset();
					int length = firstPosition.getLength() + vetorLinhas.length;
					//sourceEditor.setHighlightRange(offset, length, true);
					
					
					IDocumentProvider docProvider = sourceEditor.getDocumentProvider();
					IDocument classeDocument = docProvider.getDocument(sourceEditor.getEditorInput());
					
					try {
						classeDocument.replace(offset, length, oldMethod + newMethods);	
						docProvider.saveDocument(new NullProgressMonitor(), new FileEditorInput(sourceFile), classeDocument, true);
						//sourceEditor.setHighlightRange(offset, length, true);	
					} catch (BadLocationException | CoreException e1) {
						e1.printStackTrace();
					}
					
					//*********************************
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JavaModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		applyRefactoringAction.setToolTipText("Apply Refactoring");
		applyRefactoringAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_DEF_VIEW));
		applyRefactoringAction.setEnabled(false);
	}
	private String eliminaDuplicaoes(String str, int[] vetorLinhas  ) {
		String[] conteudoLinhas = str.split("\n");
		String resultado = "";
		
		for(int i = 0; i < vetorLinhas.length ; i ++ ) {
			if (vetorLinhas[i] <= 1) {
				resultado += conteudoLinhas[i] + "\n"; 
			}
		}		
		return resultado;
	}
	
	private String createNewMethods(String str, int[] vetorLinhas, String nomeMetodo) {
		
		//Corrigindo nome do m�todo
		nomeMetodo = nomeMetodo.replace("(", "");
		nomeMetodo = nomeMetodo.replace(")", "");
		nomeMetodo = nomeMetodo.replace(" ", "");
		String nomeNovoMetodo = nomeMetodo + "Extracted";
		
		String aux = "";
		String resultado = "";
		
		String[] conteudoLinhas = str.split("\n");
		
		for(int i = 2; i <= numMaximoDuplicate; i ++) {
			aux = "";
			for (int j = 0; j < conteudoLinhas.length; j++) {
				if (vetorLinhas[j] == 0 || vetorLinhas[j] == i) {
					aux += conteudoLinhas[j] + "\n"; 
				}
			}
			int extraido = i - 1;
			aux = aux.replace(nomeMetodo + "()", nomeNovoMetodo + extraido + "()");
			resultado +=  "\n\t/* Refactored method by RAIDE */\n" + aux + "\n";
			//resultado += aux + "\n\n";
		}		
		//JOptionPane.showConfirmDialog(null, resultado);
		return resultado;
	}
	public Position getPositioAssertion(String path, int line) throws IOException {
		int inicio = 0, tamanho = 0, contaLinha, caracteres;
		
		contaLinha = 1;
		caracteres = 0;
		
		File file  = new File(path);
		BufferedReader  leitor = new BufferedReader(new FileReader(file));
		String st;
		while ((st = leitor.readLine()) != null) {
			caracteres += st.length() +1 ;
			if (contaLinha == line) {
				tamanho = st.length();
				inicio = (int) (caracteres - tamanho);
				break;
			}
			contaLinha++;
		} 		
		return new Position(inicio, tamanho);
		
	}
	private List<TestSmellDescription> extraiSmellsMetodo(String nomeMetodo){       

		List<TestSmellDescription> lista = new ArrayList<TestSmellDescription>();
		nomeMetodo = nomeMetodo.replace(" ", "");
		for(TestSmellDescription smell : testSmells) {
			if (smell.getMethodName().equals(nomeMetodo + " \n"))
				lista.add(smell);
		}  
	    return lista;
    }
	private static int [] mapeiaAsserDuplicados(int tam, List<TestSmellDescription> lista, int linhaInicial){
        int teste[] = new int [tam];
        int diferencaLinha = linhaInicial;
        
        //Seta todas as linhas como ZERO
        for (int i = 0; i < tam; i++){
            teste[i] = 0;
        }
        //Seta a ordem do assert, 1: original, 2: primeira c�pia, etc....
        
        for (TestSmellDescription smell: lista){
        	//int vetorSmellLinha[] = {109, 113};
        	String aux = smell.getLinePositionBegin().replace(" ", "");
        	int vetorSmellLinha[] = stringToIntArray(aux);
        	for (int j = 0; j < vetorSmellLinha.length ; j++) {
        		teste[vetorSmellLinha[j] - diferencaLinha] = j + 1;
        	}        	
        }        
        return teste;
    }
	public static int [] stringToIntArray(String descricaoLinhas) {
		ArrayList<Integer> arrayInt = new ArrayList();
		 
		String[] arrayString = descricaoLinhas.split(",");
		int tamanhoArrayStr = arrayString.length;
		
		if (tamanhoArrayStr > numMaximoDuplicate) {
			numMaximoDuplicate = tamanhoArrayStr;
		}
		
		int vetor[] = new int [tamanhoArrayStr];
		for(int i = 0; i < tamanhoArrayStr ; i++){
		    vetor[i] = Integer.parseInt(arrayString[i]);
		}
		 
		return vetor;
	}
	
	public String copyMethod(String path, int begin, int end) throws IOException {
		int inicio = 0, tamanho = 0, contaLinha, caracteres;
		
		contaLinha = 1;
		caracteres = 0;
		
		File file  = new File(path);
		BufferedReader  leitor = new BufferedReader(new FileReader(file));
		String st;
		String strMethod = "";
		while ((st = leitor.readLine()) != null) {
			caracteres += st.length() +1 ;
			if (contaLinha == begin) {				
				tamanho = st.length();
				inicio = (int) (caracteres - tamanho);
			}
			if (contaLinha >= begin && contaLinha < end) {
				strMethod += st + "\n";				
			}
			if (contaLinha == end) {				
				strMethod += st;
				break;
			}
			contaLinha++;
		} 		
		//return new Position(inicio, tamanho);
		return strMethod;
	}
	
	public Position getPositioMethod(String path, int linhaInicial, int linhaFinal) throws IOException {
		int inicio = 0, tamanho = 0, contaLinha, caracteres;
		
		contaLinha = 1;
		caracteres = 0;
		
		File file  = new File(path);
		BufferedReader leitor = new BufferedReader(new FileReader(file));
		String st;
		while ((st = leitor.readLine()) != null) {
			caracteres += st.length() +1 ;
			if (contaLinha == linhaInicial) { 		
				inicio = (int) (caracteres - st.length());				
			}
			if (contaLinha >= linhaInicial && contaLinha <= linhaFinal)
				tamanho += st.length();
			if (contaLinha == linhaFinal) 
				break;
		
			contaLinha++;
		} 		
		return new Position(inicio, tamanho);
	}
	public IFile fileToIfile(File file) {
		 	 
		 IFile iFile = ResourcesPlugin.getWorkspace().getRoot().getFile((IPath) file);
		 return iFile;

	}

	private void hookDoubleClickAction() {
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		tableViewer.getControl().setFocus();
	}

	public void dispose() {
		super.dispose();
		getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(selectionListener);
	}

	private List<CandidateRefactoring> getPrerequisiteRefactorings(CandidateRefactoring candidateRefactoring) {
		List<CandidateRefactoring> moveMethodPrerequisiteRefactorings = new ArrayList<CandidateRefactoring>();
		List<CandidateRefactoring> extractMethodPrerequisiteRefactorings = new ArrayList<CandidateRefactoring>();
		if(candidateRefactoringTable != null) {
			Set<String> entitySet = candidateRefactoring.getEntitySet();
			for(CandidateRefactoring candidate : candidateRefactoringTable) {
				if(candidate instanceof MethodExtractionCandidateRefactoring) {
					if(entitySet.contains(candidate.getSourceEntity())/* && candidateRefactoring.getTarget().equals(candidate.getTarget())*/)
						moveMethodPrerequisiteRefactorings.add(candidate);
				}
			}
		}
		if(!moveMethodPrerequisiteRefactorings.isEmpty())
			return moveMethodPrerequisiteRefactorings;
		else
			return extractMethodPrerequisiteRefactorings;
	}

	private CandidateRefactoring[] getTable() {
		CandidateRefactoring[] table = null;
		try {
			IWorkbench wb = PlatformUI.getWorkbench();
			IProgressService ps = wb.getProgressService();
			if(ASTReader.getSystemObject() != null && activeProject.equals(ASTReader.getExaminedProject())) {
				new ASTReader(activeProject, ASTReader.getSystemObject(), null);
			}
			else {
				ps.busyCursorWhile(new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							new ASTReader(activeProject, monitor);
						} catch (CompilationErrorDetectedException e) {
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), MESSAGE_DIALOG_TITLE,
											"Compilation errors were detected in the project. Fix the errors before using JDeodorant.");
								}
							});
						}
					}
				});
			}
			//Worker-0: Decoration Calculation
			SystemObject systemObject = ASTReader.getSystemObject();
			if(systemObject != null) {
				Set<ClassObject> classObjectsToBeExamined = new LinkedHashSet<ClassObject>();
				if(selectedPackageFragmentRoot != null) {
					classObjectsToBeExamined.addAll(systemObject.getClassObjects(selectedPackageFragmentRoot));
				}
				else if(selectedPackageFragment != null) {
					classObjectsToBeExamined.addAll(systemObject.getClassObjects(selectedPackageFragment));
				}
				else if(selectedCompilationUnit != null) {
					classObjectsToBeExamined.addAll(systemObject.getClassObjects(selectedCompilationUnit));
				}
				else if(selectedType != null) {
					classObjectsToBeExamined.addAll(systemObject.getClassObjects(selectedType));
				}
				else {
					classObjectsToBeExamined.addAll(systemObject.getClassObjects());
				}
				final Set<String> classNamesToBeExamined = new LinkedHashSet<String>();
				for(ClassObject classObject : classObjectsToBeExamined) {
					if(!classObject.isEnum() && !classObject.isInterface() && !classObject.isGeneratedByParserGenenator())
						classNamesToBeExamined.add(classObject.getName());
				}
				
				MySystem system = new MySystem(systemObject, false);
				
				final DistanceMatrix distanceMatrix = new DistanceMatrix(system);
				final List<MethodExtractionCandidateRefactoring> moveMethodCandidateList = new ArrayList<MethodExtractionCandidateRefactoring>();
				
				try {
					callDuplicateAssert();
				} catch (IOException e) {
					e.printStackTrace();
				}				
				MethodExtractionCandidateRefactoring addExp;
				int n = testSmells.size();
				for (int i = 0; i< n; i++) {
					String superClass = testSmells.get(i).getClass().getSuperclass().getName();
					
					MyClass minhaClasse = new MyClass(
							testSmells.get(i).getClassName(), 
							testSmells.get(i).getFilePath(), 
							superClass, 
							new ClassObject());
					
					MyClass minhaOutraClasse = new MyClass(testSmells.get(i).getClassName(), testSmells.get(i).getClassName());
					MyMethod meuMeuMetodo = new MyMethod(testSmells.get(i).getClassName(), testSmells.get(i).getMethodName() + testSmells.get(i).getLinePositionBegin(), "");
					addExp = new MethodExtractionCandidateRefactoring(system, minhaClasse, minhaOutraClasse, meuMeuMetodo, testSmells.get(i).getLinePositionBegin(), testSmells.get(i).getBeginMethod(), testSmells.get(i).getEndMethod()  );
					moveMethodCandidateList.add(addExp);
				}
				table = new CandidateRefactoring[moveMethodCandidateList.size()];
				int counter = 0;
				for(MethodExtractionCandidateRefactoring candidate : moveMethodCandidateList) {
					table[counter] = candidate;
					counter++;
				}
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (CompilationErrorDetectedException e) {
			MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), MESSAGE_DIALOG_TITLE,
					"Compilation errors were detected in the project. Fix the errors before using JDeodorant.");
		}

		return table;	

	}

	protected class MyToolTip extends ToolTip {
		public static final String HEADER_FONT = Policy.JFACE + ".TOOLTIP_HEAD_FONT";

		public MyToolTip(Control control) {
			super(control);
		}

		protected Composite createToolTipContentArea(Event event, Composite parent) {
			Composite comp = new Composite(parent,SWT.NONE);
			GridLayout gl = new GridLayout(1,false);
			gl.marginBottom=0;
			gl.marginTop=0;
			gl.marginHeight=0;
			gl.marginWidth=0;
			gl.marginLeft=0;
			gl.marginRight=0;
			gl.verticalSpacing=1;
			comp.setLayout(gl);

			Composite topArea = new Composite(comp,SWT.NONE);
			GridData data = new GridData(SWT.FILL,SWT.FILL,true,false);
			data.widthHint=200;
			topArea.setLayoutData(data);
			topArea.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));

			gl = new GridLayout(1,false);
			gl.marginBottom=2;
			gl.marginTop=2;
			gl.marginHeight=0;
			gl.marginWidth=0;
			gl.marginLeft=5;
			gl.marginRight=2;

			topArea.setLayout(gl);

			Label label = new Label(topArea,SWT.NONE);
			label.setText("APPLY FIRST");
			label.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			label.setFont(JFaceResources.getFontRegistry().get(HEADER_FONT));
			//label.setForeground(JFaceResources.getColorRegistry().get(HEADER_FG_COLOR));
			label.setLayoutData(new GridData(GridData.FILL_BOTH));

			Table table = tableViewer.getTable();
			Point coords = new Point(event.x, event.y);
			TableItem item = table.getItem(coords);
			if(item != null) {
				List<CandidateRefactoring> prerequisiteRefactorings = getPrerequisiteRefactorings((CandidateRefactoring)item.getData());
				if(!prerequisiteRefactorings.isEmpty()) {
					final CandidateRefactoring firstPrerequisite = prerequisiteRefactorings.get(0);
					Composite comp2 = new Composite(comp,SWT.NONE);
					comp2.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
					FillLayout layout = new FillLayout();
					layout.marginWidth=5;
					comp2.setLayout(layout);
					Link link = new Link(comp2,SWT.NONE);
					link.setText("<a>" + firstPrerequisite.getSourceEntity() + "\n->" + firstPrerequisite.getTarget() + "</a>");
					link.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
					link.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							setSelectedLine(firstPrerequisite);
						}
					});
					comp2.setLayoutData(new GridData(GridData.FILL_BOTH));
				}
			}
			return comp;
		}

		protected boolean shouldCreateToolTip(Event event) {
			Table table = tableViewer.getTable();
			Point coords = new Point(event.x, event.y);
			TableItem item = table.getItem(coords);
			if(item != null) {
				List<CandidateRefactoring> prerequisiteRefactorings = getPrerequisiteRefactorings((CandidateRefactoring)item.getData());
				if(!prerequisiteRefactorings.isEmpty())
					return true;
			}
			return false;
		}
	}

	private void saveResults() {
		FileDialog fd = new FileDialog(getSite().getWorkbenchWindow().getShell(), SWT.SAVE);
		fd.setText("Save Results");
		String[] filterExt = { "*.txt" };
		fd.setFilterExtensions(filterExt);
		String selected = fd.open();
		if(selected != null) {
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(selected));
				Table table = tableViewer.getTable();
				TableColumn[] columns = table.getColumns();
				for(int i=0; i<columns.length; i++) {
					if(i == columns.length-1)
						out.write(columns[i].getText());
					else
						out.write(columns[i].getText() + "\t");
				}
				out.newLine();
				for(int i=0; i<table.getItemCount(); i++) {
					TableItem tableItem = table.getItem(i);
					for(int j=0; j<table.getColumnCount(); j++) {
						if(j == table.getColumnCount()-1)
							out.write(tableItem.getText(j));
						else
							out.write(tableItem.getText(j) + "\t");
					}
					out.newLine();
				}
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}



	public void setSelectedLine(final CandidateRefactoring candidateRefactoring) {
		Table table = tableViewer.getTable();
		for(int i=0; i<table.getItemCount(); i++) {
			Object tableElement = tableViewer.getElementAt(i);
			CandidateRefactoring candidate = (CandidateRefactoring)tableElement;
			if(candidate.equals(candidateRefactoring)) {
				table.setSelection(i);
				break;
			}
		}
	}

	private void openPackageExplorerViewPart() {
		try {
			CodeSmellVisualizationDataSingleton.setCandidates(candidateRefactoringTable);
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IViewPart viewPart = page.findView(CodeSmellPackageExplorer.ID);
			CodeSmellPackageExplorer.CODE_SMELL_TYPE = CodeSmellType.FEATURE_ENVY;
			if(viewPart != null)
				page.hideView(viewPart);
			page.showView(CodeSmellPackageExplorer.ID);

		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
	public IJavaProject getProject() {
		return project;
	}

	public void setProject(IJavaProject javaProject) {
		this.project = javaProject;
	}
}