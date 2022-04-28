package org.sqme;
import java.util.Iterator;

import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.Settings;
import org.moeaframework.core.Solution;
import org.moeaframework.core.comparator.DominanceComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;

/**
 * A population that maintains the property of pair-wise non-dominance between
 * all solutions. When the {@code add} method is invoked with a new solution,
 * all solutions currently in the population that are dominated by the new
 * solution are removed. If the new solution is dominated by any member of the
 * population, the new solution is not added.
 */
public class UncertainNondominatedPopulation extends NondominatedPopulation {

	/**
	 * The dominance comparator used by this non-dominated population.
	 */
	protected final DominanceComparator comparator;
	
	/**
	 * If {@code true}, allow duplicate solutions in this non-dominated
	 * population.  Duplicate solutions are those whose Euclidean distance
	 * is smaller than {@code Settings.EPSILON}.
	 */
	protected final boolean allowDuplicates;

	/**
	 * Constructs an empty non-dominated population using the Pareto dominance
	 * relation.
	 */
	public UncertainNondominatedPopulation() {
		this(new UncertainObjectiveComparator());
	}

	/**
	 * Constructs an empty non-dominated population using the specified 
	 * dominance relation.
	 * 
	 * @param comparator the dominance relation used by this non-dominated
	 *        population
	 */
	public UncertainNondominatedPopulation(DominanceComparator comparator) {
		this(comparator, false);
	}
	
	/**
	 * Constructs an empty non-dominated population using the specified 
	 * dominance relation.
	 * 
	 * @param comparator the dominance relation used by this non-dominated
	 *        population
	 * @param allowDuplicates allow duplicate solutions into the non-dominated
	 *        population
	 */
	public UncertainNondominatedPopulation(DominanceComparator comparator,
			boolean allowDuplicates) {
		super();
		this.comparator = comparator;
		this.allowDuplicates = allowDuplicates;
	}

	/**
	 * Constructs a non-dominated population using the Pareto dominance relation
	 * and initialized with the specified solutions.
	 * 
	 * @param iterable the solutions used to initialize this non-dominated
	 *        population
	 */
	public UncertainNondominatedPopulation(Iterable<? extends Solution> iterable) {
		this();
		addAll(iterable);
	}

	/**
	 * Constructs a non-dominated population using the specified dominance
	 * comparator and initialized with the specified solutions.
	 * 
	 * @param comparator the dominance relation used by this non-dominated
	 *        population
	 * @param iterable the solutions used to initialize this non-dominated
	 *        population
	 */
	public UncertainNondominatedPopulation(DominanceComparator comparator,
			Iterable<? extends Solution> iterable) {
		this(comparator);
		addAll(iterable);
	}

	/**
	 * If {@code newSolution} is dominates any solution or is non-dominated with
	 * all solutions in this population, the dominated solutions are removed and
	 * {@code newSolution} is added to this population. Otherwise,
	 * {@code newSolution} is dominated and is not added to this population.
	 */
	@Override
	public boolean add(Solution newSolution) {
		Iterator<Solution> iterator = iterator();

		while (iterator.hasNext()) {
			Solution oldSolution = iterator.next();
			int flag = comparator.compare(newSolution, oldSolution);
			
			if (flag < 0) {
				iterator.remove();
			} else if (flag > 0) {
				return false;
			} else if (!allowDuplicates &&
					distance(newSolution, oldSolution) < Settings.EPS) {
				return false;
			}
		}

		return data.add(newSolution);
	}

	/**
	 * Replace the solution at the given index with the new solution, but only
	 * if the new solution is non-dominated.  To maintain non-dominance within
	 * this population, any solutions dominated by the new solution will also
	 * be replaced.
	 */
	@Override
	public void replace(int index, Solution newSolution) {
		Iterator<Solution> iterator = iterator();

		while (iterator.hasNext()) {
			Solution oldSolution = iterator.next();
			int flag = comparator.compare(newSolution, oldSolution);

			if (flag < 0) {
				iterator.remove();
			} else if (flag > 0) {
				return;
			} else if (!allowDuplicates &&
					distance(newSolution, oldSolution) < Settings.EPS) {
				return;
			}
		}

		super.replace(index, newSolution);
	}

	/**
	 * Adds the specified solution to the population, bypassing the
	 * non-domination check. This method should only be used when a
	 * non-domination check has been performed elsewhere, such as in a subclass.
	 * <p>
	 * <b>This method should only be used internally, and should never be made
	 * public by any subclasses.</b>
	 * 
	 * @param newSolution the solution to be added
	 * @return true if the population was modified as a result of this operation
	 */
	protected boolean forceAddWithoutCheck(Solution newSolution) {
		return super.add(newSolution);
	}

	/**
	 * Returns the Euclidean distance between two solutions in objective space.
	 * 
	 * @param s1 the first solution
	 * @param s2 the second solution
	 * @return the distance between the two solutions in objective space
	 */
	protected double distance(Solution s1, Solution s2) {
		double distance = 0.0;
		UncertainSolution sol1 = (UncertainSolution)s1;
		UncertainSolution sol2 = (UncertainSolution)s2;
		
		for (int i = 0; i < sol1.getNumberOfUncertainObjectives(); i++) {
			Interval i1 = sol1.getUncertainObjective(i);
			Interval i2 = sol2.getUncertainObjective(i);
			
			distance += (Math.pow(i1.getLowerBound() - i2.getLowerBound(), 2) + Math
					.pow(i1.getUpperBound() - i2.getUpperBound(), 2)) / 2;
		}

		return Math.sqrt(distance);
	}

	/**
	 * Returns the dominance comparator used by this non-dominated population.
	 * 
	 * @return the dominance comparator used by this non-dominated population
	 */
	public DominanceComparator getComparator() {
		return comparator;
	}

}
