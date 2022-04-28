package org.sqme;

import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.examples.ga.knapsack.Knapsack;
import org.moeaframework.util.Vector;

public class UncertainExample {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		NondominatedPopulation result = new UncertainExecutor()
				.withProblemClass(UncertainProblem.class)
				.withAlgorithm("UMOEA").withMaxEvaluations(50000).run();

		// print the results
		for (int i = 0; i < result.size(); i++) {
			UncertainSolution solution = (UncertainSolution) result.get(i);
			Interval[] objectives = solution.getUncertainObjectives();

			System.out.println("Solution " + (i + 1) + ":");
			System.out.println("    performance: " + objectives[0]);
			System.out.println("    reliability : " + objectives[1]);
			System.out.println("    Binary String: " + solution.getVariable(0));
		}
	}

}
