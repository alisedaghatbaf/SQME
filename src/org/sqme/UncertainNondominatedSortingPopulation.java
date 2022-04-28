package org.sqme;


import java.util.Comparator;

import java.util.Iterator;
import static com.arch.UncertainSorting.RANK_ATTRIBUTE;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.NondominatedSortingPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.core.comparator.DominanceComparator;
import org.moeaframework.core.comparator.RankComparator;

public class UncertainNondominatedSortingPopulation extends NondominatedSortingPopulation {

	
	/**
	 * Constructs an empty population that maintains the {@code rank} and
	 * {@code crowdingDistance} attributes for its solutions.
	 */
	public UncertainNondominatedSortingPopulation() {
		// TODO Auto-generated constructor stub
		this(new UncertainObjectiveComparator());
	}

	/**
	 * Constructs an empty population that maintains the {@code rank} and
	 * {@code crowdingDistance} attributes for its solutions.
	 * 
	 * @param comparator
	 *            the dominance comparator
	 */
	public UncertainNondominatedSortingPopulation(DominanceComparator comparator) {
		super(new UncertainSorting(comparator));

		
	}

	/**
	 * Constructs a population initialized with the specified solutions that
	 * maintains the {@code rank} and {@code crowdingDistance} attributes for
	 * its solutions.
	 * 
	 * @param comparator
	 *            the dominance comparator
	 * @param iterable
	 *            the solutions used to initialize this population
	 */
	public UncertainNondominatedSortingPopulation(
			DominanceComparator comparator,
			Iterable<? extends Solution> iterable) {
		this(comparator);
		addAll(iterable);
	}

	/**
	 * Constructs a population initialized with the specified solutions that
	 * maintains the {@code rank} and {@code crowdingDistance} attributes for
	 * its solutions.
	 * 
	 * @param iterable
	 *            the solutions used to initialize this population
	 */
	public UncertainNondominatedSortingPopulation(
			Iterable<? extends Solution> iterable) {
		this(new UncertainObjectiveComparator(), iterable);
	}

	/**
	 * Equivalent to calling {@code truncate(size, 
	 * new NondominatedSortingComparator())}.
	 * 
	 * @param size
	 *            the target population size after truncation
	 */
	public void truncate(int size) {
		truncate(size, new UncertainObjectiveComparator());
	}


	

}
