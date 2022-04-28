package org.sqme;

import java.util.ArrayList;

import java.util.List;

import org.moeaframework.core.FastNondominatedSorting;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.core.comparator.DominanceComparator;
import org.moeaframework.core.comparator.ObjectiveComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;

/**
 * Fast non-dominated sorting algorithm for dominance depth ranking. Assigns the
 * {@code rank} and {@code crowdingDistance} attributes to solutions. Solutions
 * of rank 0 belong to the Pareto non-dominated front.
 * <p>
 * References:
 * <ol>
 * <li>Deb et al (2002). "A Fast and Elitist Multiobjective Genetic Algorithm:
 * NSGA-II." IEEE Transactions on Evolutionary Computation. 6(2):182-197.
 * </ol>
 */
public class UncertainSorting extends FastNondominatedSorting {

	/**
	 * Constructs a fast non-dominated sorting operator using Pareto dominance.
	 */
	public UncertainSorting() {
		this(new UncertainObjectiveComparator());
	}

	/**
	 * Constructs a fast non-dominated sorting operator using the specified
	 * dominance comparator.
	 * 
	 * @param comparator
	 *            the dominance comparator
	 */
	public UncertainSorting(DominanceComparator comparator) {
		super(comparator);

	}

	/**
	 * Computes and assigns the {@code crowdingDistance} attribute to solutions.
	 * The specified population should consist of solutions within the same
	 * front/rank.
	 * 
	 * @param front
	 *            the population whose solutions are to be evaluated
	 */
	public void updateCrowdingDistance(Population front) {
		int n = front.size();

		if (n < 3) {
			for (Solution solution : front) {
				solution.setAttribute(CROWDING_ATTRIBUTE,
						Double.POSITIVE_INFINITY);
			}
		} else {
			int numberOfObjectives = ((UncertainSolution) front.get(0))
					.getNumberOfUncertainObjectives();

			for (Solution solution : front) {
				solution.setAttribute(CROWDING_ATTRIBUTE, 0.0);
			}

			for (int i = 0; i < numberOfObjectives; i++) {
				front.sort(new UncertainObjectiveComparator(i));

				front.get(0).setAttribute(CROWDING_ATTRIBUTE,
						Double.POSITIVE_INFINITY);
				front.get(n - 1).setAttribute(CROWDING_ATTRIBUTE,
						Double.POSITIVE_INFINITY);

				for (int j = 1; j < n - 1; j++) {
					UncertainSolution curSol = (UncertainSolution) front.get(j);
					double distance = (Double) curSol
							.getAttribute(CROWDING_ATTRIBUTE);
					UncertainSolution nextSol = (UncertainSolution) front
							.get(j + 1);
					UncertainSolution prevSol = (UncertainSolution) front
							.get(j - 1);
					distance += (UncertainSolution.distance(curSol, prevSol) + UncertainSolution
							.distance(curSol, nextSol)) / 2;

					front.get(j).setAttribute(CROWDING_ATTRIBUTE, distance);
				}
			}
		}
	}

}
