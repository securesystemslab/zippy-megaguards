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
package edu.uci.megaguards.python.fallback;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

import edu.uci.megaguards.MGNodeOptions;
import edu.uci.megaguards.MGOptions;
import edu.uci.megaguards.analysis.exception.MGException;
import edu.uci.megaguards.backend.ExecutionMode;
import edu.uci.megaguards.fallback.MGFallbackHandler;
import edu.uci.megaguards.log.MGLog;
import edu.uci.megaguards.python.analysis.MGPythonPreCheck;
import edu.uci.python.nodes.function.FunctionRootNode;
import edu.uci.python.runtime.PythonOptions;
import edu.uci.python.runtime.function.PFunction;

public class ReduceFallback extends MGFallbackHandler<PFunction> {

    private boolean prechecked;
    protected boolean precheckedVerdect;

    public ReduceFallback() {
        super();
        this.prechecked = false;
        this.precheckedVerdect = true;
    }

    @TruffleBoundary
    private boolean preCheck(PFunction node) {
        if (!this.prechecked) {
            try {
                this.prechecked = true;
                reason = "Pre-Check";
                MGPythonPreCheck check = new MGPythonPreCheck(true);
                FunctionRootNode functionNodeRoot = (FunctionRootNode) node.getCallTarget().getRootNode();
                check.check(functionNodeRoot.getBody());
                if (MGNodeOptions.hasOptions(functionNodeRoot.getBody().hashCode())) {
                    MGNodeOptions.addOptions(node.hashCode(), MGNodeOptions.getOptions(functionNodeRoot.getBody().hashCode()));
                }
                if (MGNodeOptions.hasOptions(node.hashCode())) {
                    if (MGNodeOptions.getOptions(node.hashCode()).isMGOff()) {
                        reason = "Selective Off";
                        this.precheckedVerdect = false;
                        return false;
                    }
                }
            } catch (MGException e) {
                if (MGOptions.Backend.Debug > 0)
                    MGLog.printException(e, null);
                this.precheckedVerdect = false;
                return false;
            }
        }
        return precheckedVerdect;
    }

    @Override
    @TruffleBoundary
    public boolean check(PFunction node) {
        if (PythonOptions.MGOff) {
            return false;
        }

        if (MGOptions.Backend.target == ExecutionMode.NormalCPU) {
            return false;
        }

        if (!preCheck(node)) {
            return false;
        }

        if (MGNodeOptions.hasOptions(node.hashCode())) {
            if (MGNodeOptions.getOptions(node.hashCode()).isMGOff()) {
                return false;
            }
        }

        if (retryLimit < 0) {
            return false;
        }

        return true;
    }

}
