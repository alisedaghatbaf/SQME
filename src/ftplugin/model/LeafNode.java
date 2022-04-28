package ftplugin.model;

import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

public class LeafNode extends ModelElement {

	boolean enabled = true;

	public LeafNode(FTModel model, ModelElement parent, String name,
			double input) {
		super(model, parent, name);
		probability = input;
		// TODO Auto-generated constructor stub
	}

	public LeafNode(FTModel model, ModelElement parent, String name,
			List<Judgment> judgs) {
		super(model, parent, name);
		judgments = judgs;
		// TODO Auto-generated constructor stub
	}

	@Override
	public IFigure createFigure() {
		Figure figure = new Figure();
		figure.setLayoutManager(new FreeformLayout());

		IFigure shape = new Ellipse();
		shape.setBounds(new Rectangle(0, 0, 60, 40));
		// shape.setBackgroundColor(org.eclipse.draw2d.ColorConstants.black);
		Label label = new Label();
		label.setText(get_name());
		label.setSize(60, 15);
		label.setLocation(new Point(0, 10));
		figure.add(shape);

		figure.add(label);
		label = new Label();
		label.setText(String.valueOf(probability));
		label.setLocation(new Point(0, 50));
		figure.add(label);
		figure.setSize(60, 40);
		return figure;
	}

	@Override
	public boolean enabled() {

		return enabled;
	}

	@Override
	public void execute() {

		// TODO Auto-generated method stub
		enabled = false;

	}

}
