package atplugin.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Font;

public class ANDNode extends ModelElement {

	public ANDNode(ATModel model, ModelElement parent, String name) {
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
		shape.setBounds(new Rectangle(5, 40, 50, 50));
		figure.add(shape);
		/*
		 * Polyline poly = new Polyline(); PointList pl = new PointList(3);
		 * pl.addPoint(0, 0); pl.addPoint(30,30); pl.addPoint(12,12);
		 * poly.setPoints(pl); poly.setLineWidth(4);
		 * poly.setForegroundColor(org.eclipse.draw2d.ColorConstants.black);
		 * figure.add(poly);
		 */
		shape = new RectangleFigure();
		shape.setBounds(new Rectangle(5, 71, 50, 10));
		figure.add(shape);
		figure.add(label);
		figure.setSize(60, 72);
		return figure;
	}

	@Override
	public boolean enabled() {

		if (enabledCount == 1)
			return false;
		for (ModelElement ch : childElements.values()) {
			if (ch.getProbability() == null && ch.getJudgments().isEmpty()) {

				return false;
			}
			if (ch instanceof LeafNode && ch.enabled())
				return false;
		}

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
			probability *= ch.getProbability();

		}

		if (!hasJudge)
			return;
		GenericTreeNode<Judgment> root = new GenericTreeNode<Judgment>(
				new Judgment(1., 1.0, 1.0));

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

		}

		for (Judgment judge : judgments) {
			judge.lowerBound *= probability;
			judge.upperBound *= probability;
		}

	}

	Judgment calculateJudgment(GenericTreeNode<Judgment> leaf) {
		Judgment result = leaf.getData().clone();
		GenericTreeNode<Judgment> curLeaf = leaf;
		do {
			GenericTreeNode<Judgment> parent = curLeaf.getParent();
			if (parent == null)
				break;
			 String text = result+" and "+parent;
			result.AND(parent.getData());
			 text+= " -> "+result;
			
			curLeaf = parent;
		} while (true);
		return result;
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
}
