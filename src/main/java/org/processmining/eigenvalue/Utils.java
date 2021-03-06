/**
 *
 *  Copyright 2019 by Anna Kalenkova <anna.kalenkova@unimelb.edu.au>
 *  Copyright 2018 by Andreas Solti <solti@ai.wu.ac.at>
 *
 *  Licensed under GNU General Public License 3.0 or later. 
 *  Some rights reserved. See COPYING, AUTHORS.
 *
 * @license GPL-3.0+ <http://spdx.org/licenses/GPL-3.0+>
 */

package org.processmining.eigenvalue;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.*;
import org.jgraph.JGraph;
import org.processmining.eigenvalue.converter.RelaxedPT2PetrinetConverter;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.petrinets.analysis.gedsim.utils.StringEditDistance;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMi;
import org.processmining.plugins.InductiveMiner.plugins.IMProcessTree;
import org.processmining.plugins.stochasticpetrinet.StochasticNetUtils;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.visualization.tree.TreeLayoutBuilder;
import org.progressmining.xeslite.plugin.OpenLogFileLiteImplPlugin;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.StatePair;
import dk.brics.automaton.Transition;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Utils {
	
	public static final String PRECISION_MEASURE = "Precision";
	public static final String GENERALIZATION_MEASURE = "Generalization";
	public static final char START = '@';


	public static final String TEST_FOLDER = "test/testfiles/";

	public static final ProMCanceller NOT_CANCELLER = new ProMCanceller() {
		@Override
		public boolean isCancelled() {
			return false;
		}
	};

	private static final String[] nameAlphabet = new String[]{"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","v","x","y","z","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","0","1","2","3","4","5","6","7","8","9"};
	

	public static ProcessTree mineProcessTree(XLog inputLog, double noiseThreshold) {
		MiningParameters parameters = new MiningParametersIMi();
		parameters.setNoiseThreshold((float)noiseThreshold);
		ProcessTree tree = IMProcessTree.mineProcessTree(inputLog, parameters);
		RelaxedPT2PetrinetConverter.postProcessMinedTree(tree);
		return tree;
	}
	
	public static XLog cloneLog(XLog log){
		XLog logClone = XFactoryRegistry.instance().currentDefault().createLog((XAttributeMap) log.getAttributes().clone());
		for (XTrace trace : log){
			XTrace traceClone = XFactoryRegistry.instance().currentDefault().createTrace((XAttributeMap) trace.getAttributes().clone());
			for (XEvent event : trace){
				XEvent eventClone = XFactoryRegistry.instance().currentDefault().createEvent((XAttributeMap) event.getAttributes().clone());
				traceClone.add(eventClone);
			}
			logClone.add(traceClone);
		}
		return logClone;
	}
	
	
	public static XLog flattenLifecycles(XLog log) {
		XLog merged = XFactoryRegistry.instance().currentDefault().createLog(log.getAttributes());
		for (XTrace trace : log){
			XTrace newTrace =  XFactoryRegistry.instance().currentDefault().createTrace(trace.getAttributes());
			for (XEvent event : trace){
				XEvent newEvent = XFactoryRegistry.instance().currentDefault().createEvent(event.getAttributes());
				String name = XConceptExtension.instance().extractName(event);
				String lc = XLifecycleExtension.instance().extractTransition(event);
				XConceptExtension.instance().assignName(newEvent, name+"_"+lc);
				newTrace.add(newEvent);
			}
			merged.add(newTrace);
		}
		return merged;
	}

	
	public static JGraph getGraphForTree(ProcessTree tree) {
		TreeLayoutBuilder builder = new TreeLayoutBuilder(tree);
		JGraph graph = builder.getJGraph();
		graph.setPreferredSize(new Dimension(1200,500));
		return graph;
	}
	
	public static Double getTraceSimilarity(XLog inputLog, XLog resultLog) {
		int numEventsDifferent = 0;
		int allEvents = 0;
		if (inputLog.size() != resultLog.size()){
			throw new IllegalArgumentException("Logs must have equal number of traces!");
		}
		Map<String,String> eventEncoding = new HashMap<>();
		for (int trIndex = 0 ; trIndex < inputLog.size(); trIndex++){
			XTrace trInput = inputLog.get(trIndex);
			XTrace trResult = resultLog.get(trIndex);
			String inputString = getTraceString(trInput, eventEncoding);
			String resultString = getTraceString(trResult, eventEncoding);
			allEvents += inputString.length();
			allEvents += resultString.length();
			numEventsDifferent += StringEditDistance.editDistance(inputString, resultString);
		}
		return 1 - (numEventsDifferent / (double)allEvents);
	}
	
	
	public static String numberOfTransitions(Automaton a) {
	    	int c = 0;
	    	for (State s : a.getStates()) {
	    		for (Transition t : s.getTransitions()) {
	    			char max = t.getMax();
	    			char min = t.getMin();
	    			int numberOfChars = max - min + 1;
	    			c += numberOfChars;
	    		}
	    	}
			return Integer.toString(c);
	}	    
	
	private static String getTraceString(XTrace trInput, Map<String, String> eventEncoding) {
		StringBuffer buf = new StringBuffer();
		for (XEvent e : trInput){
			String evName = XConceptExtension.instance().extractName(e);
			if (!eventEncoding.containsKey(evName)){
				eventEncoding.put(evName, nameAlphabet[eventEncoding.size()]);
			}
			buf.append(eventEncoding.get(evName));
		}
		return buf.toString();
	}

    /**
     * Extracts a name of an object (e.g. XLog) and returns the parameter defaultName if the name is not set
     * @param object {@link XAttributable} some object that potentially has a name
     * @param defaultName String default name
     * @return String
     */
	public static String getName(XAttributable object, String defaultName){
        return getOrDefault(XConceptExtension.instance().extractName(object), defaultName);
    }
    public static String getName(PetrinetGraph net, String defaultName){
	    return getOrDefault(net.getLabel(), defaultName);
    }
    private static String getOrDefault(String name, String defaultName) {
        if (name == null || name.trim().isEmpty()){
            name = defaultName;
        }
        return name;
    }



	/**
	 * Loads a log from the tests/testfiles folder of the plugin.
	 *
	 * @param name the file name (including the suffix such as .xes, .xes.gz, or .mxml)
	 * @return the loaded {@link XLog}
	 * @throws Exception
	 */
	public static XLog loadLog(String name) throws Exception {
		return loadLog(new File(TEST_FOLDER+name));
	}

	/**
	 * Opens a Log from a given file.
	 *
	 * @param file {@link File} containing the log.
	 * @return the loaded {@link XLog}
	 * @throws Exception
	 */
	public static XLog loadLog(File file) throws Exception {
		OpenLogFileLiteImplPlugin openPlugin = new OpenLogFileLiteImplPlugin();
		return (XLog) openPlugin.importFile(StochasticNetUtils.getDummyUIContext(), file);
	}
	
	/**
	 * Adding silent transitions to automaton
	 * 
	 * @param a
	 */
	public static void addTau(Automaton a) {
    	
    	Set<StatePair> pairs = new HashSet<StatePair>();
    	for (dk.brics.automaton.State s : a.getStates()) {
        	for (dk.brics.automaton.Transition t : s.getTransitions()) {
        		pairs.add(new StatePair(s, t.getDest()));
        	}
        	
        }
        a.addEpsilons(pairs);
    }
	
	/**
	 * Constructing short array from byte array
	 * 
	 * @param byteArray
	 * @return
	 */
	public static short[] toShortArray(byte[] byteArray) {
		
		int size = byteArray.length;
		short[] shortArray = new short[size];

		for (int index = 0; index < size; index++) {
		    shortArray[index] = (short) byteArray[index];
		}
		
		return shortArray;
	}
	
	public static Automaton unionWithStart (Automaton a1, Automaton a2) {
		
		
		State initState1 = a1.getInitialState();
		State initState2 = a2.getInitialState();
		
		Transition t1 = new Transition(START, initState1);
		if (initState2 == null) {
			State s = new State();
			s.addTransition(t1);
			a2.setInitialState(s);
			
		} else {
			initState2.addTransition(t1);

		}
		return a2;
	}
	
	public static Automaton removeStart(Automaton a) {
		
		Collection<StatePair> newEpsilons = new HashSet<StatePair>();
		State newStart = new State();
		
		for (State s : a.getStates()) {
			for (Transition t : s.getTransitions()) {
				if ((t.getMin() == START) && (t.getMax() == START)) {
					newEpsilons.add(new StatePair(newStart, t.getDest()));
				}
			}
		}
		
		a.setInitialState(newStart);
		a.addEpsilons(newEpsilons);
		return a;
	}
	

	public static Automaton union (Automaton a1, Automaton a2) {
		
		State initState1 = a1.getInitialState();
		State initState2 = a2.getInitialState();
		
		State s = new State();
	
		
		StatePair pair1 = new StatePair(s, initState1);
		StatePair pair2 = new StatePair(s, initState2);
		Set<StatePair> setEpsilons = new HashSet<StatePair>();
		setEpsilons.add(pair1);
		setEpsilons.add(pair2);
		
		a2.setInitialState(s);
		a2.addEpsilons(setEpsilons);
		
		return a2;
	}
	
	
	
	public static Automaton skipKSteps(Automaton a, int k) {
		
		Collection<StatePair> newEpsilons = new HashSet<StatePair>();
		
		Automaton[] allAutomata = new Automaton[k+1];
		
		for(int i=0; i<=k; i++) {
			allAutomata[i] = a.clone();
			if (k-i > 0) {
				addAcceptingStates(allAutomata[i], allAutomata[i].getAcceptStates(), k-i);
			}
			
			//System.out.println("Automata: [" + i + "] " + allAutomata[i]);
			
			
			if(i > 0) {
				for (State s : allAutomata[i-1].getStates()) {
					for (Transition outTransition : s.getTransitions()) {
						State outState = outTransition.getDest();
						State nextState = findStateWithNumber(allAutomata[i], outState.getNumber());
						newEpsilons.add(new StatePair(s, nextState));
						//System.out.println("Adding new pair: " + s + " and " + nextState);
					}
				}
			}
		}
		
		Automaton resultAutomaton = new Automaton();
		for(int i=0; i<=k; i++) {
			Utils.unionWithStart(allAutomata[i], resultAutomaton);
		}
		
		//System.out.println("Result automaton before epsilons " + resultAutomaton);
		resultAutomaton.addEpsilons(newEpsilons);
		//System.out.println("Result automaton after epsilons " + resultAutomaton);
			
		
		
		resultAutomaton.determinize();
		resultAutomaton.minimize();
		removeStart(resultAutomaton);
		
		//System.out.println("Result automaton " + resultAutomaton);
		return resultAutomaton;
	}
	
	
	private static void addAcceptingStates (Automaton a, Set<State> startStates, int numberOfSkips) {
		
		Set<State> nextAcceptingStates = new HashSet<State>();
		
		for(State s: a.getStates()) {
			for (Transition outTransition : s.getTransitions()) {
				State outState = outTransition.getDest();
				if(startStates.contains(outState)) {
					nextAcceptingStates.add(s);
				}
			}
		}
		
		for(State s: nextAcceptingStates) {
			s.setAccept(true);
		}
		
		if(numberOfSkips > 1) {
			addAcceptingStates(a, nextAcceptingStates, numberOfSkips - 1);
		}
	}
	
	private static State findStateWithNumber(Automaton a, int number) {
		
		for(State s : a.getStates()) {
			if(s.getNumber() == number) {
				return s;
			}
		}
		
		return null;
	}
	
//	public static Automaton expandTree(Automaton a) {
//		for(State s: a.getStates()) {
//			Set<Transition> inTransitions = getInEdges(s, a);
//			if(inTransitions.size() > 1) {
//				for(Transition t : inTransitions) {
//					State sNew = new State();
//					
//					
//				}
//			}
//		}
//	}
//	
//	public static Set<State> getOutStates(State s) {
//		Set<State> outStates = new HashSet<State>();
//		for()
//		
//	}
	
	public static Set<Transition> getInEdges(State s, Automaton a) {
		Set<Transition> result = new HashSet<Transition>();
		
		for(State sNew : a.getStates()) {
			for(Transition t : sNew.getTransitions()) {
				if(t.getDest().equals(s)) {
					result.add(t);
				}
			}
		}
		
		return result;
	}
	
	public static String singlePlural(int count, String singular, String plural)
	{
	  return count==1 ? singular : plural;
	}
}
