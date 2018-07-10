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
package edu.uci.python.builtins;

import java.math.BigInteger;
import java.util.List;

import org.python.core.Py;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.UnexpectedResultException;

import edu.uci.megaguards.analysis.exception.MGException;
import edu.uci.megaguards.backend.MGMap;
import edu.uci.megaguards.python.ast.MGPythonTree;
import edu.uci.megaguards.python.fallback.MapFallback;
import edu.uci.python.builtins.BuiltinConstructorsFactory.MapNodeFactory;
import edu.uci.python.nodes.EmptyNode;
import edu.uci.python.nodes.PNode;
import edu.uci.python.nodes.control.GetIteratorNode;
import edu.uci.python.nodes.control.GetIteratorNodeFactory;
import edu.uci.python.nodes.function.PythonBuiltinNode;
import edu.uci.python.nodes.truffle.PythonTypesGen;
import edu.uci.python.nodes.truffle.PythonTypesUtil;
import edu.uci.python.runtime.PythonContext;
import edu.uci.python.runtime.datatype.PComplex;
import edu.uci.python.runtime.datatype.PDict;
import edu.uci.python.runtime.datatype.PFrozenSet;
import edu.uci.python.runtime.datatype.PIterable;
import edu.uci.python.runtime.datatype.PNone;
import edu.uci.python.runtime.datatype.PRange;
import edu.uci.python.runtime.exception.StopIterationException;
import edu.uci.python.runtime.function.PArguments;
import edu.uci.python.runtime.function.PFunction;
import edu.uci.python.runtime.function.PythonCallable;
import edu.uci.python.runtime.iterator.PIterator;
import edu.uci.python.runtime.iterator.PStringIterator;
import edu.uci.python.runtime.iterator.PZip;
import edu.uci.python.runtime.misc.JavaTypeConversions;
import edu.uci.python.runtime.object.PythonObject;
import edu.uci.python.runtime.sequence.PBaseSet;
import edu.uci.python.runtime.sequence.PBytes;
import edu.uci.python.runtime.sequence.PEnumerate;
import edu.uci.python.runtime.sequence.PList;
import edu.uci.python.runtime.sequence.PSequence;
import edu.uci.python.runtime.sequence.PSet;
import edu.uci.python.runtime.sequence.PString;
import edu.uci.python.runtime.sequence.PTuple;
import edu.uci.python.runtime.standardtype.PythonClass;

/**
 * @author Gulfem
 * @author zwei
 */

public final class BuiltinConstructors extends PythonBuiltins {

    @Override
    protected List<com.oracle.truffle.api.dsl.NodeFactory<? extends PythonBuiltinNode>> getNodeFactories() {
        return BuiltinConstructorsFactory.getFactories();
    }

    // bool([x])
    @Builtin(name = "bool", minNumOfArguments = 0, maxNumOfArguments = 1, isConstructor = true)
    @GenerateNodeFactory
    public abstract static class BoolNode extends PythonBuiltinNode {

        @Specialization
        public boolean bool(int arg) {
            return arg != 0;
        }

        @Specialization
        public boolean bool(double arg) {
            return arg != 0.0;
        }

        @Specialization
        public boolean bool(String arg) {
            return !arg.isEmpty();
        }

        @Specialization
        public boolean bool(@SuppressWarnings("unused") PNone arg) {
            return false;
        }

        @Specialization
        public boolean bool(@SuppressWarnings("unused") PythonObject object) {
            return true;
        }

        @Fallback
        public boolean bool(Object object) {
            return JavaTypeConversions.toBoolean(object);
        }
    }

    // bytes([source[, encoding[, errors]]])
    @Builtin(name = "bytes", minNumOfArguments = 0, maxNumOfArguments = 3, isConstructor = true)
    @GenerateNodeFactory
    public abstract static class BytesNode extends PythonBuiltinNode {

        @SuppressWarnings("unused")
        @Specialization
        public PBytes bytes(PNone source, PNone encoding, PNone errors) {
            return new PBytes();
        }
    }

    // complex([real[, imag]])
    @Builtin(name = "complex", minNumOfArguments = 0, maxNumOfArguments = 2, isConstructor = true)
    @GenerateNodeFactory
    public abstract static class ComplexNode extends PythonBuiltinNode {

        @Specialization
        public PComplex complexFromDoubleDouble(double real, double imaginary) {
            return new PComplex(real, imaginary);
        }

        @SuppressWarnings("unused")
        @Specialization
        public PComplex complexFromDouble(double real, PNone image) {
            return new PComplex(real, 0);
        }

        @SuppressWarnings("unused")
        @Specialization
        public PComplex complexFromNone(PNone real, PNone image) {
            return new PComplex(0, 0);
        }

        @Specialization
        public PComplex complexFromObjectObject(Object real, Object imaginary) {
            if (real instanceof String) {
                if (!(imaginary instanceof PNone)) {
                    throw Py.TypeError("complex() can't take second arg if first is a string");
                }

                String realPart = (String) real;
                return JavaTypeConversions.convertStringToComplex(realPart);
            }

            throw Py.TypeError("can't convert real " + real + " imag " + imaginary);
        }
    }

    // dict(**kwarg)
    // dict(mapping, **kwarg)
    // dict(iterable, **kwarg)
    @Builtin(name = "dict", minNumOfArguments = 0, takesVariableArguments = true, isConstructor = true)
    @GenerateNodeFactory
    public abstract static class DictionaryNode extends PythonBuiltinNode {

        @SuppressWarnings("unused")
        @Specialization(guards = "emptyArgument(args)")
        public PDict dictEmpty(PTuple args) {
            return new PDict();
        }

        @Specialization(guards = {"oneArgument(args)", "firstArgIsDict(args)"})
        public PDict dictFromDict(PTuple args) {
            return new PDict(((PDict) args.getItem(0)).getMap());
        }

        @Specialization(guards = {"oneArgument(args)", "firstArgIsIterable(args)"})
        public PDict dictFromIterable(PTuple args) {
            PIterable iterable = (PIterable) args.getItem(0);
            PIterator iter = iterable.__iter__();
            return new PDict(iter);
        }

        @Specialization(guards = {"oneArgument(args)", "firstArgIsIterator(args)"})
        public PDict dictFromIterator(PTuple args) {
            PIterator iter = (PIterator) args.getItem(0);
            return new PDict(iter);
        }

        @SuppressWarnings("unused")
        @Fallback
        public PDict dictionary(Object args) {
            throw new RuntimeException("invalid args for dict()");
        }
    }

    // enumerate(iterable, start=0)
    @Builtin(name = "enumerate", hasFixedNumOfArguments = true, fixedNumOfArguments = 1, takesKeywordArguments = true, keywordNames = {"start"}, isConstructor = true)
    @GenerateNodeFactory
    public abstract static class EnumerateNode extends PythonBuiltinNode {
        /**
         * TODO enumerate can take a keyword argument start, and currently that's not supported.
         */

        @SuppressWarnings("unused")
        @Specialization()
        public PEnumerate enumerate(String str, PNone keywordArg) {
            PString pstr = new PString(str);
            return new PEnumerate(pstr);
        }

        @SuppressWarnings("unused")
        @Specialization()
        public PEnumerate enumerate(PIterable iterable, PNone keywordArg) {
            return new PEnumerate(iterable);
        }

        @Specialization
        public PEnumerate enumerate(Object arg, Object keywordArg) {
            CompilerAsserts.neverPartOfCompilation();
            if (keywordArg instanceof PNone) {
                throw new RuntimeException("enumerate does not support iterable object " + arg);
            } else {
                throw new RuntimeException("enumerate does not support keyword argument " + keywordArg);
            }
        }
    }

    // float([x])
    @Builtin(name = "float", minNumOfArguments = 0, maxNumOfArguments = 1, isConstructor = true)
    @GenerateNodeFactory
    public abstract static class FloatNode extends PythonBuiltinNode {

        @Specialization
        public double floatFromInt(int arg) {
            return arg;
        }

        @Specialization
        public double floatFromFloat(double arg) {
            return arg;
        }

        @Specialization
        public double floatFromString(String arg) {
            return JavaTypeConversions.convertStringToDouble(arg);
        }

        @Specialization
        public double floatFromObject(Object arg) {
            if (arg instanceof PNone) {
                return 0.0;
            }

            throw Py.TypeError("can't convert " + arg.getClass().getSimpleName() + " to float ");
        }
    }

    // frozenset([iterable])
    @Builtin(name = "frozenset", minNumOfArguments = 0, maxNumOfArguments = 1, isConstructor = true)
    @GenerateNodeFactory
    public abstract static class FrozenSetNode extends PythonBuiltinNode {

        @SuppressWarnings("unused")
        @Specialization(guards = "emptyArguments(arg)")
        public PFrozenSet frozensetEmpty(Object arg) {
            return new PFrozenSet();
        }

        @Specialization
        public PFrozenSet frozenset(String arg) {
            return new PFrozenSet(new PStringIterator(arg));
        }

        @Specialization
        public PFrozenSet frozenset(PBaseSet baseSet) {
            return new PFrozenSet(baseSet);
        }

        @Specialization
        public PFrozenSet frozensetSequence(PSequence sequence) {
            return new PFrozenSet(sequence.__iter__());
        }

        @Specialization
        public PFrozenSet frozensetIterator(PIterator iterator) {
            PFrozenSet set = new PFrozenSet(iterator);
            return set;
        }

        @SuppressWarnings("unused")
        @Specialization
        public PFrozenSet frozenset(VirtualFrame frame, Object arg) {
            throw new UnsupportedOperationException();
        }
    }

    // int(x=0)
    // int(x, base=10)
    @Builtin(name = "int", minNumOfArguments = 0, maxNumOfArguments = 1, takesKeywordArguments = true, keywordNames = {"base"}, isConstructor = true)
    @GenerateNodeFactory
    public abstract static class IntNode extends PythonBuiltinNode {

        @SuppressWarnings("unused")
        @Specialization(guards = "noKeywordArg(arg,keywordArg)")
        public int createInt(int arg, Object keywordArg) {
            return arg;
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "noKeywordArg(arg,keywordArg)")
        public int createInt(long arg, Object keywordArg) {
            return Long.valueOf(arg).intValue();
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "noKeywordArg(arg,keywordArg)")
        public BigInteger createInt(BigInteger arg, Object keywordArg) {
            return arg;
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "noKeywordArg(arg,keywordArg)")
        public Object createInt(double arg, Object keywordArg) {
            return JavaTypeConversions.doubleToInt(arg);
        }

        @SuppressWarnings("unused")
        @Specialization
        public int createInt(PNone none, Object keywordArg) {
            return 0;
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "noKeywordArg(arg,keywordArg)")
        public Object createInt(String arg, Object keywordArg) {
            return JavaTypeConversions.stringToInt(arg, 10);
        }

        @Fallback
        public Object createInt(Object arg, Object keywordArg) {
            if (keywordArg instanceof PNone) {
                return JavaTypeConversions.toInt(arg);
            } else {
                throw new RuntimeException("Not implemented integer with base: " + keywordArg);
            }
        }

        @SuppressWarnings("unused")
        public static boolean noKeywordArg(Object arg, Object keywordArg) {
            return (keywordArg instanceof PNone);
        }
    }

    // list([iterable])
    @Builtin(name = "list", minNumOfArguments = 0, maxNumOfArguments = 1, isConstructor = true)
    @GenerateNodeFactory
    public abstract static class ListNode extends PythonBuiltinNode {

        @Specialization
        public PList listString(String arg) {
            char[] chars = arg.toCharArray();
            PList list = new PList();

            for (char c : chars) {
                list.append(c);
            }

            return list;
        }

        @Specialization
        public PList listSequence(PList list) {
            return new PList(list.getStorage().copy());
        }

        @Specialization
        public PList listIterator(PIterator iterator) {
            return new PList(iterator);
        }

        @Specialization
        public PList listIterable(PIterable iterable) {
            return new PList(iterable.__iter__());
        }

        @Specialization
        public PList listObject(Object arg) {
            CompilerAsserts.neverPartOfCompilation();
            throw new RuntimeException("list does not support iterable object " + arg);
        }
    }

    // map(function, iterable, ...)
    @Builtin(name = "map", minNumOfArguments = 2, takesVariableArguments = true, isConstructor = true)
    @GenerateNodeFactory
    public abstract static class MapNode extends PythonBuiltinNode {

        @Child private MGMap<PNode, PFunction> MGmap;

        public MapNode() {
            this.MGmap = new MGMap.Uninitialized<>(MGPythonTree.PyTREE, new MapFallback());
        }

        public MapNode(MGMap<PNode, PFunction> MGmap) {
            this.MGmap = MGmap;
        }

        public abstract PNode[] getArguments();

        @Specialization
        public Object mapString(PythonCallable function, String str, PTuple iterators) {
            return doMap(function, new PString(str).__iter__(), iterators);
        }

        @SuppressWarnings("unused")
        // TODO: Disabled after merge.
        // @Specialization
        public Object mapClassIterable(PythonClass clazz, PIterable iterable, PTuple iterators) {
            PIterator iter = iterable.__iter__();
            PList list = new PList();

            try {
                while (true) {
                    Object item = iter.__next__();
                    PythonObject obj = PythonContext.newPythonObjectInstance(clazz);
                    Object[] selfWithArgs = new Object[2];

                    selfWithArgs[0] = obj;
                    selfWithArgs[1] = item;

                    PythonCallable initMethod = clazz.lookUpMethod("__init__");
                    if (initMethod != null) {
                        initMethod.call(selfWithArgs);
                        list.append(obj);
                    }

                }
            } catch (StopIterationException e) {

            }

            return list;
        }

        @Specialization(guards = "isOptimized()", rewriteOn = MGException.class)
        public Object mapFunctionIterableMGOpt(PFunction function, PList iterable, PTuple iterators) {
            return MGmap.map(function, iterable.len(), iterable, iterators.getArray());
        }

        @Specialization(guards = "isRetry(function)", rewriteOn = MGException.class)
        public Object mapFunctionIterableReOpt(PFunction function, PList iterable, PTuple iterators) {
            Object ret = MGmap.map(getSourceSection(), function, function.getFrameDescriptor(), iterable.len(), iterable, iterators.getArray());
            replace(MapNodeFactory.create(MGmap, getArguments(), getContext()), "MegaGuards Opt");
            return ret;
        }

        @Specialization(guards = "isFailed(function)")
        public Object mapFunctionIterable(PFunction function, PList iterable, PTuple iterators) {
            return doMap(function, iterable.__iter__(), iterators);
        }

        @Specialization
        public Object mapFunctionIterableRetry(PFunction function, PList iterable, PTuple iterators) {
            return replace(MapNodeFactory.create(new MGMap.Uninitialized<>(MGmap), getArguments(), getContext()), "MegaGuards Opt").mapFunctionIterableReOpt(function, iterable, iterators);
        }

        @Specialization
        public Object mapFunctionIterable(PythonCallable function, PIterable iterable, PTuple iterators) {
            return doMap(function, iterable.__iter__(), iterators);
        }

        private static PList doMap(PythonCallable mappingFunction, PIterator iter, PTuple iterators) {
            PList list = new PList();
            Object[] objIters = iterators.getArray();
            final int argsLen = objIters.length;
            PIterator[] iters = new PIterator[argsLen];
            for (int i = 0; i < argsLen; i++) {
                iters[i] = objIters[i] instanceof PList ? ((PList) objIters[i]).__iter__() : (PIterator) objIters[i];
            }
            final Object[] args = new Object[1 + argsLen];
            try {
                while (true) {
                    args[0] = iter.__next__();
                    for (int i = 1; i < (argsLen + 1); i++) {
                        args[i] = iters[i - 1].__next__();
                    }
                    list.append(mappingFunction.call(PArguments.createWithUserArguments(args)));
                }
            } catch (StopIterationException e) {

            }

            return list;
        }

        @SuppressWarnings("unused")
        @Specialization
        public Object mapSequence(Object function, Object iterable, PTuple iterators) {
            throw new RuntimeException("map is not supported for " + function + " " + function.getClass() + " iterable " + iterable + " " + iterable.getClass());
        }

        public boolean preCheck(PFunction function) {
            return ((MapFallback) MGmap.getFallback()).check(function);
        }

        public boolean isOptimized() {
            return !MGmap.isUninitialized();
        }

        public boolean isRetry(PFunction function) {
            return !isOptimized() && preCheck(function);
        }

        public boolean isFailed(PFunction function) {
            return !isOptimized() && !preCheck(function);
        }

    }

    // object()
    @Builtin(name = "object", maxNumOfArguments = 1, isConstructor = true)
    @GenerateNodeFactory
    public abstract static class ObjectNode extends PythonBuiltinNode {

        @Specialization
        public PythonObject doObject(@SuppressWarnings("unused") PNone none) {
            PythonContext context = getContext();
            return PythonContext.newPythonObjectInstance(context.getObjectClass());
        }

        @Specialization
        public PythonObject doObject(PythonObject newInstance) {
            return newInstance;
        }
    }

    // range(stop)
    // range(start, stop[, step])
    @Builtin(name = "range", minNumOfArguments = 1, maxNumOfArguments = 3, isConstructor = true)
    @GenerateNodeFactory
    public abstract static class RangeNode extends PythonBuiltinNode {

        @SuppressWarnings("unused")
        @Specialization(guards = "caseStop(start,step)")
        public PSequence rangeStop(int stop, Object start, Object step) {
            return new PRange(stop);
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "caseStop(start,step)")
        public PSequence rangeStop(long stop, Object start, Object step) {
            return new PRange(((Long) stop).intValue());
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "caseStop(start,step)")
        public PSequence rangeStop(BigInteger stop, Object start, Object step) {
            return new PRange(stop.intValue());
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "caseStartStop(step)")
        public PSequence rangeStartStop(int start, int stop, Object step) {
            return new PRange(start, stop);
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "caseStartStop(step)")
        public PSequence rangeStartStop(int start, long stop, Object step) {
            return new PRange(start, ((Long) stop).intValue());
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "caseStartStop(step)")
        public PSequence rangeStartStop(long start, int stop, Object step) {
            return new PRange(((Long) start).intValue(), stop);
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "caseStartStop(stop,start,step)")
        public PSequence rangeStartStop(long start, long stop, Object step) {
            return new PRange(((Long) start).intValue(), ((Long) stop).intValue());
        }

        @Specialization
        public PSequence rangeStartStopStep(int start, int stop, int step) {
            return new PRange(start, stop, step);
        }

        @Specialization
        public PSequence rangeStartStopStep(long start, long stop, long step) {
            return new PRange((int) start, ((Long) stop).intValue(), ((Long) step).intValue());
        }

        @TruffleBoundary
        @Specialization(guards = "isNumber(stop)")
        public PSequence rangeStartStopStep(Object start, Object stop, Object step) {
            if (isNumber(stop)) {
                int intStop = 0;
                if (stop instanceof Integer)
                    intStop = (int) stop;
                else if (stop instanceof Long)
                    intStop = ((Long) (stop)).intValue();
                else
                    intStop = ((BigInteger) stop).intValue();

                if (start instanceof PNone)
                    return new PRange(intStop);

                if (isNumber(start)) {
                    int intStart = 0;
                    if (start instanceof Integer)
                        intStart = (int) start;
                    else if (start instanceof Long)
                        intStart = ((Long) (start)).intValue();
                    else
                        intStart = ((BigInteger) start).intValue();

                    if (step instanceof PNone)
                        return new PRange(intStart, intStop);

                    if (isNumber(step)) {
                        int intStep = 0;
                        if (step instanceof Integer)
                            intStep = (int) step;
                        else if (step instanceof Long)
                            intStep = ((Long) (step)).intValue();
                        else
                            intStep = ((BigInteger) step).intValue();

                        return new PRange(intStart, intStop, intStep);
                    }
                }
            }

            throw Py.TypeError("range does not support " + start + ", " + stop + ", " + step);
        }

        @TruffleBoundary
        @Specialization(guards = "!isNumber(stop)")
        public PSequence rangeError(Object start, Object stop, Object step) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            throw Py.TypeError("range does not support " + start + ", " + stop + ", " + step);
        }

        public static boolean isNumber(Object value) {
            return value instanceof Integer || value instanceof Long || value instanceof BigInteger;
        }

        public static boolean caseStop(Object start, Object step) {
            return start == PNone.NONE && step == PNone.NONE;
        }

        public static boolean caseStartStop(Object step) {
            return step == PNone.NONE;
        }

        @SuppressWarnings("unused")
        public static boolean caseStartStop(long start, long stop, Object step) {
            return step == PNone.NONE;
        }

    }

    // set([iterable])
    @Builtin(name = "set", minNumOfArguments = 0, maxNumOfArguments = 1, isConstructor = true)
    @GenerateNodeFactory
    public abstract static class SetNode extends PythonBuiltinNode {

        @Specialization
        public PSet set(String arg) {
            return new PSet(new PStringIterator(arg));
        }

        @Specialization
        public PSet set(PSequence sequence) {
            return new PSet(sequence.__iter__());
        }

        @Specialization
        public PSet set(PBaseSet baseSet) {
            return new PSet(baseSet);
        }

        @Specialization
        public PSet set(PIterator iterator) {
            return new PSet(iterator);
        }

        @Specialization(guards = "emptyArguments(none)")
        public PSet set(@SuppressWarnings("unused") PNone none) {
            return new PSet();
        }

        @SuppressWarnings("unused")
        @Specialization
        public PSet set(VirtualFrame frame, Object arg) {
            if (!(arg instanceof Iterable<?>)) {
                throw Py.TypeError("'" + PythonTypesUtil.getPythonTypeName(arg) + "' object is not iterable");
            } else {
                throw new RuntimeException("set does not support iterable object " + arg);
            }
        }
    }

    // str(object='')
    // str(object=b'', encoding='utf-8', errors='strict')
    @Builtin(name = "str", minNumOfArguments = 0, maxNumOfArguments = 1, takesKeywordArguments = true, takesVariableKeywords = true, keywordNames = {"object, encoding, errors"}, isConstructor = true)
    @GenerateNodeFactory
    public abstract static class StrNode extends PythonBuiltinNode {

        @Specialization
        public String str(boolean arg) {
            return arg ? "True" : "False";
        }

        @Specialization
        public String str(int val) {
            return Integer.toString(val);
        }

        @Specialization
        public String str(double arg) {
            return JavaTypeConversions.doubleToString(arg);
        }

        @TruffleBoundary
        @Specialization
        public String str(PythonObject obj) {
            return PythonBuiltinNode.callAttributeSlowPath(obj, "__str__");
        }

        @Specialization
        public String str(Object arg) {
            return arg.toString();
        }
    }

    // tuple([iterable])
    @Builtin(name = "tuple", minNumOfArguments = 0, maxNumOfArguments = 1, isConstructor = true)
    @GenerateNodeFactory
    public abstract static class TupleNode extends PythonBuiltinNode {

        @Specialization()
        public PTuple tuple(String arg) {
            return new PTuple(new PStringIterator(arg));
        }

        @Specialization()
        public PTuple tuple(PIterable iterable) {
            return new PTuple(iterable.__iter__());
        }

        @Specialization()
        public PTuple tuple(PIterator iterator) {
            return new PTuple(iterator);
        }

        @Specialization
        public PTuple tuple(Object arg) {
            throw new RuntimeException("tuple does not support iterable object " + arg);
        }
    }

    // zip(*iterables)
    @Builtin(name = "zip", minNumOfArguments = 0, takesVariableArguments = true, isConstructor = true)
    @GenerateNodeFactory
    public abstract static class ZipNode extends PythonBuiltinNode {

        @Child protected GetIteratorNode getIterator;

        @Specialization
        public PZip zip(PTuple args) {
            if (getIterator == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                getIterator = insert(GetIteratorNodeFactory.create(EmptyNode.create()));
            }

            PIterator[] iterables = new PIterator[args.len()];

            for (int i = 0; i < args.len(); i++) {
                iterables[i] = getIterable(args.getItem(i));
            }

            return new PZip(iterables);
        }

        private PIterator getIterable(Object arg) {
            try {
                return PythonTypesGen.expectPIterator(getIterator.executeWith(null, arg));
            } catch (UnexpectedResultException e) {
                throw new RuntimeException("zip does not support iterable object " + arg.getClass());
            }
        }
    }

}
