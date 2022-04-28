package tpnplugin.model;

import java.util.ArrayList;
import java.util.List;

import net.sf.javailp.Linear;
import net.sf.javailp.OptType;
import net.sf.javailp.Problem;
import net.sf.javailp.Result;
import net.sf.javailp.Solver;
import net.sf.javailp.SolverFactory;
import net.sf.javailp.SolverFactoryGLPK;

public class LPP {

	private TPNModel tpn;
	private Problem problem;

	public LPP(TPNModel pn) {
		tpn = pn;
	}

	public double solveTLB(int[] combInd, Transition t) {
		problem = new Problem();

		defineConstraints(problem, combInd);
		defineThroughputLBObjective(problem, t);
		defineVariableTypes(problem);
			SolverFactory factory = new SolverFactoryGLPK();
		Solver solver = factory.get();
		Result result = solver.solve(problem);
		if (result == null)
			return -1;

		return result.get("x" + tpn.getTransitionIndex(t)).doubleValue();
	}

	public double solveTUB(int[] combInd, Transition t) {
		problem = new Problem();

		defineConstraints(problem, combInd);
		defineThroughputUBObjective(problem, t);
		defineVariableTypes(problem);
		
		SolverFactory factory = new SolverFactoryGLPK();
		Solver solver = factory.get();
		Result result = solver.solve(problem);

		if (result == null)
			return -1;
		return result.get("x" + tpn.getTransitionIndex(t)).doubleValue();
	}

	private void defineVariableTypes(Problem problem) {
		for (int i = 0; i < tpn.transitions.size(); i++) {
			problem.setVarType("x" + i, Double.class);
			problem.setVarType("s" + i, Double.class);
		}
		for (int i = 0; i < tpn.places.size(); i++) {
			problem.setVarType("p" + i, Double.class);

		}
	}

	private void defineThroughputLBObjective(Problem problem, Transition trans) {
		Linear linear = new Linear();
		linear.add(1, "x" + tpn.getTransitionIndex(trans));
		problem.setObjective(linear, OptType.MIN);
	}

	private void defineThroughputUBObjective(Problem problem, Transition trans) {
		Linear linear = new Linear();
		linear.add(1, "x" + tpn.getTransitionIndex(trans));
		problem.setObjective(linear, OptType.MAX);
	}

	private double computeN(int placeInd) {
		Problem problem = new Problem();
		int placeCount = tpn.places.size();
		int transitionCount = tpn.transitions.size();

		for (int i = 0; i < placeCount; i++) {
			Linear linear = new Linear();
			linear.add(1, "p" + i);
			problem.add(linear, ">=", 0);
		}

		for (int i = 0; i < transitionCount; i++) {
			Linear linear = new Linear();
			linear.add(1, "s" + i);
			problem.add(linear, ">=", 0);
		}

		for (int i = 0; i < placeCount; i++) {
			Linear linear = new Linear();
			linear.add(1, "p" + i); // m(p_i)
			for (int j = 0; j < transitionCount; j++) {
				int aux = -tpn.F[i][j] + tpn.B[i][j];
				if (aux != 0)
					linear.add(aux, "s" + j);
			}
			problem.add(linear, "=", tpn.places.get(i).getInitialMarking());

		}

		Linear linear = new Linear();
		linear.add(1, "p" + placeInd);
		problem.setObjective(linear, OptType.MAX);
		for (int i = 0; i < placeCount; i++) {
			problem.setVarType("p" + i, Double.class);
		}
		for (int i = 0; i < transitionCount; i++) {
			problem.setVarType("s" + i, Double.class);
		}
		
	
		SolverFactory factory = new SolverFactoryGLPK();
		Solver solver = factory.get();
		Result result = solver.solve(problem);
		
		return result.get("p" + placeInd).doubleValue();
	}

	public void defineConstraints(Problem problem, int[] combInd) {
		// \forall t \in T, and \forall p \in \preset(p), x(t) <= m(p)/
		// (s(t)·Pre(p,t))
		Linear linear;
		int placeCount = tpn.places.size();
		int transitionCount = tpn.transitions.size();
		for (int i = 0; i < transitionCount; i++) {
			Transition t = tpn.transitions.get(i);
			if (t.judgments.get(combInd[i]).lowerBound == 0)
				continue;
			for (int j = 0; j < tpn.places.size(); j++) {
				Place p = tpn.places.get(j);
				if (!t.inputPlaces.contains(p))
					continue;
				linear = new Linear();
				linear.add(1., "x" + i);
				linear.add(
						-1.0
								/ (t.judgments.get(combInd[i]).lowerBound * tpn.B[j][i]),
						"p" + i);
				problem.add(linear, "<=", 0.);
			}
			
		}
		
		double[] N = new double[placeCount];
		for (int i = 0; i < placeCount; i++) {
			N[i] = computeN(i);

		}

		// \forall t in T, \preset{t} = {p}
		/* x(t)·s(t) >= (m(p) - Pre(p, t) + 1)/ Pre(p, t) */
		// \forall t in T, \preset{t} = {p_1, p_2}, where smb(p_1) < smb(p_2)
		// (structural marking bound)
		/*
		 * x(t)·s(t)·Pre(p_1, t) >= m(p_1) - Pre(p_1, t) + 1 - smb(p_1)·(1 -
		 * (m(p_2)- Pre(p_2, t) + 1)/(smb(p_2) - Pre(p_2, t) + 1))
		 */
		for (int i = 0; i < transitionCount; i++) {
			Transition t = tpn.transitions.get(i);
			if (t.judgments.get(combInd[i]).lowerBound == 0)
				continue;
			linear = new Linear();
			if (t.inputPlaces.size() == 1) {
				int placeInd = tpn.getPlaceIndex((Place) t.inputPlaces
						.toArray()[0]);
				
				int k = (int) N[placeInd] / tpn.B[placeInd][i];
				linear.add(t.judgments.get(combInd[i]).upperBound, "x" + i);
				if (!Double.isInfinite(N[placeInd])) {
					linear.add(-k / (N[placeInd] - k * tpn.B[placeInd][i] + 1),
							"p" + placeInd);
					problem.add(linear, ">=", k * (-k * tpn.B[placeInd][i] + 1)
							/ (N[placeInd] - k * tpn.B[placeInd][i] + 1));
				} else {
					linear.add(-1. / tpn.B[placeInd][i], "p" + placeInd);
					problem.add(linear, ">=", -1 + 1.0 / tpn.B[placeInd][i]);
				}

			} else if (t.inputPlaces.size() == 2) {

				Place p1 = (Place) t.inputPlaces.toArray()[0];

				Place p2 = (Place) t.inputPlaces.toArray()[1];
				int ind1 = tpn.getPlaceIndex(p1), ind2 = tpn.getPlaceIndex(p2);
				double N1 = N[ind1], N2 = N[ind2];

				if (N1 > N2) {
					double tmp = N1;
					N1 = N2;
					N2 = tmp;
					int tind = ind1;
					ind1 = ind2;
					ind2 = tind;

				}
				linear.add(
							tpn.B[ind1][i] * 1.0
									/ t.judgments.get(combInd[i]).upperBound,
							"x" + i);
				linear.add(-1, "p" + ind1);
				
				linear.add(-N1 / (N2 - tpn.B[ind2][i] + 1), "p" + ind2);
				problem.add(linear, ">=", -tpn.B[ind1][i] + 1 - N1 + N1
						* (-tpn.B[ind2][i] + 1) / (N2 - tpn.B[ind2][i] + 1));

			} else {
				System.out.println("transition " + t
						+ " has more than two input places");
			}

		}

		for (int i = 0; i < placeCount; i++) {
			Place p = tpn.places.get(i);
			linear = new Linear();
			linear.add(1, "p" + i); // m(p_i)
			for (int j = 0; j < transitionCount; j++) {
				int aux = -tpn.F[i][j] + tpn.B[i][j];
				if (aux != 0)
					linear.add(aux, "s" + j);
			}
			// Create constraint m_p - C(p,:)·\sigma = m0(p)
			problem.add(linear, "=", p.getInitialMarking());
		}
		/* Token flow */
		/*
		 * sum(t \in \preset{p}) X(t)·Post(p, t) >= sum(t \in \postset{p})
		 * X(t)·Pre(p, t), \forall p \in P
		 */
		for (int i = 0; i < placeCount; i++) {
			linear = new Linear();
			Place p = tpn.places.get(i);
			List<Transition> trans = tpn.getInputTransitions(p);
			for (int j = 0; j < trans.size(); j++) {
				int tranInd = tpn.getTransitionIndex(trans.get(j));
				linear.add(tpn.F[i][tranInd], "x" + tranInd);
			}
			trans = tpn.getOutputTransitions(p);

			for (int j = 0; j < trans.size(); j++) {
				int tranInd = tpn.getTransitionIndex(trans.get(j));

				linear.add(-tpn.B[i][tranInd], "x" + tranInd); // X(t)·Pre(p,
																// t)
			}

			String relation = ">=";
			if (!Double.isInfinite(N[i]))
				relation = "=";
			problem.add(linear, relation, 0);

		}

		/* Positive values: m, \sigma >= 0, x(t) >= 0 \forall t in T */
		for (int i = 0; i < placeCount; i++) {
			linear = new Linear();
			linear.add(1, "p" + i);
			problem.add(linear, ">=", 0);
		}

		for (int i = 0; i < transitionCount; i++) {
			linear = new Linear();
			linear.add(1, "s" + i);
			problem.add(linear, ">=", 0);
		}
		for (int i = 0; i < transitionCount; i++) {
			linear = new Linear();
			linear.add(1, "x" + i);
			problem.add(linear, ">=", 0);
		}

	}
}
