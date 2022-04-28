package umlsim;

import java.util.Random;

public class Scheduler extends Component{
	private ReportingEngine eng;

	public Scheduler() {
		super(new SchedulerServer());
		// TODO Auto-generated constructor stub
		eng = new ReportingEngine();
	}


	private Data generateView() {
		// TODO Auto-generated method stub
		Data dt = new Data();
		Util.hostDemand(getHost(), 3);
		Util.setError(dt, 0.00005);
		return dt;
	}

	private void processRequest() {
		Data dt = new Data();
		Util.hostDemand(getHost(), 2);
		Util.setError(dt, 0.00005);
		
	}

	private boolean decide() {
		Random rnd = new Random();
		double val = rnd.nextDouble();
		if (val < 0.7)
			return true;
		return false;
	}

	public Data requestData() {
		// TODO Auto-generated method stub
		processRequest();
		if (decide())
			return generateView();
		return eng.reportRequest();
	}
}
