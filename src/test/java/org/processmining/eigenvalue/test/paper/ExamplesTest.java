///**
// *
// *  Copyright 2019 by Anna Kalenkova <anna.kalenkova@unimelb.edu.au>
// *  Copyright 2018 by Andreas Solti <solti@ai.wu.ac.at>
// *
// *  Licensed under GNU General Public License 3.0 or later. 
// *  Some rights reserved. See COPYING, AUTHORS.
// *
// * @license GPL-3.0+ <http://spdx.org/licenses/GPL-3.0+>
// */
//
//package org.processmining.eigenvalue.test.paper;
//
//import com.google.common.base.Joiner;
//import dk.brics.automaton.Automaton;
//import dk.brics.automaton.State;
//import dk.brics.automaton.Transition;
//import no.uib.cipr.matrix.sparse.CompColMatrix;
//import org.junit.Assert;
//import org.junit.Test;
//import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
//import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
//import org.processmining.eigenvalue.Utils;
//import org.processmining.eigenvalue.automata.PrecisionRecallComputer;
//import org.processmining.eigenvalue.automata.TopologicalEntropyComputer;
//import org.processmining.eigenvalue.data.EntropyPrecisionRecall;
//import org.processmining.eigenvalue.data.EntropyResult;
//import org.processmining.eigenvalue.test.TestUtils;
//import org.processmining.framework.plugin.ProMCanceller;
//import org.processmining.models.graphbased.directed.petrinet.Petrinet;
//import org.processmining.models.graphbased.directed.petrinet.elements.Place;
//import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;
//import org.processmining.models.semantics.petrinet.Marking;
//import org.processmining.plugins.stochasticpetrinet.StochasticNetUtils;
//
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.OutputStreamWriter;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.TreeMap;
//import java.util.stream.IntStream;
//
///**
// * Creates examples used in the publication "Quotients for Behavioural Comparison -
// *                                          Framework and Applications in Process Mining"
// *
// * Stores the examples in {@link TestUtils#TEST_OUTPUT_FOLDER}.
// */
//public class ExamplesTest {
//
//    public static Map<Character, Character> charMap = new HashMap<>();
//    static{
//        charMap.put('a',"\u0000".charAt(0));
//        charMap.put('b',"\u0001".charAt(0));
//        charMap.put('c',"\u0002".charAt(0));
//        charMap.put('d',"\u0003".charAt(0));
//        charMap.put('e',"\u0004".charAt(0));
//        charMap.put('f',"\u0005".charAt(0));
//        charMap.put('g',"\u0006".charAt(0));
//        charMap.put('h',"\u0007".charAt(0));
//        charMap.put('i',"\u0008".charAt(0));
//        charMap.put('u',"\u0009".charAt(0));
//        charMap.put('v',"\u0010".charAt(0));
//        charMap.put('w',"\u0011".charAt(0));
//    }
//
//    public static void wire(State from, char label, State to){
//        from.addTransition(new Transition(charMap.get(label),to));
//    }
//    
//    private static final String TAU = "tau";
//    
//    private static final String[] AB = new String[] {"ab", "ba"};
//    private static final String[] ABBAAC = new String[] {"", "ab", "ba", "ac", "bab"};
//    private static final String[] BAA = new String[] {"bad", "bab"};
//
//    public static Automaton getABC() {
//        Automaton a = new Automaton();
//        State sI = new State();
//        State sII = new State();
//        State sIII = new State();
//        State sIV = new State();
//        a.setInitialState(sI);
//        sIV.setAccept(true);
//        wire(sI, 'a', sII);
//        wire(sII, 'b', sIII);
//        wire(sIII, 'c', sIV);
//        return a;
//    }
//
//    public static Automaton getABC_D() {
//        Automaton a = new Automaton();
//        State sI = new State();
//        State sII = new State();
//        State sIII = new State();
//        State sIV = new State();
//        State sV = new State();
//        a.setInitialState(sI);
//        sIV.setAccept(true);
//        sV.setAccept(true);
//        wire(sI, 'a', sII);
//        wire(sII, 'b', sIII);
//        wire(sIII, 'c', sIV);
//        wire(sIV, 'd', sV);
//        return a;
//    }
//
//    public static Automaton getABC_D_or_E() {
//        Automaton a = new Automaton();
//        State sI = new State();
//        State sII = new State();
//        State sIII = new State();
//        State sIV = new State();
//        State sV = new State();
//        State sVI = new State();
//        a.setInitialState(sI);
//        sIV.setAccept(true);
//        sV.setAccept(true);
//        sVI.setAccept(true);
//        wire(sI, 'a', sII);
//        wire(sII, 'b', sIII);
//        wire(sIII, 'c', sIV);
//        wire(sIV, 'd', sV);
//        wire(sIV, 'e', sVI);
//        return a;
//    }
//
//    public static Automaton getS1() {
//        Automaton a = new Automaton();
//        State sI = new State();
//        State sII = new State();
//        State sIII = new State();
//        State sIV = new State();
//        a.setInitialState(sI);
//        sI.setAccept(true);
//        wire(sI, 'a', sII);
//        wire(sII, 'b', sII);
//        wire(sII, 'c', sII);
//        wire(sII, 'f', sIII);
//        wire(sIII, 'e', sI);
//        wire(sII, 'd', sIV);
//        wire(sIV, 'e', sI);
//        return a;
//    }
//
//    public static Automaton getS2() {
//        Automaton a = new Automaton();
//        State sA = new State();
//        State sB = new State();
//        State sC = new State();
//        State sD = new State();
//        State sE = new State();
//        a.setInitialState(sA);
//        sA.setAccept(true);
//        wire(sA, 'a', sB);
//        wire(sB, 'b', sC);
//        wire(sC, 'c', sB);
//        wire(sB, 'b', sD);
//        wire(sD, 'd', sE);
//        wire(sE, 'e', sA);
//        return a;
//    }
//
//    public static Automaton getS3() {
//        Automaton a = new Automaton();
//        State s1 = new State();
//        State s2 = new State();
//        State s3 = new State();
//        State s4 = new State();
//        State s5 = new State();
//        State s6 = new State();
//        a.setInitialState(s1);
//        s6.setAccept(true);
//        wire(s1, 'a', s2);
//        wire(s2, 'b', s3);
//        wire(s3, 'c', s4);
//        wire(s3, 'd', s5);
//        wire(s4, 'd', s5);
//        wire(s5, 'e', s6);
//        return a;
//    }
//
//    public static Automaton getS4() {
//        Automaton a = new Automaton();
//        State sF = new State();
//        State sG = new State();
//        State sH = new State();
//        State sI = new State();
//        a.setInitialState(sF);
//        sF.setAccept(true);
//        wire(sF, 'a', sG);
//        wire(sG, 'b', sH);
//        wire(sH, 'c', sG);
//        wire(sH, 'd', sI);
//        wire(sI, 'e', sF);
//        return a;
//    }
//
//    public static Automaton getS5() {
//        Automaton a = new Automaton();
//        State s1 = new State();
//        State s2 = new State();
//        State s3 = new State();
//        State s4 = new State();
//        State s5 = new State();
//        State s6 = new State();
//        a.setInitialState(s1);
//        s1.setAccept(true);
//        wire(s1, 'a', s2);
//        wire(s2, 'b', s3);
//        wire(s3, 'c', s4);
//        wire(s4, 'b', s5);
//        wire(s5, 'c', s4);
//        wire(s5, 'd', s6);
//        wire(s6, 'e', s1);
//        return a;
//    }
//
//    public static Automaton getXStar() {
//        Automaton a = new Automaton();
//        State s1 = new State();
//        s1.setAccept(true);
//        a.setInitialState(s1);
//        wire(s1, 'a', s1);
//        wire(s1, 'b', s1);
//        wire(s1, 'c', s1);
//        wire(s1, 'd', s1);
//        wire(s1, 'e', s1);
//        return a;
//    }
//    
//    
//    /**
//     * Construct model for motivating example
//     * 
//     * @return
//     */
//    public static Automaton getMotivModel() {
//        Automaton a = new Automaton();
//        State s1 = new State();
//        State s2 = new State();
//        State s3 = new State();
//        State s4 = new State();
//        State s5 = new State();
//        State s6 = new State();
//        State s7 = new State();
//        
//        a.setInitialState(s1);
//        s7.setAccept(true);
//       
//        wire(s1, 'a', s2);
//        wire(s2, 'b', s3);
//        wire(s2, 'c', s4);
//        wire(s3, 'c', s5);
//        wire(s4, 'b', s5);
//        wire(s5, 'd', s6);
//        wire(s6, 'e', s7);
//        
//        return a;
//    }
//    
//
//    /**
//     * Automaton 1 for paper
//     * 
//     * @return
//     */
//    public static Automaton getSimpleABCAutomaton() {
//        Automaton a = new Automaton();
//        State s1 = new State();
//        State s2 = new State();
//        State s3 = new State();
//        State s4 = new State();
//        a.setInitialState(s1);
//        s4.setAccept(true);
//       
//        wire(s1, 'a', s2);
//        wire(s1, 'b', s3);
//        wire(s2, 'b', s4);
//        wire(s2, 'c', s4);
//        wire(s3, 'a', s4);
//        
//        return a;
//    }
//    
//    /**
//     * Automaton 2 for paper
//     * 
//     * @return
//     */
//    public static Automaton getSimpleABCLoopAutomaton() {
//        Automaton a = new Automaton();
//        State s1 = new State();
//        State s2 = new State();
//        State s3 = new State();
//        State s4 = new State();
//        a.setInitialState(s1);
//        s4.setAccept(true);
//       
//        wire(s1, 'a', s2);
//        wire(s1, 'b', s3);
//        wire(s2, 'b', s4);
//        wire(s2, 'c', s4);
//        wire(s3, 'a', s4);
//        wire(s4, 'd', s3);
//        
//        return a;
//    }
//    
//	/**
//	 * Define models for qualitative test
//	 * @return
//	 */
//    public static AcceptingPetriNet getOriginalModel() {
//
//		Petrinet net = new PetrinetImpl("Original model");
//		Place p1 = net.addPlace("p1");
//		Place p2 = net.addPlace("p2");
//		Place p3 = net.addPlace("p3");
//		Place p4 = net.addPlace("p4");
//		Place p5 = net.addPlace("p5");
//		Place p6 = net.addPlace("p6");
//		Place p7 = net.addPlace("p7");
//		Place p8 = net.addPlace("p8");
//		Place p9 = net.addPlace("p9");
//		Place p10 = net.addPlace("p10");
//
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition aTransition = net
//				.addTransition(charMap.get('a').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition bTransition = net
//				.addTransition(charMap.get('b').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition cTransition = net
//				.addTransition(charMap.get('c').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition dTransition = net
//				.addTransition(charMap.get('d').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition eTransition = net
//				.addTransition(charMap.get('e').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition fTransition = net
//				.addTransition(charMap.get('f').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition gTransition = net
//				.addTransition(charMap.get('g').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition hTransition = net
//				.addTransition(charMap.get('h').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition iTransition = net
//				.addTransition(charMap.get('i').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition tauTransition = net
//				.addTransition(TAU);
//		tauTransition.setInvisible(true);
//
//		net.addArc(p1, aTransition);
//		net.addArc(aTransition, p2);
//		net.addArc(p2, bTransition);
//		net.addArc(p2, cTransition);
//		net.addArc(cTransition, p3);
//		net.addArc(p3, tauTransition);
//		net.addArc(p3, gTransition);
//		net.addArc(bTransition, p4);
//		net.addArc(cTransition, p4);
//		net.addArc(p4, dTransition);
//		net.addArc(bTransition, p5);
//		net.addArc(p5, eTransition);
//		net.addArc(tauTransition, p6);
//		net.addArc(gTransition, p6);
//		net.addArc(p6, hTransition);
//		net.addArc(dTransition, p7);
//		net.addArc(p7, eTransition);
//		net.addArc(p7, fTransition);
//		net.addArc(hTransition, p8);
//		net.addArc(p8, fTransition);
//		net.addArc(eTransition, p9);
//		net.addArc(fTransition, p9);
//		net.addArc(p9, iTransition);
//		net.addArc(iTransition, p10);
//
//		Marking initialMarking = new Marking();
//		initialMarking.add(p1);
//
//		Marking finalMarking = new Marking();
//		finalMarking.add(p10);
//
//		AcceptingPetriNet acceptingNet = new AcceptingPetriNetImpl(net, initialMarking, finalMarking);
//
//		return acceptingNet;
//	}
//
//	public static AcceptingPetriNet getOneTrace() {
//
//		Petrinet net = new PetrinetImpl("One trace");
//		Place p1 = net.addPlace("p1");
//		Place p2 = net.addPlace("p2");
//		Place p3 = net.addPlace("p3");
//		Place p4 = net.addPlace("p4");
//		Place p5 = net.addPlace("p5");
//		Place p6 = net.addPlace("p6");
//
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition aTransition = net
//				.addTransition(charMap.get('a').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition bTransition = net
//				.addTransition(charMap.get('b').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition dTransition = net
//				.addTransition(charMap.get('d').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition eTransition = net
//				.addTransition(charMap.get('e').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition iTransition = net
//				.addTransition(charMap.get('i').toString());
//
//		net.addArc(p1, aTransition);
//		net.addArc(aTransition, p2);
//		net.addArc(p2, bTransition);
//		net.addArc(bTransition, p3);
//		net.addArc(p3, dTransition);
//		net.addArc(dTransition, p4);
//		net.addArc(p4, eTransition);
//		net.addArc(eTransition, p5);
//		net.addArc(p5, iTransition);
//		net.addArc(iTransition, p6);
//
//		Marking initialMarking = new Marking();
//		initialMarking.add(p1);
//
//		Marking finalMarking = new Marking();
//		finalMarking.add(p6);
//
//		AcceptingPetriNet acceptingNet = new AcceptingPetriNetImpl(net, initialMarking, finalMarking);
//
//		return acceptingNet;
//	}
//
//	public static AcceptingPetriNet getSeparateTraces() {
//
//		Petrinet net = new PetrinetImpl("Separate traces");
//
//		Place p1 = net.addPlace("p1");
//		Place p2 = net.addPlace("p2");
//		Place p3 = net.addPlace("p3");
//		Place p4 = net.addPlace("p4");
//		Place p5 = net.addPlace("p5");
//		Place p6 = net.addPlace("p6");
//
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition a1Transition = net
//				.addTransition(charMap.get('a').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition b1Transition = net
//				.addTransition(charMap.get('b').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition d1Transition = net
//				.addTransition(charMap.get('d').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition e1Transition = net
//				.addTransition(charMap.get('e').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition i1Transition = net
//				.addTransition(charMap.get('i').toString());
//
//		net.addArc(p1, a1Transition);
//		net.addArc(a1Transition, p2);
//		net.addArc(p2, b1Transition);
//		net.addArc(b1Transition, p3);
//		net.addArc(p3, d1Transition);
//		net.addArc(d1Transition, p4);
//		net.addArc(p4, e1Transition);
//		net.addArc(e1Transition, p5);
//		net.addArc(p5, i1Transition);
//		net.addArc(i1Transition, p6);
//
//		Place p7 = net.addPlace("p7");
//		Place p8 = net.addPlace("p8");
//		Place p9 = net.addPlace("p9");
//		Place p10 = net.addPlace("p10");
//		Place p11 = net.addPlace("p11");
//		Place p12 = net.addPlace("p12");
//
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition a2Transition = net
//				.addTransition(charMap.get('a').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition c2Transition = net
//				.addTransition(charMap.get('c').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition d2Transition = net
//				.addTransition(charMap.get('d').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition g2Transition = net
//				.addTransition(charMap.get('g').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition h2Transition = net
//				.addTransition(charMap.get('h').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition f2Transition = net
//				.addTransition(charMap.get('f').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition i2Transition = net
//				.addTransition(charMap.get('i').toString());
//
//		net.addArc(p1, a2Transition);
//		net.addArc(a2Transition, p7);
//		net.addArc(p7, c2Transition);
//		net.addArc(c2Transition, p8);
//		net.addArc(p8, d2Transition);
//		net.addArc(d2Transition, p9);
//		net.addArc(p9, g2Transition);
//		net.addArc(g2Transition, p10);
//		net.addArc(p10, h2Transition);
//		net.addArc(h2Transition, p11);
//		net.addArc(p11, f2Transition);
//		net.addArc(f2Transition, p12);
//		net.addArc(p12, i2Transition);
//		net.addArc(i2Transition, p6);
//
//		Place p13 = net.addPlace("p13");
//		Place p14 = net.addPlace("p14");
//		Place p15 = net.addPlace("p15");
//		Place p16 = net.addPlace("p16");
//		Place p17 = net.addPlace("p17");
//		Place p18 = net.addPlace("p18");
//
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition a3Transition = net
//				.addTransition(charMap.get('a').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition c3Transition = net
//				.addTransition(charMap.get('c').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition g3Transition = net
//				.addTransition(charMap.get('g').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition d3Transition = net
//				.addTransition(charMap.get('d').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition h3Transition = net
//				.addTransition(charMap.get('h').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition f3Transition = net
//				.addTransition(charMap.get('f').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition i3Transition = net
//				.addTransition(charMap.get('i').toString());
//
//		net.addArc(p1, a3Transition);
//		net.addArc(a3Transition, p13);
//		net.addArc(p13, c3Transition);
//		net.addArc(c3Transition, p14);
//		net.addArc(p14, g3Transition);
//		net.addArc(g3Transition, p15);
//		net.addArc(p15, d3Transition);
//		net.addArc(d3Transition, p16);
//		net.addArc(p16, h3Transition);
//		net.addArc(h3Transition, p17);
//		net.addArc(p17, f3Transition);
//		net.addArc(f3Transition, p18);
//		net.addArc(p18, i3Transition);
//		net.addArc(i3Transition, p6);
//
//		Place p19 = net.addPlace("p19");
//		Place p20 = net.addPlace("p20");
//		Place p21 = net.addPlace("p21");
//		Place p22 = net.addPlace("p22");
//		Place p23 = net.addPlace("p23");
//
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition a4Transition = net
//				.addTransition(charMap.get('a').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition c4Transition = net
//				.addTransition(charMap.get('c').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition h4Transition = net
//				.addTransition(charMap.get('h').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition d4Transition = net
//				.addTransition(charMap.get('d').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition f4Transition = net
//				.addTransition(charMap.get('f').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition i4Transition = net
//				.addTransition(charMap.get('i').toString());
//
//		net.addArc(p1, a4Transition);
//		net.addArc(a4Transition, p19);
//		net.addArc(p19, c4Transition);
//		net.addArc(c4Transition, p20);
//		net.addArc(p20, h4Transition);
//		net.addArc(h4Transition, p21);
//		net.addArc(p21, d4Transition);
//		net.addArc(d4Transition, p22);
//		net.addArc(p22, f4Transition);
//		net.addArc(f4Transition, p23);
//		net.addArc(p23, i4Transition);
//		net.addArc(i4Transition, p6);
//
//		Place p24 = net.addPlace("p24");
//		Place p25 = net.addPlace("p25");
//		Place p26 = net.addPlace("p26");
//		Place p27 = net.addPlace("p27");
//		Place p28 = net.addPlace("p28");
//
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition a5Transition = net
//				.addTransition(charMap.get('a').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition c5Transition = net
//				.addTransition(charMap.get('c').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition d5Transition = net
//				.addTransition(charMap.get('d').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition h5Transition = net
//				.addTransition(charMap.get('h').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition f5Transition = net
//				.addTransition(charMap.get('f').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition i5Transition = net
//				.addTransition(charMap.get('i').toString());
//
//		net.addArc(p1, a5Transition);
//		net.addArc(a5Transition, p24);
//		net.addArc(p24, c5Transition);
//		net.addArc(c5Transition, p25);
//		net.addArc(p25, d5Transition);
//		net.addArc(d5Transition, p26);
//		net.addArc(p26, h5Transition);
//		net.addArc(h5Transition, p27);
//		net.addArc(p27, f5Transition);
//		net.addArc(f5Transition, p28);
//		net.addArc(p28, i5Transition);
//		net.addArc(i5Transition, p6);
//
//		Marking initialMarking = new Marking();
//		initialMarking.add(p1);
//
//		Marking finalMarking = new Marking();
//		finalMarking.add(p6);
//
//		AcceptingPetriNet acceptingNet = new AcceptingPetriNetImpl(net, initialMarking, finalMarking);
//
//		return acceptingNet;
//	}
//
//	public static AcceptingPetriNet getFlowerModel() {
//
//		Petrinet net = new PetrinetImpl("Flower model");
//		Place p1 = net.addPlace("p1");
//		Place p2 = net.addPlace("p2");
//		Place p3 = net.addPlace("p3");
//
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition aTransition = net
//				.addTransition(charMap.get('a').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition bTransition = net
//				.addTransition(charMap.get('b').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition cTransition = net
//				.addTransition(charMap.get('c').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition dTransition = net
//				.addTransition(charMap.get('d').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition eTransition = net
//				.addTransition(charMap.get('e').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition fTransition = net
//				.addTransition(charMap.get('f').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition gTransition = net
//				.addTransition(charMap.get('g').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition hTransition = net
//				.addTransition(charMap.get('h').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition iTransition = net
//				.addTransition(charMap.get('i').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition tau1Transition = net
//				.addTransition(TAU);
//		tau1Transition.setInvisible(true);
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition tau2Transition = net
//				.addTransition(TAU);
//		tau2Transition.setInvisible(true);
//
//		net.addArc(p1, tau1Transition);
//		net.addArc(tau1Transition, p2);
//		net.addArc(p2, aTransition);
//		net.addArc(aTransition, p2);
//		net.addArc(p2, bTransition);
//		net.addArc(bTransition, p2);
//		net.addArc(p2, cTransition);
//		net.addArc(cTransition, p2);
//		net.addArc(p2, dTransition);
//		net.addArc(dTransition, p2);
//		net.addArc(p2, eTransition);
//		net.addArc(eTransition, p2);
//		net.addArc(p2, fTransition);
//		net.addArc(fTransition, p2);
//		net.addArc(p2, gTransition);
//		net.addArc(gTransition, p2);
//		net.addArc(p2, hTransition);
//		net.addArc(hTransition, p2);
//		net.addArc(p2, iTransition);
//		net.addArc(iTransition, p2);
//		net.addArc(p2, tau2Transition);
//		net.addArc(tau2Transition, p3);
//
//		Marking initialMarking = new Marking();
//		initialMarking.add(p1);
//
//		Marking finalMarking = new Marking();
//		finalMarking.add(p3);
//
//		AcceptingPetriNet acceptingNet = new AcceptingPetriNetImpl(net, initialMarking, finalMarking);
//
//		return acceptingNet;
//	}
//
//	public static AcceptingPetriNet getAllParallelModel() {
//
//		Petrinet net = new PetrinetImpl("All parallel model");
//		Place p1 = net.addPlace("p1");
//		Place p2 = net.addPlace("p2");
//		Place p3 = net.addPlace("p3");
//		Place p4 = net.addPlace("p4");
//		Place p5 = net.addPlace("p5");
//		Place p6 = net.addPlace("p6");
//		Place p7 = net.addPlace("p7");
//		Place p8 = net.addPlace("p8");
//		Place p9 = net.addPlace("p9");
//		Place p10 = net.addPlace("p10");
//		Place p11 = net.addPlace("p11");
//		Place p12 = net.addPlace("p12");
//		Place p13 = net.addPlace("p13");
//		Place p14 = net.addPlace("p14");
//		Place p15 = net.addPlace("p15");
//		Place p16 = net.addPlace("p16");
//		Place p17 = net.addPlace("p17");
//		Place p18 = net.addPlace("p18");
//		Place p19 = net.addPlace("p19");
//		Place p20 = net.addPlace("p20");
//
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition aTransition = net
//				.addTransition(charMap.get('a').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition bTransition = net
//				.addTransition(charMap.get('b').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition cTransition = net
//				.addTransition(charMap.get('c').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition dTransition = net
//				.addTransition(charMap.get('d').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition eTransition = net
//				.addTransition(charMap.get('e').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition fTransition = net
//				.addTransition(charMap.get('f').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition gTransition = net
//				.addTransition(charMap.get('g').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition hTransition = net
//				.addTransition(charMap.get('h').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition iTransition = net
//				.addTransition(charMap.get('i').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition tau1Transition = net
//				.addTransition(TAU);
//		tau1Transition.setInvisible(true);
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition tau2Transition = net
//				.addTransition(TAU);
//		tau2Transition.setInvisible(true);
//
//		net.addArc(p1, tau1Transition);
//		net.addArc(tau1Transition, p2);
//		net.addArc(tau1Transition, p3);
//		net.addArc(tau1Transition, p4);
//		net.addArc(tau1Transition, p5);
//		net.addArc(tau1Transition, p6);
//		net.addArc(tau1Transition, p7);
//		net.addArc(tau1Transition, p8);
//		net.addArc(tau1Transition, p9);
//		net.addArc(tau1Transition, p10);
//		net.addArc(p2, aTransition);
//		net.addArc(p3, bTransition);
//		net.addArc(p4, cTransition);
//		net.addArc(p5, dTransition);
//		net.addArc(p6, eTransition);
//		net.addArc(p7, fTransition);
//		net.addArc(p8, gTransition);
//		net.addArc(p9, hTransition);
//		net.addArc(p10, iTransition);
//		net.addArc(aTransition, p11);
//		net.addArc(bTransition, p12);
//		net.addArc(cTransition, p13);
//		net.addArc(dTransition, p14);
//		net.addArc(eTransition, p15);
//		net.addArc(fTransition, p16);
//		net.addArc(gTransition, p17);
//		net.addArc(hTransition, p18);
//		net.addArc(iTransition, p19);
//		net.addArc(p11, tau2Transition);
//		net.addArc(p12, tau2Transition);
//		net.addArc(p13, tau2Transition);
//		net.addArc(p14, tau2Transition);
//		net.addArc(p15, tau2Transition);
//		net.addArc(p16, tau2Transition);
//		net.addArc(p17, tau2Transition);
//		net.addArc(p18, tau2Transition);
//		net.addArc(p19, tau2Transition);
//		net.addArc(tau2Transition, p20);
//
//		Marking initialMarking = new Marking();
//		initialMarking.add(p1);
//
//		Marking finalMarking = new Marking();
//		finalMarking.add(p20);
//
//		AcceptingPetriNet acceptingNet = new AcceptingPetriNetImpl(net, initialMarking, finalMarking);
//
//		return acceptingNet;
//	}
//
//	public static AcceptingPetriNet getRoundRobinModel() {
//
//		Petrinet net = new PetrinetImpl("Round robin model");
//		Place p1 = net.addPlace("p1");
//		Place p2 = net.addPlace("p2");
//		Place p3 = net.addPlace("p3");
//		Place p4 = net.addPlace("p4");
//		Place p5 = net.addPlace("p5");
//		Place p6 = net.addPlace("p6");
//		Place p7 = net.addPlace("p7");
//		Place p8 = net.addPlace("p8");
//		Place p9 = net.addPlace("p9");
//		Place p10 = net.addPlace("p10");
//		Place p11 = net.addPlace("p11");
//		Place p12 = net.addPlace("p12");
//		Place p13 = net.addPlace("p13");
//		Place p14 = net.addPlace("p14");
//		Place p15 = net.addPlace("p15");
//		Place p16 = net.addPlace("p16");
//		Place p17 = net.addPlace("p17");
//		Place p18 = net.addPlace("p18");
//		Place p19 = net.addPlace("p19");
//		Place p20 = net.addPlace("p20");
//
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition aTransition = net
//				.addTransition(charMap.get('a').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition bTransition = net
//				.addTransition(charMap.get('b').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition cTransition = net
//				.addTransition(charMap.get('c').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition dTransition = net
//				.addTransition(charMap.get('d').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition eTransition = net
//				.addTransition(charMap.get('e').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition fTransition = net
//				.addTransition(charMap.get('f').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition gTransition = net
//				.addTransition(charMap.get('g').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition hTransition = net
//				.addTransition(charMap.get('h').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition iTransition = net
//				.addTransition(charMap.get('i').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition tau1Transition = net
//				.addTransition(TAU);
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition tau2Transition = net
//				.addTransition(TAU);
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition tau3Transition = net
//				.addTransition(TAU);
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition tau4Transition = net
//				.addTransition(TAU);
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition tau5Transition = net
//				.addTransition(TAU);
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition tau6Transition = net
//				.addTransition(TAU);
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition tau7Transition = net
//				.addTransition(TAU);
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition tau8Transition = net
//				.addTransition(TAU);
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition tau9Transition = net
//				.addTransition(TAU);
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition tau10Transition = net
//				.addTransition(TAU);
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition tau11Transition = net
//				.addTransition(TAU);
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition tau12Transition = net
//				.addTransition(TAU);
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition tau13Transition = net
//				.addTransition(TAU);
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition tau14Transition = net
//				.addTransition(TAU);
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition tau15Transition = net
//				.addTransition(TAU);
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition tau16Transition = net
//				.addTransition(TAU);
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition tau17Transition = net
//				.addTransition(TAU);
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition tau18Transition = net
//				.addTransition(TAU);
//
//		tau1Transition.setInvisible(true);
//		tau2Transition.setInvisible(true);
//		tau3Transition.setInvisible(true);
//		tau4Transition.setInvisible(true);
//		tau5Transition.setInvisible(true);
//		tau6Transition.setInvisible(true);
//		tau7Transition.setInvisible(true);
//		tau8Transition.setInvisible(true);
//		tau9Transition.setInvisible(true);
//		tau10Transition.setInvisible(true);
//		tau11Transition.setInvisible(true);
//		tau12Transition.setInvisible(true);
//		tau13Transition.setInvisible(true);
//		tau14Transition.setInvisible(true);
//		tau15Transition.setInvisible(true);
//		tau16Transition.setInvisible(true);
//		tau17Transition.setInvisible(true);
//		tau18Transition.setInvisible(true);
//
//		net.addArc(p2, aTransition);
//		net.addArc(aTransition, p3);
//		net.addArc(p3, bTransition);
//		net.addArc(bTransition, p4);
//		net.addArc(p4, cTransition);
//		net.addArc(cTransition, p5);
//		net.addArc(p5, dTransition);
//		net.addArc(dTransition, p6);
//		net.addArc(p6, eTransition);
//		net.addArc(eTransition, p7);
//		net.addArc(p7, fTransition);
//		net.addArc(fTransition, p8);
//		net.addArc(p8, gTransition);
//		net.addArc(gTransition, p9);
//		net.addArc(p9, hTransition);
//		net.addArc(hTransition, p10);
//		net.addArc(p10, iTransition);
//		net.addArc(iTransition, p2);
//
//		net.addArc(p1, tau1Transition);
//		net.addArc(p1, tau3Transition);
//		net.addArc(p1, tau5Transition);
//		net.addArc(p1, tau7Transition);
//		net.addArc(p1, tau9Transition);
//		net.addArc(p1, tau11Transition);
//		net.addArc(p1, tau13Transition);
//		net.addArc(p1, tau15Transition);
//		net.addArc(p1, tau17Transition);
//
//		net.addArc(tau2Transition, p20);
//		net.addArc(tau4Transition, p20);
//		net.addArc(tau6Transition, p20);
//		net.addArc(tau8Transition, p20);
//		net.addArc(tau10Transition, p20);
//		net.addArc(tau12Transition, p20);
//		net.addArc(tau14Transition, p20);
//		net.addArc(tau16Transition, p20);
//		net.addArc(tau18Transition, p20);
//
//		net.addArc(p2, tau2Transition);
//		net.addArc(p3, tau4Transition);
//		net.addArc(p4, tau6Transition);
//		net.addArc(p5, tau8Transition);
//		net.addArc(p6, tau10Transition);
//		net.addArc(p7, tau12Transition);
//		net.addArc(p8, tau14Transition);
//		net.addArc(p9, tau16Transition);
//		net.addArc(p10, tau18Transition);
//
//		net.addArc(tau17Transition, p2);
//		net.addArc(tau1Transition, p3);
//		net.addArc(tau3Transition, p4);
//		net.addArc(tau5Transition, p5);
//		net.addArc(tau7Transition, p6);
//		net.addArc(tau9Transition, p7);
//		net.addArc(tau11Transition, p8);
//		net.addArc(tau13Transition, p9);
//		net.addArc(tau15Transition, p10);
//
//		net.addArc(tau1Transition, p11);
//		net.addArc(p11, tau2Transition);
//		net.addArc(tau3Transition, p12);
//		net.addArc(p12, tau4Transition);
//		net.addArc(tau5Transition, p13);
//		net.addArc(p13, tau6Transition);
//		net.addArc(tau7Transition, p14);
//		net.addArc(p14, tau8Transition);
//		net.addArc(tau9Transition, p15);
//		net.addArc(p15, tau10Transition);
//		net.addArc(tau11Transition, p16);
//		net.addArc(p16, tau12Transition);
//		net.addArc(tau13Transition, p17);
//		net.addArc(p17, tau14Transition);
//		net.addArc(tau15Transition, p18);
//		net.addArc(p18, tau16Transition);
//		net.addArc(tau17Transition, p19);
//		net.addArc(p19, tau18Transition);
//
//		Marking initialMarking = new Marking();
//		initialMarking.add(p1);
//
//		Marking finalMarking = new Marking();
//		finalMarking.add(p20);
//
//		AcceptingPetriNet acceptingNet = new AcceptingPetriNetImpl(net, initialMarking, finalMarking);
//
//		return acceptingNet;
//	}
//
//	public static AcceptingPetriNet getHGParallelModel() {
//
//		Petrinet net = new PetrinetImpl("HG Parallel model");
//		Place p1 = net.addPlace("p1");
//		Place p2 = net.addPlace("p2");
//		Place p3 = net.addPlace("p3");
//		Place p4 = net.addPlace("p4");
//		Place p5 = net.addPlace("p5");
//		Place p6 = net.addPlace("p6");
//		Place p7 = net.addPlace("p7");
//		Place p8 = net.addPlace("p8");
//		Place p9 = net.addPlace("p9");
//		Place p10 = net.addPlace("p10");
//		Place p11 = net.addPlace("p11");
//
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition aTransition = net
//				.addTransition(charMap.get('a').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition bTransition = net
//				.addTransition(charMap.get('b').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition cTransition = net
//				.addTransition(charMap.get('c').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition dTransition = net
//				.addTransition(charMap.get('d').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition eTransition = net
//				.addTransition(charMap.get('e').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition fTransition = net
//				.addTransition(charMap.get('f').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition gTransition = net
//				.addTransition(charMap.get('g').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition hTransition = net
//				.addTransition(charMap.get('h').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition iTransition = net
//				.addTransition(charMap.get('i').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition tau1Transition = net
//				.addTransition(TAU);
//		tau1Transition.setInvisible(true);
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition tau2Transition = net
//				.addTransition(TAU);
//		tau2Transition.setInvisible(true);
//
//		net.addArc(p1, aTransition);
//		net.addArc(aTransition, p2);
//		net.addArc(p2, bTransition);
//		net.addArc(p2, cTransition);
//		net.addArc(bTransition, p3);
//		net.addArc(cTransition, p3);
//		net.addArc(cTransition, p4);
//		net.addArc(cTransition, p5);
//		net.addArc(bTransition, p6);
//		net.addArc(p3, dTransition);
//		net.addArc(p4, tau1Transition);
//		net.addArc(p4, gTransition);
//		net.addArc(p5, hTransition);
//		net.addArc(p5, tau2Transition);
//		net.addArc(p6, eTransition);
//		net.addArc(dTransition, p7);
//		net.addArc(tau1Transition, p8);
//		net.addArc(gTransition, p8);
//		net.addArc(tau2Transition, p9);
//		net.addArc(hTransition, p9);
//		net.addArc(p7, eTransition);
//		net.addArc(p7, fTransition);
//		net.addArc(p8, fTransition);
//		net.addArc(p9, fTransition);
//		net.addArc(eTransition, p10);
//		net.addArc(fTransition, p10);
//		net.addArc(p10, iTransition);
//		net.addArc(iTransition, p11);
//
//		Marking initialMarking = new Marking();
//		initialMarking.add(p1);
//
//		Marking finalMarking = new Marking();
//		finalMarking.add(p11);
//
//		AcceptingPetriNet acceptingNet = new AcceptingPetriNetImpl(net, initialMarking, finalMarking);
//
//		return acceptingNet;
//	}
//
//	public static AcceptingPetriNet getHGSelfLoops() {
//
//		Petrinet net = new PetrinetImpl("HG Self-loops model");
//		Place p1 = net.addPlace("p1");
//		Place p2 = net.addPlace("p2");
//		Place p3 = net.addPlace("p3");
//		Place p4 = net.addPlace("p4");
//		Place p5 = net.addPlace("p5");
//		Place p7 = net.addPlace("p7");
//		Place p9 = net.addPlace("p9");
//		Place p10 = net.addPlace("p10");
//
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition aTransition = net
//				.addTransition(charMap.get('a').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition bTransition = net
//				.addTransition(charMap.get('b').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition cTransition = net
//				.addTransition(charMap.get('c').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition dTransition = net
//				.addTransition(charMap.get('d').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition eTransition = net
//				.addTransition(charMap.get('e').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition fTransition = net
//				.addTransition(charMap.get('f').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition gTransition = net
//				.addTransition(charMap.get('g').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition hTransition = net
//				.addTransition(charMap.get('h').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition iTransition = net
//				.addTransition(charMap.get('i').toString());
//
//		net.addArc(p1, aTransition);
//		net.addArc(aTransition, p2);
//		net.addArc(p2, bTransition);
//		net.addArc(p2, cTransition);
//		net.addArc(cTransition, p3);
//		net.addArc(p3, hTransition);
//		net.addArc(p3, gTransition);
//		net.addArc(gTransition, p3);
//		net.addArc(hTransition, p3);
//		net.addArc(bTransition, p4);
//		net.addArc(cTransition, p4);
//		net.addArc(p4, dTransition);
//		net.addArc(bTransition, p5);
//		net.addArc(p5, eTransition);
//		net.addArc(dTransition, p7);
//		net.addArc(p7, eTransition);
//		net.addArc(p3, fTransition);
//		net.addArc(p7, fTransition);
//		net.addArc(eTransition, p9);
//		net.addArc(fTransition, p9);
//		net.addArc(p9, iTransition);
//		net.addArc(iTransition, p10);
//
//		Marking initialMarking = new Marking();
//		initialMarking.add(p1);
//
//		Marking finalMarking = new Marking();
//		finalMarking.add(p10);
//
//		AcceptingPetriNet acceptingNet = new AcceptingPetriNetImpl(net, initialMarking, finalMarking);
//
//		return acceptingNet;
//	}
//
//	public static AcceptingPetriNet getDSelfLoopModel() {
//
//		Petrinet net = new PetrinetImpl("D Self-loop model");
//		Place p1 = net.addPlace("p1");
//		Place p2 = net.addPlace("p2");
//		Place p3 = net.addPlace("p3");
//		Place p4 = net.addPlace("p4");
//		Place p5 = net.addPlace("p5");
//		Place p6 = net.addPlace("p6");
//		Place p8 = net.addPlace("p8");
//		Place p9 = net.addPlace("p9");
//		Place p10 = net.addPlace("p10");
//
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition aTransition = net
//				.addTransition(charMap.get('a').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition bTransition = net
//				.addTransition(charMap.get('b').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition cTransition = net
//				.addTransition(charMap.get('c').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition dTransition = net
//				.addTransition(charMap.get('d').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition eTransition = net
//				.addTransition(charMap.get('e').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition fTransition = net
//				.addTransition(charMap.get('f').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition gTransition = net
//				.addTransition(charMap.get('g').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition hTransition = net
//				.addTransition(charMap.get('h').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition iTransition = net
//				.addTransition(charMap.get('i').toString());
//		org.processmining.models.graphbased.directed.petrinet.elements.Transition tauTransition = net
//				.addTransition(TAU);
//		tauTransition.setInvisible(true);
//
//		net.addArc(p1, aTransition);
//		net.addArc(aTransition, p2);
//		net.addArc(p2, bTransition);
//		net.addArc(p2, cTransition);
//		net.addArc(cTransition, p3);
//		net.addArc(p3, tauTransition);
//		net.addArc(p3, gTransition);
//		net.addArc(bTransition, p4);
//		net.addArc(cTransition, p4);
//		net.addArc(dTransition, p4);
//		net.addArc(p4, dTransition);
//
//		net.addArc(bTransition, p5);
//		net.addArc(p5, eTransition);
//		net.addArc(tauTransition, p6);
//		net.addArc(gTransition, p6);
//		net.addArc(p6, hTransition);
//		net.addArc(p4, eTransition);
//		net.addArc(p4, fTransition);
//		net.addArc(hTransition, p8);
//		net.addArc(p8, fTransition);
//		net.addArc(eTransition, p9);
//		net.addArc(fTransition, p9);
//		net.addArc(p9, iTransition);
//		net.addArc(iTransition, p10);
//
//		Marking initialMarking = new Marking();
//		initialMarking.add(p1);
//
//		Marking finalMarking = new Marking();
//		finalMarking.add(p10);
//
//		AcceptingPetriNet acceptingNet = new AcceptingPetriNetImpl(net, initialMarking, finalMarking);
//
//		return acceptingNet;
//	}
//    
//    public static Automaton getL1() {
//        return TestUtils.getLogAutomaton("abde", "abcbcde");
//    }
//
//    public static Automaton getL2() {
//        return TestUtils.getLogAutomaton("abde","abcbcde","abccde","afe","afe");
//    }
//
//    public static Automaton getL3() {
//        return TestUtils.getLogAutomaton("abcbcde", "abbf", "afe");
//    }
//    
//    /**
//     * Automata for paper
//     * @return
//     */
//    public static Automaton getLABBA() {
//        return TestUtils.getLogAutomaton(AB);
//    }
//    public static Automaton getLABBAACBAB() {
//        return TestUtils.getLogAutomaton(ABBAAC);
//    }
//    public static Automaton getLBADBAB() {
//        return TestUtils.getLogAutomaton(BAA);
//    }
//    
//    /**
//     * Define logs for motivating example
//     * 
//     * @return
//     */
//    public static Automaton getL1Motiv() {
//        return TestUtils.getLogAutomaton("abbce");
//    }
//    
//    public static Automaton getL2Motiv() {
//        return TestUtils.getLogAutomaton("abbcde");
//    }
//    
//    /**
//     * Define log for qualitative test
//     * @return
//     */
//    public static Automaton getLQualitative() {
//        return TestUtils.getLogAutomaton("abdei", "acdghfi", "acgdhfi", "achdfi", "acdhfi");
//    }
//
//
//    @Test
//    public void testS1() {
//        TestUtils.outputPNG(getS1(),"S1_orig");
//    }
//
//    @Test
//    public void testS1L1() {
//        String mName = "S1";
//        String lName = "L1";
//
//        Automaton aM = getS1();
//        Automaton aL = getL1();
//
//
//        getPrecisionAndRecall(mName, lName, aM, aL, false);
//    }
//
//    @Test
//    public void testS1L2() {
//        String mName = "S1";
//        String lName = "L2";
//
//        Automaton aM = getS1();
//        Automaton aL = getL2();
//
//
//        getPrecisionAndRecall(mName, lName, aM, aL, false);
//    }
//
//    @Test
//    public void testS1L3() {
//        String mName = "S1";
//        String lName = "L3";
//
//        Automaton aM = getS1();
//        Automaton aL = getL3();
//
//        Automaton aMaL = aM.intersection(aL, Utils.NOT_CANCELLER);
//        aMaL.minimize(Utils.NOT_CANCELLER);
//        TestUtils.outputPNG(aMaL, "S1_intersect_L3");
//
//        getPrecisionAndRecall(mName, lName, aM, aL, false);
//    }
//
//    @Test
//    public void testRelations() {
//        // check if S5 in S4 in S1
//
//        // check S5 in S4:
//        Automaton s1 = getS1();
//        Automaton s4 = getS4();
//        Automaton s5 = getS5();
//
//        // s4 in s1, if intersection = s4
//        Automaton s1s4 = s1.intersection(s4, Utils.NOT_CANCELLER);
//        Assert.assertEquals(s1s4, s4);
//        Assert.assertEquals(s1s4.getNumberOfStates(), s4.getNumberOfStates());
//
//        // s5 in s4 -> s4 cap s5 = s5
//        Automaton s4s5 = s4.intersection(s5, Utils.NOT_CANCELLER);
//        Assert.assertEquals(s4s5, s5);
//
//        // s5 in s4 in s1 -> s1 cap s5 = s5
//        Automaton s1s5 = s1.intersection(s5, Utils.NOT_CANCELLER);
//        Assert.assertEquals(s1s5, s5);
//    }
//
//    @Test
//    public void testQuotientsSection4() {
//        Automaton s1 = getS1();
//        Automaton s4 = getS4();
//        Automaton s5 = getS5();
//
//        Automaton sX = getXStar();
//
//        EntropyResult resultS1 = PrecisionRecallComputer.getResult("S1", s1.getNumberOfStates(), TopologicalEntropyComputer.getTopologicalEntropy(s1, "s1", Utils.NOT_CANCELLER, 0.0));
//        EntropyResult resultS4 = PrecisionRecallComputer.getResult("S4", s4.getNumberOfStates(), TopologicalEntropyComputer.getTopologicalEntropy(s4, "s4", Utils.NOT_CANCELLER, 0.0));
//        EntropyResult resultS5 = PrecisionRecallComputer.getResult("S5", s5.getNumberOfStates(), TopologicalEntropyComputer.getTopologicalEntropy(s5, "s5", Utils.NOT_CANCELLER, 0.0));
//
//        EntropyResult resultSX = PrecisionRecallComputer.getResult("X", sX.getNumberOfStates(), TopologicalEntropyComputer.getTopologicalEntropy(sX, "X", Utils.NOT_CANCELLER, 0.0));
//
//        double u = resultS4.largestEigenvalue/resultS1.largestEigenvalue;
//        double v = resultS5.largestEigenvalue/resultS1.largestEigenvalue;
//        double w = resultS5.largestEigenvalue/resultS4.largestEigenvalue;
//
//        double x = resultS5.largestEigenvalue/resultSX.largestEigenvalue;
//
//        System.out.println("u = "+u);
//        System.out.println("v = "+v);
//        System.out.println("w = "+w);
//
//        System.out.println("x = "+x);
//
//        Assert.assertTrue("v smaller than w (Lemma 4.2)", v < w);
//        Assert.assertTrue("v smaller than u (Lemma 4.3)", v < u);
//        Assert.assertTrue("x smaller than w (Lemma 4.3)", x < w);
//
//        System.out.println("X = "+resultS4.largestEigenvalue);
//        System.out.println("Y = "+resultS5.largestEigenvalue);
//
//        toCSV(s4, "S4");
//        toCSV(s5, "S5");
//    }
//
//    @Test
//    public void testLittleRecallExperiment() {
//        Automaton sAbc = getABC();
//        Automaton lAbcd = getABC_D();
//        Automaton lAbcd_or_e = getABC_D_or_E();
//
//        Automaton sl1 = sAbc.intersection(lAbcd, Utils.NOT_CANCELLER);
//        Automaton sl2 = sAbc.intersection(lAbcd_or_e, Utils.NOT_CANCELLER);
//
//
//        printResult(sAbc, "abc", lAbcd, "abc_d", 3/5.);
//
//        printResult(sAbc, "abc", lAbcd_or_e, "abc_d_or_e", 3/5.);
//    }
//
//    private void printResult(Automaton m, String mName, Automaton l, String lName, double fittingTracesFraction) {
//        EntropyPrecisionRecall result = PrecisionRecallComputer.getPrecisionAndRecall(m, mName, l, lName, m.intersection(l, Utils.NOT_CANCELLER), fittingTracesFraction, Utils.NOT_CANCELLER);
//        System.out.println("---------------------------------");
//        System.out.println("Results of "+mName+" and "+lName);
//        System.out.println("Precision: "+result.getPrecision());
//        System.out.println("Recall: "+result.getRecall());
//        System.out.println("Largest Eigenvalue LM: "+result.getLogModelResult().largestEigenvalue);
//    }
//
//    @Test
//    public void testCombinations() throws IOException {
//        Map<String,Automaton> models = new TreeMap<>();
//        Map<String,Automaton> logs = new TreeMap<>();
//
//        models.put("S1", getS1());
//        models.put("S2", getS2());
//        models.put("S3", getS3());
//
//        logs.put("L1", getL1());
//        logs.put("L2", getL2());
//        logs.put("L3", getL3());
//
//        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(TestUtils.TEST_OUTPUT_FOLDER,"example_results.csv")));
//
//        writer.write("Model;Log;Recall;Precision\n");
//
//        //Table<String, String, EntropyPrecisionRecall> results = HashBasedTable.create();
//        for (String model : models.keySet()){
//            for (String log : logs.keySet()){
//                EntropyPrecisionRecall result = getPrecisionAndRecall(model, log, models.get(model), logs.get(log), false);
//                writer.write(Joiner.on(";").join(new Object[]{model, log, result.getRecall(), result.getPrecision()})+"\n");
//                //results.put(model, log, result);
//            }
//        }
//        writer.close();
//    }
//
//    @Test
//    public void testS1Matrix() {
//        toCSV(getS1(), "S1");
//    }
//
//    @Test
//    public void testS3Matrix() {
//        toCSV(getS3(), "S3");
//    }
//
//    @Test
//    public void testL3Matrix() {
//        toCSV(getL3(), "L3");
//    }
//    
//    /**
//     * Motivating example
//     * 
//     * @throws IOException
//     */
//    @Test
//	public void testMotivForPaper() throws IOException {
//
//		FileOutputStream fileOut = new FileOutputStream(TestUtils.TEST_OUTPUT_FOLDER + "/motiv_results.csv");
//		OutputStreamWriter dataWriter = new OutputStreamWriter(fileOut);
//
//
//		Automaton a = getMotivModel();
//
//		EntropyPrecisionRecall result1 = 
//				getPrecisionAndRecall("motiv_model","motiv_log1", a, getL1Motiv(), false);
//		EntropyPrecisionRecall result2 = 
//				getPrecisionAndRecall("motiv_model","motiv_log2", a, getL2Motiv(), false);
//		
//		EntropyPrecisionRecall result3 = 
//				getPrecisionAndRecall("motiv_model_tau","motiv_log1_tau", addTau(a), addTau(getL1Motiv()), true);
//		EntropyPrecisionRecall result4 = 
//				getPrecisionAndRecall("motiv_model_tau","motiv_log2_tau", addTau(a), addTau(getL2Motiv()), true);
//					
//		dataWriter.write(new String(Joiner.on(";")
//				.join(new Object[] {
//						result1.getPrecision(), result1.getRecall(),
//						result2.getPrecision(), result2.getRecall(),
//						result3.getPrecision(), result3.getRecall(),
//						result4.getPrecision(), result4.getRecall()
//})
//				+ "\n"));
//
//		dataWriter.close();
//	}
//    
//    /**
//     * Paper example
//     * 
//     * @throws IOException
//     */
//    @Test
//	public void testForPaper() throws IOException {
//
//		FileOutputStream fileOut = new FileOutputStream(TestUtils.TEST_OUTPUT_FOLDER + "/paper_results.csv");
//		OutputStreamWriter dataWriter = new OutputStreamWriter(fileOut);
//
//
//		Automaton a1 = getSimpleABCAutomaton();
//		Automaton a2 = getSimpleABCLoopAutomaton();
//
//		EntropyPrecisionRecall result1 = 
//				getPrecisionAndRecall("AB_model","AB_log", a1, getLABBA(), false);
//		EntropyPrecisionRecall result2 = 
//				getPrecisionAndRecall("AB_model","ABBAAC_log", a1, getLABBAACBAB(), false);
//		EntropyPrecisionRecall result3 = 
//				getPrecisionAndRecall("AB_model","BAA_log", a1, getLBADBAB(), false);
//		EntropyPrecisionRecall result4 = 
//				getPrecisionAndRecall("ABLoop_model","AB_log", a2, getLABBA(), false);
//		EntropyPrecisionRecall result5 = 
//				getPrecisionAndRecall("ABLoop_model","ABBAAC_log", a2, getLABBAACBAB(), false);
//		EntropyPrecisionRecall result6 = 
//				getPrecisionAndRecall("ABLoop_model","BAA_log", a2, getLBADBAB(), false);
//		
//		EntropyPrecisionRecall result7 = 
//				getPrecisionAndRecall("AB_model_tau","AB_log_tau", addTau(a1), addTau(getLABBA()), true);
//		EntropyPrecisionRecall result8 = 
//				getPrecisionAndRecall("AB_model_tau","ABBAAC_log_tau", addTau(a1), addTau(getLABBAACBAB()), true);
//		EntropyPrecisionRecall result9 = 
//				getPrecisionAndRecall("AB_model_tau","BAA_log_tau", addTau(a1), addTau(getLBADBAB()), true);
//		EntropyPrecisionRecall result10 = 
//				getPrecisionAndRecall("ABLoop_model_tau","AB_log_tau", addTau(a2), addTau(getLABBA()), true);
//		EntropyPrecisionRecall result11 = 
//				getPrecisionAndRecall("ABLoop_model_tau","ABBAAC_log_tau", addTau(a2), addTau(getLABBAACBAB()), true);
//		EntropyPrecisionRecall result12 = 
//				getPrecisionAndRecall("ABLoop_model_tau","BAA_log_tau", addTau(a2), addTau(getLBADBAB()), true);
//			
//		dataWriter.write(new String(Joiner.on(";")
//				.join(new Object[] {
//						result1.getPrecision(), result1.getRecall(),
//						result2.getPrecision(), result2.getRecall(),
//						result3.getPrecision(), result3.getRecall(),
//						result4.getPrecision(), result4.getRecall(),
//						result5.getPrecision(), result5.getRecall(),
//						result6.getPrecision(), result6.getRecall(),
//						result7.getPrecision(), result7.getRecall(),
//						result8.getPrecision(), result8.getRecall(),
//						result9.getPrecision(), result9.getRecall(),
//						result10.getPrecision(), result10.getRecall(),
//						result11.getPrecision(), result11.getRecall(),
//						result12.getPrecision(), result12.getRecall()})
//				+ "\n"));
//
//		dataWriter.close();
//	}
//    
//    /**
//     * Qualitative test
//     * 
//     * @throws IOException
//     */
//    @Test
//    public void testQualitative() throws IOException {
//        Map<String, AcceptingPetriNet> models = new TreeMap<>();
//        Map<String, Automaton> logs = new TreeMap<>();
//
//        
//        models.put("Original model", getOriginalModel());
//        models.put("One trace", getOneTrace());
//        models.put("Separate traces", getSeparateTraces());
//        models.put("Flower model", getFlowerModel()); 
//        models.put("HG Parallel model", getHGParallelModel());
//        models.put("HG Self-loops", getHGSelfLoops());
//        models.put("D Self-loop", getDSelfLoopModel());
//        models.put("All Parallel", getAllParallelModel());
//        models.put("Round Robin model", getRoundRobinModel());
//        
//        
//        
//        logs.put("Test log", getLQualitative());
//        TestUtils.outputPNG(getLQualitative(), "Original log");
//        
//        FileOutputStream fileOut = new FileOutputStream(TestUtils.TEST_OUTPUT_FOLDER + "/qualitative_results.csv");
//        OutputStreamWriter dataWriter = new OutputStreamWriter(fileOut);
//        
//       
//      
//        String header = "Model;Log;Recall;Precision;Recall (M'^L')/L';Precision (M'^L')/M';Recall (M^L')/L';Precision (M^L')/M;"
//        		+ "Recall (M'^L)/L;Precision (M'^L)/M';"
//        		+ " \n";
//       
//       dataWriter.write(header);
//       
//        for (String model : models.keySet()){
//            for (String log : logs.keySet()){
//            	EntropyPrecisionRecall result = PrecisionRecallComputer.getPrecisionAndRecall(StochasticNetUtils.getDummyUIContext(),
//                		Utils.NOT_CANCELLER, logs.get(log), models.get(model), false);
//                EntropyPrecisionRecall resultTau = PrecisionRecallComputer.getPrecisionAndRecall(StochasticNetUtils.getDummyUIContext(),
//                		Utils.NOT_CANCELLER, addTau(logs.get(log)), models.get(model), true);
//                EntropyPrecisionRecall resultTauM = PrecisionRecallComputer.getPrecisionAndRecall(StochasticNetUtils.getDummyUIContext(),
//                		Utils.NOT_CANCELLER, logs.get(log), models.get(model), true);
//                EntropyPrecisionRecall resultTauL = PrecisionRecallComputer.getPrecisionAndRecall(StochasticNetUtils.getDummyUIContext(),
//                		Utils.NOT_CANCELLER, addTau(logs.get(log)), models.get(model), false);             
//                dataWriter.write(new String (Joiner.on(";").join(new Object[]{model, log, result.getRecall(), result.getPrecision(), 
//                		resultTau.getRecall(), resultTau.getPrecision(), resultTauL.getRecall(), resultTauL.getPrecision(), 
//                		resultTauM.getRecall(), resultTauM.getPrecision()})+"\n"));
//            }
//        }
//        dataWriter.close();
//    }
//
//    private void toCSV(Automaton a, String name) {
//        a.setDeterministic(false);
//        a.determinize(Utils.NOT_CANCELLER);
//        a.minimize(Utils.NOT_CANCELLER);
//        TestUtils.outputPNG(a,name+"_matrix");
//
//        CompColMatrix matrix = TopologicalEntropyComputer.getCompressedSparseMatrix(a).getMatrix();
//
//        EntropyResult topologicalEntropy = TopologicalEntropyComputer.getTopologicalEntropy(a, name, Utils.NOT_CANCELLER);
//
//        String filename = name+"_matrix.csv";
//
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(TestUtils.TEST_OUTPUT_FOLDER, filename)))) {
//            writer.write("largest Eigenvalue:;"+topologicalEntropy.largestEigenvalue+"\n");
//
//            writer.write("Adjacency Matrix:\n");
//
//            writer.write(";to "); // first value is the row
//            writer.write(Joiner.on(";to ").join(IntStream.range(0, matrix.numColumns()).iterator()));
//            writer.write("\n");
//            for (int row = 0; row < matrix.numRows(); row++){
//                writer.write("from "+row+":");
//                for (int col = 0; col < matrix.numColumns(); col++){
//                    writer.write(";"+ matrix.get(col,row));
//                }
//                writer.write("\n");
//            }
//
//
//        } catch (IOException e){
//            e.printStackTrace();
//        }
//    }
//
//
//    private EntropyPrecisionRecall getPrecisionAndRecall(String mName, String lName, Automaton aM, Automaton aL, boolean tauLog) {
//        TestUtils.outputPNG(aM, mName);
//
//        TestUtils.outputPNG(aL, lName);
//
//        Automaton aLM = aM.intersection(aL, Utils.NOT_CANCELLER);
//
//        TestUtils.outputPNG(aL, lName+"_intersect_"+mName);
//
//        EntropyPrecisionRecall result = PrecisionRecallComputer.getPrecisionAndRecall(aM, mName, aL, lName, aLM, 1.0, Utils.NOT_CANCELLER);
//        System.out.println("---------------------------------");
//        System.out.println("Results of "+mName+" and "+lName);
//        System.out.println("Precision: "+result.getPrecision());
//        System.out.println("Recall: "+result.getRecall());
//        System.out.println("Largest Eigenvalue LM: "+result.getLogModelResult().largestEigenvalue);
//        return result;
//    }
//
//    public static EntropyResult getTopologicalEntropy(Automaton a, String name) {
//        return TopologicalEntropyComputer.getTopologicalEntropy(a, name, Utils.NOT_CANCELLER);
//    }
//    
//    /**
//     * Adding silent transitions
//     * 
//     * @param a
//     * @return
//     */
//    public static Automaton addTau(Automaton a) {
//        Automaton automatonWithTau = a.clone();
//        Utils.addTau(automatonWithTau);
//        automatonWithTau.determinize(ProMCanceller.NEVER_CANCEL);
//        return automatonWithTau;
//    }
//}
