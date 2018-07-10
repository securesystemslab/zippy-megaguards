/*
 * Copyright (c) 2018, Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.uci.megaguards.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;

import edu.uci.megaguards.MGEnvVars;
import edu.uci.megaguards.MGLanguage;
import edu.uci.megaguards.MGOptions;
import edu.uci.megaguards.backend.ExecutionMode;
import edu.uci.megaguards.backend.parallel.opencl.OpenCLMGR;
import edu.uci.megaguards.log.MGLog;
import edu.uci.megaguards.log.MGLogOption;
import edu.uci.megaguards.python.MGPythonInit;
import edu.uci.python.runtime.PythonOptions;

public class MGTests {

    public static final String MGDataSet = MGEnvVars.MGHome + File.separator + "dataset" + File.separator;

    public static final String MGSrcTestPath = "megaguards" + File.separator;
    public static final String MGTestDataSet = MGDataSet + "test" + File.separator;
    public static final String MGRodiniaTestData = MGTestDataSet + "rodinia" + File.separator;

    public static final String MGBenchmarkPath = "megaguards-benchmark-suite" + File.separator;
    public static final String MGHPCPath = MGBenchmarkPath + "hpc" + File.separator + "Python" + File.separator;
    public static final String MGRodiniaPath = MGBenchmarkPath + "rodinia" + File.separator + "Python" + File.separator;
    public static final String MGRodiniaData = MGDataSet + "benchmark" + File.separator + "rodinia" + File.separator;

    public static void setMGParallelOptions() {
        setMGParallelOptions(1);
    }

    /**
     * @param threshold
     */
    public static void setMGParallelOptions(long threshold) {
        MGLanguage.INSTANCE.getOptionValues();
        MGOptions.junit = true;

        MGOptions.MGOff = false;
        PythonOptions.MGOff = false;

        MGOptions.Backend.target = ExecutionMode.OpenCLAuto;
        OpenCLMGR.MGR.isValidAutoDevice(false);
        // OpenCLMGR.MGR.isValidCPUDevice(false);
        // OpenCLMGR.MGR.isValidGPUDevice(false);
        MGOptions.Backend.concurrentCompiler = false;
        MGOptions.Backend.AthenaPet = true;
        MGOptions.Backend.offloadThreshold = 0;
        MGOptions.Backend.AthenaPetJNIDebug = 0;
        MGOptions.Backend.AthenaPetDebug = 0;
        MGOptions.Backend.BoundCheckDebug = 0;
        // MGOptions.Parallel.Debug = 4;
        // MGOptions.Debug = 4;
        // MGOptions.logging = true;
        // MGOptions.Parallel.cleanup = true;

        // MegaGuards Log
        // MGOptions.Log.Summary = true;
        // MGOptions.Log.JSON = true;

        // MGOptions.Log.Exception = true;
        // MGOptions.Log.ExceptionStack = true;

        MGLogOption.enableOption("GeneratedCode");
        // MGOptions.Log.TraceUnboxing = true;
        // MGLogOption.enableOption("UnboxTime");

        // MGLog.setJSONFile("profile.json");
        // MGOptions.Log.NodeProfileJSON = true;
        // MGOptions.Log.TotalDataTransfer = true;
        // MGOptions.Log.CompilationTime = true;

        // MGOptions.Log.OpenCLTranslationTime = true;
        // MGOptions.Log.DependenceTime = true;
        // MGOptions.Log.TranslationTime = true;
        // MGOptions.Log.TotalTime = true;
        // MGOptions.Log.Code = true;
        // MGOptions.Log.CoreExecutionTime = true;
        // MGOptions.Log.StorageConversionTime = true;
        // MGOptions.Log. = true;

        // MGOptions.forceLongType = true;
        // PythonOptions.forceLongType = true;

        MGPythonInit.INSTANCE.MGInitialization();
    }

    public static void releaseMGParallelOptions() {
        MGOptions.Backend.target = ExecutionMode.NormalCPU;
        MGOptions.Backend.concurrentCompiler = false;
        MGOptions.MGOff = true;
        PythonOptions.MGOff = true;
        MGOptions.boundCheck = true;
        MGOptions.Backend.AthenaPetJNIDebug = 0;
        MGOptions.Backend.AthenaPetDebug = 0;
        MGOptions.Backend.Debug = 0;
        MGOptions.Debug = 0;

        // MGOptions.forceLongType = false;
        // PythonOptions.forceLongType = false;

        MGLog.getLogs().clear();
    }

    public static boolean isOpenCLDeviceAvailable() {
        if (OpenCLMGR.MGR.getNumDevices() == 0) {
            return false;
        }
        boolean valid = false;
        setMGParallelOptions();
        switch (MGOptions.Backend.target) {
            case OpenCLGPU:
                if (OpenCLMGR.GPUs.size() > 0)
                    valid = true;
                break;
            case OpenCLCPU:
                if (OpenCLMGR.CPUs.size() > 0)
                    valid = true;
                break;
            case OpenCLAuto:
                if (OpenCLMGR.GPUs.size() > 0 && OpenCLMGR.CPUs.size() > 0)
                    valid = true;
                break;
        }
        releaseMGParallelOptions();
        return valid;
    }

    public static void setMGTruffleOptions() {
        MGOptions.junit = true;

        MGOptions.MGOff = false;
        PythonOptions.MGOff = false;

        MGOptions.Backend.offloadThreshold = 0;
        MGOptions.Backend.target = ExecutionMode.Truffle;
        MGOptions.Backend.BoundCheckDebug = 0;
        MGOptions.Debug = 0;
        // MGOptions.logging = true;

        // MegaGuards Log
        // MGOptions.Log.Summary = true;
        // MGOptions.Log.JSON = true;

        // MGOptions.Log.Exception = true;
        // MGOptions.Log.ExceptionStack = true;

        // MGOptions.Log.TraceUnboxing = true;
        // MGLogOption.enableOption("UnboxTime");

        // MGLog.setJSONFile("profile.json");
        // MGOptions.Log.NodeProfileJSON = true;
        // MGOptions.Log.TotalDataTransfer = true;
        // MGOptions.Log.CompilationTime = true;

        // MGOptions.Log.TranslationTime = true;
        // MGOptions.Log.TotalTime = true;
        // MGOptions.Log.CoreExecutionTime = true;
        // MGOptions.Log.StorageConversionTime = true;

        MGPythonInit.INSTANCE.MGInitialization();
    }

    public static void releaseMGTruffleOptions() {
        MGOptions.Backend.target = ExecutionMode.NormalCPU;
        MGOptions.MGOff = true;
        MGOptions.boundCheck = true;
        MGOptions.Backend.Debug = 0;
        MGOptions.Debug = 0;
        PythonOptions.forceLongType = false;
        PythonOptions.MGOff = true;

        MGLog.getLogs().clear();
    }

    public static void verifyMGExecution(Path script, int lastLogSize, int expectedTruffle, int expectedParallel) {
        ArrayList<MGLog> logs = MGLog.getLogs();
        long executedTruffle = 0;
        long executedParallel = 0;
        for (int i = lastLogSize; i < logs.size(); i++) {
            MGLog log = logs.get(i);
            executedTruffle += log.getOptionValueLong("TotalTruffleExecutions");
            executedParallel += log.getOptionValueLong("TotalKernelExecutions");
        }

        assertEquals("MegaGuards (Truffle) didn't execute as expected: " + script.toString(), expectedTruffle, executedTruffle);
        assertEquals("MegaGuards (OpenCL ) didn't execute as expected: " + script.toString(), expectedParallel, executedParallel);
    }

    public static int lastLogSize() {
        return MGLog.getLogs().size();
    }
}
