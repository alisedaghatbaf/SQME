package hcsanplugin.model;

import java.util.Random;

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

public class InstantaneousActivity extends Activity {
	static boolean genRand;
	double probability = 1;

	public InstantaneousActivity(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public InstantaneousActivity(String name, double prob) {
		super(name);
		probability = prob;
		// TODO Auto-generated constructor stub
	}

	@Override
	public IFigure createFigure() {
		Figure figure = new Figure();
		figure.setLayoutManager(new FreeformLayout());

		IFigure shape = new RectangleFigure();
		shape.setBounds(new Rectangle(47, 15, 6, 30));
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

	public void body() {


		if (enabled()) {

			complete();

		}
		while (Sim_system.running()) {
			genRand = true;
			Sim_event e = new Sim_event();
			// Get the next event
			sim_get_next(e);

			//synchronized (this) {

				if (e.event_time() != -1 && enabled()) {
					if (genRand) {
						int rand = new Random().nextInt(100);
						if (rand < probability * 100) {
							sim_process(0);
							complete();
							sim_completed(e);

						}
						genRand = false;
					} else {
						sim_process(0);
						complete();
						sim_completed(e);

					}

				}
			//}
			// Process the event

		}
	}
}
