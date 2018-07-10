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
package edu.uci.megaguards.python.unbox;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;

import edu.uci.megaguards.analysis.exception.TypeException;
import edu.uci.megaguards.object.ArrayInfo;
import edu.uci.megaguards.object.DataType;
import edu.uci.megaguards.unbox.Boxed;
import edu.uci.megaguards.unbox.Unboxer;
import edu.uci.python.nodes.PNode;
import edu.uci.python.runtime.datatype.PComplex;
import edu.uci.python.runtime.sequence.PList;
import edu.uci.python.runtime.sequence.PTuple;
import edu.uci.python.runtime.sequence.storage.BoolSequenceStorage;
import edu.uci.python.runtime.sequence.storage.DoubleSequenceStorage;
import edu.uci.python.runtime.sequence.storage.IntSequenceStorage;
import edu.uci.python.runtime.sequence.storage.ListSequenceStorage;
import edu.uci.python.runtime.sequence.storage.LongSequenceStorage;
import edu.uci.python.runtime.sequence.storage.SequenceStorage;
import edu.uci.python.runtime.sequence.storage.TupleSequenceStorage;

public abstract class PythonBoxed extends Boxed<PNode> {

    public static final PythonBoxed PyBOX = new GenericBoxed(null);

    public PythonBoxed(PNode valueNode, DataType type) {
        super(valueNode, type);
    }

    public static class GenericBoxed extends PythonBoxed {

        public GenericBoxed(PNode valueNode) {
            super(valueNode, DataType.None);
        }

        @Override
        public Object getUnboxed(VirtualFrame frame, Object v) {
            return valueNode.execute(frame);
        }

    }

    public static class IntBoxed extends PythonBoxed {

        public IntBoxed(PNode valueNode) {
            super(valueNode, DataType.Int);
        }

        @Override
        public Object getUnboxed(VirtualFrame frame, Object v) {
            Object newVal = valueNode.execute(frame);
            final int value;
            if (newVal instanceof Integer) {
                return newVal;
            } else if (newVal instanceof Long) {
                value = ((Long) newVal).intValue();
                if (value == (long) newVal) {
                    return value;
                }
            }

            throw TypeException.INSTANCE.message("Guard Failed! (Type miss-match)");
        }

    }

    public static class IntLengthBoxed extends PythonBoxed {

        public IntLengthBoxed(PNode valueNode) {
            super(valueNode, DataType.Int);
        }

        @Override
        public Object getUnboxed(VirtualFrame frame, Object v) {
            Object value = valueNode.execute(frame);
            if (value instanceof Integer) {
                return value;
            } else if (value instanceof PList) // stop variable in For statement
                return ((PList) value).len();
            else if (value instanceof PTuple) // stop variable in For statement
                return ((PTuple) value).len();

            throw TypeException.INSTANCE.message("Guard Failed! (Type miss-match)");
        }

    }

    public static class LongBoxed extends PythonBoxed {

        public LongBoxed(PNode valueNode) {
            super(valueNode, DataType.Long);
        }

        @Override
        public Object getUnboxed(VirtualFrame frame, Object v) {
            Object newVal = valueNode.execute(frame);
            if (newVal instanceof Long) {
                return newVal;
            } else if (newVal instanceof Integer) {
                return (long) newVal;
            }

            throw TypeException.INSTANCE.message("Guard Failed! (Type miss-match)");
        }

    }

    public static class DoubleBoxed extends PythonBoxed {

        public DoubleBoxed(PNode valueNode) {
            super(valueNode, DataType.Double);
        }

        @Override
        public Object getUnboxed(VirtualFrame frame, Object v) {
            Object newVal = valueNode.execute(frame);
            if (newVal instanceof Double) {
                return newVal;
            }

            throw TypeException.INSTANCE.message("Guard Failed! (Type miss-match)");
        }

    }

    public static class ComplexRealBoxed extends PythonBoxed {

        public ComplexRealBoxed(PNode valueNode) {
            super(valueNode, DataType.Double);
        }

        @Override
        public Object getUnboxed(VirtualFrame frame, Object v) {
            Object newVal = valueNode.execute(frame);
            if (newVal instanceof PComplex) {
                return ((PComplex) newVal).getReal();
            }

            throw TypeException.INSTANCE.message("Guard Failed! (Type miss-match)");
        }

    }

    public static class ComplexImagBoxed extends PythonBoxed {

        public ComplexImagBoxed(PNode valueNode) {
            super(valueNode, DataType.Double);
        }

        @Override
        public Object getUnboxed(VirtualFrame frame, Object v) {
            Object newVal = valueNode.execute(frame);
            if (newVal instanceof PComplex) {
                return ((PComplex) newVal).getImag();
            }

            throw TypeException.INSTANCE.message("Guard Failed! (Type miss-match)");
        }

    }

    public static class BooleanBoxed extends PythonBoxed {

        public BooleanBoxed(PNode valueNode) {
            super(valueNode, DataType.Bool);
        }

        @Override
        public Object getUnboxed(VirtualFrame frame, Object v) {
            Object newVal = valueNode.execute(frame);
            if (newVal instanceof Boolean) {
                return newVal;
            }

            throw TypeException.INSTANCE.message("Guard Failed! (Type miss-match)");
        }

    }

    public abstract static class ArrayBoxed extends PythonBoxed {

        protected final Unboxer boxed;

        public ArrayBoxed(PNode valueNode, Unboxer boxed, DataType type) {
            super(valueNode, type);
            this.boxed = boxed;
        }

        @Override
        public Unboxer getUnboxer() {
            return boxed;
        }

        public static class IntArrayBoxed extends ArrayBoxed {

            public IntArrayBoxed(PNode valueNode, Unboxer boxed) {
                super(valueNode, boxed, DataType.IntArray);
            }

            @Override
            public Object getUnboxed(VirtualFrame frame, Object v) {
                final Object val = valueNode.execute(frame);
                if (val instanceof PList) {
                    final PList storage = (PList) val;
                    if (storage.getStorage() instanceof IntSequenceStorage) {
                        final IntSequenceStorage IntStorage = ((IntSequenceStorage) storage.getStorage());
                        final Object values = IntStorage.getInternalIntArray();
                        boxed.getInfo().setSize(0, IntStorage.length());
                        boxed.setValue(values);
                        boxed.setChanged(storage.getStorage().isChangedAndReset());
                        Boxed.addBoxed(boxed);
                    } else
                        throw TypeException.INSTANCE.message("Guard Failed! (Type miss-match)");
                } else
                    throw TypeException.INSTANCE.message("Guard Failed! (Type miss-match)");

                return boxed;
            }

        }

        public static class LongArrayBoxed extends ArrayBoxed {

            public LongArrayBoxed(PNode valueNode, Unboxer boxed) {
                super(valueNode, boxed, DataType.LongArray);
            }

            @Override
            public Object getUnboxed(VirtualFrame frame, Object v) {
                final Object val = valueNode.execute(frame);
                if (val instanceof PList) {
                    final PList storage = (PList) val;
                    if (storage.getStorage() instanceof LongSequenceStorage) {
                        final LongSequenceStorage longStorage = ((LongSequenceStorage) storage.getStorage());
                        final Object values = longStorage.getInternalLongArray();
                        boxed.getInfo().setSize(0, longStorage.length());
                        boxed.setValue(values);
                        boxed.setChanged(storage.getStorage().isChangedAndReset());
                        Boxed.addBoxed(boxed);
                    } else
                        throw TypeException.INSTANCE.message("Guard Failed! (Type miss-match)");
                } else
                    throw TypeException.INSTANCE.message("Guard Failed! (Type miss-match)");

                return boxed;
            }

        }

        public static class DoubleArrayBoxed extends ArrayBoxed {

            public DoubleArrayBoxed(PNode valueNode, Unboxer boxed) {
                super(valueNode, boxed, DataType.DoubleArray);
            }

            @Override
            public Object getUnboxed(VirtualFrame frame, Object v) {
                final Object val = valueNode.execute(frame);
                if (val instanceof PList) {
                    final PList storage = (PList) val;
                    if (storage.getStorage() instanceof DoubleSequenceStorage) {
                        final DoubleSequenceStorage DoubleStorage = ((DoubleSequenceStorage) storage.getStorage());
                        final Object values = DoubleStorage.getInternalDoubleArray();
                        boxed.getInfo().setSize(0, DoubleStorage.length());
                        boxed.setValue(values);
                        boxed.setChanged(storage.getStorage().isChangedAndReset());
                        Boxed.addBoxed(boxed);
                    } else
                        throw TypeException.INSTANCE.message("Guard Failed! (Type miss-match)");
                } else
                    throw TypeException.INSTANCE.message("Guard Failed! (Type miss-match)");

                return boxed;
            }

        }

        public abstract static class BoolArrayBoxed extends ArrayBoxed {

            public BoolArrayBoxed(PNode valueNode, Unboxer boxed) {
                super(valueNode, boxed, DataType.BoolArray);
            }

            @Override
            public Object getUnboxed(VirtualFrame frame, Object v) {
                final Object val = valueNode.execute(frame);
                if (val instanceof PList) {
                    final PList storage = (PList) val;
                    if (storage.getStorage() instanceof BoolSequenceStorage) {
                        final BoolSequenceStorage BoolStorage = ((BoolSequenceStorage) storage.getStorage());
                        final Object values = BoolStorage.getInternalBoolArray();
                        boxed.getInfo().setSize(0, BoolStorage.length());
                        boxed.setValue(values);
                        boxed.setChanged(storage.getStorage().isChangedAndReset());
                        Boxed.addBoxed(boxed);
                    } else
                        throw TypeException.INSTANCE.message("Guard Failed! (Type miss-match)");
                } else
                    throw TypeException.INSTANCE.message("Guard Failed! (Type miss-match)");

                return boxed;
            }

        }

        public abstract static class ArrayNDBoxed extends ArrayBoxed {

            public ArrayNDBoxed(PNode valueNode, Unboxer boxed) {
                super(valueNode, boxed, boxed.getKind());
            }

            public static class ListSequence extends ArrayNDBoxed {

                public ListSequence(PNode valueNode, Unboxer boxed) {
                    super(valueNode, boxed);
                }

                protected static void updateArrayInfo(ListSequenceStorage storage, ArrayInfo info) {
                    if (info.getType() != storage.getKind())
                        throw TypeException.INSTANCE.message("Guard Failed! (Type miss-match)");
                    final int dims = info.getDim();
                    info.resetDim();
                    final PList[] lists = storage.getInternalListArray();
                    info.addSize(storage.length());
                    PythonUnboxerUtil.traverseLists(lists[0], info);
                    if (dims != info.getDim())
                        throw TypeException.INSTANCE.message("Guard Failed! (Dimention miss-match)");
                }

                @Override
                public Object getUnboxed(VirtualFrame frame, Object v) {
                    final Object val = valueNode.execute(frame);
                    if (val instanceof PList) {
                        final PList storage = (PList) val;
                        if (storage.getStorage() instanceof ListSequenceStorage) {
                            final int hashCode = val.hashCode();
                            final ListSequenceStorage listStorage = ((ListSequenceStorage) storage.getStorage());
                            updateArrayInfo(listStorage, boxed.getInfo());
                            boxed.setOriginHashCode(hashCode);
                            boxed.setValue(listStorage);
                            boxed.setChanged(storage.getStorage().isChangedAndReset());
                            Boxed.addBoxed(boxed);
                        } else
                            throw TypeException.INSTANCE.message("Guard Failed! (Type miss-match)");
                    } else
                        throw TypeException.INSTANCE.message("Guard Failed! (Type miss-match)");

                    return boxed;
                }

            }

            @TruffleBoundary
            private static ArrayInfo traverseUpdateTuples(PTuple tuple, ArrayInfo info) {
                info.addSize(tuple.len());
                final Object first = tuple.getArray()[0];
                if (first instanceof PTuple)
                    PythonUnboxerUtil.traverseTuples((PTuple) first, info);
                else if (first instanceof PList) {
                    final SequenceStorage storage = ((PList) first).getStorage();
                    info.addSize(storage.length());
                    if (info.getType() != storage.getClass())
                        throw TypeException.INSTANCE.message("Guard Failed! (Type miss-match)");
                } else {
                    if (info.getType() != first.getClass())
                        throw TypeException.INSTANCE.message("Guard Failed! (Type miss-match)");
                }
                return info;
            }

            public static class Tuple extends ArrayNDBoxed {

                public Tuple(PNode valueNode, Unboxer boxed) {
                    super(valueNode, boxed);
                }

                protected static void updateArrayInfo(PTuple tuple, ArrayInfo info) {
                    final int dims = info.getDim();
                    info.resetDim();
                    traverseUpdateTuples(tuple, info);
                    if (dims != info.getDim())
                        throw TypeException.INSTANCE.message("Guard Failed! (Dimention miss-match)");
                }

                @Override
                public Object getUnboxed(VirtualFrame frame, Object v) {
                    final Object val = valueNode.execute(frame);
                    if (val instanceof PTuple) {
                        final int hashCode = val.hashCode();
                        final PTuple tuple = (PTuple) val;
                        updateArrayInfo(tuple, boxed.getInfo());
                        boxed.setOriginHashCode(hashCode);
                        boxed.setValue(tuple);
                        Boxed.addBoxed(boxed);
                    } else
                        throw TypeException.INSTANCE.message("Guard Failed! (Type miss-match)");

                    return boxed;
                }

            }

            public static class TupleSequence extends ArrayNDBoxed {

                public TupleSequence(PNode valueNode, Unboxer boxed) {
                    super(valueNode, boxed);
                }

                @TruffleBoundary
                private static void updateArrayInfo(TupleSequenceStorage storage, ArrayInfo info) {
                    final int dims = info.getDim();
                    info.resetDim();
                    final PTuple[] tuples = storage.getInternalPTupleArray();
                    info.addSize(storage.length());
                    traverseUpdateTuples(tuples[0], info);
                    if (dims != info.getDim())
                        throw TypeException.INSTANCE.message("Guard Failed! (Dimention miss-match)");
                }

                @Override
                public Object getUnboxed(VirtualFrame frame, Object v) {
                    final Object val = valueNode.execute(frame);
                    if (val instanceof PList) {
                        final PList storage = (PList) val;
                        if (storage.getStorage() instanceof TupleSequenceStorage) {
                            final int hashCode = val.hashCode();
                            final TupleSequenceStorage listStorage = ((TupleSequenceStorage) storage.getStorage());
                            updateArrayInfo(listStorage, boxed.getInfo());
                            boxed.setOriginHashCode(hashCode);
                            boxed.setValue(listStorage);
                            boxed.setChanged(listStorage.isChangedAndReset());
                            Boxed.addBoxed(boxed);
                        } else
                            throw TypeException.INSTANCE.message("Guard Failed! (Type miss-match)");
                    } else
                        throw TypeException.INSTANCE.message("Guard Failed! (Type miss-match)");

                    return boxed;
                }

            }

        }

    }

}
