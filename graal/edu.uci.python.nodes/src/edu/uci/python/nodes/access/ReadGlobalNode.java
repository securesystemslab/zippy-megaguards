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
package edu.uci.python.nodes.access;

import org.python.core.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.CompilerDirectives.SlowPath;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.literal.*;
import edu.uci.python.nodes.object.*;
import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.standardtype.*;

public abstract class ReadGlobalNode extends PNode implements ReadNode, HasPrimaryNode {

    protected final String attributeId;
    protected final PythonContext context;
    protected final PythonModule globalScope;

    public ReadGlobalNode(PythonContext context, PythonModule globalScope, String attributeId) {
        this.attributeId = attributeId;
        this.context = context;
        this.globalScope = globalScope;
    }

    public static ReadGlobalNode create(PythonContext context, PythonModule globalScope, String attributeId) {
        return new UninitializedReadGlobalNode(context, globalScope, attributeId);
    }

    @Override
    public PNode makeWriteNode(PNode rhs) {
        return new UninitializedStoreAttributeNode(this.attributeId, new ObjectLiteralNode(globalScope), rhs);
    }

    @Override
    public PNode extractPrimary() {
        return new ObjectLiteralNode(globalScope);
    }

    public String getAttributeId() {
        return attributeId;
    }

    public PythonModule extractGlobaScope() {
        return globalScope;
    }

    protected final Object respecialize(VirtualFrame frame) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return replace(new UninitializedReadGlobalNode(context, globalScope, attributeId)).execute(frame);
    }

    public static final class ReadGlobalDirectNode extends ReadGlobalNode {

        private final Assumption globalScopeStable;
        @Child protected LoadAttributeNode load;

        public ReadGlobalDirectNode(PythonContext context, PythonModule globalScope, String attributeId) {
            super(context, globalScope, attributeId);
            this.globalScopeStable = globalScope.getStableAssumption();
            this.load = new UninitializedLoadAttributeNode(attributeId, new ObjectLiteralNode(globalScope));
        }

        @Override
        public Object execute(VirtualFrame frame) {
            try {
                globalScopeStable.check();
                return load.execute(frame);
            } catch (InvalidAssumptionException e) {
                return respecialize(frame);
            }
        }

        @Override
        public int executeInt(VirtualFrame frame) throws UnexpectedResultException {
            try {
                globalScopeStable.check();
                return load.executeInt(frame);
            } catch (InvalidAssumptionException e) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return PythonTypesGen.PYTHONTYPES.expectInteger(respecialize(frame));
            }
        }

        @Override
        public double executeDouble(VirtualFrame frame) throws UnexpectedResultException {
            try {
                globalScopeStable.check();
                return load.executeDouble(frame);
            } catch (InvalidAssumptionException e) {
                return PythonTypesGen.PYTHONTYPES.expectDouble(respecialize(frame));
            }
        }

        @Override
        public boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
            try {
                globalScopeStable.check();
                return load.executeBoolean(frame);
            } catch (InvalidAssumptionException e) {
                return PythonTypesGen.PYTHONTYPES.expectBoolean(respecialize(frame));
            }
        }
    }

    public static final class ReadBuiltinCachedNode extends ReadGlobalNode {

        private final Assumption globalScopeStable;
        private final Assumption builtinsModuleStable;
        private final Object cachedBuiltin;

        public ReadBuiltinCachedNode(PythonContext context, PythonModule globalScope, String attributeId, Object cachedBuiltin) {
            super(context, globalScope, attributeId);
            this.globalScopeStable = globalScope.getStableAssumption();
            this.builtinsModuleStable = context.getPythonBuiltinsLookup().lookupModule("__builtins__").getStableAssumption();
            this.cachedBuiltin = cachedBuiltin;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            try {
                globalScopeStable.check();
                builtinsModuleStable.check();
                return cachedBuiltin;
            } catch (InvalidAssumptionException e) {
                return respecialize(frame);
            }
        }
    }

    public static final class UninitializedReadGlobalNode extends ReadGlobalNode {

        public UninitializedReadGlobalNode(PythonContext context, PythonModule globalScope, String attributeId) {
            super(context, globalScope, attributeId);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            CompilerAsserts.neverPartOfCompilation();

            Object value = globalScope.getAttribute(attributeId);

            if (value == PNone.NONE) {
                value = context.getPythonBuiltinsLookup().lookupModule("__builtins__").getAttribute(attributeId);
            } else {
                replace(new ReadGlobalDirectNode(context, globalScope, attributeId));
                return value;
            }

            if (value == PNone.NONE) {
                value = slowPathLookup();
            } else {
                replace(new ReadBuiltinCachedNode(context, globalScope, attributeId, value));
            }

            return value;
        }

        @SlowPath
        protected Object slowPathLookup() {
            Object value = PySystemState.builtins.__finditem__(attributeId);

            if (value == null) {
                throw Py.NameError("name \'" + attributeId + "\' is not defined");
            }

            return value;
        }
    }

}
