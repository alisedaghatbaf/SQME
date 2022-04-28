package atplugin.views;

import java.awt.event.MouseAdapter;

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

import atplugin.model.ATModel;
import atplugin.model.ModelElement;

public class NewModelAction extends Action implements IWorkbenchAction {

	static TreeViewer viewer;
	private static final String ID = "ftplugin.views.NewModelAction";
	private static final String[] FILTER_NAMES = { "UML Model Files (*.uml)",
			"All Files (*.*)" };

	// These filter extensions are used to filter which files are displayed.
	private static final String[] FILTER_EXTS = { "*.uml", "*.*" };

	public NewModelAction() {
		setId(ID);
	}

	public void run() {
		ATModel model = ATModel.createMarketModel();
		ATView.displayModel(model.getRootElement());
		viewer = new TreeViewer(
				StructureView.composite);
		viewer.setContentProvider(new ATContentProvider());
		viewer.setLabelProvider(new ATLabelProvider());
		viewer.setInput(model.getRootElement());
		
		// treeview.expandAll();
		StructureView.composite.layout();
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				// TODO Auto-generated method stub
				IStructuredSelection sel = (IStructuredSelection) event
						.getSelection();
				ModelElement myInput = (ModelElement) sel.getFirstElement();
				ATView.displayModel(myInput);
				
			}
		});
		
		
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

				FTModel model = FTModel.createModel(umlPath);
				final TreeViewer viewer = new TreeViewer(
						StructureView.composite);
				viewer.setContentProvider(new HCSANContentProvider());
				viewer.setLabelProvider(new HCSANLabelProvider());
				viewer.setInput(model);
				// treeview.expandAll();
				StructureView.composite.layout();
				viewer.addSelectionChangedListener(new ISelectionChangedListener() {

					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						// TODO Auto-generated method stub
						IStructuredSelection sel = (IStructuredSelection) event
								.getSelection();
						FTModel myInput = (FTModel) sel.getFirstElement();
						FTView.displayModel(myInput);
					}
				});
				FTView.displayModel(model);
				MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
				menuMgr.setRemoveAllWhenShown(true);
				menuMgr.addMenuListener(new IMenuListener() {
					@Override
					public void menuAboutToShow(IMenuManager manager) {
						Action action = new Action() {
							public void run() {
								super.run();
								// TODO do something
							}
						};
						action.setText("New Measure");
						
						manager.add(action);
					}
				});

				Menu menu = menuMgr.createContextMenu(viewer.getTree());
				viewer.getTree().setMenu(menu);
			}

		});
*/
	}

	public void dispose() {
	}

}
