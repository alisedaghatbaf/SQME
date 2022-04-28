package tpnplugin.views;

import org.sqme.Activator;
import tpnplugin.model.Transition;
import tpnplugin.model.TPNModel;
import tpnplugin.model.ModelElement;
import tpnplugin.model.Place;
import java.awt.Color;

import javax.swing.text.StyleConstants.ColorConstants;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.*;
import org.eclipse.swt.widgets.Button;
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Polygon;
import org.eclipse.draw2d.PolygonShape;
import org.eclipse.draw2d.PolylineShape;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.Triangle;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;
import org.eclipse.zest.core.widgets.CGraphNode;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.CompositeLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.GridLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.HorizontalShift;
import org.eclipse.zest.layouts.algorithms.HorizontalTreeLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;

import tpnplugin.views.EvaluateAction;
import tpnplugin.views.NewModelAction;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class TPNView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "tpnplugin.views.TPNView";

	static Graph graph;
	static Composite composite;

	public void createPartControl(Composite parent) {
		composite = parent;
		NewModelAction newAction = new NewModelAction();
		newAction.setText("New TPN Model");
		newAction.setImageDescriptor(Activator
				.getImageDescriptor("icons/new_con.gif"));
		getViewSite().getActionBars().getToolBarManager().add(newAction);
		EvaluateAction simAction = new EvaluateAction();
		simAction.setText("Start Evaluation");
		simAction.setImageDescriptor(Activator
				.getImageDescriptor("icons/run_exc.gif"));
		getViewSite().getActionBars().getToolBarManager().add(simAction);
		/*
		 * TPNModel model = new TPNModel(); SimplePlace sp = new
		 * SimplePlace("sp1"); model.modelElements.add(sp);
		 * InstantaneousActivity ia = new InstantaneousActivity("ia1");
		 * 
		 * ia.addInputPlace(sp);
		 * 
		 * ColoredPlace cp = new ColoredPlace("cp1", "tp1");
		 * model.modelElements.add(cp); ia.addOutputPlace(cp);
		 * model.modelElements.add(ia); TimedActivity ta = new
		 * TimedActivity("ta"); ta.addInputPlace(cp);
		 * model.modelElements.add(ta);
		 * 
		 * MacroActivity ma = new MacroActivity("ma"); ma.addInputPlace(cp);
		 * model.modelElements.add(ma);
		 */
		/*
		 * TPNModel model =
		 * TPNModel.createModel("../../../Projects/UMLModel/model.uml");
		 * 
		 * 
		 * 
		 * for (ModelElement me : model.modelElements.values()) { me.node = new
		 * CGraphNode(graph, SWT.NONE, me.createFigure()); }
		 * 
		 * for (ModelElement me : model.modelElements.values()) { if (me
		 * instanceof Activity) { for (Place p : ((Activity) me).inputPlaces)
		 * new GraphConnection(graph, ZestStyles.CONNECTIONS_DIRECTED, p.node,
		 * me.node); for (Place p : ((Activity) me).outputPlaces) new
		 * GraphConnection(graph, ZestStyles.CONNECTIONS_DIRECTED, me.node,
		 * p.node); } }
		 */

	}

	/**
	 * Passing the focus request to the viewer's control.
	 */

	public void setFocus() {
	}

	public static void displayModel(TPNModel model) {
		if (graph != null)
			graph.dispose();

		LayoutAlgorithm[] algorithms = {

		new SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING),
				new GridLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING) };
		graph = new Graph(composite, SWT.NONE);
		graph.setLayoutAlgorithm(new CompositeLayoutAlgorithm(algorithms), true);
		// Selection listener on graphConnect or GraphNode is not supported
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=236528
		graph.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println(e);
			}

		});

		for (ModelElement me : model.modelElements.values()) {

			me.node = new CGraphNode(graph, SWT.NONE, me.createFigure());
		}

		for (ModelElement me : model.modelElements.values()) {
			
			if (me instanceof Transition) {

				for (Place p : ((Transition) me).inputPlaces) {
					
						GraphConnection gc = new GraphConnection(graph,
								ZestStyles.CONNECTIONS_DIRECTED, p.node,
								me.node);
						gc.setLineColor(new org.eclipse.swt.graphics.Color(
								Display.getCurrent(), 0, 0, 0));
					
				}
				for (Place p : ((Transition) me).outputPlaces){
					
						GraphConnection gc = new GraphConnection(graph,
								ZestStyles.CONNECTIONS_DIRECTED, me.node,
								p.node);
						gc.setLineColor(new org.eclipse.swt.graphics.Color(
								Display.getCurrent(), 0, 0, 0));
				}
			}
		}
		composite.layout();

	}

}