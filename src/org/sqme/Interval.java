package org.sqme;

import java.lang.annotation.Target;

public class Interval {

	private final double lowerBound, upperBound;

	public Interval(double lb, double ub) {
		lowerBound = lb;
		upperBound = ub;
	}

	public double getLowerBound() {
		return lowerBound;
	}

	public double getUpperBound() {
		return upperBound;
	}

	public boolean includes(double value) {
		return lowerBound <= value && value <= upperBound;
	}

	public boolean equals(Interval arg0) {
		return lowerBound == arg0.lowerBound && upperBound == arg0.upperBound;
	}

	public boolean includes(Interval interval) {
		return lowerBound <= interval.lowerBound
				&& upperBound >= interval.upperBound;
	}

	public Interval intersect(Interval target) {
		if (this.includes(target))
			return new Interval(target.lowerBound, target.upperBound);
		if (target.includes(this))
			return new Interval(lowerBound, upperBound);
		if (this.includes(target.lowerBound))
			return new Interval(target.lowerBound, upperBound);
		if (this.includes(target.upperBound))
			return new Interval(lowerBound, target.upperBound);
		return null;
	}
	
	public double getMidpoint(){
		return (lowerBound + upperBound)/2;
	}
	
	
	public double width(){
		return upperBound-lowerBound;
	}
	
	public double overlapWidth(Interval target){
		Interval intersection = this.intersect(target);
		if(intersection == null)
			return 0;
		return intersection.upperBound - intersection.lowerBound;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "[" + lowerBound + ", " + upperBound + "]";
	}

	public double confidenceLevel(Interval target) {
		double lb1 = lowerBound;
		double lb2 = target.lowerBound;
		double ub1 = upperBound;
		double ub2 = target.upperBound;
		double beta_max = Math.max(ub1, ub2);
		double beta_min = 0.;
		if (beta_max == ub1) {
			beta_min = Math.max(lb1, ub2);
		} else {
			beta_min = Math.max(lb2, ub1);
		}
		Interval maximal = new Interval(beta_min, beta_max);
		double dist = target.distance(maximal);
		return dist / (dist + distance(maximal));
	}

	private double distance(Interval target) {
		return Math.sqrt((Math.pow(lowerBound - target.lowerBound, 2) + Math
				.pow(upperBound - target.upperBound, 2)) / 2);

	}

}