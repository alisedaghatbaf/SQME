package atplugin.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import jmetal.problems.singleObjective.OneMax;

import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

public class ORNode extends ModelElement {

	public ORNode(ATModel model, ModelElement parent, String name) {
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

		shape.setBounds(new Rectangle(10, 40, 40, 40));
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
		probability = 1.0;
		boolean hasJudge = false;
		for (ModelElement ch : childElements.values()) {
			if (!ch.getJudgments().isEmpty()) {
				hasJudge = true;
				continue;
			}
			probability *= 1 - ch.getProbability();

		}
		probability = 1 - probability;
		if (!hasJudge)
			return;
		GenericTreeNode<Judgment> root = new GenericTreeNode<Judgment>(
				new Judgment(0.0, 0.0, 1.0));
		for (ModelElement ch : childElements.values()) {
			List<Judgment> chJudgs = ch.getJudgments();
			if (chJudgs.isEmpty())
				continue;
			createJudgeTree(root, chJudgs);

		}
		List<GenericTreeNode<Judgment>> leaves = new ArrayList<GenericTreeNode<Judgment>>();
		getLeafNodes(root, leaves);

		for (GenericTreeNode<Judgment> leaf : leaves) {

			Judgment result = calculateJudgment(leaf);
			judgments.add(result);
			System.out.println(result);
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

	Judgment calculateJudgment(GenericTreeNode<Judgment> leaf) {

		Judgment result = new Judgment(1., 1., 1.);
		GenericTreeNode<Judgment> curLeaf = leaf;
		List<Judgment> branch = new ArrayList<Judgment>();
		do {
			branch.add(curLeaf.getData());
			curLeaf = curLeaf.getParent();
		} while (curLeaf.getParent() != null);
		
		for (Judgment j : branch) {
		
			result.AND(Judgment.OneMinus(j));
	
		}

		return Judgment.OneMinus(result);
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
