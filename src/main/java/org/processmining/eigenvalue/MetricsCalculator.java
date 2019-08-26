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
				long start = System.nanoTime();
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
				long time = System.nanoTime()-start;
				System.out.println(String.format("The automaton %s constracted (including deternminization and minimization)"
						+ " in %s nanoseconds.", mName, time));
				} else {
					infiniteM = true;
				}
				
				// ------------------Retrieved automaton--------------------------------
				start = System.nanoTime();
				Set<String> retrievedTraces = aM.getFiniteStrings();
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
				System.out.println(String.format("The automaton %s constracted (including deternminization and minimization)"
						+ " in %s nanoseconds.", lName, time));
				} else {
					infiniteL = true;
				}
			} 
			if (!bEfficient || infiniteM) {
				// ------------------Adding tau to relevant automaton-------------------
				Utils.addTau(aM);
					
					// Determinization of automaton
					System.out.println("Starting determinization of automaton " + mName);
					long start = System.nanoTime();
					aM.determinize(ProMCanceller.NEVER_CANCEL);
					long time = System.nanoTime()-start;
					System.out.println(String.format("The automaton %s determinized in %s nanoseconds.", mName, time));
					
					// Minimization of automaton
					System.out.println("Starting minimization of automaton " + mName);
					start = System.nanoTime();
					aM.minimize(ProMCanceller.NEVER_CANCEL);
					time = System.nanoTime()-start;
					System.out.println(String.format("The automaton %s minimized"
							+ " in %s nanoseconds.", mName, time));
			}
			if (!bEfficient || infiniteL) {
				// ------------------Adding tau to retrieved automaton-------------------
				Utils.addTau(aL);
				
				// Determinization of automaton
				System.out.println("Starting determinization of automaton " + lName);
				long start = System.nanoTime();
				aL.determinize(ProMCanceller.NEVER_CANCEL);
				long time = System.nanoTime()-start;
				System.out.println(String.format("The automaton %s determinized in %s nanoseconds.", lName, time));
				
				// Minimization of automaton
				System.out.println("Starting minimization of automaton " + lName);
				start = System.nanoTime();
				aL.minimize(ProMCanceller.NEVER_CANCEL);
				time = System.nanoTime()-start;
				System.out.println(String.format("The automaton %s minimized"
						+ " in %s nanoseconds.", lName, time));
			}
		}
		
		EntropyResult resultM = TopologicalEntropyComputer.getTopologicalEntropy(aM, mName);
		EntropyResult resultL = TopologicalEntropyComputer.getTopologicalEntropy(aL, lName);
		Automaton aLM = aM.intersection(aL, ProMCanceller.NEVER_CANCEL);
		EntropyResult resultLM = TopologicalEntropyComputer.getTopologicalEntropy(aLM, lName);
		
		double recall = resultLM.largestEigenvalue / resultL.largestEigenvalue;
		double precision = resultLM.largestEigenvalue / resultM.largestEigenvalue;
		
		return new Pair<Double, Double>(recall, precision);
	}
}
