package atplugin.model;

import java.util.List;

public class UCNode extends ANDNode {

	public UCNode(ATModel model, ModelElement parent, String name,
			Double execProb) {
		super(model, parent, name);
		// TODO Auto-generated constructor stub
		LeafNode epNode = new LeafNode(model, this, "ep_" + name, execProb);

	}

	public UCNode(ATModel model, ModelElement parent, String name,
			List<Judgment> judgs) {
		super(model, parent, name);
		LeafNode epNode = new LeafNode(model, this, "ep_" + name, judgs);

	}
	
	

}
