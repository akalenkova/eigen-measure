package org.processmining.eigenvalue.automata;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Map;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.EfficientPetrinetSemantics;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.models.semantics.petrinet.impl.EfficientPetrinetSemanticsImpl;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.StatePair;
import gnu.trove.map.hash.TCustomHashMap;
import gnu.trove.set.hash.TCustomHashSet;
import gnu.trove.set.hash.THashSet;
import gnu.trove.strategy.HashingStrategy;

public class AcceptingPetriNet2automaton {

	/**
	 * The ReachabilityGraph class of ProM gives errors.
	 * 
	 * Idea: construct an automaton directly.
	 * 
	 * @param net
	 * @param canceller
	 * @throws AutomatonFailedException
	 */

	public static Automaton convert(AcceptingPetriNet net, long maxStates, ProMCanceller canceller)
			throws AutomatonFailedException {
		Automaton result = new Automaton();

		Map<byte[], State> marking2state = new TCustomHashMap<>(hashingStrategy);

		EfficientPetrinetSemantics semantics = new EfficientPetrinetSemanticsImpl(net.getNet(),
				net.getInitialMarking());

		//transform the final markings
		TCustomHashSet<byte[]> finalMarkings = new TCustomHashSet<>(hashingStrategy);
		for (Marking finalMarking : net.getFinalMarkings()) {
			finalMarkings.add(semantics.convert(finalMarking));
		}

		//make an initial marking and add it to the automaton
		byte[] initialMarking = semantics.convert(net.getInitialMarking());
		State initialState = new State();
		result.setInitialState(initialState);
		marking2state.put(initialMarking, initialState);

		initialState.setAccept(net.getFinalMarkings().contains(net.getInitialMarking()));

		THashSet<StatePair> silentTransitions = new THashSet<>();

		//process all reachable markings
		ArrayDeque<byte[]> markingsToBeProcessed = new ArrayDeque<>();
		byte[] currentMarking = initialMarking;
		State currentState = initialState;
		long statesCounter = 0;
		while (currentMarking != null) {
			//System.out.println("Consider new marking. States seen: " + statesCounter + ", states in queue: " + markingsToBeProcessed.size());
			semantics.setState(currentMarking);
			for (Transition transition : net.getNet().getTransitions()) {
				if (semantics.isEnabled(transition)) {

					//execute the transition, record the new marking and immediately put the semantics back for the next round.
					semantics.directExecuteExecutableTransition(transition);
					byte[] newMarking = semantics.getState();
					semantics.setState(currentMarking);

					//look up the destination state, or create it if it doesn't exist yet
					State newState = marking2state.get(newMarking);
					if (newState == null) {
						newState = new State();
						marking2state.put(newMarking, newState);
						newState.setAccept(finalMarkings.contains(newMarking));

						//as this is a new state, we have to process it later on
						markingsToBeProcessed.add(newMarking);
						statesCounter++;
						if (statesCounter > maxStates) {
							throw new AutomatonFailedException("too many states in the reachability graph");
						}
					}

					//connect the two states with an automaton step
					if (transition.isInvisible()) {
						if (currentState != newState) {
							silentTransitions.add(new StatePair(currentState, newState));
						}
					} else {
						currentState.addTransition(
								new dk.brics.automaton.Transition(transition.getLabel().charAt(0), newState));
					}
				}

				if (canceller.isCancelled()) {
					return null;
				}
			}

			//take the next marking to be processed for the next round
			currentMarking = markingsToBeProcessed.poll();
			if (currentMarking != null) {
				currentState = marking2state.get(currentMarking);
			}

			if (canceller.isCancelled()) {
				return null;
			}
		}

		if (!silentTransitions.isEmpty()) {
			//System.out.println("add " + silentTransitions.size() + " silent transitions to " + statesCounter + " states");
			result.addEpsilons(silentTransitions);
			if (canceller.isCancelled()) {
				return null;
			}
		}

		result.minimize();
		if (canceller.isCancelled()) {
			return null;
		}

		return result;
	}

	public static final HashingStrategy<byte[]> hashingStrategy = new HashingStrategy<byte[]>() {
		private static final long serialVersionUID = 1L;

		public int computeHashCode(byte[] object) {
			return Arrays.hashCode(object);
		}

		public boolean equals(byte[] o1, byte[] o2) {
			return Arrays.equals(o1, o2);
		}
	};
}