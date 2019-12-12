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


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.processmining.eigenvalue.automata.TopologicalEntropyComputer;
import org.processmining.eigenvalue.data.EntropyResult;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;



public class MetricsCalculator {
	
	final static int INITIAL_NUMBER_OF_TRACES = 100;
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
	public static Pair<Double, Double> calculate(Automaton aL, String mName, Automaton aM, String lName, boolean bTau,
			boolean bEfficient) {
		boolean perfectReplay = false;
		if(bTau) {	
			
			if(aL.subsetOf(aM)) {
				perfectReplay = true;
				//System.out.println("Perfect replay optimization");
			}
			boolean infiniteM = false; // the relevant behavior contains infinite number of traces
			boolean infiniteL = false; // the retrieved behavior contains infinite number of traces
			
			// ------------------Efficient computation-------------------
			if(bEfficient) {
				
				// ------------------Relevant automaton--------------------------------
				System.out.println(String.format(
						"Constructing (with optimization) deterministic minimal automaton for REL."));
				long start = System.currentTimeMillis();
				
				// START CHUNKS APPROACH--------------------------------------------------------------------------
					
				Set<String> relevantTraces = aL.getFiniteStrings();
				if (relevantTraces != null) {
					
				excludeSubstrings(relevantTraces);
				List<String> listOfReleventTraces = new ArrayList<String>(relevantTraces);
				
				listOfReleventTraces.sort(Comparator.comparing( String::toString));
				
				int numberOfTraces = listOfReleventTraces.size();

				System.out.println("Number of remaining traces: " + numberOfTraces + ".");
				Automaton a = new Automaton();
				Iterator<String> iterator = listOfReleventTraces.iterator();
				Automaton tmpCollection = new Automaton();
				int cnt = 1;
				Automaton aTmp = new Automaton();
//				int chunk = numberOfTraces / 3;
//				if(chunk == 0) {
//					chunk = 1;
//				}
				int chunk = 100;
				while (iterator.hasNext()) {
					System.out.println("Trace number: " + cnt + ".");
					long time = System.currentTimeMillis(); 
					aTmp = Automaton.makeString(iterator.next());
					Utils.addTau(aTmp);
					aTmp.determinize();
					aTmp.minimize();
					tmpCollection = Utils.union(tmpCollection, aTmp);

					
					System.out.println("Size of TMP automaton:    " + aTmp.getNumberOfStates() + ".");

					if(cnt % chunk == 0) { 
//						if (chunk > 10) {
//							chunk = chunk / 2 ;
//						} else {
//							chunk = 10;
//						}
						System.gc();
						tmpCollection.determinize();
						tmpCollection.minimize();
						a = Utils.union(a, aTmp);
						tmpCollection = new Automaton();
						a.determinize();
						a.minimize();
					}
					
					System.out.println("Size of RESULT automaton: " + a.getNumberOfStates() + ".");
					long calculationTime = System.currentTimeMillis() - time;
					System.out.println("Calculation time: " +  calculationTime + " ms.");
					cnt++;
				}
				System.gc();
				a = Utils.union(a, aTmp);
				a.determinize();
				a.minimize();
				aL = a;
				
				// END CHUNKS APPROACH--------------------------------------------------------------------------

			
					long time = System.currentTimeMillis() - start;
					System.out.println(
							String.format("The deterministic version of REL was constructed (with optimization) in  %s ms.", time));
					System.out.println(
							String.format("The minimized deterministic version of automaton REL has %s %s and %s %s.",  aL.getNumberOfStates(), 
									Utils.singlePlural(aL.getNumberOfStates(), "state", "states"), Utils.numberOfTransitions(aL),
									Utils.singlePlural(new Integer(Utils.numberOfTransitions(aL)), "transition", "transitions")));
				} else {
					System.out.println(String.format(
							"REL automaton accepts infinite number of traces. Algorithm (with optimization) cannot be applied."));
					infiniteL = true;
				}
				// ------------------Retrieved automaton--------------------------------
				System.out.println(String.format(
						"Constructing (with optimization) deterministic minimal automaton for RET."));
				start = System.currentTimeMillis();
				Set<String> retrievedTraces = aM.getFiniteStrings();
				if (retrievedTraces != null) {
					Automaton a = new Automaton();
					for (String trace : retrievedTraces) {
						Automaton aTmp = Automaton.makeString(trace);
						Utils.addTau(aTmp);
						aTmp.determinize();
						aTmp.minimize();
						a = a.union(aTmp);
						a.determinize();
						a.minimize();
					}
					aM = a.clone();
					long time = System.currentTimeMillis() - start;
					System.out.println(
							String.format("The deterministic version of RET was constructed (with optimization) in  %s ms.", time));
					System.out.println(
							String.format("The minimized deterministic version of automaton RET has %s %s and %s %s.", aM.getNumberOfStates(),
									Utils.singlePlural(aM.getNumberOfStates(), "state", "states"), Utils.numberOfTransitions(aM), 
									Utils.singlePlural(new Integer(Utils.numberOfTransitions(aM)), "transition", "transitions")));
				} else {
					System.out.println(String.format(
							"RET automaton accepts infinite number of traces. Algorithm (with optimization) cannot be applied."));
					infiniteM = true;
				}
				
			}
			
			if (!bEfficient || infiniteL) {
				
				// ------------------Adding tau to retrieved automaton-------------------
				//System.out.println("Starting preliminary minimization of relevant automaton with tau");
				System.out.println("Minimizing automaton REL.");
				long start = System.currentTimeMillis();
				aL.minimize();
				long time = System.currentTimeMillis() - start;
				System.out.println(
						String.format("Automaton REL was minimized in                              %s ms.", time));
				System.out.println(
						String.format("The minimized version of REL has %s %s and %s %s.", aL.getNumberOfStates(),
								Utils.singlePlural(aL.getNumberOfStates(), "state", "states"), Utils.numberOfTransitions(aL),
								Utils.singlePlural(new Integer(Utils.numberOfTransitions(aL)), "transition", "transitions")));
				
				Utils.addTau(aL);
				
				// Determinization of automaton
				System.out.println("Determinizing the minimized version of REL.");
				start = System.currentTimeMillis();
				aL.determinize();
				time = System.currentTimeMillis() - start;
				System.out.println(
						String.format("The minimized version of REL was determinized in            %s ms.", time));
				System.out.println(
						String.format("The determinized version of the minimized version of REL has %s %s and %s %s.",  aL.getNumberOfStates(),
								Utils.singlePlural(aL.getNumberOfStates(), "state", "states"), Utils.numberOfTransitions(aL),
								Utils.singlePlural(new Integer(Utils.numberOfTransitions(aL)), "transition", "transitions")));

				// Minimization of automaton
				System.out.println("Minimizing deterministic version of automaton REL.");
				start = System.currentTimeMillis();
				aL.minimize();
				time = System.currentTimeMillis() - start;
				System.out.println(
						String.format("The deterministic version of REL was minimized in           %s ms.", time));
				System.out.println(
						String.format("The minimized deterministic version of automaton REL has %s %s and %s %s.",  aL.getNumberOfStates(),
								Utils.singlePlural(aL.getNumberOfStates(), "state", "states"), Utils.numberOfTransitions(aL),
								Utils.singlePlural(new Integer(Utils.numberOfTransitions(aL)), "transition", "transitions")));
			}
			
			if (!bEfficient || infiniteM) {
				// ------------------Adding tau to relevant automaton-------------------
				// Minimization of automaton
				//System.out.println("Starting preliminary minimization of retrieved automaton with tau");
				System.out.println("Minimizing automaton RET.");
				long start = System.currentTimeMillis();
				aM.minimize();
				long time = System.currentTimeMillis() - start;
				System.out.println(
						String.format("Automaton RET was minimized in                              %s ms.", time));
				System.out.println(
						String.format("The minimized version of RET has %s %s and %s %s.", aM.getNumberOfStates(),
								Utils.singlePlural(aM.getNumberOfStates(), "state", "states"), Utils.numberOfTransitions(aM), 
								Utils.singlePlural(new Integer(Utils.numberOfTransitions(aM)), "transition", "transitions")));

				Utils.addTau(aM);
				
				// Determinization of automaton
				System.out.println("Determinizing the minimized version of RET.");
				start = System.currentTimeMillis();
				aM.determinize();
				time = System.currentTimeMillis() - start;
				System.out.println(
						String.format("The minimized version of RET was determinized in            %s ms.", time));
				System.out.println(
						String.format("The determinized version of the minimized version of RET has %s %s and %s %s.",aM.getNumberOfStates(),
								Utils.singlePlural(aM.getNumberOfStates(), "state", "states"), Utils.numberOfTransitions(aM), 
								Utils.singlePlural(new Integer(Utils.numberOfTransitions(aM)), "transition", "transitions")));
				
				// Minimization of automaton
				System.out.println("Minimizing deterministic version of automaton RET.");
				start = System.currentTimeMillis();
				aM.minimize();
				time = System.currentTimeMillis() - start;
				System.out.println(
						String.format("The deterministic version of RET was minimized in           %s ms.", time));
				System.out.println(
						String.format("The minimized deterministic version of automaton RET has %s %s and %s %s.", aM.getNumberOfStates(),
								Utils.singlePlural(aM.getNumberOfStates(), "state", "states"), Utils.numberOfTransitions(aM), 
								Utils.singlePlural(new Integer(Utils.numberOfTransitions(aM)), "transition", "transitions")));
			}
		}
		
		System.gc();
		long start = System.currentTimeMillis();
		Automaton aLM;
		if(perfectReplay) {
			//System.out.println("Perfect replay optimization");
			aLM = aL;
		} else {
			if(aL.subsetOf(aM)) {
				//System.out.println("Perfect replay optimization");
				aLM = aL;
			} else {
				aLM = aM.intersection(aL);
				System.gc();
				aLM.minimize();
			}
		}
	
		long time = System.currentTimeMillis() - start;
		System.out.println(String.format("The intersection INT of RET and REL constructed in          %s ms.", time));
		System.out.println(String.format("Automaton INT has %s %s and %s %s.\n", aLM.getNumberOfStates(),
				Utils.singlePlural(aLM.getNumberOfStates(), "state", "states"), Utils.numberOfTransitions(aLM), 
				Utils.singlePlural(new Integer(Utils.numberOfTransitions(aLM)), "transition", "transitions")));

		
		System.gc();
		EntropyResult resultL = TopologicalEntropyComputer.getTopologicalEntropy(aL, lName);
		EntropyResult resultM = TopologicalEntropyComputer.getTopologicalEntropy(aM, mName);		
		EntropyResult resultLM = TopologicalEntropyComputer.getTopologicalEntropy(aLM, "INT");
		
		double recall = resultLM.largestEigenvalue / resultL.largestEigenvalue;
		double precision = resultLM.largestEigenvalue / resultM.largestEigenvalue;
		
    	System.out.println(String.format("Precision: %s.", precision));
		System.out.println(String.format("Recall: %s.", recall));
		
		System.out.println(String.format("Precision computed in                                               %s ms.", resultM.computationMillis + resultLM.computationMillis));
		System.out.println(String.format("Recall computed in                                                  %s ms.", resultL.computationMillis + resultLM.computationMillis));
		System.out.println(String.format("Both values of precision and recall computed in                     %s ms.", resultM.computationMillis +resultL.computationMillis + resultLM.computationMillis));
		
		return new Pair<Double, Double>(recall, precision);
	}
	
	
	private static void excludeSubstrings(Set<String> setOfStrings) {
		Set<String> analysedStrings = new HashSet(setOfStrings);
		Set<String> goodStrings = new HashSet<String>();
		int cnt = 0; // number of removed strings
		boolean removed = false;
		while(analysedStrings.size() > 0) {
			String longestString = getLongestString(analysedStrings);
			analysedStrings.remove(longestString);
			removed = false;
			for(String goodString : goodStrings) {
				if(isSubstring(goodString, longestString)) {
					setOfStrings.remove(longestString);
					cnt++;
					removed = true;
					break;
				}
			}
			if(!removed) {
				goodStrings.add(longestString);
			}
		}
		
		System.out.println("Number of removed traces:   " + cnt + ".");
	}
	
	private static String getLongestString(Set<String> setOfStrings) {
		String maxString = "";
		for(String s: setOfStrings) {
			if(s.length() > maxString.length()) {
				maxString = s;
			}
		}
		
		return maxString;
	}
	
	/**
	 * Check that s2 is inside s1
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	private static boolean isSubstring(String s1, String s2) {

		int k = 0; // the current

		for (int i = 0; i < s2.length(); i++) {
			boolean found = false;
			for (int j = k; j < s1.length(); j++) {

				if (s2.charAt(i) == s1.charAt(j)) {
					if (i == s2.length() - 1) {
						return true;
					}
					found = true;
					k = j + 1;
					break;
				}
			}
			if(!found) {
				return false;
			}
		}
		return false;
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
						String.format("Efficiently constructing deterministic minimal automaton for traces with tau."));
				long start = System.currentTimeMillis();
				Set<String> traces = model.getFiniteStrings();

				if (traces != null) {
					Automaton a = new Automaton();
					int cnt = 0;
					System.out.println("Size " + traces.size() + ".");
					for (String trace : traces) {
						//System.out.println(cnt);
						cnt++;
						Automaton aTmp = Automaton.makeString(trace);
						Utils.addTau(aTmp);
						aTmp.determinize();
						aTmp.minimize();
						a = a.union(aTmp);
						a.determinize();
						a.minimize();
					}
					model = a.clone();
					long time = System.currentTimeMillis() - start;
					System.out.println(
							String.format("Efficient construction of automaton with tau took            %s ms.", time));
					System.out.println(String.format("The number of states:                                        %s.",
							model.getNumberOfStates()));
					System.out.println(String.format("The number of transitions:                                   %s.",
							Utils.numberOfTransitions(model)));
				} else {
					System.out.println(String.format(
							"Automaton accepts infinite number of traces. Efficient algorithm cannot be applied."));
					infinite = true;
				}
			}
			if (!bEfficient || infinite) {
				// ------------------Adding tau to automaton-------------------
				//System.out.println("Starting preliminary minimization of automaton with tau");
				long start = System.currentTimeMillis();
				model.minimize();
				long time = System.currentTimeMillis() - start;
				System.out.println(
						String.format("The automaton with tau is minimized in                      %s ms.", time));
				System.out.println(String.format("The number of states:                                        %s.",
						model.getNumberOfStates()));
				System.out.println(String.format("The number of transitions:                                   %s.",
						Utils.numberOfTransitions(model)));

				Utils.addTau(model);

				// Determinization of automaton
				System.out.println("Starting determinization of automaton with tau.");
				start = System.currentTimeMillis();
				model.determinize();
				time = System.currentTimeMillis() - start;
				System.out.println(
						String.format("The automaton with tau is determinized in                   %s ms.", time));
				System.out.println(String.format("The number of states:                                        %s.",
						model.getNumberOfStates()));
				System.out.println(String.format("The number of transitions:                                   %s.",
						Utils.numberOfTransitions(model)));

				// Minimization of automaton
				System.out.println("Starting minimization of retrieved automaton with tau.");
				start = System.currentTimeMillis();
				model.minimize();
				time = System.currentTimeMillis() - start;
				System.out.println(
						String.format("The retrieved automaton with tau is minimized in             %s ms.", time));
				System.out.println(String.format("The number of states:                                        %s.",
						model.getNumberOfStates()));
				System.out.println(String.format("The number of transitions:                                   %s.",
						Utils.numberOfTransitions(model)));
			}

		}
		EntropyResult entropy = TopologicalEntropyComputer.getTopologicalEntropy(model, name);
		Automaton maxModel = constructAllBehavior(model);
		EntropyResult maxEntropy = TopologicalEntropyComputer.getTopologicalEntropy(maxModel, name);
		
		return entropy.topologicalEntropy/maxEntropy.topologicalEntropy;
	}
	
	 private static Automaton constructAllBehavior(Automaton a) {
		 
		 Automaton result = new Automaton();
		 State initialState = new State();
		 result.setInitialState(initialState);
		 initialState.setAccept(true);
		 // Obtain alphabet
		 Set<Byte> alphabet = new HashSet<Byte>(); 
		 for(State s : a.getStates()) {
			 for(Transition t : s.getTransitions()) {
				 for(char c=t.getMin(); c <=t.getMax(); c++) {
					 alphabet.add((byte)c);
				 }
			 }
		 }
		 for(Byte b : alphabet) {
			 System.out.println(b);
			 dk.brics.automaton.Transition t = new dk.brics.automaton.Transition((char)b.byteValue(), initialState);
			 initialState.addTransition(t);
		 }
		 return result;
	 }

}
