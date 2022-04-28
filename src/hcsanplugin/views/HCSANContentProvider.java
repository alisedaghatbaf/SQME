package hcsanplugin.views;

import hcsanplugin.model.HCSANModel;
import hcsanplugin.model.MacroActivity;
import hcsanplugin.model.ModelElement;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class HCSANContentProvider implements ITreeContentProvider {
	boolean firstTime = true;
	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object[] getElements(Object inputElement) {
		
		if(firstTime){
			firstTime = false;
			return new Object[]{inputElement};
		}
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		Set<HCSANModel> elements = new HashSet<HCSANModel>();
		if (parentElement instanceof HCSANModel) {

			HCSANModel element = (HCSANModel) parentElement;
			for (ModelElement el : element.modelElements.values()) {
				if (el instanceof MacroActivity){
					elements.add(((MacroActivity) el).internalModel);
				}
			}
		}
		return elements.toArray();
	}

	@Override
	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		return ((HCSANModel) element).parent;
	}

	@Override
	public boolean hasChildren(Object element) {
		// TODO Auto-generated method stub
		return getChildren(element).length > 0;
	}

}
