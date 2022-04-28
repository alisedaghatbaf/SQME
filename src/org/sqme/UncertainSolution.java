package org.sqme;

import java.io.Serializable;
import java.util.Map;

import org.moeaframework.core.FrameworkException;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;

public class UncertainSolution extends Solution {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Interval[] uncertainObjectives;

	public UncertainSolution(Interval[] uncertainObjectives) {
		this(0, uncertainObjectives.length, 0);
		for (int i = 0; i < uncertainObjectives.length; i++)
			setUncertainObjective(i, uncertainObjectives[i]);

		// TODO Auto-generated constructor stub
	}

	public UncertainSolution(int numberOfVariables,
			int numberOfUncertainObjectives, int numberOfConstraints) {
		super(numberOfVariables, numberOfUncertainObjectives,
				numberOfConstraints);
		uncertainObjectives = new Interval[numberOfUncertainObjectives];
	}

	/**
	 * Copy constructor.
	 * 
	 * @param solution
	 *            the solution being copied
	 */
	protected UncertainSolution(UncertainSolution solution) {
		this(solution.getNumberOfVariables(), solution
				.getNumberOfUncertainObjectives(), solution
				.getNumberOfConstraints());

		for (int i = 0; i < solution.getNumberOfVariables(); i++) {
			setVariable(i, solution.getVariable(i).copy());
		}

		for (int i = 0; i < getNumberOfUncertainObjectives(); i++) {
			setUncertainObjective(i, solution.getUncertainObjective(i));
		}

		for (int i = 0; i < getNumberOfConstraints(); i++) {
			setConstraint(i, solution.getConstraint(i));
		}
	}

	/**
	 * Returns an independent copy of this solution. It is required that
	 * {@code x.copy()} is completely independent from {@code x} . This means
	 * any method invoked on {@code x.copy()} in no way alters the state of
	 * {@code x} and vice versa. It is typically the case that
	 * {@code x.copy().getClass() == x.getClass()} and
	 * {@code x.copy().equals(x)}
	 * <p>
	 * Note that a solution's attributes are not copied, as the attributes are
	 * generally specific to each instance.
	 * 
	 * @return an independent copy of this solution
	 */
	public UncertainSolution copy() {
		return new UncertainSolution(this);
	}

	/**
	 * Returns the number of objectives defined by this solution.
	 * 
	 * @return the number of objectives defined by this solution
	 */
	public int getNumberOfUncertainObjectives() {
		return uncertainObjectives.length;
	}

	/**
	 * Returns the objective at the specified index.
	 * 
	 * @param index
	 *            index of the objective to return
	 * @return the objective at the specified index
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range
	 *             {@code (index < 0) || (index >= getNumberOfObjectives())}
	 */
	public Interval getUncertainObjective(int index) {
		return uncertainObjectives[index];
	}

	@Override
	public double getObjective(int index) throws FrameworkException {
		throw new FrameworkException(
				"invalid invokation of getObjective method for uncertain parameters");
	}

	private static double overlappingDegree(UncertainSolution solution1,
			UncertainSolution solution2) {
		double result = 1.;
		for (int i = 0; i < solution1.getNumberOfUncertainObjectives(); i++) {
			Interval i1 = solution1.getUncertainObjective(i);
			Interval i2 = solution2.getUncertainObjective(i);

			result *= i1.overlapWidth(i2);
		}
		return result;
	}

	public static double distance(UncertainSolution solution1,
			UncertainSolution solution2) {
		double sum = 0.;
		for (int i = 0; i < solution1.getNumberOfUncertainObjectives(); i++) {
			Interval i1 = solution1.getUncertainObjective(i);
			Interval i2 = solution2.getUncertainObjective(i);
			sum += Math.abs(i1.getMidpoint() - i2.getMidpoint());

		}
		return sum
				/ (overlappingDegree(solution1, solution2) + solution1.volume()
						+ solution2.volume() + 1);
	}

	private double volume() {
		double result = 1.0;
		for (Interval i : uncertainObjectives)
			result *= i.width();
		return result;
	}

	/**
	 * Sets the objective at the specified index.
	 * 
	 * @param index
	 *            index of the objective to set
	 * @param objective
	 *            the new value of the objective being set
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range
	 *             {@code (index < 0) || (index >= getNumberOfObjectives())}
	 */
	public void setUncertainObjective(int index, Interval objective) {
		uncertainObjectives[index] = objective;
	}

	/**
	 * Sets all objectives of this solution.
	 * 
	 * @param objectives
	 *            the new objectives for this solution
	 * @throws IllegalArgumentException
	 *             if {@code objectives.length !=
	 *         getNumberOfObjectives()}
	 */
	public void setUncertainObjectives(Interval[] objectives) {
		if (objectives.length != this.uncertainObjectives.length) {
			throw new IllegalArgumentException("invalid number of objectives");
		}

		for (int i = 0; i < objectives.length; i++) {
			uncertainObjectives[i] = objectives[i];
		}
	}

	/**
	 * Returns an array containing the objectives of this solution. Modifying
	 * the returned array will not modify the internal state of this solution.
	 * 
	 * @return an array containing the objectives of this solution
	 */
	public Interval[] getUncertainObjectives() {
		return uncertainObjectives.clone();
	}

}
