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

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.Node;

import edu.uci.megaguards.MGNodeOptions;
import edu.uci.megaguards.MGOptions;
import edu.uci.megaguards.analysis.exception.MGException;
import edu.uci.megaguards.backend.ExecutionMode;
import edu.uci.megaguards.backend.MGFor;
import edu.uci.megaguards.fallback.MGFallbackHandler;
import edu.uci.megaguards.log.MGLog;
import edu.uci.megaguards.python.analysis.MGPythonPreCheck;
import edu.uci.megaguards.python.analysis.MGPythonPrivatizationCheck;
import edu.uci.megaguards.python.node.MGForNode;
import edu.uci.megaguards.python.node.MGForNodeFactory;
import edu.uci.python.nodes.control.ElseNode;
import edu.uci.python.nodes.control.ForNode;
import edu.uci.python.nodes.control.ForNodeFactory;
import edu.uci.python.nodes.control.GetIteratorNode;
import edu.uci.python.nodes.function.FunctionRootNode;
import edu.uci.python.nodes.generator.ComprehensionNode.ListComprehensionNode;

public class ForFallback extends MGFallbackHandler<MGForNode> {

    protected boolean privatizationChecked;
    protected boolean privatizationVerdect;

    public ForFallback() {
        super();
        this.privatizationChecked = false;
        this.privatizationVerdect = true;

    }

    public MGForNode fallbackRetry(MGForNode node) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        MGForNode replacement = MGForNodeFactory.create(node.getBody(), node.getTarget(), new MGFor.Uninitialized<>(node.getMGFor()), (GetIteratorNode) node.getIterator());
        replacement.assignSourceSection(node.getSourceSection());
        if (MGOptions.Backend.Debug > 0) {
            final int line = node.getSourceSection().getStartLine();
            final String filename = node.getSourceSection().getSource().getName() + ":" + line;
            String msg = " replace " + node.getClass().getSimpleName();
            msg += " with " + replacement.getClass().getSimpleName();
            msg += "  (" + reason + ") de-optimize";
            MGLog.println(filename, msg);
        }
        return replacement;
    }

    public ForNode fallback(MGForNode node) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        ForNode replacement = ForNodeFactory.create(node.getBody(), node.getTarget(), (GetIteratorNode) node.getIterator());
        replacement.assignSourceSection(node.getSourceSection());
        if (MGOptions.Backend.Debug > 0) {
            final int line = node.getSourceSection().getStartLine();
            final String filename = node.getSourceSection().getSource().getName() + ":" + line;
            String msg = " replace " + node.getClass().getSimpleName();
            msg += " with " + replacement.getClass().getSimpleName();
            msg += "  (" + reason + ")";
            MGLog.println(filename, msg);
        }
        return replacement;
    }

    @TruffleBoundary
    private boolean privatizationCheck(MGForNode node) {
        if (!privatizationChecked) {
            try {
                privatizationChecked = true;
                reason = "Pre-Check";
                MGPythonPreCheck check = new MGPythonPreCheck(false);
                check.check(node);
                if (MGNodeOptions.hasOptions(node.getBody().hashCode())) {
                    if (MGNodeOptions.getOptions(node.getBody().hashCode()).isMGOff()) {
                        reason = "Selective Off";
                        this.privatizationVerdect = false;
                        return false;
                    }
                }
                MGPythonPrivatizationCheck pcheck = new MGPythonPrivatizationCheck(check.getVarTable(), node);
                reason = "Privatization";
                pcheck.check();
            } catch (MGException e) {
                if (MGOptions.Backend.Debug > 0)
                    MGLog.printException(e, node.getSourceSection());
                this.privatizationVerdect = false;
                return false;
            }
        }
        return privatizationVerdect;
    }

    @TruffleBoundary
    protected static boolean childOfFunction(Node node) {
        if (node == null)
            return false;
        if (node instanceof FunctionRootNode)
            return true;

        return childOfFunction(node.getParent());
    }

    @Override
    @TruffleBoundary
    public boolean check(MGForNode node) {
        if (MGOptions.Backend.target == ExecutionMode.NormalCPU) {
            reason = "MegaGuards is disabled";
            return false;
        }

        if (MGNodeOptions.hasOptions(node.getBody().hashCode())) {
            if (MGNodeOptions.getOptions(node.getBody().hashCode()).isMGOff()) {
                reason = "Selectively disabled";
                return false;
            }
        }

        if ((node.getParent() instanceof ListComprehensionNode)) {
            reason = "Coverage";
            return false;
        }

        if ((node.getParent() instanceof ElseNode)) {
            reason = "Coverage";
            return false;
        }

        if (!childOfFunction(node)) {
            reason = "Global Scop";
            return false;
        }

        if (!privatizationCheck(node)) {
            return false;
        }

        if (retryLimit < 0) {
            reason = "Retry Limit";
            return false;
        }

        return true;
    }

}
