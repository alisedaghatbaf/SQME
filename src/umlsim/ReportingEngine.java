package umlsim;

import java.util.Random;

public class ReportingEngine extends Component {
	private Database db;

	public ReportingEngine() {
		super(new ApplicationServer());
		// TODO Auto-generated constructor stub
		db = new Database();
	}

	public Data reportRequest() {
		// TODO Auto-generated method stub
		Data dt = null;
		if (decide())
			dt = db.getBriefDBData();
		else
			dt = db.getDetailedDBData();
		return generateReport(dt);
	}

	private boolean decide() {
		// TODO Auto-generated method stub
		Random rnd = new Random();
		double val = rnd.nextDouble();
		if (val < 0.9)
			return true;
		return false;
	}

	private Data generateReport(Data dt) {
		
		Util.hostDemand(getHost(), 4);
		Util.setError(dt, 0.000012);
		
		return dt;
	}
}
