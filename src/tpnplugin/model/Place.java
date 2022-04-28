package tpnplugin.model;

import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

public class Place extends ModelElement {

	private int marking;
	private int initialMarking;

	public Place(String string) {
		super(string);
		marking = 0;

	}

	public Place(String name, String label) {
		super(name, label);
		marking = 0;

	}

	public void setInitialMarking(int count) {
		initialMarking = marking = count;
	}

	public int getInitialMarking() {
		return initialMarking;
	}

	public void addToken() {
		marking++;

	}

	public int marking() {
		return marking;
	}

	public void addToken(int count) {
		marking += count;

	}

	public void removeToken() {
		if (marking > 0)
			marking--;
	}

	public void clearMarking() {
		marking = 0;
	}

	@Override
	public IFigure createFigure() {
		Figure figure = new Figure();
		figure.setLayoutManager(new FreeformLayout());

		IFigure shape = new Ellipse();
		shape.setBounds(new Rectangle(20, 15, 30, 30));
		Label label = new Label();
		label.setText(getName());
		label.setSize(70, 15);
		label.setLocation(new Point(0, 45));
		figure.add(shape);
		Label label2 = new Label();
		label2.setText(getLabel());
		label2.setSize(70, 15);
		label2.setFont(JFaceResources.getFontRegistry().getItalic(
				JFaceResources.DEFAULT_FONT));
		label2.setLocation(new Point(0, 60));
		figure.add(label);
		figure.add(label2);
		figure.setSize(70, 75);
		return figure;
	}

}
