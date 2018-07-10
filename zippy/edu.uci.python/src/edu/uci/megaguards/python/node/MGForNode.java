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
package edu.uci.megaguards.python.node;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;

import edu.uci.megaguards.MGOptions;
import edu.uci.megaguards.analysis.exception.MGException;
import edu.uci.megaguards.backend.MGFor;
import edu.uci.megaguards.log.MGLog;
import edu.uci.megaguards.python.ast.MGPythonTree;
import edu.uci.megaguards.python.fallback.ForFallback;
import edu.uci.python.ast.VisitorIF;
import edu.uci.python.nodes.PNode;
import edu.uci.python.nodes.control.ForNode;
import edu.uci.python.nodes.control.GetIteratorNode;
import edu.uci.python.nodes.control.LoopNode;
import edu.uci.python.nodes.frame.WriteNode;
import edu.uci.python.nodes.function.FunctionRootNode;
import edu.uci.python.runtime.datatype.PGenerator;
import edu.uci.python.runtime.datatype.PNone;
import edu.uci.python.runtime.function.PFunction;
import edu.uci.python.runtime.iterator.PDoubleIterator;
import edu.uci.python.runtime.iterator.PIntegerIterator;
import edu.uci.python.runtime.iterator.PIntegerSequenceIterator;
import edu.uci.python.runtime.iterator.PIterator;
import edu.uci.python.runtime.iterator.PLongIterator;
import edu.uci.python.runtime.iterator.PLongSequenceIterator;
import edu.uci.python.runtime.iterator.PRangeIterator;
import edu.uci.python.runtime.iterator.PSequenceIterator;

@NodeInfo(shortName = "for")
@NodeChild(value = "iterator", type = GetIteratorNode.class)
@GenerateNodeFactory
public abstract class MGForNode extends LoopNode {

    @Child protected PNode target;
    @Child private MGFor<PNode, PFunction> MGSpecialized;
    private final ForFallback fallback;

    public MGForNode(PNode body, PNode target, MGFor<PNode, PFunction> call) {
        super(body);
        this.target = target;
        assert target instanceof WriteNode;
        this.MGSpecialized = call;
        this.fallback = (ForFallback) call.getFallback();
    }

    public MGForNode(PNode body, PNode target) {
        this(body, target, new MGFor.Uninitialized<>(MGPythonTree.PyTREE, new ForFallback()));
    }

    private static String getFunctionName(Node node) {
        if (node instanceof FunctionRootNode)
            return ((FunctionRootNode) node).getFunctionName();

        if (node == null)
            return null;

        return getFunctionName(node.getParent());
    }

    public PNode getTarget() {
        return target;
    }

    public MGFor<PNode, PFunction> getMGFor() {
        return MGSpecialized;
    }

    public ForNode replace() {
        return this.replace(((ForFallback) MGSpecialized.getFallback()).fallback(this));
    }

    public boolean preCheck() {
        return fallback.check(this);
    }

    public boolean isOptimized() {
        return !MGSpecialized.isUninitialized();
    }

    public boolean isRetry() {
        return preCheck();
    }

    public boolean isFailed() {
        return !isOptimized() && !preCheck();
    }

    @Override
    public String toString() {
        if (getSourceSection() != null)
            return getSourceSection().getSource().getName() + ":" + getSourceSection().getStartLine() + ": " + getSourceSection().getCharacters().toString();
        return super.toString();
    }

    public abstract PNode getIterator();

    @Specialization(guards = "isOptimized()", rewriteOn = MGException.class)
    public Object doPRangeMGOptimized(VirtualFrame frame, PRangeIterator prange) {
        final int start = prange.getStart();
        final int stop = prange.getStop();
        final int step = prange.getStep();
        if (start == stop) {
            return PNone.NONE;
        }

        MGSpecialized.forLoop(frame, start, stop, step);

        return PNone.NONE;
    }

    @Specialization(guards = "isRetry()", rewriteOn = MGException.class)
    public Object doPRangeMGUninitialized(VirtualFrame frame, PRangeIterator prange) {
        final int start = prange.getStart();
        final int stop = prange.getStop();
        final int step = prange.getStep();
        if (start == stop) {
            return PNone.NONE;
        }
        final String fn = getFunctionName(this);
        final MGFor<PNode, PFunction> n = MGSpecialized.forLoop(frame, getEncapsulatingSourceSection(), fn, target, body, start, stop, step);
        CompilerDirectives.transferToInterpreterAndInvalidate();
        MGForNode replacement = MGForNodeFactory.create(getBody(), getTarget(), n, (GetIteratorNode) getIterator());
        replacement.assignSourceSection(getSourceSection());
        if (MGOptions.Backend.Debug > 0) {
            final int line = getSourceSection().getStartLine();
            final String filename = getSourceSection().getSource().getName() + ":" + line;
            String msg = "(optimize)";
            MGLog.println(filename, msg);
        }
        replace(replacement);
        return PNone.NONE;
    }

    @Specialization(guards = "isFailed()")
    public Object doPRange(VirtualFrame frame, PRangeIterator range) {
        return replace().doPRange(frame, range);
    }

    public Object doPRangeZipPy(VirtualFrame frame, int start, int stop, int step) {
        int i = start;
        for (; i < stop; i += step) {
            ((WriteNode) target).executeWrite(frame, i);
            body.executeVoid(frame);
        }
        return PNone.NONE;
    }

    @Specialization
    public Object doPRangeRetry(VirtualFrame frame, PRangeIterator prange) {
        final int start = prange.getStart();
        final int stop = prange.getStop();
        final int step = prange.getStep();
        if (start == stop) {
            return PNone.NONE;
        }

        doPRangeZipPy(frame, start, stop, step);
        replace(fallback.fallbackRetry(this));
        return PNone.NONE;
    }

    @Specialization
    public Object doIntegerSequenceIterator(VirtualFrame frame, PIntegerSequenceIterator iterator) {
        return replace().doIntegerSequenceIterator(frame, iterator);
    }

    @Specialization
    public Object doIntegerIterator(VirtualFrame frame, PIntegerIterator iterator) {
        return replace().doIntegerIterator(frame, iterator);
    }

    @Specialization
    public Object doLongSequenceIterator(VirtualFrame frame, PLongSequenceIterator iterator) {
        return replace().doLongSequenceIterator(frame, iterator);
    }

    @Specialization
    public Object doLongIterator(VirtualFrame frame, PLongIterator iterator) {
        return replace().doLongIterator(frame, iterator);
    }

    @Specialization
    public Object doDoubleIterator(VirtualFrame frame, PDoubleIterator iterator) {
        return replace().doDoubleIterator(frame, iterator);
    }

    @Specialization(guards = "isObjectStorageIterator(iterator)")
    public Object doObjectStorageIterator(VirtualFrame frame, PSequenceIterator iterator) {
        return replace().doObjectStorageIterator(frame, iterator);
    }

    @Specialization
    public Object doIterator(VirtualFrame frame, PSequenceIterator iterator) {
        return replace().doIterator(frame, iterator);
    }

    @Specialization
    public Object doGenerator(VirtualFrame frame, PGenerator generator) {
        return replace().doGenerator(frame, generator);
    }

    @Specialization
    public Object doIterator(VirtualFrame frame, PIterator iterator) {
        return replace().doIterator(frame, iterator);
    }

    @Override
    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        return visitor.visitMGForNode(this);
    }

}
