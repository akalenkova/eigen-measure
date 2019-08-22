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


import org.apache.commons.math3.util.Pair;
import org.processmining.eigenvalue.automata.TopologicalEntropyComputer;
import org.processmining.eigenvalue.data.EntropyResult;
import org.processmining.framework.plugin.ProMCanceller;

import dk.brics.automaton2.Automaton;

public class MetricsCalculator {
	
	
	public static void main(String[] args) {
		
	}
	
	/**
	 * Calculates recall and precision for the given automaton and event log
	 * 
	 * @param aM model
	 * @param aL log
	 * @param bTau flag indicating whether tau steps are to be added
	 * @param bEfficient flag indicating whether the values are to be computed efficiently
	 * @return returns a pair (recall, precision)
	 */
	public static Pair<Double, Double> calculate(Automaton aM, String mName, Automaton aL, String lName, boolean bTau,
			boolean bEfficient) {

		EntropyResult resultM = TopologicalEntropyComputer.getTopologicalEntropy(aM, mName);
		EntropyResult resultL = TopologicalEntropyComputer.getTopologicalEntropy(aL, lName);
		Automaton aLM = aM.intersection(aL, ProMCanceller.NEVER_CANCEL);
		EntropyResult resultLM = TopologicalEntropyComputer.getTopologicalEntropy(aLM, lName);
		
		double recall = resultLM.largestEigenvalue / resultL.largestEigenvalue;
		double precision = resultLM.largestEigenvalue / resultM.largestEigenvalue;
		
		return new Pair<Double, Double>(recall, precision);
	}
}
