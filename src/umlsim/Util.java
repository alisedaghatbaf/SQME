package umlsim;

import java.util.Random;

public class Util {
	
	public static void setError(Data dt, double prob){
		Random rnd = new Random();
		double val = rnd.nextDouble();
		if (val < prob)
			dt.setError(true);
	}
	
	public static void hostDemand(Host host, double demand){
		
	}

}
