package ftplugin.model;

import java.util.List;

public class UCNode extends ANDNode {

	public UCNode(FTModel model, ModelElement parent, String name,
			Double execProb) {
		super(model, parent, name);
		// TODO Auto-generated constructor stub
		 new LeafNode(model, this, "ep_" + name, execProb);

	}

	public UCNode(FTModel model, ModelElement parent, String name,
			List<Judgment> judgs) {
		super(model, parent, name);
		new LeafNode(model, this, "ep_" + name, judgs);

	}
	
	

}
