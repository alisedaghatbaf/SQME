package hcsanplugin.model;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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

import eduni.simjava.Sim_system;

public class HCSANModel {
	public Map<String, ModelElement> modelElements;
	String name;
	static Map<String, String> parameters;
	HCSANModel parent;

	public HCSANModel(String name) {
		modelElements = new LinkedHashMap<String, ModelElement>();
		this.name = name;

	}

	public void addElement(String name, ModelElement el) {

		modelElements.put(name, el);
	} 

	public static String getParameter(String name) {
		return parameters.get(name);
	}

	public static HCSANModel createDefaultModel() {
		HCSANModel model = new HCSANModel("MIS");

		return model;
	}

	public static Element getElementById(Element parent, String id) {

		NodeList nl = parent.getChildNodes();

		Queue<NodeList> queue = new LinkedList<NodeList>();
		queue.add(nl);
		while (!queue.isEmpty()) {
			nl = queue.remove();
			for (int i = 0; i < nl.getLength(); i++) {
				Node cur = nl.item(i);
				if (cur.getNodeType() == Node.ELEMENT_NODE) {
					Element el = (Element) cur;
					if (el.getAttribute("xmi:id").equals(id))
						return el;
				}
				queue.add(cur.getChildNodes());
			}

		}

		return null;
	}

	public static HCSANModel createModel(String umlPath) {
		Sim_system.initialise();

		HCSANModel model = null;
		try {
			int rand = 0;
			File fXmlFile = new File(umlPath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			NodeList elements = doc.getElementsByTagName("packagedElement");
			NodeList execHosts = doc.getElementsByTagName("GQAM:GaExecHost");
			NodeList commHosts = doc.getElementsByTagName("GQAM:GaCommHost");
			NodeList partitions = doc.getElementsByTagName("group");
			NodeList runtimeInstances = doc
					.getElementsByTagName("PAM:PaRunTInstance");
			Element modelElement = (Element) doc.getElementsByTagName(
					"uml:Model").item(0);
			String modelName = modelElement.getAttribute("name");
			model = new HCSANModel(modelName);
			NodeList contexts = doc.getElementsByTagName("context");
			NodeList paSteps = doc.getElementsByTagName("PAM:PaStep");
			NodeList acqSteps = doc.getElementsByTagName("GQAM:GaAcqStep");
			NodeList relSteps = doc.getElementsByTagName("GQAM:GaRelStep");

			parameters = new HashMap<String, String>();
			for (int c = 0; c < contexts.getLength(); c++) {
				Element context = (Element) contexts.item(c);
				String param = context.getTextContent();
				String[] parts = param.split("=");
				if (parts.length == 2)
					parameters.put(parts[0].trim().substring(3),
							parts[1].trim());
				else
					parameters.put(parts[0].substring(4), null);
			}
			for (int temp = 0; temp < elements.getLength(); temp++) {

				Node nNode = elements.item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					if (eElement.getAttribute("xmi:type").equals("uml:Node")) {// hardware
						String maName = modelName + "."
								+ eElement.getAttribute("name");

						final MacroActivity ma = new MacroActivity(maName);
						model.addElement(eElement.getAttribute("name"), ma);
						ma.internalModel.setParent(model);
						ma.internalModel = new HCSANModel(ma.get_sname());
						boolean isExec = false;
						String id = eElement.getAttribute("xmi:id");
						for (int i = 0; i < execHosts.getLength(); i++) {
							Element cur = (Element) execHosts.item(i);
							if (cur.getAttribute("base_Classifier").equals(id)) {
								isExec = true;
								final ColoredPlace req = new ColoredPlace(
										ma.get_name() + "." + "req", "Action",
										true, false);

								ma.internalModel.addElement("req", req);

								final ColoredPlace resp = new ColoredPlace(
										ma.get_name() + "." + "resp", "Action",
										false, true);
								ma.internalModel.modelElements
										.put("resp", resp);
								final ColoredPlace queue = new ColoredPlace(
										ma.get_name() + "." + "queue", "Action");
								ma.internalModel.addElement("queue", queue);
								final SimplePlace rel = new SimplePlace(
										ma.get_name() + "." + "rel", true,
										false);
								ma.internalModel.addElement("rel", rel);
								final SimplePlace idle = new SimplePlace(
										ma.get_name() + "." + "idle");
								int resMult = 1;
								if (cur.hasAttribute("resMult")) {
									resMult = Integer.parseInt(cur
											.getAttribute("resMult"));
								}
								idle.addToken(new Token("Integer", String
										.valueOf(resMult)));
								ma.internalModel.addElement("idle", idle);

								InstantaneousActivity enqueue = new InstantaneousActivity(
										ma.get_name() + "." + "enqueue");
								ma.internalModel.addElement("enqueue", enqueue);
								enqueue.addInputPlace(req);
								enqueue.addOutputPlace(queue);
								enqueue.defineGates(ma.internalModel,
										new Gate("ig_enqueue",
												enqueue) {

											@Override
											public Token execute(Token t) {
												Token token = req.removeToken();
												return token;
											}

											@Override
											public boolean predicate() {
												return req.marking().size() > 0;
											}

										}, new Gate("og_enqueue",
												enqueue) {
											@Override
											public Token execute(Token t) {
												owner.addToken(queue, t);

												return null;

											}

										});

								InstantaneousActivity approve = new InstantaneousActivity(
										ma.get_name() + "." + "approve");
								ma.internalModel.addElement("approve", approve);
								approve.addInputPlace(queue);
								approve.addInputPlace(idle);
								approve.addOutputPlace(resp);
								if (cur.hasAttribute("commTxOvh")
										|| cur.hasAttribute("commRcvOvh")) {
									ColoredPlace tmp = new ColoredPlace(
											ma.get_name() + "." + "tmp",
											"Action");
									approve.addOutputPlace(tmp);
									ma.internalModel.addElement("tmp", tmp);
								}

								approve.defineGates(ma.internalModel,
										new Gate("ig_approve",
												approve) {
											@Override
											public Token execute(Token t) {
												idle.removeToken();
												return queue.removeToken();

											}

											public boolean predicate() {
												return queue.marking().size() > 0
														&& idle.marking()
																.size() > 0;
											}

										}, new Gate("og_approve",
												approve) {
											@Override
											public Token execute(Token t) {
												if (t.value.contains("msgSize")) {
													ColoredPlace tmp = (ColoredPlace) ma.internalModel.modelElements
															.get("tmp");
													owner.addToken(tmp, t);
												} else
													owner.addToken(resp, t);
												return null;

											}
										});
								InstantaneousActivity release = new InstantaneousActivity(
										ma.get_name() + "." + "release");
								ma.internalModel.addElement("release", release);
								release.addInputPlace(rel);
								release.addOutputPlace(idle);
								release.defineGates(ma.internalModel,
										new Gate("ig_release",
												release) {

											@Override
											public Token execute(Token t) {

												return rel.removeToken();
											}

											@Override
											public boolean predicate() {

												return rel.marking().size() > 0;
											}
										}, new Gate("og_release",
												release) {
											@Override
											public Token execute(Token t) {
												owner.addToken(idle, t);
												return null;
											}
										});

								if (cur.hasAttribute("commTxOvh")
										&& cur.hasAttribute("commRcvOvh")) {
									TimedActivity commTx = new TimedActivity(
											maName + "." + "commTxOvh",
											cur.getAttribute("commTxOvh"));
									ma.internalModel.addElement("commTx",
											commTx);
									final ColoredPlace tmp = (ColoredPlace) ma.internalModel.modelElements
											.get("tmp");

									commTx.addInputPlace(tmp);
									TimedActivity commRcv = new TimedActivity(
											ma.get_name() + "." + "commRcvOvh",
											cur.getAttribute("commRcvOvh"));
									ma.internalModel.addElement("commRcv",
											commRcv);
									final ColoredPlace tmp2 = new ColoredPlace(
											ma.get_name() + "." + "tmp2",
											"Action");
									ma.internalModel.addElement("tmp2", tmp2);
									commRcv.addInputPlace(tmp2);
									commTx.addOutputPlace(tmp2);
									commRcv.addOutputPlace(resp);
									commTx.defineGates(ma.internalModel,
											new Gate("ig_commRcv",
													commTx) {
												@Override
												public Token execute(Token t) {
													return tmp.removeToken();
												}

												@Override
												public boolean predicate() {

													return tmp.marking().size() > 0;
												}
											},
											new Gate("og_comRcv",
													commTx) {
												@Override
												public Token execute(Token t) {
													owner.addToken(tmp2, t);
													return null;
												}
											});
									commRcv.defineGates(ma.internalModel,new Gate("ig_"
											+ commRcv.get_name(), commRcv) {
										@Override
										public Token execute(Token t) {
											return tmp2.removeToken();
										}

										@Override
										public boolean predicate() {

											return tmp2.marking().size() > 0;
										}
									}, new Gate("og_" + commRcv.get_name(),
											commRcv) {
										@Override
										public Token execute(Token t) {
											owner.addToken(resp, t);
											return null;
										}
									});

								} else if (cur.hasAttribute("commTxOvh")) {
									TimedActivity commTx = new TimedActivity(
											ma.get_name() + "." + "commTxOvh",
											cur.getAttribute("commTxOvh"));
									ma.internalModel.addElement("commTx",
											commTx);
									final ColoredPlace tmp = (ColoredPlace) ma.internalModel.modelElements
											.get("tmp");

									commTx.addInputPlace(tmp);

									commTx.addOutputPlace(resp);
									commTx.defineGates(ma.internalModel,
											new Gate("ig_" + commTx.get_name(),
													commTx) {
												@Override
												public Token execute(Token t) {
													return tmp.removeToken();
												}

												@Override
												public boolean predicate() {

													return tmp.marking().size() > 0;
												}
											},
											new Gate("og_" + commTx.get_name(),
													commTx) {
												@Override
												public Token execute(Token t) {
													owner.addToken(resp, t);
													return null;
												}
											});
								} else if (cur.hasAttribute("commRcvOvh")) {

									TimedActivity commRcv = new TimedActivity(
											ma.get_name() + "." + "commRcvOvh",
											cur.getAttribute("commRcvOvh"));
									ma.internalModel.addElement("commRcv",
											commRcv);
									final ColoredPlace tmp = (ColoredPlace) ma.internalModel.modelElements
											.get("tmp");

									commRcv.addInputPlace(tmp);
									commRcv.addOutputPlace(resp);
									commRcv.defineGates(ma.internalModel,new Gate("ig_"
											+ commRcv.get_name(), commRcv) {
										@Override
										public Token execute(Token t) {
											return tmp.removeToken();
										}

										@Override
										public boolean predicate() {

											return tmp.marking().size() > 0;
										}
									}, new Gate("og_" + commRcv.get_name(),
											commRcv) {
										@Override
										public Token execute(Token t) {
											owner.addToken(resp, t);
											return null;
										}
									});
								}
							}
						}

						if (!isExec) {
							for (int i = 0; i < commHosts.getLength(); i++) {
								final Element cur = (Element) commHosts.item(i);
								if (cur.getAttribute("base_Classifier").equals(
										id)) {

									final ColoredPlace in = new ColoredPlace(
											ma.get_name() + "." + "in",
											"Action", true, false);
									ma.internalModel.modelElements
											.put("in", in);
									final ColoredPlace out = new ColoredPlace(
											ma.get_name() + "." + "out",
											"Action", false, true);
									ma.internalModel.addElement("out", out);
									Activity transfer = null;
									if (cur.hasAttribute("blockT")) {
										transfer = new TimedActivity(
												ma.get_name() + "."
														+ "transfer",
												cur.getAttribute("blockT"));
										ma.internalModel.addElement("transfer",
												transfer);

										transfer.addInputPlace(in);
										transfer.addOutputPlace(out);
									} else {
										transfer = new InstantaneousActivity(
												ma.get_name() + "."
														+ "transfer");
										ma.internalModel.addElement("transfer",
												transfer);
										transfer.addInputPlace(in);
										transfer.addOutputPlace(out);
									}
									if (cur.hasAttribute("capacity")) {
										final SimplePlace capacity = new SimplePlace(
												ma.get_name() + "."
														+ "capacity");
										ma.internalModel.addElement("capacity",
												capacity);
										transfer.addInputPlace(capacity);
										transfer.addOutputPlace(capacity);

										String[] capacityParts = cur
												.getAttribute("capacity")
												.split("/");
										capacity.addToken(new Token("int",
												capacityParts[0]));
										TimedActivity initialize = new TimedActivity(
												ma.get_name() + "."
														+ "initialize", "1 "
														+ capacityParts[1]);

										ma.internalModel.addElement(
												"initialize", initialize);
										initialize.addInputPlace(capacity);
										initialize.addOutputPlace(capacity);
										initialize
												.defineGates(ma.internalModel,
														new Gate(
																"ig_initialize"
																		,
																initialize) {
															@Override
															public Token execute(
																	Token t) {
																capacity.removeToken();
																return null;
															}

															@Override
															public boolean predicate() {

																return true;
															}
														},
														new Gate(
																"og_initialize"
																		,
																initialize) {
															@Override
															public Token execute(
																	Token t) {
																owner.addToken(
																		capacity,
																		new Token(
																				"Integer",
																				cur.getAttribute("capacity")));
																return null;
															}
														});

										// define gates for transfer
									} else {
										transfer.defineGates(ma.internalModel,
												new Gate("ig_"
														+ transfer.get_name(),
														transfer) {
													Token tt;

													@Override
													public Token execute(Token t) {
														in.removeToken(tt);
														return tt;
													}

													@Override
													public boolean predicate() {

														if (in.marking().size() == 0)
															return false;
														for (Token t : in
																.marking())
															if (t.value
																	.contains("msgSize")) {
																tt = t;
																break;
															}
														return tt != null;
													}
												},
												new Gate("og_"
														+ transfer.get_name(),
														transfer) {
													@Override
													public Token execute(Token t) {
														owner.addToken(out, t);
														return null;
													}

												});
									}
								}
							}
						}

					}
				}
			}
			Element eElement = null;
			NodeList activityNodes = null;
			Set<MacroActivity> modelMAs = new HashSet<MacroActivity>();
			for (int temp = 0; temp < elements.getLength(); temp++) {

				Node nNode = elements.item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					eElement = (Element) nNode;

					if (eElement.getAttribute("xmi:type")
							.equals("uml:Activity")) {// activity diagram of //
														// system
						activityNodes = eElement.getChildNodes();
						for (int i = 0; i < activityNodes.getLength(); i++) {
							Node activityNode = activityNodes.item(i);

							if (activityNode.getNodeType() == Node.ELEMENT_NODE) {

								Element activityElement = (Element) activityNode;
								String maName = modelName + "."
										+ activityElement.getAttribute("name");

								MacroActivity activityMA = new MacroActivity(
										maName);
								modelMAs.add(activityMA);
								model.addElement(
										activityElement.getAttribute("name"),
										activityMA);
								activityMA.internalModel.setParent(model);
							}
						}
						break;
					}
				}
			}

			for (int i = 0; i < activityNodes.getLength(); i++) {
				Node activityNode = activityNodes.item(i);

				if (activityNode.getNodeType() == Node.ELEMENT_NODE) {

					Element activityElement = (Element) activityNode;
					String tmpName = activityElement.getAttribute("name");
					MacroActivity tmpMA = (MacroActivity) model.modelElements
							.get(tmpName);
					NodeList behaviorNodes = activityElement.getChildNodes();
					for (int j = 0; j < behaviorNodes.getLength(); j++) {
						Node behaviorNode = behaviorNodes.item(j);
						if (behaviorNode.getNodeType() == Node.ELEMENT_NODE) {
							Element behaviorElement = (Element) behaviorNode;
							String maName = tmpName;
							MacroActivity activityMA = tmpMA;
							if (behaviorElement.hasAttribute("inPartition")) {
								String partId = behaviorElement
										.getAttribute("inPartition");
								Element partElement = getElementById(
										activityElement, partId);
								String partName = partElement
										.getAttribute("name");
								MacroActivity partMA = (MacroActivity) activityMA.internalModel.modelElements
										.get(partName);
								if (partMA == null) {
									partMA = new MacroActivity(
											activityMA.get_name() + "."
													+ partName);
									modelMAs.add(partMA);

									activityMA.internalModel.addElement(
											partName, partMA);
									final SimplePlace pool = new SimplePlace(
											partMA.get_name() + ".pool");
									partMA.internalModel.modelElements.put(
											"pool", pool);
									if (partElement.hasAttribute("poolSize")) {
										pool.addToken(new Token(
												"Integer",
												partElement
														.getAttribute("poolSize")));

									} else
										pool.addToken(new Token("Integer", "1"));
									InstantaneousActivity acquire = new InstantaneousActivity(
											partMA.get_name() + ".acquire");
									partMA.internalModel.modelElements.put(
											"acquire", acquire);
									acquire.addInputPlace(pool);
									final SimplePlace acquired = new SimplePlace(
											partMA.get_name() + ".init");
									partMA.internalModel.modelElements.put(
											"init", acquired);

									acquire.addOutputPlace(acquired);
									InstantaneousActivity release = new InstantaneousActivity(
											partMA.get_name() + ".release");
									partMA.internalModel.modelElements.put(
											"release", release);
									release.addOutputPlace(pool);
									final SimplePlace finalP = new SimplePlace(
											partMA.get_name() + ".final");
									partMA.internalModel.modelElements.put(
											"final", finalP);
									release.addInputPlace(finalP);
									acquire.defineGates(partMA.internalModel,new Gate("ig_acquire"
											, acquire) {

										@Override
										public Token execute(Token t) {
											pool.removeToken();
											return null;
										}

										@Override
										public boolean predicate() {
											return pool.marking().size() > 0;
										}
									}, new Gate("og_acquire",
											acquire) {
										@Override
										public Token execute(Token t) {

											acquired.addToken(

											new Token("Integer", "1"));

											return null;
										}
									});
									release.defineGates(partMA.internalModel,new Gate("ig_release"
											, release) {

										@Override
										public Token execute(Token t) {
											finalP.removeToken();
											return null;
										}

										@Override
										public boolean predicate() {
											return finalP.marking().size() > 0;
										}
									}, new Gate("og_release" ,
											release) {
										@Override
										public Token execute(Token t) {

											pool.addToken(

											new Token("Integer", "1"));

											return null;
										}
									});

								}
								maName = partName;
								activityMA = partMA;
							}
							if (behaviorElement.getAttribute("xmi:type")
									.equals("uml:OpaqueAction")) {

								Activity activity = null;
								String id = behaviorElement
										.getAttribute("xmi:id");
								Element inputValue = null;
								NodeList bchNodes = behaviorElement
										.getChildNodes();
								for (int bc = 0; bc < bchNodes.getLength(); bc++) {
									Node curNode = bchNodes.item(bc);
									if (curNode instanceof Element) {
										Element curBC = (Element) curNode;
										if (curBC.getNodeName().equals(
												"inputValue")) {
											inputValue = curBC;
											break;
										}
									}
								}
								String colorType = null;
								if (inputValue != null) {
									Element colorNode = getElementById(
											doc.getDocumentElement(),
											inputValue.getAttribute("type"));
									colorType = colorNode.getAttribute("name");
								}

								Element paStep = null;
								for (int p = 0; p < paSteps.getLength(); p++) {
									Element curStep = (Element) paSteps.item(p);
									if (curStep.getAttribute(
											"base_NamedElement").equals(id)) {
										paStep = curStep;
										break;
									}
								}
								Element acqStep = null;
								for (int p = 0; p < acqSteps.getLength(); p++) {
									Element curStep = (Element) acqSteps
											.item(p);

									if (curStep.getAttribute(
											"base_NamedElement").equals(id)) {
										acqStep = curStep;
										break;
									}
								}
								Element relStep = null;
								for (int p = 0; p < relSteps.getLength(); p++) {
									Element curStep = (Element) relSteps
											.item(p);

									if (curStep.getAttribute(
											"base_NamedElement").equals(id)) {
										relStep = curStep;
										break;
									}
								}

								NodeList workloads = doc
										.getElementsByTagName("GQAM:GaWorkloadEvent");

								Element workload = null;
								for (int p = 0; p < workloads.getLength(); p++) {
									Element curEvent = (Element) workloads
											.item(p);

									if (curEvent.getAttribute(
											"base_NamedElement").equals(id)) {
										workload = curEvent;

										break;
									}
								}
								if (workload != null) {
									String pattern = workload
											.getAttribute("pattern");
									String[] parts = pattern.split("[()]");
									if (parts[0].trim().equals("closed")) {
										String[] patternAttribs = parts[1]
												.split("\\,");
										String population = patternAttribs[0]
												.split("=")[1].trim();
										// extDelay
										final SimplePlace users = new SimplePlace(
												activityMA.get_name() + "."
														+ "users");
										users.addToken(new Token("Integer",
												population));
										activityMA.internalModel.modelElements
												.put("users", users);
										Activity wlActivity = null;
										if (patternAttribs.length == 2) {
											String extDelay = patternAttribs[1]
													.split("=")[1].trim();
											wlActivity = new TimedActivity(
													activityMA.get_name() + "."
															+ "workload",
													extDelay);
										} else
											wlActivity = new InstantaneousActivity(
													activityMA.get_name() + "."
															+ "workload");

										wlActivity.addInputPlace(users);
										Place ld = null;
										if (colorType == null) {
											ld = new SimplePlace(
													activityMA.get_name() + "."
															+ "load");
										} else {
											ld = new ColoredPlace(
													activityMA.get_name() + "."
															+ "load", colorType);
										}

										final Place load = ld;
										activityMA.internalModel.modelElements
												.put("load", load);
										wlActivity.addOutputPlace(load);
										activityMA.internalModel.modelElements
												.put("workload", wlActivity);
										final String cType = colorType;
										wlActivity.defineGates(activityMA.internalModel,
												new Gate(
														"ig_"
																+ wlActivity
																		.get_name(),
														wlActivity) {

													@Override
													public Token execute(Token t) {
														users.removeToken();
														return null;
													}

													@Override
													public boolean predicate() {
														return users.marking()
																.size() > 0;
													}
												},
												new Gate(
														"og_"
																+ wlActivity
																		.get_name(),
														wlActivity) {
													@Override
													public Token execute(Token t) {
														if (cType != null) {
															owner.addToken(
																	load,
																	new Token(
																			cType,
																			cType
																					+ new Random()
																							.nextInt(100)));
														} else {
															owner.addToken(
																	load,
																	new Token(
																			"Integer",
																			"1"));

														}
														return null;
													}
												});
										InstantaneousActivity endActivity = new InstantaneousActivity(
												activityMA.get_name() + "."
														+ "end");
										activityMA.internalModel.modelElements
												.put("end", endActivity);
										endActivity.addOutputPlace(users);
										final SimplePlace fin = new SimplePlace(
												activityMA.get_name() + "."
														+ "final");
										activityMA.internalModel.modelElements
												.put("final", fin);
										endActivity.addInputPlace(fin);
										endActivity.defineGates(activityMA.internalModel,
												new Gate("ig_end",
														endActivity) {
													@Override
													public Token execute(Token t) {
														fin.removeToken();
														return null;
													}

													@Override
													public boolean predicate() {
														return fin.marking()
																.size() > 0;

													}
												},
												new Gate("og_"
														+ endActivity
																.get_name(),
														endActivity) {
													@Override
													public Token execute(Token t) {
														owner.addToken(
																users,
																new Token(
																		"Integer",
																		"1"));
														return null;
													}
												});
									}
								}
								if (paStep == null) {
									String activityName = behaviorElement
											.getAttribute("name");
									activity = new InstantaneousActivity(
											activityMA.get_name() + "."
													+ activityName);
									activityMA.internalModel.addElement(
											activityName, activity);

									activity.defineGates(activityMA.internalModel,new Gate("ig_"
											+ activityName, activity) {
										Place inActivity;

										@Override
										public Token execute(Token t) {
											return inActivity.marking().get(0);
										}

										@Override
										public boolean predicate() {
											inActivity = (Place) owner.inputPlaces
													.toArray()[0];

											return inActivity.marking().size() > 0;
										}

									}, new Gate("og_" + activityName,
											activity) {
										@Override
										public Token execute(Token t) {
											Place outActivity = (Place) owner.outputPlaces
													.toArray()[0];
											owner.addToken(outActivity, t);
											return null;
										}
									});
									if (acqStep != null) {
										String resId = acqStep
												.getAttribute("acqRes");
										Element resEl = getElementById(
												doc.getDocumentElement(), resId);
										String resName = resEl
												.getAttribute("name");
										SimplePlace sp = (SimplePlace) activityMA.internalModel.modelElements
												.get(resName);
										if (sp == null) {
											sp = new SimplePlace(
													activityMA.get_name() + "."
															+ resName);
											activityMA.internalModel.modelElements
													.put(resName, sp);
										}
										activity.addInputPlace(sp);
									}
									if (relStep != null) {
										String resId = relStep
												.getAttribute("relRes");
										Element resEl = getElementById(
												doc.getDocumentElement(), resId);
										String resName = resEl
												.getAttribute("name");
										SimplePlace sp = (SimplePlace) activityMA.internalModel.modelElements
												.get(resName);
										if (sp == null) {
											sp = new SimplePlace(
													activityMA.get_name() + "."
															+ resName);
											activityMA.internalModel.modelElements
													.put(resName, sp);
										}
										activity.addOutputPlace(sp);
									}
								} else {
									final String activityName = behaviorElement
											.getAttribute("name");
									String hostDemand = null;
									String extOpDemand = null;
									for (int c = 0; c < paStep.getChildNodes()
											.getLength(); c++) {
										Node chNode = paStep.getChildNodes()
												.item(c);
										if (chNode.getNodeName().equals(
												"hostDemand")) {
											hostDemand = ((Element) chNode)
													.getTextContent().trim();

										} else if (chNode.getNodeName().equals(
												"extOpDemand")) {
											extOpDemand = ((Element) chNode)
													.getTextContent().trim();

										}
									}
									if (hostDemand == null) {
										activity = new InstantaneousActivity(
												activityMA.get_name() + "."
														+ activityName);
										activityMA.internalModel.modelElements
												.put(activityName, activity);

										activity.defineGates(activityMA.internalModel,
												new Gate("ig_"
														+ activityName,
														activity) {
													Place inActivity;

													@Override
													public Token execute(Token t) {
														return inActivity
																.marking().get(
																		0);
													}

													@Override
													public boolean predicate() {
														inActivity = (Place) owner.inputPlaces
																.toArray()[0];

														return inActivity
																.marking()
																.size() > 0;
													}

												},
												new Gate("og_"
														+ activityName,
														activity) {
													@Override
													public Token execute(Token t) {
														Place outActivity = (Place) owner.outputPlaces
																.toArray()[0];
														owner.addToken(
																outActivity, t);
														return null;
													}
												});
										if (acqStep != null) {
											String resId = acqStep
													.getAttribute("acqRes");

											Element resEl = getElementById(
													doc.getDocumentElement(),
													resId);
											String resName = resEl
													.getAttribute("name");
											SimplePlace sp = (SimplePlace) activityMA.internalModel.modelElements
													.get(resName);
											if (sp == null) {
												sp = new SimplePlace(
														activityMA.get_name()
																+ "." + resName);
												activityMA.internalModel.modelElements
														.put(resName, sp);
											}
											activity.addInputPlace(sp);
										}
										if (relStep != null) {
											String resId = relStep
													.getAttribute("relRes");

											Element resEl = getElementById(
													doc.getDocumentElement(),
													resId);
											String resName = resEl
													.getAttribute("name");
											SimplePlace sp = (SimplePlace) activityMA.internalModel.modelElements
													.get(resName);
											if (sp == null) {
												sp = new SimplePlace(
														activityMA.get_name()
																+ "." + resName);
												activityMA.internalModel.modelElements
														.put(resName, sp);
											}
											activity.addOutputPlace(sp);
										}
									} else {

										InstantaneousActivity initActivity = new InstantaneousActivity(
												activityMA.get_name() + "."
														+ "init_"
														+ activityName);
										activityMA.internalModel.modelElements
												.put("init_" + activityName,
														initActivity);
										activity = new TimedActivity(
												activityMA.get_name() + "."
														+ activityName,
												hostDemand);
										activityMA.internalModel.modelElements
												.put(activityName, activity);

										Place dt = null;
										if (colorType == null) {
											dt = new SimplePlace(
													activityMA.get_name() + "."
															+ "data_"
															+ activityName);
										} else {
											dt = new ColoredPlace(
													activityMA.get_name() + "."
															+ "data_"
															+ activityName,
													colorType);
										}
										final Place data = dt;
										activityMA.internalModel.modelElements
												.put("data_" + activityName,
														data);

										initActivity.addOutputPlace(data);
										activity.addInputPlace(data);

										String hostId = paStep
												.getAttribute("host");
										Element execElement = getElementById(
												doc.getDocumentElement(),
												hostId);
										Element hostElement = getElementById(
												modelElement,
												execElement
														.getAttribute("base_Classifier"));
										String hostName = hostElement
												.getAttribute("name");
										MacroActivity hostMA = (MacroActivity) model.modelElements
												.get(hostName);
										MacroActivity lanMA = null;
										boolean hasLAN = false;
										if (extOpDemand != null) {
											lanMA = (MacroActivity) model.modelElements
													.get(extOpDemand);
											hasLAN = activityMA.internalModel.modelElements
													.containsKey(extOpDemand);
										}
										boolean hasHost = activityMA.internalModel.modelElements
												.containsKey(hostName);

										ColoredPlace reqH = null;
										ColoredPlace acqH = null;
										ColoredPlace acqL = null;
										SimplePlace relH = null;
										if (hasHost) {
											reqH = (ColoredPlace) activityMA.internalModel.modelElements
													.get("req_" + hostName);
											acqH = (ColoredPlace) activityMA.internalModel.modelElements
													.get("acq_" + hostName);
											relH = (SimplePlace) activityMA.internalModel.modelElements
													.get("rel_" + hostName);
											if (hasLAN) {
												acqL = (ColoredPlace) activityMA.internalModel.modelElements
														.get("acq_"
																+ extOpDemand);

											}
										} else {
											activityMA.internalModel.modelElements
													.put(hostName, hostMA);
											reqH = new ColoredPlace(
													activityMA.get_name() + "."
															+ "req_" + hostName,
													"Action");
											activityMA.internalModel.modelElements
													.put("req_" + hostName,
															reqH);
											acqH = new ColoredPlace(
													activityMA.get_name() + "."
															+ "acq_" + hostName,
													"Action");
											activityMA.internalModel.modelElements
													.put("acq_" + hostName,
															acqH);
											relH = new SimplePlace(
													activityMA.get_name() + "."
															+ "rel_" + hostName);
											activityMA.internalModel.modelElements
													.put("rel_" + hostName,
															relH);
											hostMA.addInputPlace(reqH);
											reqH.bind(
													(ColoredPlace) hostMA.internalModel.modelElements
															.get("req"), 1);

											hostMA.addOutputPlace(acqH);
											acqH.bind(
													(ColoredPlace) hostMA.internalModel.modelElements
															.get("resp"), -1);
											hostMA.addInputPlace(relH);
											relH.bind(
													(SimplePlace) hostMA.internalModel.modelElements
															.get("rel"), 1);
											if (extOpDemand != null) {
												activityMA.internalModel.modelElements
														.put(extOpDemand, lanMA);

												acqL = new ColoredPlace(
														activityMA.get_name()
																+ "." + "acq_"
																+ extOpDemand,
														"Action");
												activityMA.internalModel.modelElements
														.put("acq_"
																+ extOpDemand,
																acqL);
												lanMA.addOutputPlace(acqL);

												lanMA.addInputPlace(acqH);
												acqH.bind(
														(ColoredPlace) lanMA.internalModel.modelElements
																.get("in"), 1);
												acqL.bind(
														(ColoredPlace) lanMA.internalModel.modelElements
																.get("out"), -1);

											}
										}

										initActivity.addOutputPlace(reqH);
										if (extOpDemand != null) {
											activity.addInputPlace(acqL);
										} else {
											activity.addInputPlace(acqH);
										}
										activity.addOutputPlace(relH);
										final boolean hasExtOpDemand = extOpDemand != null;
										final Place rqH = reqH, rlH = relH, aqH = acqH, aqL = acqL;
										initActivity.defineGates(activityMA.internalModel,
												new Gate("ig_init_" + activityName,
														initActivity) {
													Place inInit;

													@Override
													public Token execute(Token t) {
														Token tt = null;
														for (Place inp : owner.inputPlaces)
															if (inp.equals(inInit))
																tt = inp.removeToken();
															else
																inp.removeToken();
														return tt;
													}

													@Override
													public boolean predicate() {
														boolean enabled = true;
														Place data = null;
														for (Place out : owner.outputPlaces)
															if (out.get_sname()
																	.startsWith(
																			"data_")) {
																data = out;
																break;
															}
														for (Place in : owner.inputPlaces) {
															if (in.marking()
																	.size() == 0)
																enabled = false;
															if (in.type
																	.equals(data.type))
																inInit = in;
														}
														return enabled;

													}
												},
												new Gate("og_init_" + activityName,
														initActivity) {
													@Override
													public Token execute(Token t) {

														owner.addToken(data, t);
														owner.addToken(
																rqH,
																new Token(
																		"Action",
																		"id="
																				+ activityName));
														return null;
													}
												});
										activity.defineGates(activityMA.internalModel,
												new Gate("ig_"
														+ activityName,
														activity) {
													Token idToken;

													@Override
													public boolean predicate() {
														Place p = aqH;
														if (hasExtOpDemand)
															p = aqL;
														if (p.marking().size() == 0
																|| data.marking()
																		.size() == 0)
															return false;
														for (Token t : p
																.marking()) {
															String id = t.value
																	.split("=")[1];
															if (id.equals(activityName)) {
																idToken = t;
																return true;
															}
														}
														return false;
													}

													@Override
													public Token execute(Token t) {
														aqH.marking().remove(
																idToken);
														return data
																.removeToken();
													}
												},
												new Gate("og_"
														+ activityName,
														activity) {
													@Override
													public Token execute(Token t) {
														for (Place outp : owner.outputPlaces) {
															if (outp.type
																	.equals(t.type))

																owner.addToken(
																		outp, t);
															else if (outp.type
																	.equals("Integer"))
																owner.addToken(
																		outp,
																		new Token(
																				"Integer",
																				"1"));
															else {
																owner.addToken(
																		outp,
																		new Token(
																				outp.type,
																				outp.type
																						+ new Random()
																								.nextInt(100)));
															}
														}
														return null;
													}	
												});
										if (acqStep != null) {
											String resId = acqStep
													.getAttribute("acqRes");

											Element resEl = getElementById(
													doc.getDocumentElement(),
													resId);
											resEl = getElementById(
													doc.getDocumentElement(),
													resEl.getAttribute("base_Classifier"));
											String resName = resEl
													.getAttribute("name");
											SimplePlace sp = (SimplePlace) activityMA.internalModel.modelElements
													.get(resName);
											if (sp == null) {
												sp = new SimplePlace(
														activityMA.get_name()
																+ "." + resName);
												activityMA.internalModel.modelElements
														.put(resName, sp);
											}
											initActivity.addInputPlace(sp);
										}
										if (relStep != null) {
											String resId = relStep
													.getAttribute("relRes");

											Element resEl = getElementById(
													doc.getDocumentElement(),
													resId);
											resEl = getElementById(
													doc.getDocumentElement(),
													resEl.getAttribute("base_Classifier"));
											String resName = resEl
													.getAttribute("name");
											SimplePlace sp = (SimplePlace) activityMA.internalModel.modelElements
													.get(resName);
											if (sp == null) {
												sp = new SimplePlace(
														activityMA.get_name()
																+ "." + resName);
												activityMA.internalModel.modelElements
														.put(resName, sp);
											}
											initActivity.addOutputPlace(sp);
										}
									}
								}

							} else if (behaviorElement.getAttribute("xmi:type")
									.equals("uml:CallBehaviorAction")) {
								String id = behaviorElement
										.getAttribute("behavior");

								Element calleeElement = getElementById(
										doc.getDocumentElement(), id);
								String calleeName = calleeElement
										.getAttribute("name");

								MacroActivity calleeMA = (MacroActivity) model.modelElements
										.get(calleeName);
								activityMA.internalModel.addElement(calleeName,
										calleeMA);
								id = behaviorElement.getAttribute("xmi:id");
								Element paStep = null;
								for (int p = 0; p < paSteps.getLength(); p++) {
									Element curStep = (Element) paSteps.item(p);
									if (curStep.getAttribute(
											"base_NamedElement").equals(id)) {
										paStep = curStep;
										break;
									}
								}
								if (paStep != null) {

									String repCount = paStep
											.getAttribute("rep");

									if (!repCount.isEmpty()) {
										final String rpCount = repCount;
										InstantaneousActivity repActivity = new InstantaneousActivity(
												activityMA.get_name() + "."
														+ "rep_" + calleeName);
										activityMA.internalModel.modelElements
												.put("rep_" + calleeName,
														repActivity);
										final SimplePlace repPlace = new SimplePlace(
												"repCount_" + calleeName);
										activityMA.internalModel.modelElements
												.put("repCount_" + calleeName,
														repPlace);
										repActivity.addOutputPlace(repPlace);
										calleeMA.addInputPlace(repPlace);
										repActivity.defineGates(activityMA.internalModel,new Gate(
												"ig_rep_" + calleeName,
												repActivity) {
											Place in;

											@Override
											public boolean predicate() {
												in = (Place) owner.inputPlaces
														.toArray()[0];
												return in.marking().size() > 0;
											}

											@Override
											public Token execute(Token t) {
												return in.removeToken();
											}
										}, new Gate("og_rep_" + calleeName,
												repActivity) {

											@Override
											public Token execute(Token t) {
												repPlace.addToken(new Token(
														"Integer", rpCount));
												return null;
											}
										});
									}

								}

							} else if (behaviorElement.getAttribute("xmi:type")
									.equals("uml:DecisionNode")) {
								String nodeName = behaviorElement
										.getAttribute("name");

								InstantaneousActivity nodeActivity = new InstantaneousActivity(
										activityMA.get_name() + "." + nodeName);
								activityMA.internalModel.addElement(nodeName,
										nodeActivity);
								String placeName = nodeName + "_place";
								Place nodePlace = new SimplePlace(
										activityMA.get_name() + "." + placeName);
								activityMA.internalModel.addElement(placeName,
										nodePlace);
								nodeActivity.addOutputPlace(nodePlace);
								nodeActivity.defineGates(activityMA.internalModel,
										new Gate("ig_"
												+ nodeActivity.get_name(),
												nodeActivity) {
											Place inPlace;

											@Override
											public boolean predicate() {
												inPlace = (Place) owner.inputPlaces
														.toArray()[0];

												// TODO Auto-generated method
												// stub
												return inPlace.marking().size() > 0;
											}

											@Override
											public Token execute(Token t) {
												// TODO Auto-generated method
												// stub
												return inPlace.removeToken();
											}
										},
										new Gate("og_"
												+ nodeActivity.get_name(),
												nodeActivity) {
											@Override
											public Token execute(Token t) {
												Place out = (Place) owner.outputPlaces
														.toArray()[0];
												owner.addToken(out, t);
												return null;
											}
										});

							} else if (behaviorElement.getAttribute("xmi:type")
									.equals("uml:MergeNode")) {
								String nodeName = behaviorElement
										.getAttribute("name");

								InstantaneousActivity nodeActivity = new InstantaneousActivity(
										activityMA.get_name() + "." + nodeName);

								activityMA.internalModel.addElement(nodeName,
										nodeActivity);
								nodeActivity.defineGates(activityMA.internalModel,
										new Gate("ig_"
												+ nodeActivity.get_name(),
												nodeActivity) {
											Place inPlace;

											@Override
											public boolean predicate() {
												for (Place inp : owner.inputPlaces)
													if (inp.marking().size() > 0) {
														inPlace = inp;
														return true;
													}

												// TODO Auto-generated method
												// stub
												return false;
											}

											@Override
											public Token execute(Token t) {
												// TODO Auto-generated method
												// stub
												return inPlace.removeToken();
											}
										},
										new Gate("og_"
												+ nodeActivity.get_name(),
												nodeActivity) {
											@Override
											public Token execute(Token t) {
												Place out = (Place) owner.outputPlaces
														.toArray()[0];
												owner.addToken(out, t);
												return null;
											}
										});

							} else if (behaviorElement.getAttribute("xmi:type")
									.equals("uml:ForkNode")
									|| behaviorElement.getAttribute("xmi:type")
											.equals("uml:JoinNode")) {
								String nodeName = behaviorElement
										.getAttribute("name");

								InstantaneousActivity nodeActivity = new InstantaneousActivity(
										activityMA.get_name() + "." + nodeName);

								activityMA.internalModel.addElement(nodeName,
										nodeActivity);

								nodeActivity.defineGates(activityMA.internalModel,
										new Gate("ig_"
												+ nodeActivity.get_name(),
												nodeActivity) {

											Place inCPlace;

											@Override
											public boolean predicate() {
												for (Object o : owner.inputPlaces
														.toArray()) {
													if (o instanceof ColoredPlace)
														inCPlace = (Place) o;
													if (((Place) o).marking()
															.size() == 0) {
														return false;
													}
												}
												return true;
											}

											@Override
											public Token execute(Token t) {
												Token tt = null;
												for (Object o : owner.inputPlaces
														.toArray()) {
													Place p = (Place) o;
													if (o.equals(inCPlace))
														tt = p.removeToken();
													else
														p.removeToken();

												}
												return tt;

											}
										},
										new Gate("og_"
												+ nodeActivity.get_name(),
												nodeActivity) {
											@Override
											public Token execute(Token t) {
												for (Object o : owner.outputPlaces) {
													Place p = (Place) o;
													if (t != null
															&& p instanceof ColoredPlace
															&& p.type
																	.equals(t.type))
														owner.addToken(p, t);
													else {
														owner.addToken(
																p,
																new Token(
																		"Integer",
																		"1"));
													}

												}
												return null;
											}

										});
							}

						}

					}

					for (MacroActivity ma : modelMAs) {
						Activity first = null, last = null;
						if (!ma.internalModel.modelElements.keySet().contains(
								"pool"))
							continue;
						for (ModelElement me : ma.internalModel.modelElements
								.values()) {
							if (me instanceof Activity) {
								if (me instanceof MacroActivity
										|| me.get_sname().equals("acquire")
										|| me.get_sname().equals("release")) {
									continue;

								}
								if (first == null) {
									first = (Activity) me;
									first.addInputPlace((Place) ma.internalModel.modelElements
											.get("init"));

								}
								last = (Activity) me;
							}
						}
						last.addOutputPlace((Place) ma.internalModel.modelElements
								.get("final"));
					}
					for (int j = 0; j < behaviorNodes.getLength(); j++) {
						Node behaviorNode = behaviorNodes.item(j);

						if (behaviorNode.getNodeType() == Node.ELEMENT_NODE) {
							Element behaviorElement = (Element) behaviorNode;
							String maName = tmpName;
							MacroActivity activityMA = tmpMA;

							if (behaviorElement.getAttribute("xmi:type")
									.equals("uml:ControlFlow")
									|| behaviorElement.getAttribute("xmi:type")
											.equals("uml:ObjectFlow")) {

								MacroActivity srcPartitionMA = null, targetPartitionMA = null;
								String srcPartitionName = null, targetPartitionName = null;
								String src = behaviorElement
										.getAttribute("source");

								String target = behaviorElement
										.getAttribute("target");
								Element srcElement = getElementById(
										activityElement, src);
								String srcType = srcElement
										.getAttribute("xmi:type");
								Element targetElement = getElementById(
										activityElement, target);

								String targetType = targetElement
										.getAttribute("xmi:type");

								String partId = srcElement
										.getAttribute("inPartition");

								if (partId.isEmpty())
									partId = ((Element) srcElement
											.getParentNode())
											.getAttribute("inPartition");
								if (!partId.isEmpty()) {
									Element partElement = getElementById(
											activityElement, partId);
									srcPartitionName = partElement
											.getAttribute("name");
									srcPartitionMA = (MacroActivity) activityMA.internalModel.modelElements
											.get(srcPartitionName);

								}

								partId = targetElement
										.getAttribute("inPartition");
								if (partId.isEmpty())
									partId = ((Element) targetElement
											.getParentNode())
											.getAttribute("inPartition");
								if (!partId.isEmpty()) {
									Element partElement = getElementById(
											activityElement, partId);
									targetPartitionName = partElement
											.getAttribute("name");
									targetPartitionMA = (MacroActivity) activityMA.internalModel.modelElements
											.get(targetPartitionName);

								}
								if (srcPartitionMA != null
										&& srcPartitionMA == targetPartitionMA) {
									activityMA = srcPartitionMA;
									srcPartitionMA = targetPartitionMA = null;
								}
								if (srcType.equals("uml:DecisionNode")) {
									Node guardChild = null;
									NodeList bchNodes = behaviorElement
											.getChildNodes();
									for (int bc = 0; bc < bchNodes.getLength(); bc++) {
										Node curNode = bchNodes.item(bc);
										if (curNode instanceof Element) {
											Element curBC = (Element) curNode;
											if (curBC.getNodeName().equals(
													"guard")) {
												guardChild = curBC;
												break;
											}
										}
									}
									boolean hasGuard = false;
									if (guardChild != null) {

										hasGuard = ((Element) guardChild)
												.getAttribute("xmi:type")
												.equals("uml:InstanceValue");

									}
									Element paStep = null;
									for (int p = 0; p < paSteps.getLength(); p++) {
										Element curStep = (Element) paSteps
												.item(p);
										if (curStep
												.getAttribute(
														"base_NamedElement")
												.equals(behaviorElement
														.getAttribute("xmi:id"))) {
											paStep = curStep;
											break;
										}
									}
									if (srcPartitionMA != null) {
										maName = srcPartitionName;

										activityMA = srcPartitionMA;
									}
									String srcActivityName = srcElement
											.getAttribute("name");

									String arcName = srcActivityName + rand++;
									InstantaneousActivity arcActivity = null;
									if (paStep != null) {
										double prob = Double.parseDouble(paStep
												.getAttribute("prob"));
										arcActivity = new InstantaneousActivity(
												activityMA.get_name() + "."
														+ arcName, prob);
									} else {
										arcActivity = new InstantaneousActivity(
												activityMA.get_name() + "."
														+ arcName);
									}
									activityMA.internalModel.addElement(
											arcName, arcActivity);
									arcActivity
											.addInputPlace((Place) activityMA.internalModel.modelElements
													.get(srcActivityName
															+ "_place"));
									arcActivity.defineGates(activityMA.internalModel,new Gate("ig_"
											+ arcActivity.get_name(),
											arcActivity) {
										Place inPlace;

										@Override
										public boolean predicate() {
											inPlace = (Place) owner.inputPlaces
													.toArray()[0];

											// TODO Auto-generated method stub
											return inPlace.marking().size() > 0;
										}

										@Override
										public Token execute(Token t) {
											// TODO Auto-generated method stub
											return inPlace.removeToken();
										}
									}, new Gate("og_" + arcActivity.get_name(),
											arcActivity) {
										@Override
										public Token execute(Token t) {
											Place out = (Place) owner.outputPlaces
													.toArray()[0];
											owner.addToken(out, t);
											return null;
										}
									});

									if (hasGuard) {

										String colorId = ((Element) guardChild)
												.getAttribute("type");

										Element colorNode = getElementById(
												doc.getDocumentElement(),
												colorId);
										String colorType = colorNode
												.getAttribute("name");
										String cpName = srcActivityName
												+ "_out" + rand++;
										ColoredPlace cp = new ColoredPlace(
												activityMA.get_name() + "."
														+ cpName, colorType);
										activityMA.internalModel.modelElements
												.put(cpName, cp);

										String targetName = targetElement
												.getAttribute("name");
										ModelElement targetMElement = activityMA.internalModel.modelElements
												.get(targetName);
										if (targetMElement instanceof Activity) {
											Activity targetA = (Activity) targetMElement;
											if (targetA instanceof TimedActivity) {
												targetA = (Activity) activityMA.internalModel.modelElements
														.get("init_"
																+ targetName);
											}

											targetA.addInputPlace(cp);
										}
										arcActivity.addOutputPlace(cp);
									} else {
										String spName = srcActivityName
												+ "_out" + rand++;
										SimplePlace sp = new SimplePlace(
												activityMA.get_name() + "."
														+ spName);
										activityMA.internalModel.modelElements
												.put(spName, sp);

										String targetName = targetElement
												.getAttribute("name");
										ModelElement targetMElement = activityMA.internalModel.modelElements
												.get(targetName);
										if (targetMElement instanceof Activity) {
											Activity targetA = (Activity) targetMElement;
											if (targetA instanceof TimedActivity) {
												targetA = (Activity) activityMA.internalModel.modelElements
														.get("init_"
																+ targetName);
											}

											targetA.addInputPlace(sp);
										}
										arcActivity.addOutputPlace(sp);
									}
								} else if (srcType.equals("uml:InitialNode")) {
									if (srcPartitionMA != null) {
										maName = srcPartitionName;

										activityMA = srcPartitionMA;
									}
									SimplePlace sp = (SimplePlace) activityMA.internalModel.modelElements
											.get("init");
									if (sp == null) {
										sp = new SimplePlace(
												activityMA.get_name() + "."
														+ "init");
										activityMA.internalModel.modelElements
												.put("init", sp);
										sp.addToken(new Token("Integer", "1"));

									}
									String targetName = targetElement
											.getAttribute("name");
									Activity targetA = (Activity) activityMA.internalModel.modelElements
											.get(targetName);
									if (targetA instanceof TimedActivity) {
										targetA = (Activity) activityMA.internalModel.modelElements
												.get("init_" + targetName);
									}

									targetA.addInputPlace(sp);
									Place load = (Place) activityMA.internalModel.modelElements
											.get("load");
									if (load != null)
										targetA.addInputPlace(load);
								} else if (srcType.equals("uml:OutputPin")) {
									String colorId = srcElement
											.getAttribute("type");
									Element colorNode = getElementById(
											doc.getDocumentElement(), colorId);
									String colorType = colorNode
											.getAttribute("name");
									String srcActivityName = ((Element) srcElement
											.getParentNode())
											.getAttribute("name");
									String targetActivityName = targetElement
											.getAttribute("name");
									if (targetType.equals("uml:InputPin")) {
										targetActivityName = ((Element) targetElement
												.getParentNode())
												.getAttribute("name");

									} else if (targetType
											.equals("uml:ActivityParameterNode")) {
										targetActivityName = null;
									}
									if (srcPartitionMA == null
											&& targetPartitionMA == null) {
										Activity srcActivity = (Activity) activityMA.internalModel.modelElements
												.get(srcActivityName);

										if (targetActivityName != null) {
											Activity targetActivity = (Activity) activityMA.internalModel.modelElements
													.get(targetActivityName);

											if (targetActivity instanceof TimedActivity) {
												targetActivity = (Activity) activityMA.internalModel.modelElements
														.get("init_"
																+ targetActivityName);

												ColoredPlace data = (ColoredPlace) activityMA.internalModel.modelElements
														.get("data_"
																+ targetActivityName);
												data.type = colorType;

											}

											String nodeName = srcActivityName
													+ "_"
													+ srcElement
															.getAttribute("name");
											ColoredPlace cp = new ColoredPlace(
													activityMA.get_name() + "."
															+ nodeName,
													colorType);

											activityMA.internalModel.modelElements
													.put(nodeName, cp);
											srcActivity.addOutputPlace(cp);

											targetActivity.addInputPlace(cp);
											if (targetType
													.equals("uml:DecisionNode")) {
												Place decisionPlace = (Place) targetActivity.outputPlaces
														.toArray()[0];
												decisionPlace = new ColoredPlace(
														decisionPlace
																.get_name(),
														colorType);
											}

										} else {// target is a parameter node
											String nodeName = targetElement
													.getAttribute("name")
													+ "_"
													+ activityMA.get_name();
											ColoredPlace cp = new ColoredPlace(
													activityMA.get_name() + "."
															+ nodeName,
													colorType, false, true);

											activityMA.internalModel.modelElements
													.put(nodeName, cp);

											srcActivity.addOutputPlace(cp);

										}
									} else if (srcPartitionMA != null
											&& targetPartitionMA == null) {
										Activity srcActivity = (Activity) srcPartitionMA.internalModel.modelElements
												.get(srcActivityName);

										if (targetActivityName != null) {
											Activity targetActivity = (Activity) activityMA.internalModel.modelElements
													.get(targetActivityName);
											if (targetActivity instanceof TimedActivity) {
												targetActivity = (Activity) activityMA.internalModel.modelElements
														.get("init_"
																+ targetActivityName);

												ColoredPlace data = (ColoredPlace) activityMA.internalModel.modelElements
														.get("data_"
																+ targetActivityName);
												data.type = colorType;

											}

											String srcNodeName = srcActivityName
													+ "_"
													+ srcElement
															.getAttribute("name");
											ColoredPlace cp1 = new ColoredPlace(
													activityMA.get_name() + "."
															+ srcPartitionName
															+ "." + srcNodeName,
													colorType, false, true);

											srcPartitionMA.internalModel.modelElements
													.put(srcNodeName, cp1);
											srcActivity.addOutputPlace(cp1);

											String targetNodeName = targetActivityName
													+ "_"
													+ targetElement
															.getAttribute("name");
											ColoredPlace cp2 = new ColoredPlace(
													activityMA.get_name() + "."
															+ targetNodeName,
													colorType);

											activityMA.internalModel.modelElements
													.put(targetNodeName, cp2);
											cp1.bind(cp2, 1);
											targetActivity.addInputPlace(cp2);
										} else {
											String srcNodeName = srcActivityName
													+ "_"
													+ srcElement
															.getAttribute("name");
											ColoredPlace cp1 = new ColoredPlace(
													activityMA.get_name() + "."
															+ srcPartitionName
															+ "." + srcNodeName,
													colorType, false, true);

											srcPartitionMA.internalModel.modelElements
													.put(srcNodeName, cp1);
											srcActivity.addOutputPlace(cp1);

											String targetNodeName = maName
													+ "_"
													+ targetElement
															.getAttribute("name");
											ColoredPlace cp2 = new ColoredPlace(
													activityMA.get_name() + "."
															+ targetNodeName,
													colorType, false, true);

											activityMA.internalModel.modelElements
													.put(targetNodeName, cp2);
											cp1.bind(cp2, 1);
											srcPartitionMA.addOutputPlace(cp2);
										}
									} else if (srcPartitionMA != null
											&& targetPartitionMA != null) {
										Activity srcActivity = (Activity) srcPartitionMA.internalModel.modelElements
												.get(srcActivityName);

										// targetActivityName cannot be null
										Activity targetActivity = (Activity) targetPartitionMA.internalModel.modelElements
												.get(targetActivityName);
										if (targetActivity instanceof TimedActivity) {

											Place data = (Place) targetPartitionMA.internalModel.modelElements
													.get("data_"
															+ targetActivityName);

											if (colorType != null) {
												data = new ColoredPlace(
														data.get_name(),
														colorType);
												targetPartitionMA.internalModel.modelElements
														.put("data_"
																+ targetActivityName,
																data);
												targetActivity
														.addInputPlace(data);
											}
											targetActivity = (Activity) targetPartitionMA.internalModel.modelElements
													.get("init_"
															+ targetActivityName);
											if (colorType != null)
												targetActivity
														.addOutputPlace(data);

										}

										String srcNodeName = srcActivityName
												+ "_"
												+ srcElement
														.getAttribute("name");
										ColoredPlace cp = new ColoredPlace(
												activityMA.get_name() + "."
														+ srcNodeName,
												colorType);

										activityMA.internalModel.modelElements
												.put(srcNodeName, cp);
										ColoredPlace cp1 = new ColoredPlace(
												srcPartitionMA.get_name() + "."
														+ srcNodeName,
												colorType, false, true);

										srcPartitionMA.internalModel.modelElements
												.put(srcNodeName, cp1);
										srcActivity.addOutputPlace(cp1);

										String targetNodeName = targetActivityName
												+ "_"
												+ targetElement
														.getAttribute("name");
										ColoredPlace cp2 = (ColoredPlace) targetPartitionMA.internalModel.modelElements
												.get(targetNodeName);
										if (cp2 == null) {
											cp2 = new ColoredPlace(
													targetPartitionMA.get_name()
															+ "."
															+ targetNodeName,
													colorType, true, false);

											targetPartitionMA.internalModel.modelElements
													.put(targetNodeName, cp2);
										}
										cp1.bind(cp, 1);

										cp2.bind(cp, -1);
										targetActivity.addInputPlace(cp2);
										srcPartitionMA.addOutputPlace(cp);
										targetPartitionMA.addInputPlace(cp);

									} else {
										Activity srcActivity = (Activity) activityMA.internalModel.modelElements
												.get(srcActivityName);

										Activity targetActivity = (Activity) targetPartitionMA.internalModel.modelElements
												.get(targetActivityName);
										if (targetActivity instanceof TimedActivity) {
											targetActivity = (Activity) targetPartitionMA.internalModel.modelElements
													.get("init_"
															+ targetActivityName);

											ColoredPlace data = (ColoredPlace) targetPartitionMA.internalModel.modelElements
													.get("data_"
															+ targetActivityName);
											data.type = colorType;

										}

										String srcNodeName = srcActivityName
												+ "_"
												+ srcElement
														.getAttribute("name");
										ColoredPlace cp1 = new ColoredPlace(
												activityMA.get_name() + "."
														+ srcNodeName,
												colorType, false, true);

										activityMA.internalModel.modelElements
												.put(srcNodeName, cp1);
										srcActivity.addOutputPlace(cp1);

										String targetNodeName = targetActivityName
												+ "_"
												+ targetElement
														.getAttribute("name");
										ColoredPlace cp2 = new ColoredPlace(
												targetPartitionMA.get_name()
														+ "." + targetNodeName,
												colorNode.getAttribute("name"));

										targetPartitionMA.internalModel.modelElements
												.put(targetNodeName, cp2);
										cp1.bind(cp2, 1);
										targetActivity.addInputPlace(cp2);

									}
								} else if (srcType
										.equals("uml:ActivityParameterNode")) {
									String colorId = srcElement
											.getAttribute("type");
									Element colorNode = getElementById(
											doc.getDocumentElement(), colorId);
									String colorType = colorNode
											.getAttribute("name");
									String nodeName = srcElement
											.getAttribute("name");
									ColoredPlace cp = new ColoredPlace(
											activityMA.get_name() + "."
													+ nodeName, colorType,
											true, false);
									activityMA.internalModel.addElement(
											nodeName, cp);

									String targetActivityName = targetElement
											.getAttribute("name");
									if (targetType.equals("uml:InputPin")) {
										targetActivityName = ((Element) targetElement
												.getParentNode())
												.getAttribute("name");
									}
									if (targetPartitionMA == null) {

										Activity targetActivity = (Activity) activityMA.internalModel.modelElements
												.get(targetActivityName);

										targetActivity.addInputPlace(cp);
										if (targetActivity instanceof TimedActivity) {
											targetActivity = (Activity) activityMA.internalModel.modelElements
													.get("init_"
															+ targetActivityName);

											ColoredPlace data = (ColoredPlace) activityMA.internalModel.modelElements
													.get("data_"
															+ targetActivityName);
											data.type = colorType;

										}

									} else {

										Activity targetActivity = (Activity) targetPartitionMA.internalModel.modelElements
												.get(targetActivityName);
										if (targetActivity instanceof TimedActivity) {
											targetActivity = (Activity) targetPartitionMA.internalModel.modelElements
													.get("init_"
															+ targetActivityName);

											ColoredPlace data = (ColoredPlace) targetPartitionMA.internalModel.modelElements
													.get("data_"
															+ targetActivityName);
											data.type = colorType;

										}

										String targetNodeName = "in_"
												+ targetActivityName;
										ColoredPlace cp2 = new ColoredPlace(
												targetPartitionMA.get_name()
														+ "." + targetNodeName,
												colorType, true, false);

										targetPartitionMA.internalModel.modelElements
												.put(targetNodeName, cp2);
										cp.bind(cp2, 1);
										targetActivity.addInputPlace(cp2);
										targetPartitionMA.addInputPlace(cp);
									}

								} else if (srcType.equals("uml:ForkNode")) {

									String srcActivityName = srcElement

									.getAttribute("name");
									String targetActivityName = targetElement
											.getAttribute("name");
									String colorType = null;
									if (targetType.equals("uml:InputPin")) {
										targetActivityName = ((Element) targetElement
												.getParentNode())
												.getAttribute("name");
										String colorId = targetElement
												.getAttribute("type");
										Element colorNode = getElementById(
												doc.getDocumentElement(),
												colorId);
										colorType = colorNode
												.getAttribute("name");

									}
									if (targetType
											.equals("uml:ActivityFinalNode")) {
										Activity srcActivity = (Activity) activityMA.internalModel.modelElements
												.get(srcActivityName);
										Place sp = (Place) activityMA.internalModel.modelElements
												.get("final");
										if (sp == null) {
											sp = new SimplePlace(
													activityMA.get_name() + "."
															+ "final");
											activityMA.internalModel.modelElements
													.put("final", sp);
										}
										srcActivity.addOutputPlace(sp);
									} else {
										if (srcPartitionMA == null
												&& targetPartitionMA == null) {
											Activity srcActivity = (Activity) activityMA.internalModel.modelElements
													.get(srcActivityName);

											Activity targetActivity = (Activity) activityMA.internalModel.modelElements
													.get(targetActivityName);
											if (targetActivity instanceof TimedActivity) {
												targetActivity = (Activity) activityMA.internalModel.modelElements
														.get("init_"
																+ targetActivityName);

											}

											String nodeName = srcActivityName
													+ "_out";
											SimplePlace cp = new SimplePlace(
													activityMA.get_name() + "."
															+ nodeName);

											activityMA.internalModel.modelElements
													.put(nodeName, cp);
											srcActivity.addOutputPlace(cp);

											targetActivity.addInputPlace(cp);

										} else if (srcPartitionMA != null
												&& targetPartitionMA == null) {
											Activity srcActivity = (Activity) srcPartitionMA.internalModel.modelElements
													.get(srcActivityName);

											Activity targetActivity = (Activity) activityMA.internalModel.modelElements
													.get(targetActivityName);

											String srcNodeName = srcActivityName
													+ "_out";
											SimplePlace cp1 = new SimplePlace(
													activityMA.get_name() + "."
															+ srcPartitionName
															+ "." + srcNodeName,
													false, true);

											srcPartitionMA.internalModel.modelElements
													.put(srcNodeName, cp1);
											srcActivity.addOutputPlace(cp1);

											String targetNodeName = targetActivityName
													+ "_"
													+ targetElement
															.getAttribute("name");
											SimplePlace cp2 = new SimplePlace(
													activityMA.get_name() + "."
															+ targetNodeName);

											activityMA.internalModel.modelElements
													.put(targetNodeName, cp2);
											cp1.bind(cp2, 1);
											targetActivity.addInputPlace(cp2);
										} else if (srcPartitionMA != null
												&& targetPartitionMA != null) {
											Activity srcActivity = (Activity) srcPartitionMA.internalModel.modelElements
													.get(srcActivityName);

											// targetActivityName cannot be null
											Activity targetActivity = (Activity) targetPartitionMA.internalModel.modelElements
													.get(targetActivityName);
											if (targetActivity instanceof TimedActivity) {
												targetActivity = (Activity) targetPartitionMA.internalModel.modelElements
														.get("init_"
																+ targetActivityName);

												if (colorType != null) {
													Place data = (Place) targetPartitionMA.internalModel.modelElements
															.get("data_"
																	+ targetActivityName);

													data = new ColoredPlace(
															data.get_name(),
															colorType);
												}

											}

											String srcNodeName = srcActivityName
													+ "_out";
											Place cp = null;
											if (colorType == null) {
												cp = new SimplePlace(
														activityMA.get_name()
																+ "."
																+ srcNodeName);
											} else {
												cp = new ColoredPlace(
														activityMA.get_name()
																+ "."
																+ srcNodeName,
														colorType);
											}
											activityMA.internalModel.modelElements
													.put(srcNodeName, cp);
											Place cp1 = null;
											if (colorType == null) {
												cp1 = new SimplePlace(
														srcPartitionMA.get_name()
																+ "."
																+ srcPartitionName
																+ "."
																+ srcNodeName,
														false, true);
											} else {
												cp1 = new ColoredPlace(
														srcPartitionMA.get_name()
																+ "."
																+ srcPartitionName
																+ "."
																+ srcNodeName,
														colorType, false, true);
											}

											srcPartitionMA.internalModel.modelElements
													.put(srcNodeName, cp1);
											srcActivity.addOutputPlace(cp1);

											String targetNodeName = targetActivityName
													+ "_in";
											Place cp2 = null;
											if (colorType == null) {
												cp2 = new SimplePlace(
														targetPartitionMA.get_name()
																+ "."
																+ targetNodeName,

														true, false);
											} else {
												cp2 = new ColoredPlace(
														targetPartitionMA
																.get_name()
																+ "."
																+ targetNodeName,
														colorType, true, false);
											}
											targetPartitionMA.internalModel.modelElements
													.put(targetNodeName, cp2);
											cp1.bind(cp, 1);

											targetActivity.addInputPlace(cp2);
											srcPartitionMA.addOutputPlace(cp);
											targetPartitionMA.addInputPlace(cp);

										} else {
											Activity srcActivity = (Activity) activityMA.internalModel.modelElements
													.get(srcActivityName);

											Activity targetActivity = (Activity) targetPartitionMA.internalModel.modelElements
													.get(targetActivityName);
											if (targetActivity instanceof TimedActivity) {
												targetActivity = (Activity) targetPartitionMA.internalModel.modelElements
														.get("init_"
																+ targetActivityName);

											}

											String srcNodeName = srcActivityName
													+ "_out";
											SimplePlace cp1 = new SimplePlace(
													activityMA.get_name() + "."
															+ srcNodeName,
													false, true);

											activityMA.internalModel.modelElements
													.put(srcNodeName, cp1);
											srcActivity.addOutputPlace(cp1);

											String targetNodeName = targetActivityName
													+ "_"
													+ targetElement
															.getAttribute("name");
											SimplePlace cp2 = new SimplePlace(
													targetPartitionMA
															.get_name()
															+ "."
															+ targetNodeName);

											targetPartitionMA.internalModel.modelElements
													.put(targetNodeName, cp2);
											cp1.bind(cp2, 1);
											targetActivity.addInputPlace(cp2);

										}
									}

								} else if (srcType
										.equals("uml:CallBehaviorAction")) {
									if (targetType
											.equals("uml:ActivityFinalNode")) {
										SimplePlace sp = (SimplePlace) activityMA.internalModel.modelElements

										.get("final");
										if (sp == null) {
											sp = new SimplePlace(
													activityMA.get_name() + "."
															+ "final");
											activityMA.internalModel.modelElements
													.put("final", sp);
										}
										String srcActivityName = ((Element) srcElement)
												.getAttribute("name");
										MacroActivity srcActivity = (MacroActivity) activityMA.internalModel.modelElements
												.get(srcActivityName);
										srcActivity.addOutputPlace(sp);
										Place maFinal = null;
										for (ModelElement me : srcActivity.internalModel.modelElements
												.values()) {
											if (me.get_sname().equals("final")) {
												sp.bind((Place) me, -1);
												break;
											}

										}
										if (maFinal == null) {
											for (ModelElement me : srcActivity.internalModel.modelElements
													.values()) {
												if (me instanceof MacroActivity) {
													MacroActivity meMA = (MacroActivity) me;
													Place f = (Place) meMA.internalModel.modelElements
															.get("final");
													if (f != null) {
														sp.bind(f, -1);

														break;
													}
												}
											}
										}
									}

								} else if (srcType.equals("uml:OpaqueAction")) {
									if (targetType
											.equals("uml:ActivityFinalNode")) {
										SimplePlace sp = null;
										if (srcPartitionMA == null) {
											sp = (SimplePlace) activityMA.internalModel.modelElements
													.get("final");
											if (sp == null) {
												sp = new SimplePlace(
														activityMA.get_name()
																+ "." + "final");
												activityMA.internalModel.modelElements
														.put("final", sp);

											}
										} else {
											sp = (SimplePlace) srcPartitionMA.internalModel.modelElements
													.get("final");
											if (sp == null) {
												sp = new SimplePlace(
														srcPartitionMA
																.get_name()
																+ "." + "final");
												srcPartitionMA.internalModel.modelElements
														.put("final", sp);

											}

										}
										String srcActivityName = ((Element) srcElement)
												.getAttribute("name");
										Activity srcActivity = null;
										if (srcPartitionMA == null) {
											srcActivity = (Activity) activityMA.internalModel.modelElements
													.get(srcActivityName);

										} else {
											srcActivity = (Activity) srcPartitionMA.internalModel.modelElements
													.get(srcActivityName);

										}

										srcActivity.addOutputPlace(sp);

									} else if (targetType
											.equals("uml:DecisionNode")) {
										String srcActivityName = srcElement

										.getAttribute("name");
										String targetActivityName = targetElement

										.getAttribute("name");
										Activity srcActivity = null, targetActivity = null;
										if (srcPartitionMA == null) {
											srcActivity = (Activity) activityMA.internalModel.modelElements
													.get(srcActivityName);

											targetActivity = (Activity) activityMA.internalModel.modelElements
													.get(targetActivityName);

										} else {
											srcActivity = (Activity) srcPartitionMA.internalModel.modelElements
													.get(srcActivityName);

											targetActivity = (Activity) srcPartitionMA.internalModel.modelElements
													.get(targetActivityName);

										}
										String nodeName = srcActivityName
												+ "_out";
										SimplePlace sp = new SimplePlace(
												activityMA.get_name() + "."
														+ nodeName);

										activityMA.internalModel.modelElements
												.put(nodeName, sp);
										srcActivity.addOutputPlace(sp);

										targetActivity.addInputPlace(sp);
									} else if (targetType
											.equals("uml:InputPin")) {
										String colorId = targetElement
												.getAttribute("type");
										Element colorNode = getElementById(
												doc.getDocumentElement(),
												colorId);
										String colorType = colorNode
												.getAttribute("name");
										String srcActivityName = srcElement

										.getAttribute("name");
										String targetActivityName = ((Element) targetElement
												.getParentNode())
												.getAttribute("name");

										if (srcPartitionMA == null
												&& targetPartitionMA == null) {
											Activity srcActivity = (Activity) activityMA.internalModel.modelElements
													.get(srcActivityName);

											Activity targetActivity = (Activity) activityMA.internalModel.modelElements
													.get(targetActivityName);

											if (targetActivity instanceof TimedActivity) {
												targetActivity = (Activity) activityMA.internalModel.modelElements
														.get("init_"
																+ targetActivityName);

												ColoredPlace data = (ColoredPlace) activityMA.internalModel.modelElements
														.get("data_"
																+ targetActivityName);
												data.type = colorType;

											}

											String nodeName = targetActivityName
													+ "_"
													+ targetElement
															.getAttribute("name");
											ColoredPlace cp = new ColoredPlace(
													activityMA.get_name() + "."
															+ nodeName,
													colorType);

											activityMA.internalModel.modelElements
													.put(nodeName, cp);
											srcActivity.addOutputPlace(cp);

											targetActivity.addInputPlace(cp);

										} else if (srcPartitionMA != null
												&& targetPartitionMA == null) {
											Activity srcActivity = (Activity) srcPartitionMA.internalModel.modelElements
													.get(srcActivityName);
											Activity targetActivity = (Activity) activityMA.internalModel.modelElements
													.get(targetActivityName);
											if (targetActivity instanceof TimedActivity) {
												targetActivity = (Activity) activityMA.internalModel.modelElements
														.get("init_"
																+ targetActivityName);

												ColoredPlace data = (ColoredPlace) activityMA.internalModel.modelElements
														.get("data_"
																+ targetActivityName);
												data.type = colorType;

											}
											String srcNodeName = srcActivityName
													+ "_"
													+ srcElement
															.getAttribute("name");
											ColoredPlace cp1 = new ColoredPlace(
													srcPartitionMA.get_name()
															+ "."
															+ srcPartitionName
															+ "." + srcNodeName,
													colorType, false, true);

											srcPartitionMA.internalModel.modelElements
													.put(srcNodeName, cp1);
											srcActivity.addOutputPlace(cp1);

											String targetNodeName = targetActivityName
													+ "_"
													+ targetElement
															.getAttribute("name");
											ColoredPlace cp2 = new ColoredPlace(
													activityMA.get_name() + "."
															+ targetNodeName,
													colorType);

											cp1.bind(cp2, 1);
											targetActivity.addInputPlace(cp2);

											activityMA.internalModel.modelElements
													.put(targetNodeName, cp2);

										} else if (srcPartitionMA != null
												&& targetPartitionMA != null) {
											Activity srcActivity = (Activity) srcPartitionMA.internalModel.modelElements
													.get(srcActivityName);
											// targetActivityName cannot be null
											Activity targetActivity = (Activity) targetPartitionMA.internalModel.modelElements
													.get(targetActivityName);
											if (targetActivity instanceof TimedActivity) {
												targetActivity = (Activity) targetPartitionMA.internalModel.modelElements
														.get("init_"
																+ targetActivityName);

												ColoredPlace data = (ColoredPlace) targetPartitionMA.internalModel.modelElements
														.get("data_"
																+ targetActivityName);
												data.type = colorType;

											}

											String srcNodeName = srcActivityName
													+ "_"
													+ srcElement
															.getAttribute("name");
											ColoredPlace cp = new ColoredPlace(
													activityMA.get_name() + "."
															+ srcNodeName,
													colorType);
											activityMA.internalModel.modelElements
													.put(srcNodeName, cp);
											ColoredPlace cp1 = new ColoredPlace(
													srcPartitionMA.get_name()
															+ "."
															+ srcPartitionName
															+ "." + srcNodeName,
													colorType, false, true);

											srcPartitionMA.internalModel.modelElements
													.put(srcNodeName, cp1);
											srcPartitionMA.addOutputPlace(cp);
											srcActivity.addOutputPlace(cp1);

											String targetNodeName = targetActivityName
													+ "_"
													+ targetElement
															.getAttribute("name");
											ColoredPlace cp2 = new ColoredPlace(
													targetPartitionMA.get_name()
															+ "."
															+ targetNodeName,
													colorType, true, false);

											targetPartitionMA.internalModel.modelElements
													.put(targetNodeName, cp2);
											targetPartitionMA.addInputPlace(cp);
											cp1.bind(cp, 1);
											cp2.bind(cp, -1);
											targetActivity.addInputPlace(cp2);

										} else {
											Activity srcActivity = (Activity) activityMA.internalModel.modelElements
													.get(srcActivityName);

											Activity targetActivity = (Activity) targetPartitionMA.internalModel.modelElements
													.get(targetActivityName);
											if (targetActivity instanceof TimedActivity) {
												targetActivity = (Activity) targetPartitionMA.internalModel.modelElements
														.get("init_"
																+ targetActivityName);

												ColoredPlace data = (ColoredPlace) targetPartitionMA.internalModel.modelElements
														.get("data_"
																+ targetActivityName);
												data.type = colorType;

											}

											String srcNodeName = srcActivityName
													+ "_"
													+ srcElement
															.getAttribute("name");
											ColoredPlace cp1 = new ColoredPlace(
													activityMA.get_name() + "."
															+ srcNodeName,
													colorNode
															.getAttribute("name"),
													false, true);

											activityMA.internalModel.modelElements
													.put(srcNodeName, cp1);

											srcActivity.addOutputPlace(cp1);

											String targetNodeName = targetActivityName
													+ "_"
													+ targetElement
															.getAttribute("name");
											ColoredPlace cp2 = new ColoredPlace(
													targetPartitionMA.get_name()
															+ "."
															+ targetNodeName,
													colorType);

											targetPartitionMA.internalModel.modelElements
													.put(targetNodeName, cp2);
											cp1.bind(cp2, 1);
											targetActivity.addInputPlace(cp2);

										}
									} else if (targetType
											.equals("uml:ActivityParameterNode")) {
										String colorId = targetElement
												.getAttribute("type");
										Element colorNode = getElementById(
												doc.getDocumentElement(),
												colorId);
										String srcActivityName = srcElement

										.getAttribute("name");

										if (srcPartitionMA == null
												&& targetPartitionMA == null) {
											Activity srcActivity = (Activity) activityMA.internalModel.modelElements
													.get(srcActivityName);

											String nodeName = targetElement
													.getAttribute("name")
													+ "_"
													+ activityMA.get_name();
											ColoredPlace cp = new ColoredPlace(
													activityMA.get_name() + "."
															+ nodeName,
													colorNode
															.getAttribute("name"),
													false, true);

											activityMA.internalModel.modelElements
													.put(nodeName, cp);
											srcActivity.addOutputPlace(cp);

										} else if (srcPartitionMA != null
												&& targetPartitionMA == null) {
											Activity srcActivity = (Activity) srcPartitionMA.internalModel.modelElements
													.get(srcActivityName);

											String srcNodeName = srcActivityName
													+ "_"
													+ srcElement
															.getAttribute("name");
											ColoredPlace cp1 = new ColoredPlace(
													srcPartitionMA.get_name()
															+ "."
															+ srcPartitionName
															+ "." + srcNodeName,
													colorNode
															.getAttribute("name"),
													false, true);

											srcPartitionMA.internalModel.modelElements
													.put(srcNodeName, cp1);
											srcActivity.addOutputPlace(cp1);

											String targetNodeName = maName
													+ "_"
													+ targetElement
															.getAttribute("name");
											ColoredPlace cp2 = new ColoredPlace(
													activityMA.get_name() + "."
															+ targetNodeName,
													colorNode
															.getAttribute("name"),
													false, true);

											activityMA.internalModel.modelElements
													.put(targetNodeName, cp2);

											cp1.bind(cp2, 1);
											srcPartitionMA.addOutputPlace(cp2);

										}
									} else if (targetType
											.equals("uml:CallBehaviorAction")) {

										String srcActivityName = srcElement

										.getAttribute("name");
										String targetActivityName = targetElement
												.getAttribute("name");
										if (srcPartitionMA == null
												&& targetPartitionMA == null) {
											Activity srcActivity = (Activity) activityMA.internalModel.modelElements
													.get(srcActivityName);

											if (targetActivityName != null) {
												String id = targetElement
														.getAttribute("xmi:id");
												Element paStep = null;
												for (int p = 0; p < paSteps
														.getLength(); p++) {
													Element curStep = (Element) paSteps
															.item(p);
													if (curStep
															.getAttribute(
																	"base_NamedElement")
															.equals(id)) {
														paStep = curStep;
														break;
													}
												}
												if (paStep != null
														&& !paStep
																.getAttribute(
																		"rep")
																.isEmpty()) {

													InstantaneousActivity targetActivity = (InstantaneousActivity) activityMA.internalModel.modelElements
															.get("rep_"
																	+ targetActivityName);

													String nodeName = srcActivityName
															+ "_out";
													SimplePlace sp = new SimplePlace(
															activityMA
																	.get_name()
																	+ "."
																	+ nodeName);
													activityMA.internalModel.modelElements
															.put(nodeName, sp);
													srcActivity
															.addOutputPlace(sp);

													targetActivity
															.addInputPlace(sp);
												} else {
													MacroActivity targetActivity = (MacroActivity) activityMA.internalModel.modelElements
															.get(targetActivityName);

													String nodeName = srcActivityName
															+ "_out";
													SimplePlace sp = new SimplePlace(
															activityMA
																	.get_name()
																	+ "."
																	+ nodeName);

													activityMA.internalModel.modelElements
															.put(nodeName, sp);
													srcActivity
															.addOutputPlace(sp);

													targetActivity
															.addInputPlace(sp);
													Place init = (Place) targetActivity.internalModel.modelElements
															.get("init");
													if (init == null) {// MA
																		// contains
																		// internal
																		// MAs
														for (ModelElement me : targetActivity.internalModel.modelElements
																.values()) {
															if (me instanceof MacroActivity) {
																MacroActivity meMA = (MacroActivity) me;
																init = (Place) meMA.internalModel.modelElements
																		.get("init");
																if (init != null)
																	break;
															}
														}
													}
													init.setInputFusion();
													sp.bind(init, 1);
												}
											}
										} else if (srcPartitionMA != null
												&& targetPartitionMA == null) {
											Activity srcActivity = (Activity) srcPartitionMA.internalModel.modelElements
													.get(srcActivityName);

											if (targetActivityName != null) {
												Activity targetActivity = (Activity) activityMA.internalModel.modelElements
														.get(targetActivityName);

												String srcNodeName = srcActivityName
														+ "_"
														+ srcElement
																.getAttribute("name");
												SimplePlace sp1 = new SimplePlace(
														activityMA.get_name()
																+ "."
																+ srcPartitionName
																+ "."
																+ srcNodeName,
														false, true);

												srcPartitionMA.internalModel.modelElements
														.put(srcNodeName, sp1);
												srcActivity.addOutputPlace(sp1);

												String targetNodeName = targetActivityName
														+ "_"
														+ targetElement
																.getAttribute("name");
												SimplePlace sp2 = new SimplePlace(
														activityMA.get_name()
																+ "."
																+ targetNodeName);

												activityMA.internalModel.modelElements
														.put(targetNodeName,
																sp2);
												sp1.bind(sp2, 1);
												targetActivity
														.addInputPlace(sp2);
											} else {
												String srcNodeName = srcActivityName
														+ "_"
														+ srcElement
																.getAttribute("name");
												SimplePlace sp1 = new SimplePlace(
														activityMA.get_name()
																+ "."
																+ srcPartitionName
																+ "."
																+ srcNodeName,
														false, true);

												srcPartitionMA.internalModel.modelElements
														.put(srcNodeName, sp1);
												srcActivity.addOutputPlace(sp1);

												String targetNodeName = maName
														+ "_"
														+ targetElement
																.getAttribute("name");
												SimplePlace sp2 = new SimplePlace(
														activityMA.get_name()
																+ "."
																+ targetNodeName,
														false, true);

												activityMA.internalModel.modelElements
														.put(targetNodeName,
																sp2);
												sp1.bind(sp2, 1);
												srcPartitionMA
														.addOutputPlace(sp2);
											}
										} else if (srcPartitionMA != null
												&& targetPartitionMA != null) {
											Activity srcActivity = (Activity) srcPartitionMA.internalModel.modelElements
													.get(srcActivityName);

											// targetActivityName cannot be null
											Activity targetActivity = (Activity) targetPartitionMA.internalModel.modelElements
													.get(targetActivityName);

											String srcNodeName = srcActivityName
													+ "_"
													+ srcElement
															.getAttribute("name");
											SimplePlace sp = new SimplePlace(
													activityMA.get_name() + "."
															+ srcNodeName);

											activityMA.internalModel.modelElements
													.put(srcNodeName, sp);
											SimplePlace sp1 = new SimplePlace(
													srcPartitionMA.get_name()
															+ "."
															+ srcPartitionName
															+ "." + srcNodeName,
													false, true);

											srcPartitionMA.internalModel.modelElements
													.put(srcNodeName, sp1);
											srcActivity.addOutputPlace(sp1);

											String targetNodeName = targetActivityName
													+ "_"
													+ targetElement
															.getAttribute("name");
											SimplePlace sp2 = new SimplePlace(
													targetPartitionMA.get_name()
															+ "."
															+ targetNodeName,

													true, false);

											targetPartitionMA.internalModel.modelElements
													.put(targetNodeName, sp2);
											sp1.bind(sp, 1);
											sp2.bind(sp, -1);
											targetActivity.addInputPlace(sp2);
											srcPartitionMA.addOutputPlace(sp);
											targetPartitionMA.addInputPlace(sp);

										} else {
											Activity srcActivity = (Activity) activityMA.internalModel.modelElements
													.get(srcActivityName);

											Activity targetActivity = (Activity) targetPartitionMA.internalModel.modelElements
													.get(targetActivityName);

											String srcNodeName = srcActivityName
													+ "_"
													+ srcElement
															.getAttribute("name");
											SimplePlace cp1 = new SimplePlace(
													activityMA.get_name() + "."
															+ srcNodeName,
													false, true);

											activityMA.internalModel.modelElements
													.put(srcNodeName, cp1);
											srcActivity.addOutputPlace(cp1);

											String targetNodeName = targetActivityName
													+ "_"
													+ targetElement
															.getAttribute("name");
											SimplePlace cp2 = new SimplePlace(
													targetPartitionMA
															.get_name()
															+ "."
															+ targetNodeName);

											targetPartitionMA.internalModel.modelElements
													.put(targetNodeName, cp2);
											cp1.bind(cp2, 1);
											targetActivity.addInputPlace(cp2);

										}
									} else if (targetType
											.equals("uml:OpaqueAction")) {

										String srcActivityName = srcElement

										.getAttribute("name");
										String targetActivityName = targetElement
												.getAttribute("name");

										if (srcPartitionMA == null
												&& targetPartitionMA == null) {
											Activity srcActivity = (Activity) activityMA.internalModel.modelElements
													.get(srcActivityName);

											if (targetActivityName != null) {
												Activity targetActivity = (Activity) activityMA.internalModel.modelElements
														.get(targetActivityName);

												String nodeName = srcActivityName
														+ "_out";
												SimplePlace sp = new SimplePlace(
														activityMA.get_name()
																+ "."
																+ nodeName);

												activityMA.internalModel.modelElements
														.put(nodeName, sp);

												srcActivity.addOutputPlace(sp);
												if (targetActivity instanceof TimedActivity) {
													targetActivity = (Activity) activityMA.internalModel.modelElements
															.get("init_"
																	+ targetActivityName);
												}

												targetActivity
														.addInputPlace(sp);
												if (targetType
														.equals("uml:DecisionNode")) {
													Place decisionPlace = (Place) targetActivity.outputPlaces
															.toArray()[0];
													decisionPlace = new SimplePlace(
															decisionPlace
																	.get_name());
												}

											}
										} else if (srcPartitionMA != null
												&& targetPartitionMA == null) {
											Activity srcActivity = (Activity) srcPartitionMA.internalModel.modelElements
													.get(srcActivityName);

											if (targetActivityName != null) {
												Activity targetActivity = (Activity) activityMA.internalModel.modelElements
														.get(targetActivityName);

												String srcNodeName = srcActivityName
														+ "_"
														+ srcElement
																.getAttribute("name");
												SimplePlace sp1 = new SimplePlace(
														activityMA.get_name()
																+ "."
																+ srcPartitionName
																+ "."
																+ srcNodeName,
														false, true);

												srcPartitionMA.internalModel.modelElements
														.put(srcNodeName, sp1);
												srcActivity.addOutputPlace(sp1);

												String targetNodeName = targetActivityName
														+ "_"
														+ targetElement
																.getAttribute("name");
												SimplePlace sp2 = new SimplePlace(
														activityMA.get_name()
																+ "."
																+ targetNodeName);

												activityMA.internalModel.modelElements
														.put(targetNodeName,
																sp2);
												sp1.bind(sp2, 1);
												if (targetActivity instanceof TimedActivity) {
													targetActivity = (Activity) activityMA.internalModel.modelElements
															.get("init_"
																	+ targetActivityName);
												}

												targetActivity
														.addInputPlace(sp2);
											} else {
												String srcNodeName = srcActivityName
														+ "_"
														+ srcElement
																.getAttribute("name");
												SimplePlace sp1 = new SimplePlace(
														activityMA.get_name()
																+ "."
																+ srcPartitionName
																+ "."
																+ srcNodeName,
														false, true);

												srcPartitionMA.internalModel.modelElements
														.put(srcNodeName, sp1);
												srcActivity.addOutputPlace(sp1);

												String targetNodeName = maName
														+ "_"
														+ targetElement
																.getAttribute("name");
												SimplePlace sp2 = new SimplePlace(
														activityMA.get_name()
																+ "."
																+ targetNodeName,
														false, true);

												activityMA.internalModel.modelElements
														.put(targetNodeName,
																sp2);
												sp1.bind(sp2, 1);
												srcPartitionMA
														.addOutputPlace(sp2);
											}
										} else if (srcPartitionMA != null
												&& targetPartitionMA != null) {
											Activity srcActivity = (Activity) srcPartitionMA.internalModel.modelElements
													.get(srcActivityName);

											// targetActivityName cannot be null
											Activity targetActivity = (Activity) targetPartitionMA.internalModel.modelElements
													.get(targetActivityName);

											String srcNodeName = srcActivityName
													+ "_out";
											SimplePlace sp = new SimplePlace(
													srcActivityName
															+ "_"
															+ targetActivityName);

											activityMA.internalModel.modelElements
													.put(sp.get_sname(), sp);
											SimplePlace sp1 = new SimplePlace(
													srcPartitionMA.get_name()
															+ "."
															+ srcPartitionName
															+ "." + srcNodeName,
													false, true);

											srcPartitionMA.internalModel.modelElements
													.put(srcNodeName, sp1);
											srcActivity.addOutputPlace(sp1);

											String targetNodeName = targetActivityName
													+ "_in";
											SimplePlace sp2 = new SimplePlace(
													targetPartitionMA.get_name()
															+ "."
															+ targetNodeName,

													true, false);

											targetPartitionMA.internalModel.modelElements
													.put(targetNodeName, sp2);
											sp1.bind(sp, 1);

											if (targetActivity instanceof TimedActivity) {
												targetActivity = (Activity) targetPartitionMA.internalModel.modelElements
														.get("init_"
																+ targetActivityName);
											}

											targetActivity.addInputPlace(sp2);
											srcPartitionMA.addOutputPlace(sp);
											targetPartitionMA.addInputPlace(sp);

										} else {
											Activity srcActivity = (Activity) activityMA.internalModel.modelElements
													.get(srcActivityName);

											Activity targetActivity = (Activity) targetPartitionMA.internalModel.modelElements
													.get(targetActivityName);

											String srcNodeName = srcActivityName
													+ "_"
													+ srcElement
															.getAttribute("name");
											SimplePlace cp1 = new SimplePlace(
													activityMA.get_name() + "."
															+ srcNodeName,
													false, true);

											activityMA.internalModel.modelElements
													.put(srcNodeName, cp1);
											srcActivity.addOutputPlace(cp1);

											String targetNodeName = targetActivityName
													+ "_"
													+ targetElement
															.getAttribute("name");
											SimplePlace cp2 = new SimplePlace(
													targetPartitionMA
															.get_name()
															+ "."
															+ targetNodeName);

											targetPartitionMA.internalModel.modelElements
													.put(targetNodeName, cp2);
											cp1.bind(cp2, 1);
											targetActivity.addInputPlace(cp2);

										}
									}
								}

							}
						}
					}
				}
			}
			for (MacroActivity ma : modelMAs) {
				for (ModelElement me : ma.internalModel.modelElements.values()) {
					if (me instanceof Place) {
						Place p = (Place) me;
						if (p.isInputFusion)
							for (Place inp : ma.inputPlaces) {
								if (inp.type.equals(p.type)) {
									inp.bind(p, 1);
								}
							}
						else if (p.isOutputFusion)
							for (Place outp : ma.outputPlaces) {
								if (outp.type.equals(p.type)) {
									outp.bind(p, -1);
								}
							}
					}

				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return model;
	}

	public static void simulate() {

		// Sim_system.set_transient_condition(Sim_system.TIME_ELAPSED, 200000);
		Sim_system.set_termination_condition(Sim_system.TIME_ELAPSED, 100000000,
				false);

		Sim_system.set_trace_detail(true, true, true);
		Sim_system.set_output_analysis(Sim_system.BATCH_MEANS);
		Sim_system.generate_graphs(true);
		long start = System.currentTimeMillis();
		Sim_system.run();
		System.out.println("sim time: " + (System.currentTimeMillis() - start));

	}

	public String getName() {
		return name;
	}

	public void setParent(HCSANModel parent) {
		this.parent = parent;
	}

	@Override
	public String toString() {
		return name;
	}

}
