package org.ufba.raide.java.refactoring.views;

import java.awt.Color;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.*;
import org.eclipse.ui.progress.IProgressService;
import org.apache.commons.lang3.text.StrTokenizer;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.internal.runtime.LocalizationUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
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
import org.ufba.raide.java.distance.ExtractClassCandidateGroup;
import org.ufba.raide.java.distance.ExtractClassCandidateRefactoring;
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
import org.ufba.raide.java.refactoring.manipulators.ExtractClassRefactoring;
import org.ufba.raide.java.refactoring.manipulators.MoveMethodRefactoring;
import org.ufba.raide.java.refactoring.views.CodeSmellPackageExplorer.CodeSmellType;
import org.ufba.raide.java.refactoring.views.DuplicateAssertView.ViewContentProvider;
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
import org.eclipse.jdt.core.IWorkingCopy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.internal.core.builder.SourceFile;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;

public class DependentTestView extends ViewPart {
	
	private static final String MESSAGE_DIALOG_TITLE = "Conditional Test Logic";
	private TableViewer tableViewer;
	private TreeViewer treeViewer;
	private Action identifyBadSmellsAction;
	private Action doubleClickAction;
	private Action applyRefactoringAction;
	private IJavaProject selectedProject;
	private IJavaProject activeProject;
	private IPackageFragmentRoot selectedPackageFragmentRoot;
	private IPackageFragment selectedPackageFragment;
	private ICompilationUnit selectedCompilationUnit;
	private IType selectedType;
	private CandidateRefactoring[] candidateRefactoringTable;
	private IJavaProject project;
	final String REFACTORING_DESCRIPTION = "Add Assertion Explanation";


    private List<TestSmellDescription> testSmells;
    
    public static String getMessageDialogTitle() {
		return MESSAGE_DIALOG_TITLE;
	}
    
    private String getNumberString(String myText) {
    	int tam = myText.length();
    	char[] vetor = myText.toCharArray();   
    	String novaString = "";
    	
    	for (int i = 0; i < tam; i++ ) {    		
    		if (Character.isDigit(vetor[i]))
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
					if(entry instanceof AddExplanationCandidateRefactoring)
						return "Conditional Test Logic";
					else
						return "";
				case 1:
					return getTextString(entry.getSourceEntity2()) + "( )";
				case 2:
					return getNumberString(entry.getSourceEntity2());
				case 3:
					return String.valueOf(entry.getPosition().offset);
				case 4:
					return entry.getSourceClass().getFilePath();
					//return "Caminho do arquivo";
				case 5:
					return REFACTORING_DESCRIPTION;
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
			AddExplanationCandidateRefactoring candidate1 = (AddExplanationCandidateRefactoring)obj1;
			AddExplanationCandidateRefactoring candidate2 = (AddExplanationCandidateRefactoring)obj2;
			return candidate1.compareTo(candidate2);
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
					//Primeiro verifica se o diretório do pacote 
					IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot)element;
					String diretorioPacote = packageFragmentRoot.getResource().getLocation().toString();

					//Depois verificados se é um pacote válido
					boolean valido = new TrataStringCaminhoTeste().diretorioTesteValido(diretorioPacote);
					
					//Se for válido, ativamos a opção
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
		/* Ordem de apresentação:
		 * 1a Coluna: TestSmell
		 * 2a Coluna: Source Method
		 * 3a Coluna: Linha inicial
		 * 4a Coluna: Linha final
		 * 5a Coluna: Refactoring Type		 * 
		 * 6a Coluna: Caminho do arquivo
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
		column2.setText("Begin");
		column2.setResizable(true);
		column2.pack();
		TableColumn column3 = new TableColumn(tableViewer.getTable(),SWT.LEFT);
		column3.setText("End");
		column3.setResizable(true);
		column3.pack();
		TableColumn column4 = new TableColumn(tableViewer.getTable(),SWT.LEFT);
		column4.setText("File Path");
		column4.setResizable(true);
		column4.pack();
		TableColumn column5 = new TableColumn(tableViewer.getTable(),SWT.LEFT);
		column5.setText("Refactoring Type");
		column5.setResizable(true);
		column5.pack();
		
		
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
	private void callAssertionRoulette() throws IOException{
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
				if(wasAlreadyOpen)
					openPackageExplorerViewPart();
				if (candidateRefactoringTable == null || candidateRefactoringTable.length == 0 ) {
					JOptionPane.showMessageDialog(null, "Conditional Test Logic not found.");				
				}
			}
		};
		identifyBadSmellsAction.setToolTipText("Identify Test Smells");
		identifyBadSmellsAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		identifyBadSmellsAction.setEnabled(false);
		
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
					
					int num = 0;
					num = Integer.valueOf(candidate.getLineNumber());
					try {
						positions.add(getPositioAssertion(filePath, num));
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
					String texto = "Assertion Roulette occurs when a test method has multiple non-documented assertions. If one of the assertions fails, you do not know which one it is. Add Assertion Explanation to remove this smell.";
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
					
					int num = 0;
					num = Integer.valueOf(candidate.getLineNumber());
					try {
						positions.add(getPositioAssertion(filePath, num));
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
					String texto = "Assertion Roulette occurs when a test method has multiple non-documented assertions. If one of the assertions fails, you do not know which one it is. Add Assertion Explanation to remove this smell.";
					for(Position position : positions) {
						SliceAnnotation annotation = new SliceAnnotation(SliceAnnotation.EXTRACTION, texto);
						annotationModel.addAnnotation(annotation, position);
					}
					Position firstPosition = positions.get(0);
					Position lastPosition = positions.get(positions.size()-1);
					int offset = firstPosition.getOffset();
					int length = lastPosition.getOffset() + lastPosition.getLength() - firstPosition.getOffset();
					sourceEditor.setHighlightRange(offset, length, true);
					
					IDocumentProvider docProvider = sourceEditor.getDocumentProvider();
					IDocument classeDocument = docProvider.getDocument(sourceEditor.getEditorInput());
					
					try {
						String teste = explanationNoEmpty(filePath, num);
						classeDocument.replace(offset-1, length, teste);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					// Salvar aquivo
					// Refirecionar para a posicao
				    final IDocument doc = docProvider.getDocument(sourceEditor.getEditorInput());
				   	try {
						docProvider.saveDocument(new NullProgressMonitor(), new FileEditorInput(sourceFile), classeDocument, true);
						AssertionParenteseLine parentese = getParentesesPosition(filePath, num);
						String refactoringText = "\"" + REFACTORING_DESCRIPTION +" here" + "\", ";
						classeDocument.replace(offset-1, parentese.position+1, parentese.textBeforeParentese + refactoringText);
						docProvider.saveDocument(new NullProgressMonitor(), new FileEditorInput(sourceFile), classeDocument, true);
					   	doubleClickAction.run();
				   	} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				   	
//				   	IStructuredSelection selection = (IStructuredSelection)tableViewer.getSelection();
//					CandidateRefactoring candidate = (CandidateRefactoring)selection.getFirstElement();
				   	
				   	
				    
				} catch (PartInitException e) {
					e.printStackTrace();
				} catch (JavaModelException e) {
					e.printStackTrace();
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		};
		applyRefactoringAction.setToolTipText("Apply Refactoring");
		applyRefactoringAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_DEF_VIEW));
		applyRefactoringAction.setEnabled(false);

	}
	public String explanationNoEmpty(String path, int line) throws IOException {
		int inicio = 0, tamanho = 0, contaLinha, caracteres;
		
		contaLinha = 1;
		String conteudoLinha = "";
		File file  = new File(path);
		BufferedReader  leitor = new BufferedReader(new FileReader(file));
		String st;
		while ((st = leitor.readLine()) != null) {
			if (contaLinha == line) {
				conteudoLinha = st; // conteudoLinha é utilizado para remover a explicação vazia
				break;
			}
			contaLinha++;
		}
		
		/*
		 *  Remove o comentário vazio
		 */
		String strFinal = "";
			
		int positionPrimeiraAspa = -1;
		int positionPrimeiroParentese = -1;
		int positinioVirgula = -1;		
		char[] ch = conteudoLinha.toCharArray();  		
		int quant_espacos = 0;		
		
		//esse for tira os espaços
		for(int j = 0; j < ch.length; j++ ){  		
			if (ch[j] == ' ') {
				quant_espacos ++;
			}
			else {
				break;
			}
		}		
		
		String aux = "";
		if (quant_espacos > 0) {
			aux = conteudoLinha.substring(quant_espacos, conteudoLinha.length());
			conteudoLinha = aux;
		}
		
		//esse for identifica a posicao de cada aspas
		char[] vetor = conteudoLinha.toCharArray();   
		for(int i = 0; i < vetor.length; i++ ){ 	
			if (vetor[i] == '(' && (positionPrimeiroParentese != -1 ))
				break;
			else if (vetor[i] == '(' && (positionPrimeiroParentese == -1 )) 
				positionPrimeiroParentese = i;
			else if (vetor[i] == '\"' && (positionPrimeiroParentese != -1) && (positionPrimeiraAspa == -1)) 
				positionPrimeiraAspa = i;
			else if (vetor[i] == ',' && positionPrimeiraAspa != -1  ) {
				positinioVirgula = i;
				break;
			}
		}
		String resultado = "";
		if (quant_espacos > 0) {
			for (int k = 0; k < quant_espacos; k++) {
				resultado += " ";
			}
		}
		
		if(positionPrimeiraAspa != -1 && positinioVirgula != -1 ) {
			resultado += conteudoLinha.substring(0, positionPrimeiraAspa);
			resultado += conteudoLinha.substring(positinioVirgula + 1, conteudoLinha.length());		
		}
		else {
			resultado += conteudoLinha;
		}
		return resultado;
	}
	
	
	class AssertionParenteseLine {
		
		int position;
		String textBeforeParentese;
		
		AssertionParenteseLine(int position, String textBeforeParentese) {
			this.position = position;
			this.textBeforeParentese = textBeforeParentese;
		}
		
		
	}
	
	public AssertionParenteseLine getParentesesPosition(String path, int line) {
		int contaLinha;
		
		contaLinha = 1;
		
		File file  = new File(path);
		BufferedReader leitor;
		int parentesePosition = -1;
		String textBeforeParentese = "";
		try {
			leitor = new BufferedReader(new FileReader(file));
			
			String st;
			
			while ((st = leitor.readLine()) != null) {
				
				if (contaLinha == line) {
					parentesePosition = st.indexOf("(");
					textBeforeParentese = st.substring(0, parentesePosition + 1);
					break;
				}
				contaLinha++;
			} 
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return new AssertionParenteseLine(parentesePosition, textBeforeParentese); 
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
				if(candidate instanceof AddExplanationCandidateRefactoring) {
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
				final List<AddExplanationCandidateRefactoring> moveMethodCandidateList = new ArrayList<AddExplanationCandidateRefactoring>();
				
				try {
					callAssertionRoulette();
				} catch (IOException e) {
					e.printStackTrace();
				}				
				AddExplanationCandidateRefactoring addExp;
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
					Position minhaPosicao = new Position(Integer.valueOf(testSmells.get(i).getLinePositionEnd().toString()), 0);
					addExp = new AddExplanationCandidateRefactoring(system, minhaClasse, minhaOutraClasse, meuMeuMetodo, testSmells.get(i).getLinePositionBegin(), minhaPosicao );
					moveMethodCandidateList.add(addExp);
				}
				table = new CandidateRefactoring[moveMethodCandidateList.size()];
				int counter = 0;
				for(AddExplanationCandidateRefactoring candidate : moveMethodCandidateList) {
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