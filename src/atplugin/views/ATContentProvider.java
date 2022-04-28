package atplugin.views;

import atplugin.model.ATModel;
import atplugin.model.ModelElement;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ATContentProvider implements ITreeContentProvider {
	boolean firstTime = true;

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub
		viewer.refresh();
		
	}

	@Override
	public Object[] getElements(Object inputElement) {

		if (firstTime) {
			firstTime = false;
			return new Object[] { inputElement };
		}
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		
		return ((ModelElement) parentElement).getChildElements().values()
				.toArray();

	}

	@Override
	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		return ((ModelElement) element).getParentElement();
	}

	@Override
	public boolean hasChildren(Object element) {
		// TODO Auto-generated method stub
		return getChildren(element).length > 0;
	}

}
