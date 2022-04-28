package tpnplugin.model;

public class Judgment {
	Double lowerBound, upperBound, pba;

	public Judgment(double lb, double ub, double p) {
		lowerBound = lb;
		upperBound = ub;
		pba = p;
	}

	public String toString() {
		return lowerBound + " " + upperBound + " : " + pba;
	}
}
