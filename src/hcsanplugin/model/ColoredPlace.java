package hcsanplugin.model;

import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import eduni.simjava.Sim_event;
import eduni.simjava.Sim_port;
import eduni.simjava.Sim_system;

public class ColoredPlace extends Place {

	public ColoredPlace(String string, String type) {
		super(string, type);

	}

	public ColoredPlace(String string, String type, boolean isInputFusion,
			boolean isOutputFusion) {
		super(string, type, isInputFusion, isOutputFusion);

	}

	@Override
	public IFigure createFigure() {
		Figure figure = new Figure();
		figure.setLayoutManager(new FreeformLayout());

		Label toplabel = new Label(":(" + type + ")");
		toplabel.setSize(100, 15);
		toplabel.setLocation(new Point(0, 0));
		figure.add(toplabel);
		IFigure shape = new Ellipse();
		shape.setBounds(new Rectangle(20, 20, 60, 25));
		figure.add(shape);
		if (isFusion()) {
			IFigure innershape = new Ellipse();
			innershape.setBounds(new Rectangle(25, 25, 50, 15));

			figure.add(innershape);
		}
		Label downlabel = new Label();
		downlabel.setText(get_sname());
		downlabel.setSize(100, 25);
		downlabel.setLocation(new Point(0, 45));

		figure.add(downlabel);
		figure.setSize(100, 70);
		return figure;
	}

}
