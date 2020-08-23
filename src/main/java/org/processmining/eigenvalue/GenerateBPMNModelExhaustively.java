package org.processmining.eigenvalue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.jbpt.petri.Flow;
import org.jbpt.petri.IPetriNet;
import org.jbpt.petri.Marking;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagramFactory;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagramImpl;
import org.processmining.models.graphbased.directed.bpmn.BPMNEdge;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.bpmn.elements.Activity;
import org.processmining.models.graphbased.directed.bpmn.elements.Event;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway;
import org.processmining.models.graphbased.directed.bpmn.elements.Event.EventTrigger;
import org.processmining.models.graphbased.directed.bpmn.elements.Event.EventType;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway.GatewayType;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.plugins.bpmn.BpmnFlow;
import org.processmining.plugins.bpmn.diagram.BpmnDiagram;
import org.processmining.plugins.bpmn.plugins.BpmnExportPlugin;
import org.processmining.plugins.converters.BPMNUtils;
import org.processmining.plugins.converters.bpmn2pn.BPMN2PetriNetConverter;
import org.processmining.plugins.converters.bpmn2pn.BPMN2PetriNetConverter_Configuration;
import org.processmining.plugins.converters.bpmn2pn.BPMN2PetriNetConverter_Configuration.LabelValue;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.StatePair;
import gnu.trove.map.TObjectShortMap;
import gnu.trove.map.custom_hash.TObjectShortCustomHashMap;
import gnu.trove.strategy.HashingStrategy;

public class GenerateBPMNModelExhaustively {
	public static String alphabet = "abc";
	

	public static int numberOfSets = 1000;

	private static String lineSeparator = System.getProperty("line.separator");
	
	private static int bpmnNum =0 ;

	private static Activity findActivityByLabel(BPMNDiagram bpmn, String label) {
		for(Activity a : bpmn.getActivities()) {
			if(a.getLabel().equals(label)) {
				return a;
			}
		}
		return null;
	}
	
	private static Collection<Activity> filterActivities(Collection<Activity> activities, int lastChangedIndex) {
	
		Collection<Activity> filteredActivities = new HashSet<Activity>();
		
		for(Activity activity : activities) {
			
			String label = activity.getLabel();
			int index = alphabet.indexOf(label);
			if(index >= lastChangedIndex) {
				filteredActivities.add(activity);
			}
		}
		
		return filteredActivities;
	}
	
	public static void generateExtensions(BPMNDiagram bpmn, Set<String> insideSkip,  Set<String> insideLoop, int k, int lastChangedIndex) {

			
			Collection<Activity> allActivities =  filterActivities(bpmn.getActivities(), lastChangedIndex);
			
			
			if (bpmn.getActivities().size() < alphabet.length()) {
				
				for(Activity activity : allActivities) {
					for(int op=0; op < 3; op++) {
						BPMNDiagram newBpmn = BPMNDiagramFactory.cloneBPMNDiagram(bpmn);
						updateBPMNModel(newBpmn, findActivityByLabel(newBpmn, activity.getLabel()), op, k);
						
						Set<String> newActivitiesInsideLoop = new HashSet<String>();
						newActivitiesInsideLoop.addAll(insideLoop);
						newActivitiesInsideLoop.remove(activity.getLabel());
						
						Set<String> newActivitiesInsideSkip = new HashSet<String>();
						newActivitiesInsideSkip.addAll(insideSkip);
						newActivitiesInsideSkip.remove(activity.getLabel());
					
						generateExtensions(newBpmn, newActivitiesInsideSkip, newActivitiesInsideLoop, k+1, alphabet.indexOf(activity.getLabel()));
					}
					if(!insideLoop.contains(activity.getLabel()) && !insideSkip.contains(activity.getLabel())) {
						BPMNDiagram newBpmn = BPMNDiagramFactory.cloneBPMNDiagram(bpmn);
						updateBPMNModel(newBpmn, findActivityByLabel(newBpmn, activity.getLabel()), 3, k);
						
						Set<String> newActivitiesInsideLoop = new HashSet<String>();
						newActivitiesInsideLoop.addAll(insideLoop);
						newActivitiesInsideLoop.add(activity.getLabel());
						
						Set<String> newActivitiesInsideSkip = new HashSet<String>();
						newActivitiesInsideSkip.addAll(insideSkip);
						
						generateExtensions(newBpmn, newActivitiesInsideSkip, newActivitiesInsideLoop, k, alphabet.indexOf(activity.getLabel()));
					}
					if(!insideSkip.contains(activity.getLabel())) {
						BPMNDiagram newBpmn = BPMNDiagramFactory.cloneBPMNDiagram(bpmn);
						updateBPMNModel(newBpmn, findActivityByLabel(newBpmn, activity.getLabel()), 4, k);
						
						Set<String> newActivitiesInsideLoop = new HashSet<String>();
						newActivitiesInsideLoop.addAll(insideLoop);
						
						Set<String> newActivitiesInsideSkip = new HashSet<String>();
						newActivitiesInsideSkip.addAll(insideSkip);
						newActivitiesInsideSkip.add(activity.getLabel());
						
						generateExtensions(newBpmn, newActivitiesInsideSkip, newActivitiesInsideLoop, k, alphabet.indexOf(activity.getLabel()));
					}		
				}
			}
			
			if (bpmn.getActivities().size() == alphabet.length()) {
				
				writeBPMNDiagram(bpmn);
			
				for(Activity activity : allActivities) {
					if(!insideLoop.contains(activity.getLabel()) && !insideSkip.contains(activity.getLabel())) {
						BPMNDiagram newBpmn = BPMNDiagramFactory.cloneBPMNDiagram(bpmn);
						updateBPMNModel(newBpmn, findActivityByLabel(newBpmn, activity.getLabel()), 3, k);
						
						Set<String> newActivitiesInsideLoop = new HashSet<String>();
						newActivitiesInsideLoop.addAll(insideLoop);
						newActivitiesInsideLoop.add(activity.getLabel());
						
						Set<String> newActivitiesInsideSkip = new HashSet<String>();
						newActivitiesInsideSkip.addAll(insideSkip);
						
						generateExtensions(newBpmn, newActivitiesInsideSkip, newActivitiesInsideLoop, k, alphabet.indexOf(activity.getLabel()));
					}
					
					if(!insideSkip.contains(activity.getLabel())) {
						BPMNDiagram newBpmn = BPMNDiagramFactory.cloneBPMNDiagram(bpmn);
						updateBPMNModel(newBpmn, findActivityByLabel(newBpmn, activity.getLabel()), 4, k);
						
						Set<String> newActivitiesInsideLoop = new HashSet<String>();
						newActivitiesInsideLoop.addAll(insideLoop);
						
						Set<String> newActivitiesInsideSkip = new HashSet<String>();
						newActivitiesInsideSkip.addAll(insideSkip);
						newActivitiesInsideSkip.add(activity.getLabel());
						
						generateExtensions(newBpmn, newActivitiesInsideSkip, newActivitiesInsideLoop, k, alphabet.indexOf(activity.getLabel()));
					}
				}
				
				
				
//				Collection<Activity> allActivities =  bpmn.getActivities();
				
//				for(Activity activity : allActivities) {
//					if(!insideLoop.contains(activity.getLabel())) {
//						BPMNDiagram newBpmn = BPMNDiagramFactory.cloneBPMNDiagram(bpmn);
//						updateBPMNModel(newBpmn, findActivityByLabel(newBpmn, activity.getLabel()), 3, k);
//						
//						Set<String> newActivitiesInsideLoop = new HashSet<String>();
//						newActivitiesInsideLoop.addAll(insideLoop);
//						newActivitiesInsideLoop.add(activity.getLabel());
//						
//						Set<String> newActivitiesInsideSkip = new HashSet<String>();
//						newActivitiesInsideSkip.addAll(insideSkip);
//						
//						generateExtensions(newBpmn, newActivitiesInsideSkip, newActivitiesInsideLoop, k);
//					}
//					if(!insideSkip.contains(activity.getLabel())) {
//						BPMNDiagram newBpmn = BPMNDiagramFactory.cloneBPMNDiagram(bpmn);
//						updateBPMNModel(newBpmn, findActivityByLabel(newBpmn, activity.getLabel()), 4, k);
//						
//						Set<String> newActivitiesInsideLoop = new HashSet<String>();
//						newActivitiesInsideLoop.addAll(insideLoop);
//						
//						Set<String> newActivitiesInsideSkip = new HashSet<String>();
//						newActivitiesInsideSkip.addAll(insideSkip);
//						newActivitiesInsideSkip.add(activity.getLabel());
//						
//						generateExtensions(newBpmn, newActivitiesInsideSkip, newActivitiesInsideLoop, k);
//					}
//				}
			}
	}

		
	
	private static void writeBPMNDiagram(BPMNDiagram bpmn) {
		try {
		//removeRedundantCyclicGateways(bpmn);
		BPMNUtils.simplifyBPMNDiagram(null, bpmn);
		//removeRedundantCyclicGateways(bpmn);

		BpmnExportPlugin exp = new BpmnExportPlugin();
		exp.export(null, bpmn, new File("bpmn" + bpmnNum + ".bpmn"));
		bpmnNum++;

		double entropy = calculateEntropyOfBPMNModel(bpmn);
		int numberOfSplittingGateways = calculateNumberOfSplittingGateways(bpmn);
		int numberOfNodes = bpmn.getNodes().size();
		int numberOfEdges = bpmn.getEdges().size();
		double density = (double) numberOfEdges / (double) (numberOfNodes * (numberOfNodes - 1));
		double cnc = (double) numberOfEdges / (double) numberOfNodes;
		double avgConnectorDegree = calculateAvgConnectorDegree(bpmn);
		int maxConnectorDegree = calculateMaxConnectorDegree(bpmn);
		double sequentiality = calculateSequentiality(bpmn);
		double cyclicity = calculateCyclicity(bpmn);
		int diameter = calculateDiameter(bpmn);
		int depth = calculateDepth(new HashSet<BPMNNode>(), retrieveStartEvent(bpmn), bpmn);
		int tokenSplit = calculateTokenSplit(bpmn);
		int controlflowComplexity = calculateControlflowComplexity(bpmn);
		double connectorHeterogenity = calculateConnectorHeterogenity(bpmn);

		// ----------------------------------------------------------------------------------------------------------------------
		Files.write(Paths.get(
				"bpmn_number_of_nodes.txt"),
				new String(entropy + " " + numberOfNodes).getBytes(), StandardOpenOption.APPEND);
		Files.write(Paths.get(
				"bpmn_number_of_nodes.txt"),
				lineSeparator.getBytes(), StandardOpenOption.APPEND);
		// ----------------------------------------------------------------------------------------------------------------------

		// ----------------------------------------------------------------------------------------------------------------------
		Files.write(Paths.get(
				"splitting_gateways.txt"),
				new String(numberOfSplittingGateways + " " + entropy).getBytes(), StandardOpenOption.APPEND);
		Files.write(Paths.get(
				"splitting_gateways.txt"),
				lineSeparator.getBytes(), StandardOpenOption.APPEND);
		// ----------------------------------------------------------------------------------------------------------------------

		// ----------------------------------------------------------------------------------------------------------------------
		Files.write(Paths.get(
				"bpmn_number_of_edges.txt"),
				new String(numberOfEdges + " " + entropy).getBytes(), StandardOpenOption.APPEND);
		Files.write(Paths.get(
				"bpmn_number_of_edges.txt"),
				lineSeparator.getBytes(), StandardOpenOption.APPEND);
		// ----------------------------------------------------------------------------------------------------------------------

		// ----------------------------------------------------------------------------------------------------------------------
		Files.write(Paths.get(
				"density.txt"),
				new String(density + " " + entropy).getBytes(), StandardOpenOption.APPEND);
		Files.write(Paths.get(
				"density.txt"),
				lineSeparator.getBytes(), StandardOpenOption.APPEND);
		// ----------------------------------------------------------------------------------------------------------------------

		// ----------------------------------------------------------------------------------------------------------------------
		Files.write(Paths.get(
				"cnc.txt"),
				new String(entropy + " " + cnc).getBytes(), StandardOpenOption.APPEND);
		Files.write(Paths.get(
				"cnc.txt"),
				lineSeparator.getBytes(), StandardOpenOption.APPEND);
		// ----------------------------------------------------------------------------------------------------------------------

		// ----------------------------------------------------------------------------------------------------------------------
		Files.write(Paths.get(
				"avg_connector_degree.txt"),
				new String(avgConnectorDegree + " " + entropy).getBytes(), StandardOpenOption.APPEND);
		Files.write(Paths.get(
				"avg_connector_degree.txt"),
				lineSeparator.getBytes(), StandardOpenOption.APPEND);
		// ----------------------------------------------------------------------------------------------------------------------

		// ----------------------------------------------------------------------------------------------------------------------
		Files.write(Paths.get(
				"max_connector_degree.txt"),
				new String(maxConnectorDegree + " " + entropy).getBytes(), StandardOpenOption.APPEND);
		Files.write(Paths.get(
				"max_connector_degree.txt"),
				lineSeparator.getBytes(), StandardOpenOption.APPEND);
		// ----------------------------------------------------------------------------------------------------------------------

		// ----------------------------------------------------------------------------------------------------------------------
		Files.write(Paths.get(
				"sequentiality.txt"),
				new String(sequentiality + " " + entropy).getBytes(), StandardOpenOption.APPEND);
		Files.write(Paths.get(
				"sequentiality.txt"),
				lineSeparator.getBytes(), StandardOpenOption.APPEND);
		// ----------------------------------------------------------------------------------------------------------------------

		// ----------------------------------------------------------------------------------------------------------------------
		Files.write(Paths.get(
				"clicity.txt"),
				new String(cyclicity + " " + entropy).getBytes(), StandardOpenOption.APPEND);
		Files.write(Paths.get(
				"cyclicity.txt"),
				lineSeparator.getBytes(), StandardOpenOption.APPEND);
		// ----------------------------------------------------------------------------------------------------------------------

		// ----------------------------------------------------------------------------------------------------------------------
		Files.write(Paths.get(
				"diameter.txt"),
				new String(diameter + " " + entropy).getBytes(), StandardOpenOption.APPEND);
		Files.write(Paths.get(
				"diameter.txt"),
				lineSeparator.getBytes(), StandardOpenOption.APPEND);
		// ----------------------------------------------------------------------------------------------------------------------

		// ----------------------------------------------------------------------------------------------------------------------
		Files.write(Paths.get(
				"depth.txt"),
				new String(depth + " " + entropy).getBytes(), StandardOpenOption.APPEND);
		Files.write(Paths.get(
				"depth.txt"),
				lineSeparator.getBytes(), StandardOpenOption.APPEND);
		// ----------------------------------------------------------------------------------------------------------------------

		// ----------------------------------------------------------------------------------------------------------------------
		Files.write(Paths.get(
				"tokenSplit.txt"),
				new String(tokenSplit + " " + entropy).getBytes(), StandardOpenOption.APPEND);
		Files.write(Paths.get(
				"tokenSplit.txt"),
				lineSeparator.getBytes(), StandardOpenOption.APPEND);
		// ----------------------------------------------------------------------------------------------------------------------

		// ----------------------------------------------------------------------------------------------------------------------
		Files.write(Paths.get(
				"controlflowComplexity.txt"),
				new String(controlflowComplexity + " " + entropy).getBytes(), StandardOpenOption.APPEND);
		Files.write(Paths.get(
				"controlflowComplexity.txt"),
				lineSeparator.getBytes(), StandardOpenOption.APPEND);
		// ----------------------------------------------------------------------------------------------------------------------

		// ----------------------------------------------------------------------------------------------------------------------
		Files.write(Paths.get(
				"connectorHeterogenity.txt"),
				new String(connectorHeterogenity + " " + entropy).getBytes(), StandardOpenOption.APPEND);
		Files.write(Paths.get(
				"connectorHeterogenity.txt"),
				lineSeparator.getBytes(), StandardOpenOption.APPEND);
		// ----------------------------------------------------------------------------------------------------------------------

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		BPMNDiagram bpmnmodel = new BPMNDiagramImpl("0");
		Activity activity = bpmnmodel.addActivity(alphabet.substring(0, 1), false, false, false, false, false);
		Event startEvent = bpmnmodel.addEvent("start", EventType.START, EventTrigger.NONE, null, false, null);
		Event endEvent = bpmnmodel.addEvent("end", EventType.END, EventTrigger.NONE, null, false, null);
		bpmnmodel.addFlow(startEvent, activity, "");
		bpmnmodel.addFlow(activity, endEvent, "");

		generateExtensions(bpmnmodel, new HashSet<String>(), new HashSet<String>(), 1, 0);

	}
	
//	private static int calculateForwardSeparabilty(Set<BPMNNode> visitedGateways,
//			BPMNNode startNode, BPMNDiagram bpmnmodel) {
//		int cnt = 0;
//		for (BPMNEdge e : bpmnmodel.getOutEdges(startNode)) {
//			BPMNNode outNode = (BPMNNode) e.getTarget();
//			if (outNode instanceof Activity) { // if splitting gateway
//				cnt = cnt + 1 + calculateForwardSeparabilty(visitedGateways, outNode, bpmnmodel);	
//			} else if(outNode instanceof Gateway) {
//				if ((bpmnmodel.getInEdges(outNode).size() > 1) && (bpmnmodel.getOutEdges(outNode).size() > 1)
//						&&(!visitedGateways.contains(outNode))) {
//					visitedGateways.add(outNode);
//					cnt = cnt + 1 + calculateForwardSeparabilty(visitedGateways, outNode, bpmnmodel);	
//				}
//			}
//		}
//		return cnt;
//	}
//	
//	private static int calculateBackwardSeparabilty(BPMNNode endNode, BPMNDiagram bpmnmodel) {
//		int cnt = 0;
//		for (BPMNEdge e : bpmnmodel.getInEdges(endNode)) {
//			BPMNNode inNode = (BPMNNode) e.getSource();
//			if (inNode instanceof Activity) { // if splitting gateway
//				cnt = cnt + 1 + calculateBackwardSeparabilty(inNode, bpmnmodel);	
//			} else if(inNode instanceof Gateway) {
//				if ((bpmnmodel.getInEdges(inNode).size() > 1) && (bpmnmodel.getOutEdges(inNode).size() > 1)) {
//					cnt = cnt + 1 + calculateBackwardSeparabilty(inNode, bpmnmodel);	
//				}
//			}
//		}
//		return cnt;
//	}
	
	private static double calculateConnectorHeterogenity(BPMNDiagram bpmnmodel) {
		int numberOfXOR = 0;
		int numberOfAND = 0;
		for(Gateway g: bpmnmodel.getGateways()) {
			if(g.getGatewayType().equals(GatewayType.DATABASED)) {
				numberOfXOR++;
			}
			if(g.getGatewayType().equals(GatewayType.PARALLEL)) {
				numberOfAND++;
			}
		}
		
		if(bpmnmodel.getGateways().size() == 0) 
		{
			return 0;
		}
			
		double shareOfXOR = (double)numberOfXOR/(double)bpmnmodel.getGateways().size();
		double shareOfAND = (double)numberOfAND/(double)bpmnmodel.getGateways().size();
		
		if ((shareOfAND == 0) || (shareOfXOR == 0)) {
			return 0;
		}
		
		return -1*(shareOfXOR*Math.log(shareOfXOR)/Math.log(2)+shareOfAND*Math.log(shareOfAND)/Math.log(2));
	}
	
	private static int calculateControlflowComplexity(BPMNDiagram bpmnmodel) {
		int cfc = 0;
		for(Gateway g: bpmnmodel.getGateways()) {
			if(g.getGatewayType().equals(GatewayType.DATABASED)) {
				if(bpmnmodel.getOutEdges(g).size() > 1) {
					 cfc += bpmnmodel.getOutEdges(g).size();
				}
			}
			if(g.getGatewayType().equals(GatewayType.PARALLEL)) {
				if(bpmnmodel.getOutEdges(g).size() > 1) {
					 cfc += 1;
				}
			}
		}
		
		return cfc;
	}
	
	private static int calculateTokenSplit(BPMNDiagram bpmnmodel) {
		int tokenSplit = 0;
		for(Gateway g: bpmnmodel.getGateways()) {
			if(g.getGatewayType().equals(GatewayType.PARALLEL)) {
				if(bpmnmodel.getOutEdges(g).size() > 1) {
					tokenSplit += bpmnmodel.getOutEdges(g).size();
				}
			}
		}
		
		return tokenSplit;
	}

	private static Event retrieveEndEvent(BPMNDiagram bpmnmodel) {
		for(Event e : bpmnmodel.getEvents()) {
			if(e.getEventType().equals(Event.EventType.END)) {
				return e;
			}
		}
		
		return null;
	}
	
	private static Event retrieveStartEvent(BPMNDiagram bpmnmodel) {
		for(Event e : bpmnmodel.getEvents()) {
			if(e.getEventType().equals(Event.EventType.START)) {
				return e;
			}
		}
		
		return null;
	}
	
	private static int calculateDepth(Set<BPMNNode> visitedSplittingGataways, BPMNNode startNode,
			BPMNDiagram bpmnmodel) {
		int maxDepth = 0;
		for (BPMNEdge e : bpmnmodel.getOutEdges(startNode)) {
			BPMNNode outNode = (BPMNNode) e.getTarget();
			if ((outNode instanceof Gateway) && (bpmnmodel.getOutEdges(outNode).size() > 1)
					&& !visitedSplittingGataways.contains(outNode)) { // if splitting gateway
				visitedSplittingGataways.add(outNode);
				int newDepth = calculateDepth(visitedSplittingGataways, outNode, bpmnmodel);
				if (newDepth + 1 > maxDepth) {
					maxDepth = newDepth + 1;
				}
			} else if (!(outNode instanceof Gateway)) {
				int newDepth = calculateDepth(visitedSplittingGataways, outNode, bpmnmodel);
				if (newDepth > maxDepth) {
					maxDepth = newDepth;
				}
			}
		}
		return maxDepth;
	}
	
	private static int calculateDiameter(BPMNDiagram bpmnmodel) {
		Map<BPMNNode, Boolean> visited = new HashMap<BPMNNode, Boolean>();
		for(BPMNNode node : bpmnmodel.getNodes()) {
			visited.put(node, false);
		}
		
		BPMNNode startEvent = retrieveStartEvent(bpmnmodel);
		BPMNNode endEvent = retrieveEndEvent(bpmnmodel);
		
		return getLongestPath(startEvent, endEvent, visited, bpmnmodel);
	}
	
	private static int getLongestPath(BPMNNode n1, BPMNNode n2, Map<BPMNNode, Boolean> visited, BPMNDiagram bpmnmodel) {
		int dist, max = 0;
		visited.put(n1, true);
		
		boolean newVerticies = false;
		for(BPMNEdge e : bpmnmodel.getOutEdges(n1)){  
	    	if(!visited.get(e.getTarget())){
	            dist = 1 + getLongestPath((BPMNNode)e.getTarget(), n2, visited, bpmnmodel);
	            if(dist > max)
	                max = dist;
	            newVerticies = true;
	        }
	    }
	    if ((!newVerticies) && (!n1.equals(n2))) {
    		max = 0; 
    	}
	    
	    visited.put(n1, false);
	    return max;
	}
	
	private static double calculateCyclicity(BPMNDiagram bpmnmodel) {
		int numberOfNodesInCycle = 0;
		
		for(BPMNNode n : bpmnmodel.getNodes()) {
			if(reachableNodes(n, bpmnmodel).contains(n)) {
				numberOfNodesInCycle++;
			}
		}
		return (double)numberOfNodesInCycle/(double)bpmnmodel.getNodes().size();
	}
	
	private static Set<BPMNNode> reachableNodes(BPMNNode n, BPMNDiagram bpmnmodel) {
		
		Set<BPMNNode> reachedNodes = new HashSet<BPMNNode>();
		boolean changed = true;
		
		for(BPMNEdge e : bpmnmodel.getOutEdges(n)) {
			BPMNNode target = (BPMNNode)e.getTarget();
			reachedNodes.add(target);
		}
		
		while(changed) {
			changed = false;
			Set<BPMNNode> nodesToAdd = new HashSet<BPMNNode>();
			for(BPMNNode reachedNode : reachedNodes) {
				for(BPMNEdge e : bpmnmodel.getOutEdges(reachedNode)) {
					BPMNNode target = (BPMNNode)e.getTarget();
					if(!reachedNodes.contains(target)) {
						nodesToAdd.add(target);
						changed = true;
					}
				}
			}
			reachedNodes.addAll(nodesToAdd);
		}
		return reachedNodes;
	}
	
	private static double calculateSequentiality(BPMNDiagram bpmnmodel) {
		int numberOfSeqArcs = 0;
		
		for(BPMNEdge<? extends BPMNNode,? extends BPMNNode> e : bpmnmodel.getEdges()) {
			if ((e.getSource() instanceof Activity) && (e.getTarget() instanceof Activity)) {
				numberOfSeqArcs++;
			}
		}
		return (double)numberOfSeqArcs/(double)bpmnmodel.getEdges().size();
	}
	
	private static double calculateAvgConnectorDegree(BPMNDiagram bpmnmodel) {
		int numberOfNodes = 0;
		int numberOfArcs = 0;
		
		for(Gateway g : bpmnmodel.getGateways()) {
			numberOfNodes++;
			numberOfArcs+=bpmnmodel.getInEdges(g).size() + bpmnmodel.getOutEdges(g).size();
		}
		return (double)numberOfArcs/(double)numberOfNodes;
	}
	
	private static int calculateMaxConnectorDegree(BPMNDiagram bpmnmodel) {
		int maxConnectorDegree = 0;

		for(Gateway g : bpmnmodel.getGateways()) {
			if (maxConnectorDegree < bpmnmodel.getInEdges(g).size() + bpmnmodel.getOutEdges(g).size()) {
				maxConnectorDegree = bpmnmodel.getInEdges(g).size() + bpmnmodel.getOutEdges(g).size();
			}
		}
		return maxConnectorDegree;
	}
	
	private static int calculateNumberOfSplittingGateways(BPMNDiagram bpmnmodel) {
		int cnt = 0;
		
		for(Gateway g : bpmnmodel.getGateways()) {
			if(bpmnmodel.getOutEdges(g).size() > 1) {
				cnt++;
			}
		}
		return cnt;
	}
	
	private static void removeRedundantCyclicGateways(BPMNDiagram bpmnmodel) {
		Set<Pair<BPMNNode, BPMNNode>> pairsOfRedundantGateways = new HashSet<Pair<BPMNNode, BPMNNode>>(); 
		for (Gateway g1 : bpmnmodel.getGateways()) {
			for(BPMNEdge<? extends BPMNNode, ? extends BPMNNode> outEdge : bpmnmodel.getOutEdges(g1)) {
				BPMNNode g2 = outEdge.getTarget();
				if(g2 instanceof Gateway) {
					boolean cycle = false;
					for(BPMNEdge<? extends BPMNNode, ? extends BPMNNode> outEdgeBack : bpmnmodel.getOutEdges(g2)) {
						BPMNNode node = outEdgeBack.getTarget();
						if(node.equals(g1)) {
							cycle = true;
						}
					}
					if(cycle) {
						pairsOfRedundantGateways.add(new Pair<BPMNNode, BPMNNode>(g1,g2));
						break;
					}
				}
			}
		}
		if(pairsOfRedundantGateways.size() == 0) {
			return;
		}
			Pair<BPMNNode, BPMNNode> pair = pairsOfRedundantGateways.iterator().next();
			Set<BPMNNode> outNodes = new HashSet<BPMNNode>();
			Set<BPMNNode> inNodes = new HashSet<BPMNNode>();
			
			for(BPMNEdge<? extends BPMNNode, ? extends BPMNNode> outEdge : bpmnmodel.getOutEdges(pair.getFirst())) {
				if(!outEdge.getTarget().equals(pair.getSecond())) {
					outNodes.add(outEdge.getTarget());
				}			
			}

			
			for(BPMNEdge<? extends BPMNNode, ? extends BPMNNode> inEdge : bpmnmodel.getInEdges(pair.getFirst())) {
				if(!inEdge.getSource().equals(pair.getSecond())) {
					inNodes.add(inEdge.getSource());
				}			
			}

			bpmnmodel.removeNode(pair.getFirst());
			
			for(BPMNNode inNode : inNodes) {
				bpmnmodel.addFlow(inNode, pair.getSecond(), "");
			}
			
			for(BPMNNode outNode : outNodes) {
				bpmnmodel.addFlow(pair.getSecond(), outNode, "");
			}
			
			removeRedundantCyclicGateways(bpmnmodel);
	}
	
	
	private static boolean updateBPMNModel(BPMNDiagram bpmnmodel, Activity activity, int operation, int currentSymbol) {
		
//		Random r = new Random();
//		k = r.nextInt(alphabet.length());
//		int randomActivityNumber = r.nextInt(bpmnmodel.getActivities().size());

//		Activity nextActivity = ((ArrayList<Activity>) bpmnmodel.getActivities())
//				.get(bpmnmodel.getActivities().size() - 1);
		
		switch(operation) {
		  case 0: // SEQUANTIAL PATTERN			 
			Collection<BPMNEdge<? extends BPMNNode, ? extends BPMNNode>> outEdges = bpmnmodel.getOutEdges(activity);
			Activity newActivity = bpmnmodel.addActivity(alphabet.substring(currentSymbol, currentSymbol + 1), false, false, false, false, false);
			for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> e : outEdges) {
				bpmnmodel.removeEdge(e);
				BPMNNode outNode = (BPMNNode) e.getTarget();
				bpmnmodel.addFlow(newActivity, outNode, "");
			}
			bpmnmodel.addFlow(activity, newActivity, "");
			return true;
			
		  case 1: // CHOICE PATTERN
			outEdges = bpmnmodel.getOutEdges(activity);
			Collection<BPMNEdge<? extends BPMNNode, ? extends BPMNNode>> inEdges = bpmnmodel.getInEdges(activity);
			newActivity = bpmnmodel.addActivity(alphabet.substring(currentSymbol, currentSymbol + 1), false, false, false, false, false);
		
	
			if ((inEdges.size() == 1) && (outEdges.size() == 1)) {
				BPMNNode inNode = inEdges.iterator().next().getSource();
				BPMNNode outNode = outEdges.iterator().next().getTarget();
				if(inNode.equals(outNode) && (inNode instanceof Gateway)) {
					bpmnmodel.addFlow(newActivity, inNode, "");
					bpmnmodel.addFlow(inNode, newActivity, "");
					return true;
				}
			}
			
			Gateway parallelGateway = null;
			Gateway splitGateway = bpmnmodel.addGateway("", GatewayType.DATABASED);
			Gateway mergeGateway = bpmnmodel.addGateway("", GatewayType.DATABASED);
			if (outEdges.size() > 1) {
				parallelGateway = bpmnmodel.addGateway("", GatewayType.PARALLEL);
			}
			for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> e : inEdges) {
				bpmnmodel.removeEdge(e);
				BPMNNode inNode = (BPMNNode) e.getSource();
				bpmnmodel.addFlow(inNode, splitGateway, "");
			}
			for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> e : outEdges) {
				bpmnmodel.removeEdge(e);
				BPMNNode outNode = (BPMNNode) (e.getTarget());
				if (parallelGateway == null) {
					bpmnmodel.addFlow(mergeGateway, outNode, "");
				} else {
					bpmnmodel.addFlow(parallelGateway, outNode, "");
				}
			}
			if(parallelGateway != null) {
				bpmnmodel.addFlow(mergeGateway, parallelGateway, "");
			}
			
			bpmnmodel.addFlow(splitGateway, activity, "");
			bpmnmodel.addFlow(splitGateway, newActivity, "");
			bpmnmodel.addFlow(activity, mergeGateway, "");
			bpmnmodel.addFlow(newActivity, mergeGateway, "");
			return true;
			
		  case 2: // PARALLEL PATTERN
			outEdges = bpmnmodel.getOutEdges(activity);
			inEdges = bpmnmodel.getInEdges(activity);
			newActivity = bpmnmodel.addActivity(alphabet.substring(currentSymbol, currentSymbol + 1), false, false, false, false, false);
			splitGateway = bpmnmodel.addGateway("", GatewayType.PARALLEL);
			mergeGateway = bpmnmodel.addGateway("", GatewayType.PARALLEL);
			Gateway exclusiveGateway = null;
			if (inEdges.size() > 1) {
				exclusiveGateway = bpmnmodel.addGateway("", GatewayType.DATABASED);
			}
			for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> e : inEdges) {
				bpmnmodel.removeEdge(e);
				BPMNNode inNode = (BPMNNode) e.getSource();
				if (exclusiveGateway == null) {
					bpmnmodel.addFlow(inNode, splitGateway, "");
				} else {
					bpmnmodel.addFlow(inNode, exclusiveGateway, "");
				}
			}
			if(exclusiveGateway != null) {
				bpmnmodel.addFlow(exclusiveGateway, splitGateway, "");
			}
			for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> e : outEdges) {
				bpmnmodel.removeEdge(e);
				BPMNNode outNode = (BPMNNode) (e.getTarget());
				bpmnmodel.addFlow(mergeGateway, outNode, "");
			}
			bpmnmodel.addFlow(splitGateway, activity, "");
			bpmnmodel.addFlow(splitGateway, newActivity, "");
			bpmnmodel.addFlow(activity, mergeGateway, "");
			bpmnmodel.addFlow(newActivity, mergeGateway, "");
			return true;
			
		  case 3: // LOOP PATTERN
//			outEdges = bpmnmodel.getOutEdges(activity);
//			inEdges = bpmnmodel.getInEdges(activity);
//			//newActivity = bpmnmodel.addActivity(alphabet.charAt(j) + "", false, false, false, false, false);
//			Gateway xorGateway = bpmnmodel.addGateway("", GatewayType.DATABASED);
//			//mergeGateway = bpmnmodel.addGateway("", GatewayType.DATABASED);
//			parallelGateway = null;
//			if (outEdges.size() > 1) {
//				parallelGateway = bpmnmodel.addGateway("", GatewayType.PARALLEL);
//			}
//			for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> e : inEdges) {
//				bpmnmodel.removeEdge(e);
//				BPMNNode inNode = (BPMNNode) e.getSource();
//				bpmnmodel.addFlow(inNode, xorGateway, "");
//			}
//			for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> e : outEdges) {
//				bpmnmodel.removeEdge(e);
//				BPMNNode outNode = (BPMNNode) (e.getTarget());
//				if (parallelGateway == null) {
//					bpmnmodel.addFlow(xorGateway, outNode, "");
//				} else {
//					bpmnmodel.addFlow(parallelGateway, outNode, "");
//				}
//			}
//			if(parallelGateway != null) {
//				bpmnmodel.addFlow(xorGateway, parallelGateway, "");
//			}
//			bpmnmodel.addFlow(xorGateway, activity, "");
//			//bpmnmodel.addFlow(newActivity, splitGateway, "");
//			bpmnmodel.addFlow(activity, xorGateway, "");
//			//bpmnmodel.addFlow(mergeGateway, newActivity, "");
//			//bpmnmodel.addFlow(splitGateway, mergeGateway, "");
//			//bpmnmodel.addFlow(mergeGateway, splitGateway, "");
//			return false;
				outEdges = bpmnmodel.getOutEdges(activity);
				inEdges = bpmnmodel.getInEdges(activity);
				
				parallelGateway = null;
				splitGateway = bpmnmodel.addGateway("", GatewayType.DATABASED);
				mergeGateway = bpmnmodel.addGateway("", GatewayType.DATABASED);
				if (outEdges.size() > 1) {
					parallelGateway = bpmnmodel.addGateway("", GatewayType.PARALLEL);
				}
				for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> e : inEdges) {
					bpmnmodel.removeEdge(e);
					BPMNNode inNode = (BPMNNode) e.getSource();
					bpmnmodel.addFlow(inNode, splitGateway, "");
				}
				for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> e : outEdges) {
					bpmnmodel.removeEdge(e);
					BPMNNode outNode = (BPMNNode) (e.getTarget());
					if (parallelGateway == null) {
						bpmnmodel.addFlow(mergeGateway, outNode, "");
					} else {
						bpmnmodel.addFlow(parallelGateway, outNode, "");
					}
				}
				if(parallelGateway != null) {
					bpmnmodel.addFlow(mergeGateway, parallelGateway, "");
				}
				
				bpmnmodel.addFlow(splitGateway, activity, "");
				bpmnmodel.addFlow(activity, mergeGateway, "");
				//if(splitGateway.equals(mergeGateway)) {
					//bpmnmodel.addFlow(splitGateway, mergeGateway, "");
					bpmnmodel.addFlow(mergeGateway, splitGateway, "");
				//}

				System.out.println("Model number " + bpmnmodel.getLabel());
				System.out.println("Cycle! ");
				return false;
		
		  case 4: // SKIP PATTERN
				outEdges = bpmnmodel.getOutEdges(activity);
				inEdges = bpmnmodel.getInEdges(activity);
				//newActivity = bpmnmodel.addActivity("tau", false, false, false, false, false);
			
		
//				if ((inEdges.size() == 1) && (outEdges.size() == 1)) {
//					BPMNNode inNode = inEdges.iterator().next().getSource();
//					BPMNNode outNode = outEdges.iterator().next().getTarget();
//					if(inNode.equals(outNode) && (inNode instanceof Gateway)) {
//						bpmnmodel.addFlow(newActivity, inNode, "");
//						bpmnmodel.addFlow(inNode, newActivity, "");
//						return false;
//					}
//				}
				
				parallelGateway = null;
				splitGateway = bpmnmodel.addGateway("", GatewayType.DATABASED);
				mergeGateway = bpmnmodel.addGateway("", GatewayType.DATABASED);
				if (outEdges.size() > 1) {
					parallelGateway = bpmnmodel.addGateway("", GatewayType.PARALLEL);
				}
				for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> e : inEdges) {
					bpmnmodel.removeEdge(e);
					BPMNNode inNode = (BPMNNode) e.getSource();
					bpmnmodel.addFlow(inNode, splitGateway, "");
				}
				for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> e : outEdges) {
					bpmnmodel.removeEdge(e);
					BPMNNode outNode = (BPMNNode) (e.getTarget());
					if (parallelGateway == null) {
						bpmnmodel.addFlow(mergeGateway, outNode, "");
					} else {
						bpmnmodel.addFlow(parallelGateway, outNode, "");
					}
				}
				if(parallelGateway != null) {
					bpmnmodel.addFlow(mergeGateway, parallelGateway, "");
				}
				
				bpmnmodel.addFlow(splitGateway, activity, "");
				bpmnmodel.addFlow(activity, mergeGateway, "");
				//if(splitGateway.equals(mergeGateway)) {
					bpmnmodel.addFlow(splitGateway, mergeGateway, "");
				//}

				return false;
		}
		
		return false;
	}
	
	private static double calculateEntropyOfBPMNModel(BPMNDiagram bpmnmodel) {
		
		// Convert to a Petri net
		BPMN2PetriNetConverter_Configuration converterConfig = new BPMN2PetriNetConverter_Configuration();
		converterConfig.labelNodesWith = LabelValue.ORIGINAL_LABEL;
		BPMN2PetriNetConverter converter = new BPMN2PetriNetConverter(bpmnmodel, converterConfig);
		converter.convert();
		Petrinet  petriNet = converter.getPetriNet();
		
		NetSystem ns = convertPNtoNS(petriNet);
		
		// Construct automaton
		Automaton a = constructAutomatonFromNetSystem(ns);
		
		try {
			Files.write(Paths.get(
					bpmnmodel.getLabel() + ".dot"),
					a.toDot().getBytes(),
					StandardOpenOption.CREATE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println(a);
		// Calculate entropy
		double entropy = MetricsCalculator.calculateEntropy(a, "", false, false, 0);
		
		return entropy;
	}
	
	
	private static NetSystem convertPNtoNS(Petrinet petriNet) {
		
		NetSystem ns = new NetSystem();
		
		Map<Place, org.jbpt.petri.Place> mapOfPlaces = new HashMap<Place, org.jbpt.petri.Place>();
		Map<org.processmining.models.graphbased.directed.petrinet.elements.Transition, org.jbpt.petri.Transition> mapOfTransitions
			= new HashMap<org.processmining.models.graphbased.directed.petrinet.elements.Transition, org.jbpt.petri.Transition>();
		
		for(org.processmining.models.graphbased.directed.petrinet.elements.Transition t : petriNet.getTransitions()) {
			org.jbpt.petri.Transition newTransition = new org.jbpt.petri.Transition(t.getLabel());
			ns.addTransition(newTransition);
			mapOfTransitions.put(t, newTransition);
			if ((t.getLabel().length() > 1) || (!alphabet.contains(t.getLabel()))){
				newTransition.setLabel("");
			}
		}

		for(Place p : petriNet.getPlaces()) {
			org.jbpt.petri.Place newPlace = new org.jbpt.petri.Place(p.getLabel());
			ns.addPlace(newPlace);
			mapOfPlaces.put(p, newPlace);
		}
		
		for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e : petriNet.getEdges()) {
			if(e.getSource() instanceof org.processmining.models.graphbased.directed.petrinet.elements.Transition) {
				ns.addFlow(mapOfTransitions.get(e.getSource()), mapOfPlaces.get(e.getTarget()));
			}
			if(e.getSource() instanceof Place) {
				ns.addFlow(mapOfPlaces.get(e.getSource()), mapOfTransitions.get(e.getTarget()));
			}
		}
		
		return ns;
	}
	
	
	private static Automaton constructAutomatonFromNetSystem(NetSystem ns) {
		Map<Collection<org.jbpt.petri.Place>, State> markingToState = new HashMap<Collection<org.jbpt.petri.Place>, State>();
		Set<Collection<org.jbpt.petri.Place>> unprocessedMarkings = new HashSet<Collection<org.jbpt.petri.Place>>();
		
		HashingStrategy<String> strategy = new HashingStrategy<String>() {

			public int computeHashCode(String object) {
				return object.hashCode();
			}

			public boolean equals(String o1, String o2) {
				return o1.equals(o2);
			}
		};
		TObjectShortMap<String> activity2short = new TObjectShortCustomHashMap<String>(strategy, 10, 0.5f, (short) -1);
		
		Automaton a = new Automaton();
		boolean containsTauLabels = false;
		
		// Construct initial state
		Collection<org.jbpt.petri.Place> initialMarking = deriveInitalMarking(ns);
		
		// Derive final marking
		Collection<org.jbpt.petri.Place> finalMarking = deriveFinalMarking(ns);

		State initialState = new State();
		markingToState.put(initialMarking, initialState);
		a.setInitialState(initialState);
		unprocessedMarkings.add(initialMarking);
		
		// Pair of states connected by tau
		Set<StatePair> tauPairs = new HashSet<StatePair>();
		
		// Construct other states
		while (!unprocessedMarkings.isEmpty()) {
			Collection<org.jbpt.petri.Place> curMarking = unprocessedMarkings.iterator().next();
			Set<org.jbpt.petri.Transition> enabledTransitions = retrieveEnabledTransitions(curMarking, ns);

			for (org.jbpt.petri.Transition enabeledTransition : enabledTransitions) {

				Marking marking = new Marking(ns);
				for (org.jbpt.petri.Place place : marking.getPetriNet().getPlaces()) {
					marking.put(place, 0);
				}
				
				for (org.jbpt.petri.Place place : curMarking) {
//					int i = 0;
//					if(marking.get(place) != null) {
//						i = marking.get(place);
//					}
//					i++;
					marking.put(place, 1);
				}
				ns.loadMarking(marking);

				ns.fire(enabeledTransition);

				Collection<org.jbpt.petri.Place> newMarking = ns.getMarking().toMultiSet();
				State curState = markingToState.get(curMarking);
				State newState = markingToState.get(newMarking);

				if (newState == null) {
					newState = new State();
					markingToState.put(newMarking, newState);
					unprocessedMarkings.add(newMarking);

					if (newMarking.containsAll(finalMarking) && finalMarking.containsAll(newMarking)) {
						newState.setAccept(true);
					}
				}
							
				char c = (char) Integer.valueOf(enabeledTransition.getLabel().hashCode()).shortValue();
				
				// If string is empty (silent)
				if (c == '\u0000') {
					System.out.println(a.getStates());
					System.out.println("Adding silent");
					tauPairs.add(new StatePair(curState, newState));
				} else {
					System.out.println(a.getStates());
					System.out.println("Adding non-silent");
					if(enabeledTransition.getLabel().contains("tau")) {
						containsTauLabels = true;
					}
					activity2short.putIfAbsent(enabeledTransition.getLabel(), (short) activity2short.size());
					dk.brics.automaton.Transition t = new dk.brics.automaton.Transition((char)activity2short.get(enabeledTransition.getLabel()), newState);
					curState.addTransition(t);
				}

			}
			unprocessedMarkings.remove(curMarking);
		}
		
		if(containsTauLabels) {
			System.out.println("Note that some transitions contain labels with tau. These transitions will not be considered as silent. "
					+ "To make a transition silent please set an empty label.");
		}
		System.out.println(a);
		a.addEpsilons(tauPairs);
		a.determinize();
		a.minimize();
		return a;
	}
	
	
//	private static Automaton removeSilentTransitons(Automaton a) {
//		
//		Automaton result = new Automaton();
//		Map<State, Set<State>> mapOfStates = new HashMap<State, Set<State>>();
//		State initState = a.getInitialState();
//		Set<State> initClosure = constructClosure(initState);
//		State newInit = new State();
//		result.setInitialState(newInit);
//		
//		mapOfStates.put(newInit, initClosure);
//		
//		
//		return result;
//	}
//	
//	private static void expandState(State s, Map<State, Set<State>> mapOfStates) {
//		Map<Integer, Set<State>> mapOfTransitions = new HashMap();
//		
//		for(State innerState : mapOfStates.get(s)) {
//			for(dk.brics.automaton.Transition t : s.getTransitions()) {
//				if((t.getMax() > '\u0000') || (t.getMin() < '\u0000')) {
//					for(char c=t.getMin() ; c<= t.getMax(); c++) {
//						Set<State> setOfDest = mapOfTransitions.get(new Integer(c));
//						if(setOfDest == null) {
//							setOfDest = new HashSet<State>();
//						}
//						setOfDest.add(t.getDest());
//					}
//				}
//			}
//		}
//		
//		for(Integer cnt : mapOfTransitions.keySet()) {
//			//...
//		}
//		
//	}
	
	private static Set<State> constructClosure(State s) {
		
		Set<State> result = new HashSet<State>();
		for(dk.brics.automaton.Transition t : s.getTransitions()) {
			if((t.getMax() == '\u0000') && (t.getMin() == '\u0000')) {
				if(!result.contains(t.getDest())) {
					result.add(t.getDest());
					result.addAll(constructClosure(t.getDest()));
				}
			}
		}
		
		return result;
	}
	
	private static Collection<org.jbpt.petri.Place> deriveFinalMarking(NetSystem ns) {

		Collection<org.jbpt.petri.Place> finalMarking = new HashSet<org.jbpt.petri.Place>();
		if (ns != null) {
			for (org.jbpt.petri.Place place : ns.getPlaces()) {
				Collection<Node> successors = ns.getDirectSuccessors(place);
				if ((successors == null) || (successors.size() == 0)) {
					finalMarking.add(place);
				} 
			}
		}
		return finalMarking;
	}
	
	private static Collection<org.jbpt.petri.Place> deriveInitalMarking(NetSystem ns) {

		Collection<org.jbpt.petri.Place> initialMarking = new HashSet<org.jbpt.petri.Place>();
		if (ns != null) {
			for (org.jbpt.petri.Place place : ns.getPlaces()) {
				Collection<Node> predecessors = ns.getDirectPredecessors(place);
				if ((predecessors == null) || (predecessors.size() == 0)) {
					initialMarking.add(place);
				} 
			}
		}
		return initialMarking;
	}
	
	private static Set<org.jbpt.petri.Transition> retrieveEnabledTransitions(Collection<org.jbpt.petri.Place> marking, NetSystem net) {

		Set<org.jbpt.petri.Transition> enabledTransitions = new HashSet<org.jbpt.petri.Transition>();

		IPetriNet<Flow, Node, org.jbpt.petri.Place, org.jbpt.petri.Transition> petriNet = net;
		for (org.jbpt.petri.Transition transition : petriNet.getTransitions()) {
			boolean isEnabled = true;
			for (Flow inFlow : petriNet.getIncomingEdges(transition)) {
				org.jbpt.petri.Place inPlace = (org.jbpt.petri.Place)inFlow.getSource();
				if (!marking.contains(inPlace)) {
					isEnabled = false;
					break;
				}
			}
			if(isEnabled) {
				enabledTransitions.add(transition);
			}
		}
		return enabledTransitions;
	}
}
