package org.sqme;

import org.moeaframework.Executor;
import org.moeaframework.core.EpsilonBoxDominanceArchive;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.comparator.ParetoDominanceComparator;

public class UncertainExecutor extends Executor {

	@Override
	protected NondominatedPopulation newArchive() {

		return new UncertainNondominatedPopulation(new UncertainObjectiveComparator());

	}

}
