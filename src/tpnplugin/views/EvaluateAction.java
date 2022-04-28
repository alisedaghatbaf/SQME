package tpnplugin.views;

import tpnplugin.model.TPNModel;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import tpnplugin.views.BPChart;

public class EvaluateAction extends Action implements IWorkbenchAction {

	private static final String ID = "tpnplugin.views.SimulateAction";

	public EvaluateAction() {
		setId(ID);
	}

	public void run() {

		TPNModel.evaluatePerformance();
		BPChart.displayChart();
	}

	public void dispose() {
	}

}
