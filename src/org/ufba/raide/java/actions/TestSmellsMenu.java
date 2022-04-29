package org.ufba.raide.java.actions;

import javax.swing.JOptionPane;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.ufba.raide.java.refactoring.views.DuplicateAssertView;

/**
 * Our sample action implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be 
 * delegated to it.
 * @see IWorkbenchWindowActionDelegate
 */
public class TestSmellsMenu implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	/**
	 * The constructor.
	 */
	public TestSmellsMenu() {
	}
	
	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		IWorkbenchPage page = window.getActivePage();
		try {
			if(action.getId().equals("org.ufba.raide.java.actions.AssertionRoulette")) {
				page.showView("org.ufba.raide.java.views.AssertionRoulette");
			}
			else if(action.getId().equals("org.ufba.raide.java.actions.ConditionalTestLogic")) {
				page.showView("org.ufba.raide.java.views.ConditionalTestLogic");
			}
			else if(action.getId().equals("org.ufba.raide.java.actions.ConstructionInstallation")) {
				page.showView("org.ufba.raide.java.views.ConstructionInstallation");
			}
			else if(action.getId().equals("org.ufba.raide.java.actions.DefaultTest")) {
				page.showView("org.ufba.raide.java.views.DefaultTest");
			}
			else if(action.getId().equals("org.ufba.raide.java.actions.DependentTest")) {
				page.showView("org.ufba.raide.java.views.DependentTest");
			}
			else if(action.getId().equals("org.ufba.raide.java.actions.DuplicateAssert")) {
				page.showView("org.ufba.raide.java.views.DuplicateAssert");
			}
			else if(action.getId().equals("org.ufba.raide.java.actions.EagerTest")) {
				page.showView("org.ufba.raide.java.views.EagerTest");
			}
			else if(action.getId().equals("org.ufba.raide.java.actions.EmptyTest")) {
				page.showView("org.ufba.raide.java.views.EmptyTest");
			}
			else if(action.getId().equals("org.ufba.raide.java.actions.ExceptionCatchingThrowing")) {
				page.showView("org.ufba.raide.java.views.ExceptionCatchingThrowing");
			}
			else if(action.getId().equals("org.ufba.raide.java.actions.GeneralFixture")) {
				page.showView("org.ufba.raide.java.views.GeneralFixture");
			}
			else if(action.getId().equals("org.ufba.raide.java.actions.IgnoredTest")) {
				page.showView("org.ufba.raide.java.views.IgnoredTest");
			}
			
			else if(action.getId().equals("org.ufba.raide.java.actions.LazyTest")) {
				page.showView("org.ufba.raide.java.views.LazyTest");
			}
				else if(action.getId().equals("org.ufba.raide.java.actions.MagicNumberTest")) {
				page.showView("org.ufba.raide.java.views.MagicNumberTest");
			}
			else if(action.getId().equals("org.ufba.raide.java.actions.MysteryGuest")) {
				page.showView("org.ufba.raide.java.views.MysteryGuest");
			}
				else if(action.getId().equals("org.ufba.raide.java.actions.PrintStatement")) {
				page.showView("org.ufba.raide.java.views.PrintStatement");
			}
			else if(action.getId().equals("org.ufba.raide.java.actions.RedundantAssertion")) {
				page.showView("org.ufba.raide.java.views.RedundantAssertion");
			}
			//
			else if(action.getId().equals("org.ufba.raide.java.actions.ResourceOptimism")) {
				page.showView("org.ufba.raide.java.views.ResourceOptimism");
			}
			else if(action.getId().equals("org.ufba.raide.java.actions.SensitiveEquality")) {
				page.showView("org.ufba.raide.java.views.SensitiveEquality");
			}
			else if(action.getId().equals("org.ufba.raide.java.actions.SleepyTest")) {
				page.showView("org.ufba.raide.java.views.SleepyTest");
			}
			else if(action.getId().equals("org.ufba.raide.java.actions.UnknownTest")) {
				page.showView("org.ufba.raide.java.views.UnknownTest");
			}
			else if(action.getId().equals("org.ufba.raide.java.actions.VerboseTest")) {
				page.showView("org.ufba.raide.java.views.VerboseTest");
			}
			
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Selection in the workbench has been changed. We 
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after 
	 * the delegate has been created.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to
	 * be able to provide parent shell for the message dialog.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}