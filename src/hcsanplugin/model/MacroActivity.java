package hcsanplugin.model;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import eduni.simjava.Sim_stat;

public class MacroActivity extends Activity {
	public HCSANModel internalModel;
	public MacroActivity(String name) {
		super(name);
		internalModel = new HCSANModel(name);
		// TODO Auto-generated constructor stub
		
	}

	@Override
	public
	IFigure createFigure() {
		Figure figure = new Figure();
		figure.setLayoutManager(new FreeformLayout());

		IFigure shape = new RectangleFigure();
		shape.setBounds(new Rectangle(35, 15, 30, 30));
		shape.setBackgroundColor(org.eclipse.draw2d.ColorConstants.black);
		Label label = new Label();
		label.setText(get_sname());
		label.setSize(100, 15);
		label.setLocation(new Point(0, 45));
		figure.add(shape);
		figure.add(label);
		figure.setSize(100, 60);
		return figure;
	}
	public void body(){}
}
