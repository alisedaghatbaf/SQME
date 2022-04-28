package ftplugin.model;


public class Judgment {
	Double lowerBound, upperBound, pba;

	public Judgment(double lb, double ub, double p) {
		lowerBound = lb;
		upperBound = ub;
		pba = p;
	}

	public void AND(Judgment target) {
		// if (lowerBound == 0)
		// lowerBound = target.lowerBound;
		// else if (target.lowerBound != 0)

		lowerBound *= target.lowerBound;

		upperBound *= target.upperBound;
		pba *= target.pba;
	}

	public Judgment Intersect(Judgment target) {
		Judgment result = new Judgment(Math.max(lowerBound, target.lowerBound),
				Math.min(upperBound, target.upperBound), target.pba);
		if (result.upperBound < result.lowerBound)
			return new Judgment(1.0, 1.0, target.pba);
		return result;
	}

	public void OR(Judgment target) {
		Judgment oneMinusThis = new Judgment(1 - upperBound, 1 - lowerBound,
				pba);
		Judgment oneMinusTarget = new Judgment(1 - target.upperBound,
				1 - target.lowerBound, target.pba);
		lowerBound = 1 - oneMinusThis.upperBound * oneMinusTarget.upperBound;
		upperBound = 1 - oneMinusThis.lowerBound * oneMinusTarget.lowerBound;

		pba = pba * target.pba;

	}
	
	public void ADD(Judgment target){
		lowerBound+=target.lowerBound;
		if(lowerBound > 1)
			lowerBound = 1.;
		upperBound+=target.upperBound;
		if(upperBound > 1)
			upperBound = 1.;
		
		pba = pba * target.pba;
	}

	public void NOR(Judgment target) {
		Judgment oneMinusThis = new Judgment(1 - upperBound, 1 - lowerBound,
				pba);
		Judgment oneMinusTarget = new Judgment(1 - target.upperBound,
				1 - target.lowerBound, target.pba);
		upperBound = oneMinusThis.upperBound * oneMinusTarget.upperBound;
		lowerBound = oneMinusThis.lowerBound * oneMinusTarget.lowerBound;
		pba = pba * target.pba;

	}

	public String toString() {
		return lowerBound + " " + upperBound + " " + pba;
	}

	public void print() {
		System.out.println(this);
	}

	public Judgment clone() {
		return new Judgment(lowerBound, upperBound, pba);
	}

}
