package org.processmining.eigenvalue.automata;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;
import org.processmining.models.semantics.petrinet.Marking;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

public class ProjectPetriNetOntoActivities {
	public static AcceptingPetriNet project(AcceptingPetriNet net, ProMCanceller canceller, String... names) {
		PetrinetImpl newPetriNet = new PetrinetImpl("projected Petri net");

		Map<PetrinetNode, PetrinetNode> net2reduced = new THashMap<>();

		//copy places
		for (Place p : net.getNet().getPlaces()) {
			net2reduced.put(p, newPetriNet.addPlace(p.getLabel()));
			
			if (canceller.isCancelled()) {
				return null;
			}
		}

		//copy transitions
		for (Transition t : net.getNet().getTransitions()) {
			if (t.isInvisible() || !ArrayUtils.contains(names, t.getLabel()) || t.getLabel().equals("tau")) {
				//copy as invisible
				Transition reducedT = newPetriNet.addTransition("tau");
				reducedT.setInvisible(true);
				net2reduced.put(t, reducedT);
			} else {
				//copy normally
				for (int i = 0; i < names.length; i++) {
					if (names[i].equals(t.getLabel())) {
						net2reduced.put(t, newPetriNet.addTransition(((char) i) + ""));
						break;
					}
				}
			}
			
			if (canceller.isCancelled()) {
				return null;
			}
		}

		//copy edges
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e : net.getNet().getEdges()) {
			if (e.getSource() instanceof Place) {
				newPetriNet.addArc((Place) net2reduced.get(e.getSource()), (Transition) net2reduced.get(e.getTarget()));
			} else {
				newPetriNet.addArc((Transition) net2reduced.get(e.getSource()), (Place) net2reduced.get(e.getTarget()));
			}
			
			if (canceller.isCancelled()) {
				return null;
			}
		}

		//copy initial marking
		Marking newInitialMarking = new Marking();
		for (Place p : net.getInitialMarking()) {
			newInitialMarking.add((Place) net2reduced.get(p), net.getInitialMarking().occurrences(p));
		}

		//copy final markings
		Set<Marking> newFinalMarkings = new THashSet<>();
		for (Marking finalMarking : net.getFinalMarkings()) {
			Marking newFinalMarking = new Marking();
			for (Place p : finalMarking) {
				newFinalMarking.add((Place) net2reduced.get(p), finalMarking.occurrences(p));
			}
			newFinalMarkings.add(newFinalMarking);
		}

		return AcceptingPetriNetFactory.createAcceptingPetriNet(newPetriNet, newInitialMarking, newFinalMarkings);
	}
}
