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

import edu.uci.megaguards.MGOptions;
import edu.uci.megaguards.analysis.exception.MGException;
import edu.uci.megaguards.backend.ExecutionMode;
import edu.uci.megaguards.fallback.MGFallbackHandler;
import edu.uci.megaguards.log.MGLog;
import edu.uci.megaguards.python.analysis.MGPythonPreCheck;
import edu.uci.python.nodes.call.PythonCallNode;
import edu.uci.python.nodes.generator.ComprehensionNode.ListComprehensionNode;

public class CallFallback extends MGFallbackHandler<PythonCallNode> {

    protected boolean preChecked;
    protected boolean preCheckVerdect;

    public CallFallback() {
        super();
        this.preChecked = false;
        this.preCheckVerdect = true;

    }

    /*-
    public PythonCallNode fallbackRetry(PythonCallNode node) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        PythonCallNode replacement = null;
        /*-
        = MGForNodeFactory.create(node.getBody(), node.getTarget(), new MGFor.Uninitialized<>(node.getMGFor()), (GetIteratorNode) node.getIterator());
        * /
        replacement.assignSourceSection(node.getSourceSection());
        if (MGOptions.Backend.Debug > 0) {
            String filename = node.getSourceSection().getSource().getName();
            int line = node.getSourceSection().getStartLine();
            String msg = " replace " + node.getClass().getSimpleName();
            msg += " with " + replacement.getClass().getSimpleName();
            msg += "  (" + reason + ") de-optimize";
            MGLog.println(filename, line, msg);
        }
        return replacement;
    }

    public PythonCallNode fallback(PythonCallNode node) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        PythonCallNode replacement = null;
        /*-
        = ForNodeFactory.create(node.getBody(), node.getTarget(), (GetIteratorNode) node.getIterator());
        * /
        replacement.assignSourceSection(node.getSourceSection());
        if (MGOptions.Backend.Debug > 0) {
            String filename = node.getSourceSection().getSource().getName();
            int line = node.getSourceSection().getStartLine();
            String msg = " replace " + node.getClass().getSimpleName();
            msg += " with " + replacement.getClass().getSimpleName();
            msg += "  (" + reason + ")";
            MGLog.println(filename, line, msg);
        }
        return replacement;
    }
    */

    @TruffleBoundary
    private boolean preCheck(PythonCallNode node) {
        if (!preChecked) {
            try {
                preChecked = true;
                reason = "Pre-Check";
                MGPythonPreCheck check = new MGPythonPreCheck(false);
                check.check(node);
                /*-
                if (MGNodeOptions.hasOptions(node.getBody().hashCode())) {
                    if (MGNodeOptions.getOptions(node.getBody().hashCode()).isMGOff()) {
                        reason = "Selective Off";
                        this.preCheckVerdect = false;
                        return false;
                    }
                }
                */
            } catch (MGException e) {
                if (MGOptions.Backend.Debug > 0)
                    MGLog.printException(e, node.getSourceSection());
                this.preCheckVerdect = false;
                return false;
            }
        }
        return preCheckVerdect;
    }

    @Override
    @TruffleBoundary
    public boolean check(PythonCallNode node) {
        if (MGOptions.Backend.target == ExecutionMode.NormalCPU) {
            reason = "MegaGuards is disabled";
            return false;
        }
        /*-
        if (MGNodeOptions.hasOptions(node.getBody().hashCode())) {
            if (MGNodeOptions.getOptions(node.getBody().hashCode()).isMGOff()) {
                reason = "Selectively disabled";
                return false;
            }
        }
        */
        if ((node.getParent() instanceof ListComprehensionNode)) {
            reason = "Coverage";
            return false;
        }

        if (!preCheck(node)) {
            return false;
        }

        if (retryLimit < 0) {
            reason = "Retry Limit";
            return false;
        }

        return true;
    }

}
