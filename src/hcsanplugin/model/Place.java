package hcsanplugin.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eduni.simjava.Sim_event;
import eduni.simjava.Sim_port;
import eduni.simjava.Sim_stat;
import eduni.simjava.Sim_system;

public abstract class Place extends ModelElement {
	boolean isInputFusion, isOutputFusion;
	private List<Token> marking;
	Set<Place> boundPlaces;
	String type;
	Map<Integer, Double> entranceTimes;
	Sim_stat stat;

	public Place(String string) {
		super(string);
		marking = new ArrayList<Token>();
		type = "Integer";
		entranceTimes = new HashMap<Integer, Double>();
		if (get_name().equals("DES.Client.final")) {
			stat = new Sim_stat();
			stat.add_measure("Response Time", Sim_stat.STATE_BASED, true);
			set_stat(stat);
		}
	}

	public Place(String string, String type) {
		super(string);
		marking = new ArrayList<Token>();
		this.type = type;

	}

	public void setInputFusion() {
		isInputFusion = true;
	}

	public void setOutputFusion() {
		isOutputFusion = true;
	}

	public Place(String name, String type, boolean isInFusion,
			boolean isOutFusion) {
		this(name, type);
		isInputFusion = isInFusion;
		isOutputFusion = isOutFusion;

	}

	public Place(String name, boolean isInFusion, boolean isOutFusion) {
		this(name);
		isInputFusion = isInFusion;
		isOutputFusion = isOutFusion;

	}

	public List<Token> marking() {
		return marking;
	}

	public boolean isFusion() {
		return isInputFusion || isOutputFusion;
	}

	public void bind(Place p, int direction) {
		if (boundPlaces == null)
			boundPlaces = new HashSet<Place>();
		if (boundPlaces.contains(p))
			return;
		boundPlaces.add(p);
		if (direction == -1) {
			Sim_port in = new Sim_port("in_" + p.get_name());
			add_port(in);
			Sim_port out = new Sim_port("out_" + get_name());
			p.add_port(out);
			Sim_system.link_ports(get_name(), in.get_pname(), p.get_name(),
					out.get_pname());
		} else if (direction == 1) {
			Sim_port in = new Sim_port("in_" + get_name());
			p.add_port(in);
			Sim_port out = new Sim_port("out_" + p.get_name());
			add_port(out);
			Sim_system.link_ports(get_name(), out.get_pname(), p.get_name(),
					in.get_pname());

		}
		p.bind(this, 0);
	}

	public synchronized void addToken(Token t) {
		if (t.value.startsWith("$"))
			t.value = HCSANModel.getParameter(t.value.substring(1));
		if (this instanceof SimplePlace) {
			if (get_name().equals("DES.Client.final")) {
				double current_time = Sim_system.sim_clock();
				for (Double d : entranceTimes.values())
					if (d.doubleValue() < current_time)
						current_time = d.doubleValue();
				stat.update("Response Time", current_time,
						Sim_system.sim_clock());

			}

			if (marking.size() == 0)
				marking.add(t);
			else {
				try {
					Token cur = marking.get(0);
					int curValue = Integer.parseInt(cur.value);
					cur.value = String.valueOf(curValue
							+ Integer.parseInt(t.value));

				} catch (Exception e) {
					System.out.println("number format: " + this + " "
							+ marking.get(0).value);
				}
			}
		} else
			marking.add(t);
		sim_trace(1, "Token " + t + " added to place " + this
				+ " and current marking is: " + marking());
		if (boundPlaces != null) {
			for (Place bp : boundPlaces) {
				for (Object o : get_ports()) {
					Sim_port p = (Sim_port) o;
					if (p.get_pname().startsWith("out_")
							&& p.get_dest_ename().equals(bp.get_name()))
						bp.addToken(t);
				}
			}
		}

	}

	public synchronized Token removeToken() {
		Token t = null;
		if (this instanceof SimplePlace) {
			try {
				if (marking.size() == 0)
					return null;
				t = marking.get(0);

				int val = Integer.parseInt(t.value);
				if (val == 1) {
					marking.remove(0);
				} else
					t.value = String.valueOf(val - 1);

			} catch (NumberFormatException e) {
				System.out.println("number format in remove token: " + this
						+ " " + t.value + " with length: " + t.value.length());
				e.printStackTrace();
				System.exit(0);
			}

		} else
			t = marking.remove(0);
		sim_trace(1, "Token " + t + " removed from place " + this
				+ " and current marking is: " + marking());
		for (Object o : get_ports()) {
			Sim_port port = (Sim_port) o;
			if (port.get_pname().startsWith("out_"))
				sim_schedule(port, 0, 0);
		}
		if (boundPlaces != null) {
			for (Place bp : boundPlaces) {
				if (bp.marking.remove(t))
					break;
			}
		}
		return t;
	}

	public synchronized void removeToken(Token t) {

		if (this instanceof SimplePlace) {

			try {
				int val = Integer.parseInt(t.value);
				if (val == 1)
					marking.remove(t);
				else
					t.value = String.valueOf(val - 1);
			} catch (Exception e) {
				System.out.println("number format in remove token: " + this
						+ " " + t.value);
			}
			if (get_sname().equals("idle")) {
				current_time = Sim_system.sim_clock();
			}
		} else
			marking.remove(t);
		for (Object o : get_ports()) {
			Sim_port port = (Sim_port) o;
			if (port.get_pname().startsWith("out_"))
				sim_schedule(port, 0, 0);
		}
		if (boundPlaces != null) {
			for (Place bp : boundPlaces) {
				if (bp.marking.remove(t))
					break;
			}
		}

	}

	public void body() {
		while (Sim_system.running()) {
			Sim_event e = new Sim_event();
			// Get the next event
			sim_get_next(e);
			// Process the event
			if (e.event_time() != -1) {
				sim_process(0);
				// The event has completed service
				sim_completed(e);
			}
			for (Object obj : get_ports()) {
				Sim_port port = (Sim_port) obj;
				if (port.get_pname().startsWith("out_")) {

					sim_schedule(port, 0, 0);
				}
			}

		}
	}

	public void clearMarking() {
		marking.clear();
	}

}
