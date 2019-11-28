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

package org.processmining.eigenvalue.automata;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RunAutomaton;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.eigenvalue.Utils;
import org.processmining.eigenvalue.data.EntropyPrecisionRecall;
import org.processmining.eigenvalue.data.EntropyResult;

import org.processmining.framework.packages.impl.CancelledException;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.projectedrecallandprecision.helperclasses.EfficientLog;
import org.processmining.projectedrecallandprecision.helperclasses.ProjectPetriNetOntoActivities;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PrecisionRecallComputer {

    private static final Log logger = LogFactory.getLog(PrecisionRecallComputer.class);

    /**
     * Computes Precision and Recall for event log and an accepting petri net.
     * Uses the default Name Classifier ({@link XLogInfoImpl#NAME_CLASSIFIER}) to
     * establish a link between model and log.
     *
     * @param context {@link PluginContext} that can be null in testing or UI-less computation
     * @param canceller {@link ProMCanceller} a handler that indicates, whether computation should be aborted due to user cancellation
     * @param log {@link XLog} the event log to check for precision & fitness
     * @param net {@link AcceptingPetriNet} that has corresponding initial and final markings set
     * @return
     */
    public static EntropyPrecisionRecall getPrecisionAndRecall(PluginContext context, ProMCanceller canceller, XLog log, AcceptingPetriNet net) {
        try {
            return getPrecisionAndRecall(context, canceller, log, net, XLogInfoImpl.NAME_CLASSIFIER);
        } catch (CancelledException e) {
            logger.info("Precision computation cancelled!");
            return null;
        }
    }

    public static EntropyPrecisionRecall getPrecisionAndRecall(Automaton aM, String mName, Automaton aL, String lName, Automaton aLM, double fittingTracesFraction, 
    		ProMCanceller canceller){
        return getPrecisionAndRecall(aM, mName, aL, lName, aLM, mName+"&"+lName, fittingTracesFraction, canceller);
    }

    public static EntropyPrecisionRecall getPrecisionAndRecall(Automaton aM, String mName, Automaton aL, String lName, Automaton aLM, String lmName, double fittingTracesFraction, 
    		ProMCanceller canceller){
        EntropyResult resultM = getResult(mName, aM.getNumberOfStates(), TopologicalEntropyComputer.getTopologicalEntropy(aM, mName, canceller));
        EntropyResult resultL = getResult(lName, aL.getNumberOfStates(), TopologicalEntropyComputer.getTopologicalEntropy(aL, lName, canceller));
        EntropyResult resultLM = getResult(lmName, aLM.getNumberOfStates(), TopologicalEntropyComputer.getTopologicalEntropy(aLM, lmName, canceller));

        return new EntropyPrecisionRecall(resultLM, resultM, resultL, fittingTracesFraction);
    }

    /**
     * Computes Precision and Recall for event log and an accepting petri net.
     *
     * @param context {@link PluginContext} that can be null in testing or UI-less computation
     * @param canceller {@link ProMCanceller} a handler that indicates, whether computation should be aborted due to user cancellation
     * @param log {@link XLog} the event log to check for precision & fitness
     * @param net {@link AcceptingPetriNet} that has corresponding initial and final markings set
     * @param classifier the classifier to be used (for example the name classifier XLogInfoImpl.NAME_CLASSIFIER)
     * @return EntropyPrecisionRecall result object encapsulating eigenvalue based precision and recall the results
     * @throws CancelledException
     */
    public static EntropyPrecisionRecall getPrecisionAndRecall(PluginContext context, ProMCanceller canceller, XLog log, AcceptingPetriNet net, XEventClassifier classifier) throws CancelledException {
        return getPrecisionAndRecall(context, canceller, log, net, classifier);
    }
    
    /**
     * Calculate precision and recall with possible TAU for model
     * 
     * @param context
     * @param canceller
     * @param log
     * @param net
     * @param tauModel
     * @return
     */
    public static EntropyPrecisionRecall getPrecisionAndRecall(PluginContext context, ProMCanceller canceller, 
    		Automaton aL, AcceptingPetriNet net, boolean tauModel) {
    	if (context != null && context.getProgress() != null) {
            context.getProgress().setValue(0);
            context.getProgress().setMaximum(100);
        }

        try{
        	checkCancelled(canceller);
        } catch(CancelledException e) {
        	e.printStackTrace();
        }

        Automaton aM = null;
        try {
            aM = AcceptingPetriNet2automaton.convert(net, Integer.MAX_VALUE, canceller);
            if(tauModel) {
            	Utils.addTau(aM);
                aM.determinize();
                aM.minimize();
            }
        } catch (AutomatonFailedException e){
            e.printStackTrace();
        }
        Automaton aLM = aM.intersection(aL);
        
        return getPrecisionAndRecall(aM, net.getNet().getLabel(), aL, "Log", aLM, 1.0, Utils.NOT_CANCELLER);
    }
    
    
    /**
     * Calculating precision and recall with possible tau
     * 
     * @param context
     * @param canceller
     * @param log
     * @param net
     * @param classifier
     * @param resultL
     * @param tauModel
     * @param tauLog
     * @return
     * @throws CancelledException
     */
    public static EntropyPrecisionRecall getPrecisionAndRecall(PluginContext context, ProMCanceller canceller, 
    		XLog log, AcceptingPetriNet net, XEventClassifier classifier, 
    		boolean tauModel, boolean tauLog) {
        
    	String name = Utils.getName(net.getNet(),"M");
    	if(tauModel) {
    		name+="_tau";
    	}
        String logName = Utils.getName(log,"L");
        if(tauLog) {
        	logName+="_tau";
    	}

        if (context != null && context.getProgress() != null) {
            context.getProgress().setValue(0);
            context.getProgress().setMaximum(100);
        }
        log(context, "Starting precision computation for "+name, 1);
        try {
        	checkCancelled(canceller);
        } catch (CancelledException e) {
        	e.printStackTrace();
        }

        // prepare
        EfficientLog elog = new EfficientLog(log, classifier);
        String[] activities = elog.getActivities();
        log(context, "Converted log to efficient log.", 28);
        if (canceller.isCancelled()){
            return null;
        }
        String[] names = getTransitionNames(net, activities);

        AcceptingPetriNet projectedNet = ProjectPetriNetOntoActivities.project(net, canceller, names);
        Automaton aM = null;
        try {
        	aM = AcceptingPetriNet2automaton.convert(projectedNet, Integer.MAX_VALUE, canceller);
            if(tauModel) {
            	Utils.addTau(aM);
            	aM.determinize();
            	aM.minimize();
            }
        } catch (AutomatonFailedException e){
            e.printStackTrace();
        }
        log(context, "Converted net to automaton.", 35);
        
        try {
        	checkCancelled(canceller);
        } catch (CancelledException e) {
        	e.printStackTrace();
        }

        log(context, "Projected net to (all) activities.", 30);
        EntropyPrecisionRecall precision = null;
        try {
        	EntropyResult resultM = getResult(name, net.getNet().getNodes().size(), TopologicalEntropyComputer.getTopologicalEntropy(aM, name, canceller));
            log(context, "Computed model automaton topology.", 60);
            checkCancelled(canceller);

            RunAutomaton ra = new RunAutomaton(aM);
            double fittingTracesFraction;
            EntropyResult resultLM;
            
                System.out.println("Processing log " + System.currentTimeMillis());
            	Pair<Double, Automaton> pair = processLog(elog, ra, false, canceller, tauLog, names);
            	System.out.println("End processing log" + System.currentTimeMillis());
            	fittingTracesFraction = pair.getA();
                log(context, "Projected log into model.", 70);
                checkCancelled(canceller);

                Automaton aL = pair.getB(); // log automaton of accepted traces
                Automaton aML = aL.intersection(aM);
                aML.minimize();
                resultLM = getResult(logName, elog.size(), TopologicalEntropyComputer.getTopologicalEntropy(aML, logName, canceller));

                log(context, "Computed log-model automaton topology.", 75);
            
            EntropyResult resultL = getResult(logName, elog.size(), TopologicalEntropyComputer.getTopologicalEntropy(aL, logName, canceller));
            precision = new EntropyPrecisionRecall(resultLM, resultM, resultL, fittingTracesFraction);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return precision;
    }
    
    private static void checkCancelled(ProMCanceller canceller) throws CancelledException {
        if (canceller.isCancelled()){
            throw new CancelledException();
        }
    }
    
    public static EntropyResult getEntropyLogResult(PluginContext context, ProMCanceller canceller, String logName, EfficientLog elog, String[] names, boolean tauLog) throws CancelledException {
        Pair<Double, Automaton> pair = processLog(elog, null, false, canceller, tauLog, names);
        log(context, "Computed Log automaton.", 80);
        checkCancelled(canceller);

        Automaton a = pair.getB();
        EntropyResult resultL = getResult(logName, elog.size(), TopologicalEntropyComputer.getTopologicalEntropy(a, logName, canceller));
        log(context, "Computed log automaton topological entropy.", 95);
        return resultL;
    }

    public static String[] getTransitionNames(AcceptingPetriNet net, String[] activities) {
        Set<String> transitionNames = new HashSet<>();
        for (org.processmining.models.graphbased.directed.petrinet.elements.Transition t : net.getNet().getTransitions()) {
            if (!t.isInvisible() && t.getLabel() != null) {
                transitionNames.add(t.getLabel());
            }
        }
        for (int i = 0; i<activities.length; i++) transitionNames.add(activities[i]);
        return transitionNames.toArray(new String[transitionNames.size()]);
    }

    private static void log(PluginContext context, String message, int degreeDone) {
        if (context != null && context.getProgress() != null) {
            context.getProgress().setValue(25);
            context.log(message);
        }
        logger.debug(message);
    }

    public static EntropyResult getResult(String name, int size, EntropyResult entResult) {
        entResult.name = name;
        entResult.size = size;
        return entResult;
    }
    
    /**
     * Process log with possible taus
     * 
     * @param log
     * @param modelAutomaton
     * @param selectOnlyFittingTraces
     * @param canceller
     * @param tauLog
     * @param names
     * @return
     */
    @SuppressWarnings("unchecked")
	public static Pair<Double, Automaton> processLog(EfficientLog log, RunAutomaton modelAutomaton, 
    		boolean selectOnlyFittingTraces, ProMCanceller canceller, boolean tauLog, String... names) {
        short[] projectionKey = log.getProjectionKey(names);
        int replayableTraces = 0;
        Automaton logAutomaton = null;

        Set<String> addedTraces = new HashSet<String>();
        for(int traceIndex = 0; traceIndex < log.size(); ++traceIndex) {
        	
        	Pair<Boolean, Automaton> p;
        	
        	if (tauLog) {
        		System.out.println("Processing trace " + traceIndex);
        		p = processTraceWithOptimization(modelAutomaton, logAutomaton, 
            		log, traceIndex, projectionKey, selectOnlyFittingTraces, tauLog, addedTraces, canceller);
        	} else {
        		p = processTrace(modelAutomaton, logAutomaton, 
                		log, traceIndex, projectionKey, selectOnlyFittingTraces, canceller);
        	}
            if (canceller.isCancelled() || p == null) {
                return null;
            }
            logAutomaton = p.getB();
            if (p.getA()) {
                ++replayableTraces;
            }
        }
//        if (logAutomaton != null) {
//        	if(tauLog) {	
//        		TestUtils.outputPNG(logAutomaton, "logTauAutomaton");
//        	} else {
//        		 logAutomaton.minimize();
//                 TestUtils.outputPNG(logAutomaton, "logutomaton");
//        	}
//        }

        System.out.println("Log automaton constructed");
        
        if (canceller.isCancelled()) {
            return null;
        } else {
            return Pair.of(replayableTraces/(double)log.size(), logAutomaton);
        }
    }

    
    /**
     * Incorporates traces into the log automaton adding tau and minimizing each time (if skips are to be added)
     * 
     * @param modelAutomaton
     * @param logAutomaton
     * @param log
     * @param trace
     * @param projectionKey short[] encoded short values that refer to the activities in the log
     * @param onlyFittingTraces flag that specifies whether only fitting traces should be added to the log automaton
     * @param tauLog flag that specifies whether skips are to be added
     * @param addedTraces set which shows which traces were already added (in order not add them once again) 
     * @param canceller
     * @return Pair of boolean, Automaton:  flag that indicates whether the trace was incorporated, and the resulting automaton.
     */
	public static Pair<Boolean, Automaton> processTraceWithOptimization(RunAutomaton modelAutomaton,
			Automaton logAutomaton, EfficientLog log, int trace, short[] projectionKey, boolean onlyFittingTraces,
			boolean tauLog, Set<String> addedTraces, ProMCanceller canceller) {
		short[] projectedTrace = log.getProjectedTrace(trace, projectionKey);

		boolean addToAutomaton = !onlyFittingTraces || modelAutomaton.run(Arrays.toString(projectedTrace));
		if (addToAutomaton) {
			if (logAutomaton == null) {
				String automatonString;
				automatonString = projectedTraceToString(projectedTrace);
				logAutomaton = Automaton.makeString(automatonString);
				logAutomaton.expandSingleton();
				if (tauLog) {
					Utils.addTau(logAutomaton);
					logAutomaton.determinize();
					logAutomaton.minimize();
				}
			} else {
				String automatonString = projectedTraceToString(projectedTrace);
				if (tauLog) {
					if (!addedTraces.contains(automatonString)) {
						Automaton curAutomaton = Automaton.makeString(automatonString);
						curAutomaton.expandSingleton();
						Utils.addTau(curAutomaton);
						curAutomaton.determinize();
						curAutomaton.minimize();

						logAutomaton = logAutomaton.union(curAutomaton);
						logAutomaton.determinize();
						logAutomaton.minimize();

						addedTraces.add(automatonString);
					}
				} else {
					logAutomaton.union(Automaton.makeString(automatonString));
				}
			}
		}
		if (canceller.isCancelled()) {
			return null;
		}
		return Pair.of(addToAutomaton, logAutomaton);
	}

    /**
     * Incorporates traces into the log automaton.
     * @param modelAutomaton {@link RunAutomaton} that encodes the model for parsing each trace
     * @param logAutomaton the current state of the log automaton that is invrementally enriched.
     * @param log {@link EfficientLog} encoded log
     * @param trace int the trace index
     * @param projectionKey short[] encoded short values that refer to the activities in the log
     * @param onlyFittingTraces flag that specifies whether only fitting traces should be added to the log automaton
     * @param canceller {@link ProMCanceller} that allows to stop the computation on user request.
     * @return Pair of boolean, Automaton:  flag that indicates whether the trace was incorporated, and the resulting automaton.
     */
	public static Pair<Boolean, Automaton> processTrace(RunAutomaton modelAutomaton, Automaton logAutomaton,
			EfficientLog log, int trace, short[] projectionKey, boolean onlyFittingTraces, ProMCanceller canceller) {
		short[] projectedTrace = log.getProjectedTrace(trace, projectionKey);

		boolean addToAutomaton = !onlyFittingTraces || modelAutomaton.run(Arrays.toString(projectedTrace));
		if (addToAutomaton) {
			String automatonString = projectedTraceToString(projectedTrace);
			if (logAutomaton == null) {
				logAutomaton = Automaton.makeString(automatonString);
				logAutomaton.expandSingleton();
			} else {
				logAutomaton.union(Automaton.makeString(automatonString));
			}
			if (canceller.isCancelled()) {
				return null;
			}
		}
		return Pair.of(addToAutomaton, logAutomaton);
	}


    public static String projectedTraceToString(short[] projectedTrace) {
        String result = "";

        for(int event = 0; event < projectedTrace.length; ++event) {
            if (projectedTrace[event] >= 0) {
                result = result + (char)projectedTrace[event];
            }
        }

        return result;
    }
}
