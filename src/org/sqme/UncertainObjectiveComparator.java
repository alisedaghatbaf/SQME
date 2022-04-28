package org.sqme;

import java.io.Serializable;
import java.util.Comparator;

import org.moeaframework.core.Solution;
import org.moeaframework.core.comparator.DominanceComparator;

/**
 * Compares two solutions using the value of a specific objective.
 */
public class UncertainObjectiveComparator implements DominanceComparator,
		Comparator<Solution>, Serializable {

	private static final long serialVersionUID = -6718367624398691971L;

	private int objective;

	public UncertainObjectiveComparator() {
		// TODO Auto-generated constructor stub
		super();
		objective = -1;
	}
	
	public UncertainObjectiveComparator(int i) {
		// TODO Auto-generated constructor stub
		super();
		objective = i;
	}
	
	

	@Override
	public int compare(Solution solution1, Solution solution2) {
		boolean dominate1 = false;
		boolean dominate2 = false;

		UncertainSolution s1 = (UncertainSolution) solution1;
		UncertainSolution s2 = (UncertainSolution) solution2;
		if (objective == -1) {
			for (int i = 0; i < s1.getNumberOfUncertainObjectives(); i++) {
				Interval i1 = s1.getUncertainObjective(i);
				Interval i2 = s2.getUncertainObjective(i);

				if (i1.confidenceLevel(i2) < i2.confidenceLevel(i1)) {
					dominate1 = true;

					if (dominate2) {
						return 0;
					}
				} else if (i1.confidenceLevel(i2) > i2.confidenceLevel(i1)) {
					dominate2 = true;

					if (dominate1) {
						return 0;
					}
				}
			}
		} else {
			Interval i1 = s1.getUncertainObjective(objective);
			Interval i2 = s2.getUncertainObjective(objective);

			if (i1.confidenceLevel(i2) < i2.confidenceLevel(i1)) {
				dominate1 = true;

				if (dominate2) {
					return 0;
				}
			} else if (i1.confidenceLevel(i2) > i2.confidenceLevel(i1)) {
				dominate2 = true;

				if (dominate1) {
					return 0;
				}
			}
		}

		if (dominate1 == dominate2) {
			return 0;
		} else if (dominate1) {
			return -1;
		} else {
			return 1;
		}
	}

}
