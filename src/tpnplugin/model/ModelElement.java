package tpnplugin.model;

import org.eclipse.draw2d.IFigure;
import org.eclipse.zest.core.widgets.CGraphNode;


public abstract class ModelElement {
	private String name, label;
	public CGraphNode node;
	

	public abstract IFigure createFigure();

	public ModelElement(String name) {
		this.name = name;
		
	}
	
	public ModelElement(String name, String label) {
		this.name = name;
		this.label = label;
	}

	public String getLabel(){
		return label;
	}
	public String getName() {
		return name;

	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return name;
	}
	
	
}
