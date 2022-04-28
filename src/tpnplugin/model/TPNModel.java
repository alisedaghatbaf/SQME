package tpnplugin.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.internal.activities.ws.ActivityMessages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import tpnplugin.model.Judgment;

public class TPNModel {
	static TPNModel currentModel;
	public Map<String, ModelElement> modelElements;
	List<Place> places;
	List<Transition> transitions;
	private String name;
	int[][] F, B, combination;
	int[] m0;
	static List<Judgment> results;

	public TPNModel(String name) {
		modelElements = new LinkedHashMap<String, ModelElement>();
		this.name = name;
		places = new ArrayList<Place>();
		transitions = new ArrayList<Transition>();
	}

	public static TPNModel getCurrentModel() {
		return currentModel;
	}

	public static void evaluatePerformance() {
		TPNModel model = currentModel;
		LPP lpp = new LPP(model);
		results = new ArrayList<Judgment>();

		for (int i = 0; i < model.combination.length; i++) {

			double lb = lpp.solveTLB(model.combination[i],
					(Transition) model.getElement("t20"));
			double ub = lpp.solveTUB(model.combination[i],
					(Transition) model.getElement("t20"));
			double pba = 1.;
			for (int j = 0; j < model.combination[0].length; j++) {
				pba *= model.transitions.get(j).judgments
						.get(model.combination[i][j]).pba;

			}

			results.add(new Judgment(lb, ub, pba));

		}

		for (Judgment ju : results) {

			System.out.println(ju);
		}

		// System.exit(0);
	}

	public void addElement(String name, ModelElement el) {

		modelElements.put(name, el);
		if (el instanceof Place)
			places.add((Place) el);
		else
			transitions.add((Transition) el);
	}

	public double max() {
		double m = results.get(0).upperBound;
		for (Judgment rootJudge : results) {
			if (m < rootJudge.upperBound)
				m = rootJudge.upperBound;
		}
		return m;
	}

	public double min() {
		double m = results.get(0).upperBound;
		for (Judgment rootJudge : results) {
			if (m > rootJudge.lowerBound)
				m = rootJudge.lowerBound;
		}
		return m;
	}

	public double belief(double value) {
		return belief(0, value);
	}

	public double belief(double lb, double ub) {
		double b = 0.00;
		for (Judgment rootJudge : results) {
			if (lb <= rootJudge.lowerBound && ub >= rootJudge.upperBound)
				b += rootJudge.pba;
		}
		return b;
	}

	public double plausability(double value) {
		return plausability(0, value);
	}

	public double plausability(double lb, double ub) {
		double p = 0.00;
		for (Judgment rootJudge : results) {
			if (!(lb > rootJudge.upperBound || ub < rootJudge.lowerBound))
				p += rootJudge.pba;
		}
		return p;
	}

	public ModelElement getElement(String name) {
		return modelElements.get(name);
	}

	public static TPNModel createDefaultModel() {
		TPNModel model = new TPNModel("example");

		Place p1 = new Place("population");
		model.addElement(p1.getName(), p1);
		p1.setInitialMarking(200);
		Place p2 = new Place("init");
		model.addElement(p2.getName(), p2);
		Place p3 = new Place("init2");
		model.addElement(p3.getName(), p3);
		Transition t1 = new Transition("extDelay");
		model.addElement(t1.getName(), t1);
		t1.addInputPlace(p1);
		t1.addOutputPlace(p2);
		t1.addOutputPlace(p3);
		t1.addJudgment(1., 1., 1.00);

		Transition t2 = new Transition("end");
		t2.addJudgment(1., 1., 1.);
		model.addElement(t2.getName(), t2);
		t2.addOutputPlace(p1);
		t2.addInputPlace(p2);
		t2.addInputPlace(p3);
		model.createMatrices();

		currentModel = model;
		return model;

	}

	public static TPNModel createCaseModel() {
		TPNModel model = new TPNModel("BS");
		Place p1 = new Place("p1");// population
		model.addElement("p1", p1);
		p1.setInitialMarking(1);

		Transition t1 = new Transition("t1", "extDelay");// extDelay
		model.addElement("t1", t1);
		t1.addJudgment(0., 0., 1.);
		t1.addInputPlace(p1);

		Place p2 = new Place("p2");
		model.addElement("p2", p2);
		t1.addOutputPlace(p2);

		Transition t2 = new Transition("t2", "cycleInit_s");// cycleInit
		model.addElement("t2", t2);
		t2.addJudgment(0., 0., 1.);
		t2.addInputPlace(p2);

		Place p3 = new Place("p3");
		model.addElement("p3", p3);
		t2.addOutputPlace(p3);

		Transition t3 = new Transition("t3", "cycleInit");// cycleInit
		model.addElement("t3", t3);
		// t3.addJudgment(1., 1., 1.);
		t3.addJudgment(.0015, .0025, 0.5);
		//t3.addJudgment(.001, .002, 0.4);
		//t3.addJudgment(.002, .003, 0.1);
		t3.addInputPlace(p3);

		Place p4 = new Place("p4");
		model.addElement("p4", p4);
		t3.addOutputPlace(p4);

		Transition t4 = new Transition("t4", "cycleInit_e");// cycleInit
		model.addElement("t4", t4);
		t4.addJudgment(0., 0., 1.);
		t4.addInputPlace(p4);

		Place p5 = new Place("p5");
		model.addElement("p5", p5);
		t4.addOutputPlace(p5);

		Place p6 = new Place("p6", "GetImg");// pool GetImg
		model.addElement("p6", p6);
		p6.setInitialMarking(1);

		Transition t5 = new Transition("t5");
		model.addElement("t5", t5);
		t5.addJudgment(.00, .00, 1.);
		t5.addInputPlace(p6);
		t5.addInputPlace(p5);
		Place p7 = new Place("p7");
		model.addElement("7p", p7);
		t5.addOutputPlace(p7);

		Transition t6 = new Transition("t6", "getBuf_s");// getBuffer
		model.addElement("t6", t6);
		t6.addJudgment(0., 0., 1.);

		t6.addInputPlace(p7);

		Place p8 = new Place("p8");
		model.addElement("p8", p8);
		t6.addOutputPlace(p8);

		Transition t7 = new Transition("t7", "getBuf");// getBuffer
		model.addElement("t7", t7);
		// t6.addJudgment(1., 1., 1.);
		t7.addJudgment(.0008, .0016, .5);
		t7.addJudgment(.001, .002, 0.5);
		t7.addInputPlace(p8);

		Place p9 = new Place("p9");
		model.addElement("p9", p9);
		t7.addOutputPlace(p9);

		Transition t8 = new Transition("t8", "getBuf_e");// getBuffer
		model.addElement("t8", t8);
		t8.addJudgment(0., 0., 1.);
		t8.addInputPlace(p9);

		Place p10 = new Place("p10");
		model.addElement("p10", p10);
		t8.addOutputPlace(p10);

		Place p11 = new Place("p11", "BufMngr");// pool BufMngr
		model.addElement("p11", p11);
		p11.setInitialMarking(1);

		Transition t9 = new Transition("t9");
		model.addElement("t9", t9);
		t9.addJudgment(.00, .00, 1.);
		t9.addInputPlace(p10);
		t9.addInputPlace(p11);
		Place p12 = new Place("p12");
		model.addElement("p12", p12);
		t9.addOutputPlace(p12);

		Transition t10 = new Transition("t10", "allocBuf_s");// allocBuffer
		model.addElement("t10", t10);
		t10.addJudgment(0., 0., 1.);
		t10.addInputPlace(p12);

		Place p13 = new Place("p13");
		model.addElement("p13", p13);
		t10.addOutputPlace(p13);

		Transition t11 = new Transition("t11", "allocBuf");// allocBuffer
		model.addElement("t11", t11);
		// t9.addJudgment(1., 1., 1.);
		t11.addJudgment(.0001, .0009, .25);
		t11.addJudgment(.0004, .0012, .25);
		t11.addJudgment(.0005, .0015, .5);
		t11.addInputPlace(p13);

		Place p14 = new Place("p14");
		model.addElement("p14", p14);
		t11.addOutputPlace(p14);

		Transition t12 = new Transition("t12", "allocBuf_e");// allocBuffer
		model.addElement("t12", t12);
		t12.addJudgment(0., 0., 1.);
		t12.addInputPlace(p14);

		Place p15 = new Place("p15");
		model.addElement("p15", p15);
		t12.addOutputPlace(p15);

		Transition t13 = new Transition("t13", "getImage_s");// getImage
		model.addElement("t13", t13);
		t13.addJudgment(0., 0., 1.);
		t13.addInputPlace(p15);

		Place p16 = new Place("p16");
		model.addElement("p16", p16);
		t13.addOutputPlace(p16);

		Transition t14 = new Transition("t14", "getIamge");// getImage
		model.addElement("t14", t14);
		// t12.addJudgment(1., 1., 1.);
		t14.addJudgment(.001, .002, .5);
		t14.addJudgment(.0012, .0018, .5);
		t14.addInputPlace(p16);

		Place p17 = new Place("p17");
		model.addElement("p17", p17);
		t14.addOutputPlace(p17);

		Transition t15 = new Transition("t15", "getImage_e");// getImage
		model.addElement("t15", t15);
		t15.addJudgment(0., 0., 1.);
		t15.addInputPlace(p17);

		Place p18 = new Place("p18");
		model.addElement("p18", p18);
		t15.addOutputPlace(p18);

		Transition t16 = new Transition("t16", "passImage_s");// passImage
		model.addElement("t16", t16);
		t16.addJudgment(0., 0., 1.);
		t16.addInputPlace(p18);

		Place p19 = new Place("p19");
		model.addElement("p19", p19);
		t16.addOutputPlace(p19);

		Transition t17 = new Transition("t17", "passImage");// passImage
		model.addElement("t17", t17);
		// t15.addJudgment(1., 1., 1.);
		t17.addJudgment(0.0005, 0.0015, .25);
		t17.addJudgment(0.0002, 0.001, .25);
		t17.addJudgment(0.0006, 0.0012, .5);
		t17.addInputPlace(p19);

		Place p20 = new Place("p20");
		model.addElement("p20", p20);
		t17.addOutputPlace(p20);

		Transition t18 = new Transition("t18", "passImage_e");// passImage
		model.addElement("t18", t18);
		t18.addJudgment(0., 0., 1.);
		t18.addInputPlace(p20);

		Place p21 = new Place("p21");
		model.addElement("p21", p21);
		t18.addOutputPlace(p21);

		Transition t19 = new Transition("t19", "fork");// fork
		model.addElement("t19", t19);
		t19.addJudgment(0., 0., 1.);
		t19.addInputPlace(p21);

		Place p22 = new Place("p22");
		model.addElement("p22", p22);
		t19.addOutputPlace(p22);

		Place p23 = new Place("p23");
		model.addElement("p23", p23);
		t19.addOutputPlace(p23);

		Transition t20 = new Transition("t20", "end");// end
		model.addElement("t20", t20);
		t20.addJudgment(0., 0., 1.);
		t20.addInputPlace(p22);
		t20.addOutputPlace(p1);
		t20.addOutputPlace(p6);

		Place p24 = new Place("p24", "Store");// pool Store
		model.addElement("p24", p24);
		p24.setInitialMarking(1);

		Transition t21 = new Transition("t21");
		model.addElement("t21", t21);
		t21.addJudgment(.00, .00, 1.);
		t21.addInputPlace(p23);
		t21.addInputPlace(p24);
		Place p25 = new Place("p25");
		model.addElement("p25", p25);
		t21.addOutputPlace(p25);

		Transition t22 = new Transition("t22", "storeImage_s");// storeImage
		model.addElement("t22", t22);
		// t19.addJudgment(1., 1., 1.);
		t22.addJudgment(0.0005, 0.001, .5);
		t22.addJudgment(0.0002, .0008, .5);
		t22.addInputPlace(p25);

		Place p26 = new Place("p26");
		model.addElement("p26", p26);
		t22.addOutputPlace(p26);

		Transition t23 = new Transition("t23", "storeImage");// storeImage
		model.addElement("t23", t23);
		// t20.addJudgment(1., 1., 1.);
		t23.addJudgment(.0005, .001, .5);
		t23.addJudgment(.0002, .0008, .5);
		t23.addInputPlace(p26);

		Place p27 = new Place("p27");
		model.addElement("p27", p27);
		t23.addOutputPlace(p27);

		Transition t24 = new Transition("t24", "storeImage_e'");// storeImage
		model.addElement("t24", t24);
		t24.addJudgment(0., 0., 1.);
		t24.addInputPlace(p27);

		Place p28 = new Place("p28");
		model.addElement("p28", p28);
		t24.addOutputPlace(p28);

		Place p29 = new Place("p29", "DB");// pool DB
		model.addElement("p29", p29);
		p29.setInitialMarking(1);

		Transition t25 = new Transition("t25");
		model.addElement("t25", t25);
		t25.addJudgment(.00, .00, 1.);
		t25.addInputPlace(p28);
		t25.addInputPlace(p29);
		Place p30 = new Place("p30");
		model.addElement("p30", p30);
		t25.addOutputPlace(p30);

		Transition t26 = new Transition("t26", "writeImage_s");// writeImage
		model.addElement("t26", t26);
		t26.addJudgment(0., 0., 1.);
		t26.addInputPlace(p30);

		Place p31 = new Place("p31");
		model.addElement("p31", p31);
		t26.addOutputPlace(p31);

		Transition t27 = new Transition("t27", "writeImage");// writeImage
		model.addElement("t27", t27);
		// t23.addJudgment(1., 1., 1.);
		t27.addJudgment(0.0005, 0.0015, .5);
		t27.addJudgment(.001, .002, .5);
		t27.addInputPlace(p31);

		Place p32 = new Place("p32");
		model.addElement("p32", p32);
		t27.addOutputPlace(p32);

		Transition t28 = new Transition("t28", "writeImage_e");// writeImage
		model.addElement("t28", t28);
		t28.addJudgment(0., 0., 1.);
		t28.addInputPlace(p32);

		Place p33 = new Place("p33");
		model.addElement("p33", p33);
		t28.addOutputPlace(p33);
		t28.addOutputPlace(p30);

		Transition t29 = new Transition("t29", "freeBuf_s");// freeBuffer
		model.addElement("t29", t29);
		t29.addJudgment(0., 0., 1.);
		t29.addInputPlace(p33);

		Place p34 = new Place("p34");
		model.addElement("p34", p34);
		t29.addOutputPlace(p34);

		Transition t30 = new Transition("t30", "freeBuf");// freeBuffer
		model.addElement("t30", t30);
		// t26.addJudgment(1., 1., 1.);
		t30.addJudgment(0.0005, 0.0015, .5);
		t30.addJudgment(.0008, .0016, .5);
		t30.addInputPlace(p30);

		Place p35 = new Place("p35");
		model.addElement("p35", p35);
		t30.addOutputPlace(p35);

		Transition t31 = new Transition("t31", "freeBuf_e");// freeBuffer
		model.addElement("t31", t31);
		t31.addJudgment(0., 0., 1.);
		t31.addInputPlace(p35);

		Place p36 = new Place("p36");
		model.addElement("p36", p36);
		t31.addOutputPlace(p36);

		Transition t32 = new Transition("t32", "releaseBuf_s");// releaseBuffer
		model.addElement("t32", t32);
		t32.addJudgment(0., 0., 1.);
		t32.addInputPlace(p36);

		Place p37 = new Place("p37");
		model.addElement("p37", p37);
		t32.addOutputPlace(p37);

		Transition t33 = new Transition("t33", "releaseBuf");// releaseBuffer
		model.addElement("t33", t33);
		// t29.addJudgment(1., 1., 1.);
		t33.addJudgment(0.0005, 0.0015, .5);
		t33.addJudgment(.0001, .0009, .25);
		t33.addJudgment(.0002, .0012, .25);
		t33.addInputPlace(p33);

		Place p38 = new Place("p38");
		model.addElement("p38", p38);
		t33.addOutputPlace(p38);

		Transition t34 = new Transition("t34", "releaseBuf_e");// releaseBuffer
		model.addElement("t34", t34);
		t34.addJudgment(0., 0., 1.);
		t34.addInputPlace(p34);
		t34.addOutputPlace(p24);
		t34.addOutputPlace(p11);

		Place p39 = new Place("p39", "CtrlNode");// controlNode
		model.addElement("p39", p39);
		p39.setInitialMarking(1);
		t2.addInputPlace(p39);
		t4.addOutputPlace(p39);
		t6.addInputPlace(p39);
		t8.addOutputPlace(p39);
		t10.addInputPlace(p39);
		t12.addOutputPlace(p39);
		t13.addInputPlace(p39);
		t15.addOutputPlace(p39);
		t16.addInputPlace(p39);
		t18.addOutputPlace(p39);
		t22.addInputPlace(p39);
		t24.addOutputPlace(p39);
		t29.addInputPlace(p39);
		t31.addOutputPlace(p39);
		t32.addInputPlace(p39);
		t34.addOutputPlace(p39);

		Place p40 = new Place("p40", "DBNode");// DBNode
		model.addElement("p40", p40);
		p40.setInitialMarking(1);
		t26.addInputPlace(p40);
		t28.addOutputPlace(p40);

		Place p41 = new Place("p41", "buffer");// buffer
		model.addElement("p41", p41);
		p41.setInitialMarking(1);
		t34.addOutputPlace(p41);
		t12.addInputPlace(p41);

		model.createMatrices();
		currentModel = model;
		return model;
	}

	public static TPNModel createExampleModel() {
		TPNModel model = new TPNModel("example");

		Place p1 = new Place("population");
		model.addElement(p1.getName(), p1);
		p1.setInitialMarking(2);
		Place p2 = new Place("init");
		model.addElement(p2.getName(), p2);

		Transition t1 = new Transition("extDelay");
		model.addElement(t1.getName(), t1);
		t1.addInputPlace(p1);
		t1.addOutputPlace(p2);
		t1.addJudgment(10, 20, 0.53333);
		t1.addJudgment(20, 50, 0.13333);
		t1.addJudgment(10, 10, 0.33333);

		Transition t2 = new Transition("end");
		t2.addJudgment(0., 0., 1.);
		model.addElement(t2.getName(), t2);
		t2.addOutputPlace(p1);

		Place p3 = new Place("pool1");
		p3.setInitialMarking(1);
		model.addElement(p3.getName(), p3);
		t2.addOutputPlace(p3);

		Place p4 = new Place("p4");
		model.addElement(p4.getName(), p4);

		Transition t3 = new Transition("start");
		model.addElement(t3.getName(), t3);
		t3.addJudgment(0., 0., 1.);
		t3.addInputPlace(p2);
		t3.addInputPlace(p3);
		t3.addOutputPlace(p4);

		Transition t4 = new Transition("action1_s", "acq_host1");
		model.addElement(t4.getName(), t4);
		t4.addInputPlace(p4);
		t4.addJudgment(0., 0., 1.);
		Place p5 = new Place("p5");
		model.addElement("p5", p5);
		t4.addOutputPlace(p5);

		Transition t5 = new Transition("action1");
		model.addElement(t5.getName(), t5);
		t5.addJudgment(0.002, 0.004, 0.4);
		t5.addJudgment(0.004, 0.006, 0.26666);
		t5.addJudgment(0.002, 0.005, 0.33333);
		t5.addInputPlace(p5);

		Place p6 = new Place("p6");
		model.addElement("p6", p6);
		t5.addOutputPlace(p6);

		Transition t6 = new Transition("action1_e", "rel_host1");
		t6.addJudgment(0., 0., 1.);
		model.addElement(t6.getName(), t6);
		t6.addInputPlace(p6);

		Place p7 = new Place("p7");
		model.addElement("p7", p7);
		t6.addOutputPlace(p7);

		Transition t7 = new Transition("fork");
		t7.addJudgment(0., 0., 1.);
		model.addElement(t7.getName(), t7);
		t7.addInputPlace(p7);

		Place p8 = new Place("p8");
		model.addElement("p8", p8);
		t7.addOutputPlace(p8);

		Place p9 = new Place("p9");
		model.addElement("p9", p9);
		t7.addOutputPlace(p9);

		Transition t8 = new Transition("dec1");
		t8.addJudgment(0., 0., 1.);
		model.addElement(t8.getName(), t8);
		t8.addInputPlace(p8);

		Transition t9 = new Transition("dec2");
		t9.addJudgment(0., 0., 1.);
		model.addElement(t9.getName(), t9);
		t9.addInputPlace(p8);

		Place p10 = new Place("p10");
		model.addElement("p10", p10);
		t8.addOutputPlace(p10);

		Place p11 = new Place("p11");
		model.addElement("p11", p11);
		t9.addOutputPlace(p11);

		Transition t10 = new Transition("action2_s", "acq_host1");
		t10.addJudgment(0., 0., 1.);
		model.addElement(t10.getName(), t10);
		t10.addInputPlace(p10);

		Transition t11 = new Transition("action3_s", "acq_host1");
		t11.addJudgment(0., 0., 1.);
		model.addElement(t11.getName(), t11);
		t11.addInputPlace(p11);

		Place p12 = new Place("p12");
		model.addElement("p12", p12);
		t10.addOutputPlace(p12);

		Transition t12 = new Transition("action2");
		t12.addJudgment(0.004, 0.004, 1.);
		model.addElement(t12.getName(), t12);
		t12.addInputPlace(p12);

		Place p13 = new Place("p13");
		model.addElement("p13", p13);
		t11.addOutputPlace(p13);

		Transition t13 = new Transition("action3");
		t13.addJudgment(0.001, 0.001, 1.);
		model.addElement(t13.getName(), t13);
		t13.addInputPlace(p13);

		Place p14 = new Place("p14");
		model.addElement("p14", p14);
		t12.addOutputPlace(p14);

		Transition t14 = new Transition("action2_e", "rel_host1");
		t14.addJudgment(0., 0., 1.);
		model.addElement(t14.getName(), t14);
		t14.addInputPlace(p14);

		Place p15 = new Place("p15");
		model.addElement("p15", p15);
		t13.addOutputPlace(p15);

		Transition t15 = new Transition("action3_e", "rel_host1");
		t15.addJudgment(0., 0., 1.);
		model.addElement(t15.getName(), t15);
		t15.addInputPlace(p15);

		Place p16 = new Place("p16");
		model.addElement("p16", p16);
		t14.addOutputPlace(p16);

		Transition t16 = new Transition("merge1");
		t16.addJudgment(0., 0., 1.);
		model.addElement(t16.getName(), t16);
		t16.addInputPlace(p16);

		Place p17 = new Place("p17");
		model.addElement("p17", p17);
		t15.addOutputPlace(p17);

		Transition t17 = new Transition("merge2");
		t17.addJudgment(0., 0., 1.);
		model.addElement(t17.getName(), t17);
		t17.addInputPlace(p17);

		Place p18 = new Place("p18");
		model.addElement("p18", p18);
		t16.addOutputPlace(p18);
		t17.addOutputPlace(p18);

		Transition t18 = new Transition("join");
		t18.addJudgment(0., 0., 1.);
		model.addElement(t18.getName(), t18);
		t18.addInputPlace(p18);

		Place p19 = new Place("p19");
		model.addElement("p19", p19);
		t18.addOutputPlace(p19);
		t2.addInputPlace(p19);// end

		Place p20 = new Place("pool2");
		p20.setInitialMarking(5);
		model.addElement("p20", p20);

		Transition tmp = new Transition("tmp");
		tmp.addJudgment(0., 0., 1.);
		model.addElement(tmp.getName(), tmp);
		tmp.addInputPlace(p20);
		tmp.addInputPlace(p9);

		Place tmpp = new Place("tmpp");
		model.addElement(tmpp.getName(), tmpp);
		tmp.addOutputPlace(tmpp);

		Transition t19 = new Transition("action4_s", "acq_host2");
		t19.addJudgment(0., 0., 1.);
		model.addElement(t19.getName(), t19);
		t19.addInputPlace(tmpp);

		Place p21 = new Place("p21");
		model.addElement("p21", p21);
		t19.addOutputPlace(p21);

		Transition t20 = new Transition("action4");
		t20.addJudgment(0.002, 0.002, 1.);
		model.addElement(t20.getName(), t20);
		t20.addInputPlace(p21);

		Place p22 = new Place("p22");
		model.addElement("p22", p22);

		Transition t21 = new Transition("action4_e", "rel_host2");
		t21.addJudgment(0., 0., 1.);
		model.addElement(t21.getName(), t21);
		t21.addInputPlace(p22);
		t20.addOutputPlace(p22);

		Place p23 = new Place("p23");
		model.addElement("p23", p23);

		Transition t22 = new Transition("send5");
		t22.addJudgment(0., 0., 1.);
		model.addElement(t22.getName(), t22);
		t22.addInputPlace(p23);
		t21.addOutputPlace(p23);

		Place p24 = new Place("p24");
		model.addElement("p24", p24);

		Transition t23 = new Transition("action5");
		t23.addJudgment(0., 0., 1.);
		model.addElement(t23.getName(), t23);
		t23.addInputPlace(p24);
		t22.addOutputPlace(p24);

		Place p25 = new Place("p25");
		model.addElement("p25", p25);

		Transition t24 = new Transition("receive5");
		t24.addJudgment(0., 0., 1.);
		model.addElement(t24.getName(), t24);
		t24.addInputPlace(p25);
		t23.addOutputPlace(p25);

		Place p26 = new Place("p26");
		model.addElement("p26", p26);
		t24.addOutputPlace(p26);
		t24.addOutputPlace(p20);// pool2
		t18.addInputPlace(p26);// join

		// transmit
		Place p27 = new Place("p27");
		model.addElement("p27", p27);
		t22.addOutputPlace(p27);

		Transition t25 = new Transition("transmit");
		t25.addJudgment(0.6, 0.6, 1.);
		model.addElement(t25.getName(), t25);
		t25.addInputPlace(p27);

		Place p28 = new Place("p28");
		model.addElement("p28", p28);
		t25.addOutputPlace(p28);
		t24.addInputPlace(p28);

		// host1
		Place p29 = new Place("idle1");
		model.addElement("idle1", p29);
		p29.setInitialMarking(4);
		t14.addOutputPlace(p29);
		t15.addOutputPlace(p29);
		t10.addInputPlace(p29);
		t11.addInputPlace(p29);
		t4.addInputPlace(p29);
		t6.addOutputPlace(p29);

		// host2
		Place p30 = new Place("idle2");
		model.addElement("idle2", p30);
		p30.setInitialMarking(8);
		t21.addOutputPlace(p30);

		t19.addInputPlace(p30);

		model.createMatrices();

		currentModel = model;
		return model;
	}

	public List<Transition> getOutputTransitions(Place p) {
		List<Transition> result = new ArrayList<Transition>();
		for (Transition t : transitions)
			if (t.inputPlaces.contains(p))
				result.add(t);
		return result;
	}

	public List<Transition> getInputTransitions(Place p) {
		List<Transition> result = new ArrayList<Transition>();
		for (Transition t : transitions)
			if (t.outputPlaces.contains(p))
				result.add(t);
		return result;
	}

	public int getPlaceIndex(Place p) {
		int i = 0;
		for (Place pp : places) {
			if (p.equals(pp))
				break;
			i++;
		}
		return i;
	}

	public int getTransitionIndex(Transition p) {
		int i = 0;
		for (Transition pp : transitions) {
			if (p.equals(pp))
				break;
			i++;
		}
		return i;
	}

	public void createMatrices() {

		int placeCount = places.size();

		int transitionCount = transitions.size();
		B = new int[placeCount][transitionCount];
		F = new int[placeCount][transitionCount];
		m0 = new int[placeCount];
		for (int i = 0; i < placeCount; i++) {
			m0[i] = places.get(i).marking();

		}

		int combSize = 1;
		for (Transition t : transitions) {
			combSize *= t.judgments.size();

		}
		combination = new int[combSize][transitionCount];

		int[] combRow = new int[transitionCount];
		for (int i = 1; i < transitionCount; i++) {
			combRow[i] = 0;
		}
		combRow[0] = -1;

		for (int i = 0; i < combSize; i++) {
			boolean hasChanged = false;
			for (int j = 0; j < transitionCount && !hasChanged; j++) {
				int newVal = combRow[j] + 1;
				if (newVal < transitions.get(j).judgments.size()) {
					combRow[j]++;
					hasChanged = true;
				} else {
					combRow[j] = 0;
				}
			}

			for (int j = 0; j < transitionCount; j++) {
				combination[i][j] = combRow[j];

			}

		}

		for (int i = 0; i < placeCount; i++) {
			for (int j = 0; j < transitionCount; j++) {
				Place p = places.get(i);
				Transition t = transitions.get(j);
				if (t.inputPlaces.contains(p))
					B[i][j] = 1;
				if (t.outputPlaces.contains(p))
					F[i][j] = 1;

			}

		}

	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

}
