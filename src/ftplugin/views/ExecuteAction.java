package ftplugin.views;

import ftplugin.model.FTModel;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

public class ExecuteAction extends Action implements IWorkbenchAction {

	private static final String ID = "ftplugin.views.SimulateAction";

	public ExecuteAction() {
		setId(ID);
	}

	public void run() {
		FTModel.execute();
		BPChart.displayChart();
		NewModelAction.viewer.update(FTModel.getCurrentModel().modelElements.values().toArray(), null);
		//BPChart.writeChartToPDF();
		
	}

	public void dispose() {
	}

}
