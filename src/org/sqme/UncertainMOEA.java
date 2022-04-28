package org.sqme;

import org.moeaframework.algorithm.AbstractEvolutionaryAlgorithm;
import org.moeaframework.algorithm.NSGAII;
import org.moeaframework.core.EpsilonBoxDominanceArchive;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.NondominatedSortingPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Selection;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

public class UncertainMOEA extends NSGAII {

	public UncertainMOEA(Problem problem,
			NondominatedSortingPopulation population,
			EpsilonBoxDominanceArchive archive, Selection selection,
			Variation variation, Initialization initialization) {
		super(problem, population, archive, selection, variation,
				initialization);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void iterate() {
		// TODO Auto-generated method stub
		UncertainNondominatedSortingPopulation population = (UncertainNondominatedSortingPopulation) super
				.getPopulation();
		EpsilonBoxDominanceArchive archive =  super
				.getArchive();
		Population offspring = new Population();
		int populationSize = population.size();

		while (offspring.size() < populationSize) {
			Solution[] parents = selection.select(variation.getArity(),
					population);
			Solution[] children = variation.evolve(parents);

			offspring.addAll(children);
		}

		evaluateAll(offspring);

		if (archive != null) {
			archive.addAll(offspring);
		}

		population.addAll(offspring);
		population.truncate(populationSize);
	}

	@Override
	public NondominatedPopulation getResult() {
		Population population = getPopulation();
		NondominatedPopulation archive = getArchive();
		NondominatedPopulation result = new UncertainNondominatedPopulation();
		
		result.addAll(population);

		if (archive != null) {
			result.addAll(archive);
		}

		return result;
	}
}
