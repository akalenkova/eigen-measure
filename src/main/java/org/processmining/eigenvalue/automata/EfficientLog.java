package org.processmining.eigenvalue.automata;

import gnu.trove.map.TObjectShortMap;
import gnu.trove.map.custom_hash.TObjectShortCustomHashMap;
import gnu.trove.strategy.HashingStrategy;

import java.util.ArrayList;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class EfficientLog {
	private ArrayList<short[]> events;
	private String[] short2activity;
	private TObjectShortMap<String> activity2short;

	public EfficientLog(XLog log, XEventClassifier classifier) {
		initialise();

		for (XTrace trace : log) {
			addTrace(trace, classifier);
		}
		finalise();
	}

	/**
	 * Build up an log. Finish the log with finalise() before using.
	 */
	public EfficientLog() {
		initialise();
	}

	private void initialise() {
		HashingStrategy<String> strategy = new HashingStrategy<String>() {
			private static final long serialVersionUID = 1613251400608549656L;

			public int computeHashCode(String object) {
				return object.hashCode();
			}

			public boolean equals(String o1, String o2) {
				return o1.equals(o2);
			}
		};
		activity2short = new TObjectShortCustomHashMap<String>(strategy, 10, 0.5f, (short) -1);
		events = new ArrayList<>();
	}

	public void addTrace(XTrace trace, XEventClassifier classifier) {
		short[] eTrace = new short[trace.size()];
		int e = 0;
		for (XEvent event : trace) {
			String activity = classifier.getClassIdentity(event);
			short a = activity2short.putIfAbsent(activity, (short) activity2short.size());
			if (a == activity2short.getNoEntryValue()) {
				//new activity
				a = (short) (activity2short.size() - 1);
			}
			eTrace[e] = a;
			e++;
		}
		events.add(eTrace);
	}

	public void finalise() {
		//create short2activity
		short2activity = new String[activity2short.size()];
		for (String activity : activity2short.keySet()) {
			short2activity[activity2short.get(activity)] = activity;
		}
	}

	/**
	 * 
	 * @param activities
	 * @return a projection key to be given to getProjectedActivity
	 */
	public short[] getProjectionKey(String... activities) {
		short[] result = new short[short2activity.length];
		for (int a = 0; a < result.length; a++) {
			result[a] = -1;
		}

		for (short a = 0; a < activities.length; a++) {
			if (activity2short.containsKey(activities[a])) {
				result[activity2short.get(activities[a])] = a;
			}
		}
		return result;
	}

	public String[] getActivities() {
		return short2activity;
	}

	public int size() {
		return events.size();
	}

	public int getTraceSize(int trace) {
		return events.get(trace).length;
	}

	/**
	 * Given a projection key, returns the projected activity at the given
	 * position. If this activity is filtered out, returns -1.
	 * 
	 * @param trace
	 * @param event
	 * @param projectionKey
	 * @return
	 */
	public short getProjectedActivity(int trace, int event, short[] projectionKey) {
		return projectionKey[events.get(trace)[event]];
	}

	public short[] getProjectedTrace(int trace, short[] projectionKey) {
		short[] result = new short[events.get(trace).length];
		for (int event = 0; event < events.get(trace).length; event++) {
			result[event] = projectionKey[events.get(trace)[event]];
		}
		return result;
	}

	public String traceToString(int trace) {
		StringBuilder result = new StringBuilder();
		for (int event = 0; event < getTraceSize(trace); event++) {
			result.append(short2activity[events.get(trace)[event]]);
			result.append(",");
		}
		return result.toString();
	}

	public String traceToString(int trace, short[] projectionKey) {
		StringBuilder result = new StringBuilder();
		for (int event = 0; event < getTraceSize(trace); event++) {
			if (projectionKey[events.get(trace)[event]] >= 0) {
				result.append(projectionKey[events.get(trace)[event]]);
				result.append(",");
			}
		}
		return result.toString();
	}

	public String projectionKeyToString(short[] projectionKey) {
		StringBuilder result = new StringBuilder();
		result.append("projecting on ");
		for (int a = 0; a < short2activity.length; a++) {
			if (projectionKey[a] >= 0) {
				result.append(short2activity[a]);
				result.append("=");
				result.append(projectionKey[a]);
				result.append(",");
			}
		}
		return result.toString();
	}
}