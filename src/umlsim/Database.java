package umlsim;

public class Database extends Component {

	public Database() {
		super(new DatabaseServer());
		// TODO Auto-generated constructor stub

	}

	public Data getBriefDBData() {
		// TODO Auto-generated method stub
		return briefDbOp();
	}

	public Data getDetailedDBData() {
		// TODO Auto-generated method stub
		cacheOp();
		return detailedDbOp();
	}

	private Data detailedDbOp() {
		// TODO Auto-generated method stub
		Data dt = new Data();
		Util.hostDemand(getHost(), 12);
		Util.setError(dt, 0.00005);
		return dt;
	}

	private Data cacheOp() {
		// TODO Auto-generated method stub
		Data dt = new Data();
		Util.hostDemand(getHost(), 2);
		Util.setError(dt, 0.00002);
		return dt;
	}

	private Data briefDbOp() {
		Data dt = new Data();
		Util.hostDemand(getHost(), 5);
		Util.setError(dt, 0.00002);
		return dt;
	}

}
