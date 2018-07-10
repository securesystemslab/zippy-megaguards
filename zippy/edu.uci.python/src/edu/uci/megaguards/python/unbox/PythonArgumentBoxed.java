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

import com.oracle.truffle.api.frame.VirtualFrame;

import edu.uci.megaguards.analysis.exception.TypeException;
import edu.uci.megaguards.object.DataType;
import edu.uci.megaguards.unbox.Boxed;
import edu.uci.megaguards.unbox.Unboxer;
import edu.uci.python.runtime.datatype.PNone;
import edu.uci.python.runtime.sequence.PList;
import edu.uci.python.runtime.sequence.PTuple;
import edu.uci.python.runtime.sequence.storage.BoolSequenceStorage;
import edu.uci.python.runtime.sequence.storage.DoubleSequenceStorage;
import edu.uci.python.runtime.sequence.storage.IntSequenceStorage;
import edu.uci.python.runtime.sequence.storage.LongSequenceStorage;

public abstract class PythonArgumentBoxed extends PythonBoxed {

    public PythonArgumentBoxed(DataType type) {
        super(null, type);
    }

    public static class IntBoxed extends PythonArgumentBoxed {

        public IntBoxed() {
            super(DataType.Int);
        }

        @Override
        public Object getUnboxed(VirtualFrame frame, Object newVal) {
            final int value;
            if (newVal instanceof Integer) {
                return newVal;
            } else if (newVal instanceof Long) {
                value = ((Long) newVal).intValue();
                if (value == (long) newVal) {
                    return value;
                }
            }
            if (newVal instanceof PNone) {
                return 0;
            }

            throw TypeException.INSTANCE.message("Guard Failed! (Type miss-match)");
        }

    }

    public static class IntLengthBoxed extends PythonArgumentBoxed {

        public IntLengthBoxed() {
            super(DataType.Int);
        }

        @Override
        public Object getUnboxed(VirtualFrame frame, Object value) {
            if (value instanceof PList) // stop variable in For statement
                return ((PList) value).len();
            else if (value instanceof PTuple) // stop variable in For statement
                return ((PTuple) value).len();

            throw TypeException.INSTANCE.message("Guard Failed! (Type miss-match)");
        }

    }

    public static class LongBoxed extends PythonArgumentBoxed {

        public LongBoxed() {
            super(DataType.Long);
        }

        @Override
        public Object getUnboxed(VirtualFrame frame, Object newVal) {
            if (newVal instanceof Long) {
                return newVal;
            } else if (newVal instanceof Integer) {
                return (long) newVal;
            }
            if (newVal instanceof PNone) {
                return 0L;
            }

            throw TypeException.INSTANCE.message("Guard Failed! (Type miss-match)");
        }

    }

    public static class DoubleBoxed extends PythonArgumentBoxed {

        public DoubleBoxed() {
            super(DataType.Double);
        }

        @Override
        public Object getUnboxed(VirtualFrame frame, Object val) {
            if (val instanceof Double) {
                return val;
            }
            if (val instanceof PNone) {
                return 0.0;
            }

            throw TypeException.INSTANCE.message("Guard Failed! (Type miss-match)");
        }

    }

    public static class NoneBoxed extends PythonArgumentBoxed {

        public NoneBoxed() {
            super(DataType.Int);
        }

        @Override
        public Object getUnboxed(VirtualFrame frame, Object val) {
            if (val instanceof PNone) {
                return 0;
            } else {
                return 1;
            }

        }

    }

    public abstract static class ArgArrayBoxed extends PythonArgumentBoxed {

        protected final Unboxer boxed;

        public ArgArrayBoxed(Unboxer boxed) {
            super(boxed.getKind());
            this.boxed = boxed;
        }

        @Override
        public Unboxer getUnboxer() {
            return boxed;
        }

        public static class IntArrayBoxed extends ArgArrayBoxed {

            public IntArrayBoxed(Unboxer boxed) {
                super(boxed);
            }

            @Override
            public Object getUnboxed(VirtualFrame frame, Object val) {
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

        public static class LongArrayBoxed extends ArgArrayBoxed {

            public LongArrayBoxed(Unboxer boxed) {
                super(boxed);
            }

            @Override
            public Object getUnboxed(VirtualFrame frame, Object val) {
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

        public static class DoubleArrayBoxed extends ArgArrayBoxed {

            public DoubleArrayBoxed(Unboxer boxed) {
                super(boxed);
            }

            @Override
            public Object getUnboxed(VirtualFrame frame, Object val) {
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

        public abstract static class BoolArrayBoxed extends ArgArrayBoxed {

            public BoolArrayBoxed(Unboxer boxed) {
                super(boxed);
            }

            @Override
            public Object getUnboxed(VirtualFrame frame, Object val) {
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
    }
}
