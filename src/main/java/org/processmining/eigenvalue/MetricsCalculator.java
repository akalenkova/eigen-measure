/**
 *
 *  Copyright 2019 by Anna Kalenkova <anna.kalenkova@unimelb.edu.au>
 *
 *  Licensed under GNU General Public License 3.0 or later. 
 *  Some rights reserved. See COPYING, AUTHORS.
 *
 * @license GPL-3.0+ <http://spdx.org/licenses/GPL-3.0+>
 */

package org.processmining.eigenvalue;


import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.processmining.eigenvalue.automata.TopologicalEntropyComputer;
import org.processmining.eigenvalue.data.EntropyResult;
import org.processmining.framework.plugin.ProMCanceller;

import dk.brics.automaton2.Automaton;
import dk.brics.automaton2.State;
import dk.brics.automaton2.Transition;

public class MetricsCalculator {
	
	
	public static void main(String[] args) {
		
	}
	
	/**
	 * Calculates recall and precision for the given automata
	 * 
	 * @param aM model
	 * @param aL log
	 * @param bTau flag indicating whether tau steps are to be added
	 * @param bEfficient flag indicating whether the values are to be computed efficiently
	 * @return returns a pair (recall, precision)
	 */
	public static Pair<Double, Double> calculate(Automaton aM, String mName, Automaton aL, String lName, boolean bTau,
			boolean bEfficient) {

		if(bTau) {	
			boolean infiniteM = false; // the relevant behavior contains infinite number of traces
			boolean infiniteL = false; // the retrieved behavior contains infinite number of traces
			// ------------------Efficient computation-------------------
			if(bEfficient) {
				
				// ------------------Retrieved automaton--------------------------------
				System.out.println(String.format(
						"Efficiently constructing deterministic minimal automaton for retrieved traces with tau"));
				long start = System.currentTimeMillis();
				Set<String> retrievedTraces = aL.getFiniteStrings();
				
				if (retrievedTraces != null) {
					Automaton a = new Automaton();
					int cnt = 0;
					System.out.println("Size " + retrievedTraces.size());
					for (String trace : retrievedTraces) {
						System.out.println(cnt);
						cnt++;
						Automaton aTmp = Automaton.makeString(trace);
						Utils.addTau(aTmp);
						aTmp.determinize(ProMCanceller.NEVER_CANCEL);
						aTmp.minimize(ProMCanceller.NEVER_CANCEL);
						a = a.union(aTmp, ProMCanceller.NEVER_CANCEL);
						a.determinize(ProMCanceller.NEVER_CANCEL);
						a.minimize(ProMCanceller.NEVER_CANCEL);
					}
					aL = a.clone();
					long time = System.currentTimeMillis() - start;
					System.out.println(
							String.format("Efficient construction of retrieved automaton with tau took  %s ms.", time));
					System.out.println(String.format("The number of states:                                        %s",
							aL.getNumberOfStates()));
					System.out.println(String.format("The number of transitions:                                   %s",
							numberOfTransitions(aL)));
				} else {
					System.out.println(String.format(
							"Retrived automaton accepts infinite number of traces. Efficient algorithm cannot be applied."));
					infiniteL = true;
				}
				// ------------------Relevant automaton--------------------------------
				System.out.println(String.format(
						"Efficiently constructing deterministic minimal automaton for relevant traces with tau."));
				start = System.currentTimeMillis();
				Set<String> relevantTraces = aM.getFiniteStrings();
				if (relevantTraces != null) {
					Automaton a = new Automaton();
					for (String trace : relevantTraces) {
						Automaton aTmp = Automaton.makeString(trace);
						Utils.addTau(aTmp);
						aTmp.determinize(ProMCanceller.NEVER_CANCEL);
						aTmp.minimize(ProMCanceller.NEVER_CANCEL);
						a = a.union(aTmp, ProMCanceller.NEVER_CANCEL);
						a.determinize(ProMCanceller.NEVER_CANCEL);
						a.minimize(ProMCanceller.NEVER_CANCEL);
					}
					aM = a.clone();
					long time = System.currentTimeMillis() - start;
					System.out.println(
							String.format("Efficient construction of relevant automaton with tau took   %s ms.", time));
					System.out.println(String.format("The number of states:                                        %s",
							aM.getNumberOfStates()));
					System.out.println(String.format("The number of transitions:                                   %s",
							numberOfTransitions(aM)));
				} else {
					System.out.println(String.format(
							"Relevant automaton accepts infinite number of traces. Efficient algorithm cannot be applied."));
					infiniteM = true;
				}
				
			} 
		
			if (!bEfficient || infiniteL) {
				// ------------------Adding tau to retrieved automaton-------------------
				System.out.println("Starting preliminary minimization of retrieved automaton with tau");
				long start = System.currentTimeMillis();
				aL.minimize(ProMCanceller.NEVER_CANCEL);
				long time = System.currentTimeMillis() - start;
				System.out.println(
						String.format("The retrieved automaton with tau is minimized in             %s ms.", time));
				System.out.println(String.format("The number of states:                                        %s", aL.getNumberOfStates()));
				System.out.println(String.format("The number of transitions:                                   %s", numberOfTransitions(aL)));
				
				Utils.addTau(aL);
				
				// Determinization of automaton
				System.out.println("Starting determinization of retrieved automaton with tau");
				start = System.currentTimeMillis();
				aL.determinize(ProMCanceller.NEVER_CANCEL);
				time = System.currentTimeMillis() - start;
				System.out.println(
						String.format("The retrieved automaton with tau is determinized in          %s ms.", time));
				System.out.println(String.format("The number of states:                                        %s", aL.getNumberOfStates()));
				System.out.println(String.format("The number of transitions:                                   %s", numberOfTransitions(aL)));

				// Minimization of automaton
				System.out.println("Starting minimization of retrieved automaton with tau");
				start = System.currentTimeMillis();
				aL.minimize(ProMCanceller.NEVER_CANCEL);
				time = System.currentTimeMillis() - start;
				System.out.println(
						String.format("The retrieved automaton with tau is minimized in             %s ms.", time));
				System.out.println(String.format("The number of states:                                        %s", aL.getNumberOfStates()));
				System.out.println(String.format("The number of transitions:                                   %s", numberOfTransitions(aL)));
			}
			
			if (!bEfficient || infiniteM) {
				// ------------------Adding tau to relevant automaton-------------------
				// Minimization of automaton
				System.out.println("Starting preliminary minimization of relevant automaton with tau");
				long start = System.currentTimeMillis();
				aM.minimize(ProMCanceller.NEVER_CANCEL);
				long time = System.currentTimeMillis() - start;
				System.out.println(
						String.format("The relevant automaton with tau is minimized in              %s ms.", time));
				System.out.println(String.format("The number of states:                                        %s", aM.getNumberOfStates()));
				System.out.println(String.format("The number of transitions:                                   %s", numberOfTransitions(aM)));

				Utils.addTau(aM);
				
				// Determinization of automaton
				System.out.println("Starting determinization of relevant automaton with tau");
				start = System.currentTimeMillis();
				aM.determinize(ProMCanceller.NEVER_CANCEL);
				time = System.currentTimeMillis() - start;
				System.out.println(
						String.format("The relevant automaton with tau is determinized in           %s ms.", time));
				System.out.println(String.format("The number of states:                                        %s", aM.getNumberOfStates()));
				System.out.println(String.format("The number of transitions:                                   %s", numberOfTransitions(aM)));
				
				// Minimization of automaton
				System.out.println("Starting minimization of relevant automaton with tau");
				start = System.currentTimeMillis();
				aM.minimize(ProMCanceller.NEVER_CANCEL);
				time = System.currentTimeMillis() - start;
				System.out.println(
						String.format("The relevant automaton with tau is minimized in              %s ms.", time));
				System.out.println(String.format("The number of states:                                        %s", aM.getNumberOfStates()));
				System.out.println(String.format("The number of transitions:                                   %s", numberOfTransitions(aM)));
			}
		}
		
		Automaton aLM = aM.intersection(aL, ProMCanceller.NEVER_CANCEL);
		aLM.minimize(ProMCanceller.NEVER_CANCEL);
		System.out.println(String.format("The number of states in intersection:                        %s", aLM.getNumberOfStates()));
		System.out.println(String.format("The number of transitions in intersection:                   %s", numberOfTransitions(aLM)));
		

		EntropyResult resultL = TopologicalEntropyComputer.getTopologicalEntropy(aL, lName);
		EntropyResult resultM = TopologicalEntropyComputer.getTopologicalEntropy(aM, mName);		
		EntropyResult resultLM = TopologicalEntropyComputer.getTopologicalEntropy(aLM, "intersection of " + mName + " and " + lName);
		
		double recall = resultLM.largestEigenvalue / resultL.largestEigenvalue;
		double precision = resultLM.largestEigenvalue / resultM.largestEigenvalue;
		
		return new Pair<Double, Double>(recall, precision);
	}
	
	/**
	 * Calculates entropy of the given automaton
	 * 
	 * @param model      automaton
	 * @param name       name of the automaton
	 * @param bTau       flag indicating whether tau steps are to be added
	 * @param bEfficient flag indicating whether the values are to be computed
	 *                   efficiently
	 * @return returns entropy (double)
	 */
	public static double calculateEntropy(Automaton model, String name, boolean bTau, boolean bEfficient) {

		if (bTau) {
			boolean infinite = false; // the behavior contains infinite number of traces
			// ------------------Efficient computation-------------------
			if (bEfficient) {

				System.out.println(
						String.format("Efficiently constructing deterministic minimal automaton for traces with tau"));
				long start = System.currentTimeMillis();
				Set<String> traces = model.getFiniteStrings();

				if (traces != null) {
					Automaton a = new Automaton();
					int cnt = 0;
					System.out.println("Size " + traces.size());
					for (String trace : traces) {
						System.out.println(cnt);
						cnt++;
						Automaton aTmp = Automaton.makeString(trace);
						Utils.addTau(aTmp);
						aTmp.determinize(ProMCanceller.NEVER_CANCEL);
						aTmp.minimize(ProMCanceller.NEVER_CANCEL);
						a = a.union(aTmp, ProMCanceller.NEVER_CANCEL);
						a.determinize(ProMCanceller.NEVER_CANCEL);
						a.minimize(ProMCanceller.NEVER_CANCEL);
					}
					model = a.clone();
					long time = System.currentTimeMillis() - start;
					System.out.println(
							String.format("Efficient construction of automaton with tau took            %s ms.", time));
					System.out.println(String.format("The number of states:                                        %s",
							model.getNumberOfStates()));
					System.out.println(String.format("The number of transitions:                                   %s",
							numberOfTransitions(model)));
				} else {
					System.out.println(String.format(
							"Automaton accepts infinite number of traces. Efficient algorithm cannot be applied."));
					infinite = true;
				}

			}
			if (!bEfficient || infinite) {
				// ------------------Adding tau to automaton-------------------
				System.out.println("Starting preliminary minimization of automaton with tau");
				long start = System.currentTimeMillis();
				model.minimize(ProMCanceller.NEVER_CANCEL);
				long time = System.currentTimeMillis() - start;
				System.out.println(
						String.format("The automaton with tau is minimized in                      %s ms.", time));
				System.out.println(String.format("The number of states:                                        %s",
						model.getNumberOfStates()));
				System.out.println(String.format("The number of transitions:                                   %s",
						numberOfTransitions(model)));

				Utils.addTau(model);

				// Determinization of automaton
				System.out.println("Starting determinization of automaton with tau");
				start = System.currentTimeMillis();
				model.determinize(ProMCanceller.NEVER_CANCEL);
				time = System.currentTimeMillis() - start;
				System.out.println(
						String.format("The automaton with tau is determinized in                   %s ms.", time));
				System.out.println(String.format("The number of states:                                        %s",
						model.getNumberOfStates()));
				System.out.println(String.format("The number of transitions:                                   %s",
						numberOfTransitions(model)));

				// Minimization of automaton
				System.out.println("Starting minimization of retrieved automaton with tau");
				start = System.currentTimeMillis();
				model.minimize(ProMCanceller.NEVER_CANCEL);
				time = System.currentTimeMillis() - start;
				System.out.println(
						String.format("The retrieved automaton with tau is minimized in             %s ms.", time));
				System.out.println(String.format("The number of states:                                        %s",
						model.getNumberOfStates()));
				System.out.println(String.format("The number of transitions:                                   %s",
						numberOfTransitions(model)));
			}

		}
		EntropyResult result = TopologicalEntropyComputer.getTopologicalEntropy(model, name);
		return result.largestEigenvalue;
	}
	
	 private static  String numberOfTransitions(Automaton a) {
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
	    
}
