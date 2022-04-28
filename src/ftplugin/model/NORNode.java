package ftplugin.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

public class NORNode extends ModelElement {

	public NORNode(FTModel model, ModelElement parent, String name) {
		super(model, parent, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public IFigure createFigure() {
		Figure figure = new Figure();
		figure.setLayoutManager(new FreeformLayout());

		IFigure shape = new RectangleFigure();
		shape.setBounds(new Rectangle(0, 0, 60, 40));
		// shape.setBackgroundColor(org.eclipse.draw2d.ColorConstants.black);
		Label label = new Label();
		label.setText(get_name());
		label.setSize(60, 15);
		label.setLocation(new Point(0, 10));
		figure.add(shape);
		shape = new Ellipse();
		shape.setBounds(new Rectangle(27, 40, 6, 6));
		figure.add(shape);
		shape = new Ellipse();
		shape.setBounds(new Rectangle(10, 46, 40, 40));
		figure.add(shape);
		shape = new Ellipse();
		shape.setBounds(new Rectangle(10, 60, 40, 40));
		figure.add(shape);
		figure.add(label);
		figure.setSize(60, 72);
		return figure;
	}

	@Override
	public boolean enabled() {
		if (enabledCount == 1)
			return false;
		for (ModelElement ch : childElements.values())
			if (ch.getProbability() == null && ch.getJudgments().isEmpty())
				return false;

		enabledCount++;
		return true;
	}

	@Override
	public void execute() {

		// TODO Auto-generated method stub
		probability = 0.0;
		boolean hasJudge = false;
		
		for (ModelElement ch : childElements.values()) {
			if (!ch.getJudgments().isEmpty()) {
				hasJudge = true;
				continue;
			}
			probability += ch.getProbability();

		}
		probability = 1 - probability;
		if (!hasJudge)
			return;
		GenericTreeNode<Judgment> root = new GenericTreeNode<Judgment>(
				new Judgment(0.0, 0.0, 1.0));
		int size = childElements.size();
		for (int i = 0; i < size - 1; i++) {
			ModelElement ch = (ModelElement) childElements.values().toArray()[i];
			List<Judgment> chJudgs = ch.getJudgments();
			if (chJudgs.isEmpty())
				continue;
			
			
			createJudgeTree(root, chJudgs);
		}
		List<GenericTreeNode<Judgment>> leaves = new ArrayList<GenericTreeNode<Judgment>>();
		getLeafNodes(root, leaves);
		
		int i = 0;
		
		List<Judgment> complementJudges = ((ModelElement) childElements
				.values().toArray()[size - 1]).getJudgments();
		int dinom = leaves.size()/complementJudges.size();
		for (GenericTreeNode<Judgment> leaf : leaves) {

			Judgment result = calculateJudgment(leaf, complementJudges.get(i));
			
			judgments.add(result);
			if((i+1)%dinom==0)
			i++;

		}

		/*
		 * List<Judgment> newJudges = new ArrayList<Judgment>(); for (Judgment
		 * judge : judgments) { ModelElement lastChild = (ModelElement)
		 * childElements.values() .toArray()[size - 1]; for (Judgment lastJ :
		 * lastChild.judgments) { Judgment limit = new Judgment(1 -
		 * judge.upperBound, 1 - judge.lowerBound, judge.pba); Judgment
		 * intersect = limit.Intersect(lastJ);
		 * 
		 * intersect.OR(judge);
		 * 
		 * newJudges.add(new Judgment(1-intersect.upperBound,
		 * 1-intersect.lowerBound, intersect.pba));
		 * 
		 * } }
		 * 
		 * judgments = newJudges;
		 */

		for (Judgment judge : judgments) {
			
			judge.lowerBound *= probability;
			judge.upperBound *= probability;
			
		}
	

	}

	<T> void getLeafNodes(GenericTreeNode<T> root,
			List<GenericTreeNode<T>> leafNodes) {

		if (root.getChildren().isEmpty()) {
			leafNodes.add(root);
			return;
		}
		for (GenericTreeNode<T> child : root.getChildren()) {
			getLeafNodes(child, leafNodes);
		}

	}

	void write(String text) {
		BufferedWriter writer = null;
		try {
			// create a temporary file

			File logFile = new File("tmp.txt");

			writer = new BufferedWriter(new FileWriter(logFile, true));
			writer.append(text + "\n");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// Close the writer regardless of what happens...
				writer.close();
			} catch (Exception e) {
			}
		}
	}

	Judgment calculateJudgment(GenericTreeNode<Judgment> leaf,
			Judgment complement) {

		Judgment result = new Judgment(complement.upperBound,
				complement.lowerBound, complement.pba);
		GenericTreeNode<Judgment> curLeaf = leaf;
		List<Judgment> branch = new ArrayList<Judgment>();
		do {
			branch.add(curLeaf.getData());
			curLeaf = curLeaf.getParent();
		} while (curLeaf.getParent() != null);
		String text= "";
		for (Judgment j : branch) {
			text+=complement+" -- "+j+" + "+result;
			
			//result.OR(j);
			result.ADD(j);
			text+="result: "+result;
		}
		text+="end of branch: "+result;
		//write(text);
		double tmp = result.lowerBound;
		result.lowerBound = 1-result.upperBound;
		result.upperBound = 1-tmp;
		
		if(result.lowerBound > result.upperBound){
			tmp = result.lowerBound;
			result.lowerBound = result.upperBound;
			result.upperBound = tmp;
		}
		
		/*
		 * 
		 * double[] coefs = new double[branch.size()]; int i = 0; double[][]
		 * vars = new double[branch.size()][branch.size()];
		 * 
		 * for (;i<coefs.length;i++) {
		 * 
		 * vars[i][i] = coefs[i] = 1;
		 * 
		 * } i = 0; LinearProgram lp = new LinearProgram(coefs); for (Judgment
		 * bj : branch) { System.out.print(bj+" -- "); lp.addConstraint(new
		 * LinearBiggerThanEqualsConstraint(vars[i], bj.lowerBound, "lc" + i));
		 * lp.addConstraint(new LinearSmallerThanEqualsConstraint(vars[i],
		 * bj.upperBound, "uc" + i));
		 * 
		 * i++; } lp.addConstraint(new LinearBiggerThanEqualsConstraint(coefs,
		 * 0, "lz" + i)); lp.addConstraint(new
		 * LinearSmallerThanEqualsConstraint(coefs, 1, "uo" + i));
		 * lp.setMinProblem(true);
		 * 
		 * 
		 * double[] sol = solver.solve(lp); result.lowerBound =
		 * result.upperBound = 1.; for(int ii=0;ii<sol.length;ii++){
		 * System.out.println(sol[ii]); result.lowerBound-=sol[ii]; }
		 * lp.setMinProblem(false); sol = solver.solve(lp);
		 * 
		 * for(int ii=0;ii<sol.length;ii++){ result.upperBound-=sol[ii]; }
		 * System.out.println(result); /* do { GenericTreeNode<Judgment> parent
		 * = curLeaf.getParent(); if (parent == null) { double tmp = 1 -
		 * result.lowerBound; result.lowerBound = 1 - result.upperBound;
		 * result.upperBound = tmp;
		 * 
		 * break; } result.OR(parent.getData());
		 * 
		 * curLeaf = parent; } while (true);
		 */

		return result;
	}

	void createJudgeTree(GenericTreeNode<Judgment> tree,
			List<Judgment> judgeList) {
		List<GenericTreeNode<Judgment>> leafNodes = new ArrayList<GenericTreeNode<Judgment>>();
		getLeafNodes(tree, leafNodes);
		for (GenericTreeNode<Judgment> leaf : leafNodes) {
			for (Judgment judgment : judgeList) {
				leaf.addChild(new GenericTreeNode<Judgment>(judgment));

			}
		}

	}

}
