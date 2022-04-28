package hcsanplugin.model;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.Triangle;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

public abstract class Gate extends ModelElement {
	Activity owner;

	public Gate(String name, Activity owner) {
		super(name);
		
		
		this.owner = owner;
		// TODO Auto-generated constructor stub
	}

	@Override
	public IFigure createFigure() {
		Figure figure = new Figure();
		figure.setLayoutManager(new FreeformLayout());

		IFigure shape = new Triangle();
		shape.setBounds(new Rectangle(35, 15, 30, 30));
		int position =  PositionConstants.EAST;
		((Triangle) shape).setDirection(position);
		shape.setBackgroundColor(org.eclipse.draw2d.ColorConstants.black);

		Label label = new Label();
		label.setText(get_name());
		label.setSize(100, 25);
		label.setLocation(new Point(0, 45));
		figure.add(shape);
		figure.add(label);
		figure.setSize(100, 70);
		return figure;
	}

	public Token execute(Token t) {
		return null;
	}

	public boolean predicate() {
		return true;
	}

	public void body() {
	}

}
