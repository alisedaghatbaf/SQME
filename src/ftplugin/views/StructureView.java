package ftplugin.views;

import org.sqme.Activator;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class StructureView extends ViewPart {
	public static final String ID = "ftplugin.views.StructureView";
	static Composite composite;
	@Override
	public void createPartControl(Composite parent) {
		composite = parent;
		NewModelAction newAction = new NewModelAction();
		newAction.setText("New Fault Tree Model");
		newAction.setImageDescriptor(Activator
				.getImageDescriptor("icons/new_con.gif"));
		getViewSite().getActionBars().getToolBarManager().add(newAction);
		ExecuteAction simAction = new ExecuteAction();
		simAction.setText("Start Simulation");
		simAction.setImageDescriptor(Activator
				.getImageDescriptor("icons/run_exc.gif"));
		getViewSite().getActionBars().getToolBarManager().add(simAction);
		/*GenerateGraphAction genAction = new GenerateGraphAction();
		genAction.setText("Generate Report");
		genAction.setImageDescriptor(Activator
				.getImageDescriptor("icons/report.gif"));
		getViewSite().getActionBars().getToolBarManager().add(genAction);*/
		
		
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

}
