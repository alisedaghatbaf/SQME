package ftplugin.model;

import java.awt.font.NumericShaper.Range;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.zest.core.widgets.CGraphNode;

public abstract class ModelElement {

	public CGraphNode node;
	int enabledCount = 0;
	String name;

	Double probability = null;
	Map<String, ModelElement> childElements;
	List<Judgment> judgments;
	ModelElement parentElement;

	public abstract IFigure createFigure();

	public abstract boolean enabled();

	public abstract void execute();

	public ModelElement(FTModel model, ModelElement parent, String name) {
		this.name = name;
		childElements = new LinkedHashMap<String, ModelElement>();
		judgments = new ArrayList<Judgment>();
		if(parent!=null)
			parent.addChild(this);
		model.addElement(getQName(), this);
	}
	
	public String getQName(){
		String qname = name;
		ModelElement parent = getParentElement();
		if(parent!=null)
			qname = parent.getQName()+"."+qname;
		return qname;
	}

	public void setProbability(Double val) {
		probability = val;
	}

	public Double getProbability() {
		return probability;
	}

	public List<Judgment> getJudgments() {
		return judgments;
	}

	public void addJudgment(double lb, double ub, double pba) {
		judgments.add(new Judgment(lb, ub, pba));
	}

	public void addChild(ModelElement child) {
		childElements.put(child.get_name(), child);
		child.parentElement = this;
	}

	public String get_name() {
		return name;

	}

	public Map<String, ModelElement> getChildElements() {
		return childElements;
	}

	public ModelElement getParentElement() {
		return parentElement;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return get_name();
	}
	
	
}
