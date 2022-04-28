package umlsim;

public class Workload {
	private static final int population = 50;

	public static void main(String[] args) {

		try {
			WebSite ws = new WebSite();
			for (int i = 0; i < population; i++) {
				
				ws.execute(S);
				Thread.sleep(50);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
}
