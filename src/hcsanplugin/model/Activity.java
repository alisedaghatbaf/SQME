package hcsanplugin.model;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.ZestStyles;

import eduni.simjava.Sim_port;
import eduni.simjava.Sim_system;

public abstract class Activity extends ModelElement {
	public Activity(String name) {
		super(name);
		inputPlaces = new HashSet<Place>();
		outputPlaces = new HashSet<Place>();
		
	}

	public Set<Place> inputPlaces, outputPlaces;
	public Gate inputGate, outputGate;

	public void addInputPlace(Place p) {
		
		inputPlaces.add(p);
		Sim_port p1 = new Sim_port("in_" + p.get_name());
		add_port(p1);
		Sim_port p2 = new Sim_port("out_" + get_name());
		p.add_port(p2);
		Sim_system.link_ports(get_name(), p1.get_pname(), p.get_name(),
				p2.get_pname());
	}

	public void addOutputPlace(Place p) {
		outputPlaces.add(p);
		Sim_port p1 = new Sim_port("out_" + p.get_name());
		add_port(p1);
		Sim_port p2 = new Sim_port("in_" + get_name());
		p.add_port(p2);
		Sim_system.link_ports(get_name(), p1.get_pname(), p.get_name(),
				p2.get_pname());
	
	}

	public void addToken(Place p, Token t) {
		p.addToken(t);
		Sim_port port = get_port("out_" + p.get_name());
		sim_schedule(port, 0.0, 0);

	}
	

	public void defineGates(HCSANModel model, Gate inGate, Gate outGate) {
		inputGate = inGate;
		outputGate = outGate;
		model.addElement("ig_"+get_name(), inGate);
		model.addElement("og_"+get_name(), outGate);
	}

	public void complete() {
		outputGate.execute(inputGate.execute(null));
	}

	public boolean enabled() {
		
		return inputGate.predicate();
	}
}
