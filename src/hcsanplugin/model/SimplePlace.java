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

public class SimplePlace extends Place {
	public SimplePlace(String string) {
		super(string);
	}

	public SimplePlace(String string, boolean isInputFusion,
			boolean isOutputFusion) {
		super(string, isInputFusion, isOutputFusion);
		try{
			if(string.endsWith("writeImage_in"))
				throw new Exception();
			
		}catch(Exception e){
			e.printStackTrace();System.exit(0);
		}
	}

	@Override
	public IFigure createFigure() {
		Figure figure = new Figure();
		figure.setLayoutManager(new FreeformLayout());

		IFigure shape = new Ellipse();
		shape.setBounds(new Rectangle(35, 15, 30, 30));
		Label label = new Label();
		label.setText(get_sname());
		label.setSize(100, 25);
		label.setLocation(new Point(0, 45));
		figure.add(shape);
		if (isFusion()) {
			IFigure innershape = new Ellipse();
			innershape.setBounds(new Rectangle(40, 20, 20, 20));
			
			figure.add(innershape);
		}
		
		figure.add(label);
		figure.setSize(100, 70);
		return figure;
	}
	
	

}
