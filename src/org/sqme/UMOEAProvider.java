package org.sqme;

import java.util.Properties;

import org.moeaframework.core.Algorithm;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.NondominatedSortingPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Variation;
import org.moeaframework.core.comparator.ChainedComparator;
import org.moeaframework.core.comparator.CrowdingComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.operator.RandomInitialization;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.core.spi.AlgorithmProvider;
import org.moeaframework.core.spi.OperatorFactory;
import org.moeaframework.util.TypedProperties;

public class UMOEAProvider extends AlgorithmProvider {

	@Override
	public Algorithm getAlgorithm(String name, Properties properties,
			Problem problem) {
		if (name.equals("UMOEA")) {
			TypedProperties typedProperties = new TypedProperties(properties);

			// allow the user to customize the population size (default to 100)
			int populationSize = typedProperties.getInt("populationSize", 100);

			// initialize the algorithm with randomly-generated solutions
			Initialization initialization = new RandomInitialization(problem,
					populationSize);
			UncertainNondominatedSortingPopulation population = new UncertainNondominatedSortingPopulation();

			TournamentSelection selection = new TournamentSelection(2,
					new ChainedComparator(new UncertainObjectiveComparator(),
							new CrowdingComparator()));

			Variation variation = OperatorFactory.getInstance().getVariation(
					null, properties, problem);

			// construct and return the RandomWalker algorithm
			return new UncertainMOEA(problem, population,null, selection, variation,initialization);

		}
		// TODO Auto-generated method stub
		return null;
	}

}
