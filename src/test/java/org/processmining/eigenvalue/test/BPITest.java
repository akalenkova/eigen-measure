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

package org.processmining.eigenvalue.test;

import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.junit.Test;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.eigenvalue.Utils;
import org.processmining.eigenvalue.automata.PrecisionRecallComputer;
import org.processmining.eigenvalue.data.EntropyPrecisionRecall;
import org.processmining.eigenvalue.tree.TreeUtils;
import org.processmining.processtree.ProcessTree;
import org.processmining.ptconversions.pn.ProcessTree2Petrinet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Tests the BPI challenge logs
 */
public class BPITest extends PrecisionRecallTest {
    @Test
    public void testCanLoadAll() {
        for (XLog log : TestUtils.getBPILogs()){
            System.out.println(Utils.getName(log, "<log>"));
            System.out.println("traces: "+log.size());
            XLogInfo info = XLogInfoFactory.createLogInfo(log);
            System.out.println("events: "+ info.getNumberOfEvents());
            System.out.println("timebounds: "+ info.getLogTimeBoundaries().getStartDate()+" - "+info.getLogTimeBoundaries().getEndDate());
            System.out.println("***************************************");
        }
    }

    /**
     * Runs all BPI Logs through the pipeline and computes their precision and recall values with a mined model
     * inductive miner (default settings).
     *
     * Stores the results as .csv file in {@link TestUtils#TEST_OUTPUT_FOLDER} in the file real_logs_results.csv
     */
    @Test
    public void testComputePrecisionRecall() {
        File outFolder = new File(TestUtils.TEST_OUTPUT_FOLDER);
        if (!outFolder.exists()){
            outFolder.mkdirs();
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outFolder, "real_logs_results.csv")))) {
            writer.write(EntropyPrecisionRecall.getHeader()+"\n");
            for (XLog log : TestUtils.getBPILogs()) {
                try {
                	writer.write(getResultString(log, false, false)+"\n");
                    writer.write(getResultString(log, true, true)+"\n");
                    writer.flush();
                } catch (ProcessTree2Petrinet.NotYetImplementedException e) {
                    e.printStackTrace();
                } catch (ProcessTree2Petrinet.InvalidProcessTreeException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String getResultString(XLog log) throws ProcessTree2Petrinet.NotYetImplementedException, ProcessTree2Petrinet.InvalidProcessTreeException {
        ProcessTree model = TreeUtils.mineTree(log);

        ProcessTree2Petrinet.PetrinetWithMarkings petrinetWithMarkings = ProcessTree2Petrinet.convert(model, true);
        AcceptingPetriNet net = new AcceptingPetriNetImpl(petrinetWithMarkings.petrinet, petrinetWithMarkings.initialMarking, petrinetWithMarkings.finalMarking);

        long startTime = System.currentTimeMillis();
        EntropyPrecisionRecall precisionRecall = PrecisionRecallComputer.getPrecisionAndRecall(this.context, Utils.NOT_CANCELLER, log,  net);
        System.out.println("Computing Precision and Recall for "+Utils.getName(log,"<log>")+" took "+((System.currentTimeMillis()-startTime)/1000.)+"s");

        return(precisionRecall.getCSVString());
    }
    

    private String getResultString(XLog log, boolean tauModel, boolean tauLog) throws ProcessTree2Petrinet.NotYetImplementedException, ProcessTree2Petrinet.InvalidProcessTreeException {
        ProcessTree model = TreeUtils.mineTree(log);

        ProcessTree2Petrinet.PetrinetWithMarkings petrinetWithMarkings = ProcessTree2Petrinet.convert(model, true);
        AcceptingPetriNet net = new AcceptingPetriNetImpl(petrinetWithMarkings.petrinet, petrinetWithMarkings.initialMarking, petrinetWithMarkings.finalMarking);

        long startTime = System.currentTimeMillis();
        EntropyPrecisionRecall precisionRecall = PrecisionRecallComputer.getPrecisionAndRecall(this.context, Utils.NOT_CANCELLER, log, net, XLogInfoImpl.NAME_CLASSIFIER, tauModel, tauLog);
        System.out.println("Computing Precision and Recall for "+Utils.getName(log,"<log>")+" took "+((System.currentTimeMillis()-startTime)/1000.)+"s");

        return(precisionRecall.getCSVString());
    }

}
