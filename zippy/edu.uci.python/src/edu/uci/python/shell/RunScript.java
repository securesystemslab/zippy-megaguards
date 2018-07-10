/*
 * Copyright (c) 2013, Regents of the University of California
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
package edu.uci.python.shell;

import java.io.OutputStream;

import com.oracle.truffle.api.source.Source;

import edu.uci.megaguards.MGOptions;
import edu.uci.megaguards.analysis.parallel.profile.ParallelNodeProfile;
import edu.uci.megaguards.backend.parallel.opencl.OpenCLExecuter;
import edu.uci.megaguards.log.MGLog;
import edu.uci.python.runtime.PythonOptions;

public class RunScript {

    public static void runThrowableScript(String[] args, Source source, OutputStream out, OutputStream err) {
        if (source != null) {
            ZipPyConsole interp = new ZipPyConsole();
            interp.init(args, null);
            interp.execfile(source, null, out, err);
        }
    }

    public static void runScript(String[] args, Source source, OutputStream out, OutputStream err) {
        runScript(args, source, null, out, err);
    }

    public static void runScript(String[] args, Source source, String workingDir, OutputStream out, OutputStream err) {
        if (source != null) {
            ZipPyConsole interp = new ZipPyConsole();
            interp.init(args, workingDir);
            try {
                interp.execfile(source, null, out, err);
            } catch (Throwable t) {
                ZipPyConsole.dispose(interp, t, false);
            } finally {
                if (!PythonOptions.MGOff) {
                    if ((MGOptions.Log.Summary))
                        MGLog.printSummary();
                    if ((MGOptions.Log.NodeProfileJSON))
                        ParallelNodeProfile.profilesSummary();

                    OpenCLExecuter.cleanUp(false);
                }
            }
        }
    }

}
