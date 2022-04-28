package atplugin.views;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import atplugin.model.ATModel;

public class ExecuteAction extends Action implements IWorkbenchAction {

	private static final String ID = "ftplugin.views.SimulateAction";

	public ExecuteAction() {
		setId(ID);
	}

	public void run() {
		ATModel.execute();
		BPChart.displayChart();
		NewModelAction.viewer.update(ATModel.getCurrentModel().modelElements.values().toArray(), null);
		
	}

	public void dispose() {
	}

}
