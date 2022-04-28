package hcsanplugin.views;

import hcsanplugin.model.HCSANModel;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

public class SimulateAction extends Action implements IWorkbenchAction {

	private static final String ID = "hcsanplugin.views.SimulateAction";

	public SimulateAction() {
		setId(ID);
	}

	public void run() {

		HCSANModel.simulate();

	}

	public void dispose() {
	}

}
