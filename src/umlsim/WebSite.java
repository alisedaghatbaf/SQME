package umlsim;

public class WebSite extends Component {

	private Scheduler ss;

	public WebSite() {
		super(new WebServer());
		// TODO Auto-generated constructor stub
		ss = new Scheduler();
	}

	private void init() {

	}

	private boolean decide(Data dt) {
		return !dt.isError();
	}

	private void displayData(Data dt) {

		Util.hostDemand(getHost(), 2);

	}

	private void displayError(Data dt) {
		Util.hostDemand(getHost(), 2);
	}

	public void execute() {
		init();

		Data dt = ss.requestData();
		if (decide(dt))
			displayData(dt);
		else
			displayError(dt);
	}

}
