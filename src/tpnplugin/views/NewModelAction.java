package tpnplugin.views;

import java.awt.event.MouseAdapter;

import tpnplugin.model.Transition;
import tpnplugin.model.TPNModel;
import tpnplugin.model.ModelElement;
import tpnplugin.model.Place;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.zest.core.widgets.CGraphNode;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.CompositeLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.GridLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;

public class NewModelAction extends Action implements IWorkbenchAction {

	private static final String ID = "tpnplugin.views.NewModelAction";
	private static final String[] FILTER_NAMES = { "UML Model Files (*.uml)",
			"All Files (*.*)" };

	// These filter extensions are used to filter which files are displayed.
	private static final String[] FILTER_EXTS = { "*.uml", "*.*" };

	public NewModelAction() {
		setId(ID);
	}

	public void run() {

		/*
		final Display display = Display.getDefault();
		final Shell shell = new Shell(display);
		shell.setText("File Dialog");

		display.asyncExec(new Runnable() {

			@Override
			public void run() {
				FileDialog dlg = new FileDialog(shell, SWT.OPEN);
				dlg.setFilterNames(FILTER_NAMES);
				dlg.setFilterExtensions(FILTER_EXTS);
				String umlPath = dlg.open();

				TPNModel model = TPNModel.createDefaultModel();
				
				TPNView.displayModel(model);
				
			}

		});*/
		
		TPNView.displayModel(TPNModel.createCaseModel());

	}

	public void dispose() {
	}

}
