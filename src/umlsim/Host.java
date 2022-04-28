package umlsim;

import java.util.LinkedList;
import java.util.Queue;

public class Host {

	private double repairRate;

	private double failureProb;

	private Queue<Integer> queue;

	public Host(double repair, double failure) {
		queue = new LinkedList<Integer>();
		setRepairRate(repair);
		setFailureProb(failure);
		// TODO Auto-generated constructor stub
	}

	public double getRepairRate() {
		return repairRate;
	}

	public void setRepairRate(double repairRate) {
		this.repairRate = repairRate;
	}

	public double getFailureProb() {
		return failureProb;
	}

	public void setFailureProb(double failureProb) {
		this.failureProb = failureProb;
	}

	public void addDemand(int d) {
		queue.add(d);
	}

	public void processDemand() {
		if(queue.isEmpty())
			return;
		int d = queue.remove();
		try {
			Thread.sleep(d);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
