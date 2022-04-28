package tpnplugin.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.ZestStyles;

public class Transition extends ModelElement {
	

	public Transition(String name) {
		super(name);
		inputPlaces = new HashSet<Place>();
		outputPlaces = new HashSet<Place>();
		judgments = new ArrayList<Judgment>();
		
	}

	public Transition(String name, String label) {
		super(name, label);
		inputPlaces = new HashSet<Place>();
		outputPlaces = new HashSet<Place>();
		judgments = new ArrayList<Judgment>();
	}

	public Transition(String name, List<Judgment> judgments) {
		this(name);
		this.judgments = judgments;
	}

	public Set<Place> inputPlaces, outputPlaces;
	List<Judgment> judgments;

	public void addInputPlace(Place p) {
		inputPlaces.add(p);

	}

	public void addJudgment(double lb, double ub, double pba) {
		judgments.add(new Judgment(lb, ub, pba));
	}

	public void addOutputPlace(Place p) {
		outputPlaces.add(p);
	}

	@Override
	public IFigure createFigure() {
		Figure figure = new Figure();
		figure.setLayoutManager(new FreeformLayout());

		IFigure shape = new RectangleFigure();
		shape.setBounds(new Rectangle(30, 15, 10, 30));
		shape.setBackgroundColor(org.eclipse.draw2d.ColorConstants.white);
		Label label = new Label();
		label.setText(getName() );
		label.setSize(70, 15);
		label.setLocation(new Point(0, 45));
		Label label2 = new Label();
		label2.setText(getLabel());
		label2.setSize(70, 15);
		label2.setFont(JFaceResources.getFontRegistry().getItalic(
				JFaceResources.DEFAULT_FONT));
		label2.setLocation(new Point(0, 60));
		figure.add(shape);
		figure.add(label);
		figure.add(label2);
		figure.setSize(70, 75);
		return figure;
	}
}
