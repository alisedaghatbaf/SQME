package hcsanplugin.views;

import hcsanplugin.model.HCSANModel;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import SJGV.SJGV;

public class GenerateGraphAction extends Action implements IWorkbenchAction {

	private static final String ID = "hcsanplugin.views.GenerateGraphAction";

	public GenerateGraphAction() {
		setId(ID);
	}

	public void run() {

		SJGV.main(new String[]{"sim_graphs.sjg"});

	}

	public void dispose() {
	}

}
