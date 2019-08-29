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
				// ------------------Relevant automaton--------------------------------
    			System.out.println(String.format("Efficiently constructing deterministic minimal automaton for relevant traces with tau."));
				long start = System.currentTimeMillis();
				Set<String> relevantTraces = aM.getFiniteStrings();
				if(relevantTraces != null) {
					Automaton a = new Automaton();
					for(String trace : relevantTraces) {
						a.incorporateTrace(Utils.toShortArray(trace.getBytes()), ProMCanceller.NEVER_CANCEL);
						Utils.addTau(a);
						a.determinize(ProMCanceller.NEVER_CANCEL);
						a.minimize(ProMCanceller.NEVER_CANCEL);
					}
					aM = a.clone();
				long time = System.currentTimeMillis()-start;
				System.out.println(String.format("Efficient construction of relevant automaton with tau took   %s ms.", time));
				System.out.println(String.format("The number of states:                                        %s", aM.getNumberOfStates()));
				System.out.println(String.format("The number of transitions:                                   %s", aM.getNumberOfTransitions()));
				} else {
					System.out.println(String.format("Relevant automaton accepts infinite number of traces. Efficient algorithm cannot be applied."));
					infiniteM = true;
				}
				
				// ------------------Retrieved automaton--------------------------------
    			System.out.println(String.format("Efficiently constructing deterministic minimal automaton for retrieved traces with tau"));
				start = System.currentTimeMillis();
				Set<String> retrievedTraces = aL.getFiniteStrings();
				if(retrievedTraces != null) {
					Automaton a = new Automaton();
					for(String trace : retrievedTraces) {
						a.incorporateTrace(Utils.toShortArray(trace.getBytes()), ProMCanceller.NEVER_CANCEL);
						Utils.addTau(a);
						a.determinize(ProMCanceller.NEVER_CANCEL);
						a.minimize(ProMCanceller.NEVER_CANCEL);
					}
					aL = a.clone();
				long time = System.nanoTime()-start;
				time = System.currentTimeMillis()-start;
				System.out.println(String.format("Efficient construction of retrieved automaton with tau took  %s ms.", time));
				System.out.println(String.format("The number of states:                                        %s", aL.getNumberOfStates()));
				System.out.println(String.format("The number of transitions:                                   %s", aL.getNumberOfTransitions()));
				} else {
					System.out.println(String.format("Retrived automaton accepts infinite number of traces. Efficient algorithm cannot be applied."));
					infiniteL = true;
				}
			} 
			if (!bEfficient || infiniteM) {
				// ------------------Adding tau to relevant automaton-------------------
				Utils.addTau(aM);

				// Determinization of automaton
				System.out.println("Starting determinization of relevant automaton with tau");
				long start = System.currentTimeMillis();
				aM.determinize(ProMCanceller.NEVER_CANCEL);
				long time = System.currentTimeMillis() - start;
				System.out.println(
						String.format("The relevant automaton with tau is determinized in           %s ms.", time));
				System.out.println(String.format("The number of states:                                        %s", aM.getNumberOfStates()));
				System.out.println(String.format("The number of transitions:                                   %s", aM.getNumberOfTransitions()));
				
				// Minimization of automaton
				System.out.println("Starting minimization of relevant automaton with tau");
				start = System.currentTimeMillis();
				aM.minimize(ProMCanceller.NEVER_CANCEL);
				time = System.currentTimeMillis() - start;
				System.out.println(
						String.format("The relevant automaton with tau is minimized in              %s ms.", time));
				System.out.println(String.format("The number of states:                                        %s", aM.getNumberOfStates()));
				System.out.println(String.format("The number of transitions:                                   %s", aM.getNumberOfTransitions()));
			}
			if (!bEfficient || infiniteL) {
				// ------------------Adding tau to retrieved automaton-------------------
				Utils.addTau(aL);

				// Determinization of automaton
				System.out.println("Starting determinization of retrieved automaton with tau");
				long start = System.currentTimeMillis();
				aL.determinize(ProMCanceller.NEVER_CANCEL);
				long time = System.currentTimeMillis() - start;
				System.out.println(
						String.format("The retrieved automaton with tau is determinized in          %s ms.", time));
				System.out.println(String.format("The number of states:                                        %s", aL.getNumberOfStates()));
				System.out.println(String.format("The number of transitions:                                   %s", aL.getNumberOfTransitions()));

				// Minimization of automaton
				System.out.println("Starting minimization of retrieved automaton with tau");
				start = System.currentTimeMillis();
				aL.minimize(ProMCanceller.NEVER_CANCEL);
				time = System.currentTimeMillis() - start;
				System.out.println(
						String.format("The retrieved automaton with tau is minimized in             %s ms.", time));
				System.out.println(String.format("The number of states:                                        %s", aL.getNumberOfStates()));
				System.out.println(String.format("The number of transitions:                                   %s", aL.getNumberOfTransitions()));
			}
		}

		EntropyResult resultM = TopologicalEntropyComputer.getTopologicalEntropy(aM, mName);
		EntropyResult resultL = TopologicalEntropyComputer.getTopologicalEntropy(aL, lName);
		Automaton aLM = aM.intersection(aL, ProMCanceller.NEVER_CANCEL);
		EntropyResult resultLM = TopologicalEntropyComputer.getTopologicalEntropy(aLM, "intersection of " + mName + " and " + lName);
		
		double recall = resultLM.largestEigenvalue / resultL.largestEigenvalue;
		double precision = resultLM.largestEigenvalue / resultM.largestEigenvalue;
		
		return new Pair<Double, Double>(recall, precision);
	}
}
