/** 
 
Copyright 2013 Intel Corporation, All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. 
*/ 

package com.intel.cosbench.driver.agent;

import java.util.*;

import com.intel.cosbench.api.storage.StorageAPI;
import com.intel.cosbench.driver.model.*;
import com.intel.cosbench.driver.operator.*;
import com.intel.cosbench.driver.util.OperationPicker;
import com.intel.cosbench.log.Logger;
import com.intel.cosbench.service.AbortedException;

public class WorkAgent extends AbstractAgent implements Session {

//    private long start; /* agent startup time */
//    private long begin; /* effective workload startup time */
//    private long end; /* effective workload shut-down time */
//    private long timeout; /* expected agent stop time */
//
//    private long lop; /* last operation performed */
//    private long lbegin; /* last sample emitted */
//    private long lsample; /* last sample collected */
//    private long lrsample; /* last sample collected during runtime */
//    private long frsample; /* first sample emitted during runtime */
//
//    private long curr; /* current time */
//    private long lcheck; /* last check point time */
//    private long check; /* next check point time */
//    private long interval; /* interval between check points */
//
//    private int totalOps; /* total operations to be performed */
//    private long totalBytes; /* total bytes to be transferred */
//	private long ltotalBytes;
//	private int ltotalOps;

    private OperationPicker operationPicker;
    private OperatorRegistry operatorRegistry;

//    private boolean isFinished = false;
    private WatchDog dog = new WatchDog();

//    private Status currMarks = new Status(); /* for snapshots */
//	private Status currMarksCloned = new Status();/* for snapshots */
//    private Status globalMarks = new Status(); /* for the final report */
    
    private WorkStats stats = new WorkStats();

    public WorkAgent() {
        /* empty */
    }

    @Override
    public void setWorkerContext(WorkerContext workerContext) {
        super.setWorkerContext(workerContext);
        dog.setWorkerContext(workerContext);
        stats.setWorkerContext(workerContext);
    }

    public void setOperationPicker(OperationPicker operationPicker) {
        this.operationPicker = operationPicker;
    }

    public void setOperatorRegistry(OperatorRegistry operatorRegistry) {
        this.operatorRegistry = operatorRegistry;
        stats.setOperatorRegistry(operatorRegistry);
    }

    @Override
    public int getIndex() {
        return workerContext.getIndex();
    }

    @Override
    public int getTotalWorkers() {
        return workerContext.getMission().getTotalWorkers();
    }

    @Override
    public Random getRandom() {
        return workerContext.getRandom();
    }

    @Override
    public StorageAPI getApi() {
        return workerContext.getStorageApi();
    }

    @Override
    public Logger getLogger() {
        return workerContext.getLogger();
    }

    @Override
    public WorkStats getStats() {
        return stats;
    }
    
    @Override
    public OperationListener getListener() {
    	return stats;
    }

    @Override
    protected void execute() {
        stats.initTimes();
        stats.initLimites();
        stats.initMarks();
        dog.watch(stats.getTimeout());
        try {
            doWork(); // launch work
        } finally {
            dog.dismiss();
        }
        /* work agent has completed execution successfully */
    }

    public void doSnapshot() {
    	stats.doSnapshot();
    }
    
//    private void initTimes() {
//        Mission mission = workerContext.getMission();
//        interval = mission.getInterval();
//        lcheck = curr = start = System.currentTimeMillis();
//        check = lcheck + interval * 1000;
//        begin = start;
//        timeout = 0L;
//        lop = lrsample = lsample = start;
//        frsample = lbegin = end = Long.MAX_VALUE;
//    }
//
//    private void initLimites() {
//        Mission mission = workerContext.getMission();
//        totalOps = mission.getTotalOps() / mission.getTotalWorkers();
//        totalBytes = mission.getTotalBytes() / mission.getTotalWorkers();
//        if (mission.getRuntime() == 0)
//            return;
//        begin = start + mission.getRampup() * 1000;
//        end = begin + mission.getRuntime() * 1000;
//        timeout = end + mission.getRampdown() * 1000;
//    }
//
//    private void initMarks() {
//        Set<String> types = new LinkedHashSet<String>();
//        for (OperatorContext op : operatorRegistry)
//            types.add(getMarkType(op.getOpType(), op.getSampleType()));
//        for (String type : types)
//            currMarks.addMark(newMark(type));
//        for (String type : types)
//            globalMarks.addMark(newMark(type));
//    }

    private void doWork() {
        while (!stats.isFinished())
            try {
                performOperation();
            } catch (AbortedException ae) {
                stats.doSummary();
                stats.finished();
            }
    }

    private void performOperation() {
//        lbegin = System.currentTimeMillis();
        Random random = workerContext.getRandom();
        String op = operationPicker.pickOperation(random);
        OperatorContext context = operatorRegistry.getOperator(op);
//        statsCallback.setOpType(context.getOperator().getOpType());
        context.getOperator().operate(this);
    }

//    @Override
//    public synchronized void onSampleCreated(Sample sample) {
//        String type = getMarkType(sample.getOpType(), sample.getSampleType());
//        currMarks.getMark(type).addToSamples(sample);
//        if (lbegin >= begin && lbegin < end && curr > begin && curr <= end) {
//            globalMarks.getMark(type).addToSamples(sample);
//            setlTotalBytes(getlTotalBytes() + sample.getBytes());
//            //operatorRegistry.getOperator(sample.getOpType()).addSample(sample);
//            if (lbegin < frsample)
//                frsample = lbegin; // first sample emitted during runtime
//            lrsample = curr; // last sample collected during runtime
//        }
//    }
//
//    public void doSnapshot() {
//		synchronized (currMarks) {
//			for (Mark mark : currMarks) {
//				currMarksCloned.addMark(mark.clone());
//				mark.clear();
//			}
//		}
//
//		long window = System.currentTimeMillis() - lcheck;
//		Report report = new Report();
//		for (Mark mark : currMarksCloned) {
//			for (Sample sample : mark.getSamples()) {
//				mark.addSample(sample);
//			}
//			report.addMetrics(Metrics.convert(mark, window));
//			mark.clear();
//		}
//
//		Snapshot snapshot = new Snapshot(report);
//		workerContext.setSnapshot(snapshot);
//		lcheck = System.currentTimeMillis();
//    }
//
//    @Override
//    public synchronized void onOperationCompleted(Result result) {
//        curr = result.getTimestamp().getTime();
//        String type = getMarkType(result.getOpType(), result.getSampleType());
//        currMarks.getMark(type).addOperation(result);
//        if (lop >= begin && lop < end && curr > begin && curr <= end){
//            globalMarks.getMark(type).addOperation(result);
//			setlTotoalOps(getlTotalOps() + 1);
//        }
//        lop = curr; // last operation performed
//        trySummary(); // make a summary report if necessary
//    }
//
//    private void trySummary() {
//        if ((timeout <= 0 || curr < timeout) // timeout
//                && (totalOps <= 0 || getlTotalOps() < totalOps) // operations
//                && (totalBytes <= 0 || getlTotalBytes() < totalBytes)) // bytes
//            return; // not finished
//        doSummary();
//        isFinished = true;
//    }
//
//    private void doSummary() {
//        long window = lrsample - frsample;
//        Report report = new Report();
//        for (Mark mark : globalMarks)
//            report.addMetrics(Metrics.convert(mark, window));
//        workerContext.setReport(report);
//    }
//	private void setlTotoalOps(int total) {
//		this.ltotalOps = total;
//	}
//
//	private int getlTotalOps() {
//		return this.ltotalOps;
//	}
//
//	private void setlTotalBytes(long totalBytes) {
//		this.ltotalBytes = totalBytes;
//	}
//
//	private long getlTotalBytes() {
//		return this.ltotalBytes;
//	}

//    private int getTotalOps() {
//        int sum = 0;
//        for (Mark mark : globalMarks)
//            sum += mark.getTotalOpCount();
//        return sum;
//    }
//
//    private long getTotalBytes() {
//        long bytes = 0;
//        for (Mark mark : globalMarks)
//            bytes += mark.getByteCount();
//        return bytes;
//    }

}
