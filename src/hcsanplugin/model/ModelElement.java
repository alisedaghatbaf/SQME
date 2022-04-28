package hcsanplugin.model;

import org.eclipse.draw2d.IFigure;
import org.eclipse.zest.core.widgets.CGraphNode;

import eduni.simjava.Sim_entity;
import eduni.simjava.Sim_stat;

public abstract class ModelElement extends Sim_entity {

	public CGraphNode node;
	
	protected Sim_stat stat;

	public abstract IFigure createFigure();

	public ModelElement(String name) {
		super(name);
		
	}

	public String get_sname() {
		String[] ns = get_name().split("\\.");
		return ns[ns.length - 1];

	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return get_name();
	}
}
