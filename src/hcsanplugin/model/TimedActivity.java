package hcsanplugin.model;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import eduni.simjava.Sim_event;
import eduni.simjava.Sim_port;
import eduni.simjava.Sim_stat;
import eduni.simjava.Sim_system;
import eduni.simjava.distributions.ContinuousGenerator;
import eduni.simjava.distributions.Sim_negexp_obj;

public class TimedActivity extends Activity {
	String duration;
	double delay;
	private Sim_negexp_obj exp_delay;

	public TimedActivity(String name, String dur) {
		super(name);
		duration = dur;
		delay = -1;
		stat = new Sim_stat();
		if (get_sname().equals("process_results"))
			stat.add_measure("Client Throughput", Sim_stat.RATE_BASED);

		set_stat(stat);
		if (duration.contains("exp(")) {
			String mean = duration.split("[()]")[1];
			exp_delay = new Sim_negexp_obj("Delay", Double.parseDouble(mean));
			add_generator(exp_delay);

		}
	}

	public double delay() {
		if (delay != -1 && exp_delay == null)
			return delay;
		if (duration.startsWith("$"))
			duration = HCSANModel.getParameter(duration.substring(1));
		duration = duration.substring(1, duration.length() - 1);

		String[] parts = duration.split("\\,");
		if (exp_delay != null) {
			delay = exp_delay.sample();
		} else {
			String val = parts[0].trim();
			if(val.isEmpty())
				delay =0;
			else
			delay = Double.parseDouble(val);
		}
		if (parts[1].trim().equals("s"))
			delay *= 1000;

		return delay;

	}

	@Override
	public IFigure createFigure() {
		Figure figure = new Figure();
		figure.setLayoutManager(new FreeformLayout());

		IFigure shape = new RectangleFigure();
		shape.setBounds(new Rectangle(45, 15, 10, 30));
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
			sim_pause(delay());
			complete();

		}
		while (Sim_system.running()) {

			Sim_event e = new Sim_event();
			// Get the next event
			sim_get_next(e);

			// Process the event

			if (e.event_time() != -1 && enabled()) {
				sim_process(delay());
				complete();
				if (get_sname().equals("process_results"))
					stat.update("Client Throughput", Sim_system.sim_clock());
				sim_completed(e);

			}
		}
	}

}