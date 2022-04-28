package org.sqme;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.BinaryVariable;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.problem.AbstractProblem;
import org.moeaframework.util.Vector;
import org.moeaframework.util.io.CommentedLineReader;

public class UncertainProblem extends AbstractProblem {

	int numberOfDesignOptions, numberOfQualityMeasures;

	public UncertainProblem(int numberOfQualityMeasures,
			int numberOfDesignChoices) {
		super(1, numberOfQualityMeasures);
		// TODO Auto-generated constructor stub
		this.numberOfDesignOptions = numberOfDesignChoices;
		this.numberOfQualityMeasures = numberOfQualityMeasures;
	}

	public UncertainProblem() {
		this(2, 10);

	}

	@Override
	public void evaluate(Solution solution) {
		// TODO Auto-generated method stub
		BinaryVariable var = (BinaryVariable) solution.getVariable(0);
		UncertainSolution sol = (UncertainSolution) solution;
		
		double perf_lb = var.cardinality() / (double)var.getNumberOfBits(), perf_ub = perf_lb
				+ 1 / var.getNumberOfBits(), reli_lb = (1- perf_ub)/2, reli_ub =  (1-perf_lb)/2;
		
		sol.setUncertainObjective(0, new Interval(perf_lb, perf_ub));
		
		sol.setUncertainObjective(1, new Interval(reli_lb, reli_ub));
	}

	@Override
	public Solution newSolution() {
		UncertainSolution solution = new UncertainSolution(1,
				numberOfQualityMeasures, 0);
		
		solution.setVariable(0, EncodingUtils.newBinary(numberOfDesignOptions));

		return solution;
	}
}
