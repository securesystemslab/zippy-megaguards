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

import edu.uci.megaguards.analysis.exception.TypeException;
import edu.uci.megaguards.object.ArrayInfo;
import edu.uci.megaguards.object.DataType;
import edu.uci.megaguards.unbox.Boxed;
import edu.uci.megaguards.unbox.Unboxer;
import edu.uci.megaguards.unbox.UnboxerUtil;
import edu.uci.python.nodes.PNode;
import edu.uci.python.runtime.sequence.PList;
import edu.uci.python.runtime.sequence.PTuple;
import edu.uci.python.runtime.sequence.storage.BoolSequenceStorage;
import edu.uci.python.runtime.sequence.storage.DoubleSequenceStorage;
import edu.uci.python.runtime.sequence.storage.IntSequenceStorage;
import edu.uci.python.runtime.sequence.storage.ListSequenceStorage;
import edu.uci.python.runtime.sequence.storage.LongSequenceStorage;
import edu.uci.python.runtime.sequence.storage.SequenceStorage;
import edu.uci.python.runtime.sequence.storage.TupleSequenceStorage;

public class PythonUnboxerUtil extends UnboxerUtil<PNode> {

    public static final PythonUnboxerUtil PyUTIL = new PythonUnboxerUtil();

    @Override
    public Object createList(DataType type, Object array) {
        SequenceStorage storage = null;

        switch (type) {
            case Int:
                storage = new IntSequenceStorage((int[]) array);
                break;
            case Double:
                storage = new DoubleSequenceStorage((double[]) array);
                break;
            case Long:
                storage = new LongSequenceStorage((long[]) array);
                break;
            case Bool:
                storage = new BoolSequenceStorage((boolean[]) array);
                break;
            default:
                throw TypeException.INSTANCE.message("Data type: " + type + "  not supported yet!");
        }

        return new PList(storage);

    }

    @TruffleBoundary
    public static ArrayInfo getTupleInfo(TupleSequenceStorage storage) {
        PTuple[] tuples = storage.getInternalPTupleArray();
        ArrayInfo info = new ArrayInfo(null, storage.length());
        traverseTuples(tuples[0], info);
        return info;
    }

    @TruffleBoundary
    public static ArrayInfo getTupleInfo(PTuple storage) {
        ArrayInfo info = new ArrayInfo(null);
        traverseTuples(storage, info);
        return info;
    }

    @TruffleBoundary
    public static ArrayInfo traverseTuples(PTuple tuple, ArrayInfo info) {
        info.addSize(tuple.len());
        Object first = tuple.getArray()[0];
        if (first instanceof PTuple)
            traverseTuples((PTuple) first, info);
        else if (first instanceof PList) {
            SequenceStorage storage = ((PList) first).getStorage();
            info.addSize(storage.length());
            info.setKind(storage.getClass());
        } else {
            info.setKind(first.getClass());
        }
        return info;
    }

    @TruffleBoundary
    public static ArrayInfo getListInfo(ListSequenceStorage storage) {
        PList[] lists = storage.getInternalListArray();
        ArrayInfo info = new ArrayInfo(storage.getKind(), storage.length());
        traverseLists(lists[0], info);
        return info;
    }

    @TruffleBoundary
    public static ArrayInfo traverseLists(PList list, ArrayInfo info) {
        info.addSize(list.len());
        SequenceStorage first = list.getStorage();
        if (first instanceof ListSequenceStorage)
            traverseLists(((ListSequenceStorage) first).getInternalListArray()[0], info);
        return info;
    }

    @Override
    public Boxed<?> BoxedInt(PNode node) {
        Boxed<?> specialized = null;
        if (node != null) {
            specialized = new PythonBoxed.IntBoxed(node);
        } else {
            specialized = new PythonArgumentBoxed.IntBoxed();
        }
        return specialized;
    }

    @Override
    public Boxed<?> BoxedLong(PNode node) {
        Boxed<?> specialized = null;
        if (node != null) {
            specialized = new PythonBoxed.LongBoxed(node);
        } else {
            specialized = new PythonArgumentBoxed.LongBoxed();
        }
        return specialized;
    }

    @Override
    public Boxed<?> BoxedDouble(PNode node) {
        Boxed<?> specialized = null;
        if (node != null) {
            specialized = new PythonBoxed.DoubleBoxed(node);
        } else {
            specialized = new PythonArgumentBoxed.DoubleBoxed();
        }
        return specialized;
    }

    @Override
    public Boxed<?> specialize1DArray(PNode node, Object val) {
        Boxed<?> specialized = null;

        Unboxer retVal = null;
        ArrayInfo info = null;
        int hashCode = val.hashCode();

        if (val instanceof PList) {
            PList storage = (PList) val;
            // Deal with only a single dimension
            if (storage.getStorage() instanceof IntSequenceStorage) {
                IntSequenceStorage intStorage = ((IntSequenceStorage) storage.getStorage());
                info = new ArrayInfo(IntSequenceStorage.class, intStorage.length());
                Object values = intStorage.getInternalIntArray();
                retVal = new PythonUnboxer.IntArray1D(values, info, hashCode, false);
                if (node != null) {
                    specialized = new PythonBoxed.ArrayBoxed.IntArrayBoxed(node, retVal);
                } else {
                    specialized = new PythonArgumentBoxed.ArgArrayBoxed.IntArrayBoxed(retVal);
                }
            } else if (storage.getStorage() instanceof LongSequenceStorage) {
                LongSequenceStorage longStorage = ((LongSequenceStorage) storage.getStorage());
                Object values = longStorage.getInternalLongArray();
                info = new ArrayInfo(LongSequenceStorage.class, longStorage.length());
                retVal = new PythonUnboxer.LongArray1D(values, info, hashCode, false);
                if (node != null) {
                    specialized = new PythonBoxed.ArrayBoxed.LongArrayBoxed(node, retVal);
                } else {
                    specialized = new PythonArgumentBoxed.ArgArrayBoxed.LongArrayBoxed(retVal);
                }
            } else if (storage.getStorage() instanceof DoubleSequenceStorage) {
                DoubleSequenceStorage doubleStorage = ((DoubleSequenceStorage) storage.getStorage());
                Object values = doubleStorage.getInternalDoubleArray();
                info = new ArrayInfo(DoubleSequenceStorage.class, doubleStorage.length());
                retVal = new PythonUnboxer.DoubleArray1D(values, info, hashCode, false);
                if (node != null) {
                    specialized = new PythonBoxed.ArrayBoxed.DoubleArrayBoxed(node, retVal);
                } else {
                    specialized = new PythonArgumentBoxed.ArgArrayBoxed.DoubleArrayBoxed(retVal);
                }
            }

            if (retVal != null)
                retVal.setChanged(storage.getStorage().isChangedAndReset());
        }
        return specialized;
    }

    @Override
    public Boxed<?> specializeArray(PNode node, Object val) {
        Boxed<?> specialized = specialize1DArray(node, val);

        if (specialized != null)
            return specialized;

        Unboxer retVal = null;
        ArrayInfo info = null;
        int hashCode = val.hashCode();

        if (val instanceof PList) {
            PList storage = (PList) val;
            // Deal with multiple dimensions
            if (storage.getStorage() instanceof ListSequenceStorage) {
                ListSequenceStorage listStorage = ((ListSequenceStorage) storage.getStorage());
                info = getListInfo(listStorage);
                if (info.getType() == IntSequenceStorage.class) {
                    retVal = new PythonUnboxer.IntArrayND.IntArrayNDList(listStorage, info, hashCode, false);
                } else if (info.getType() == LongSequenceStorage.class) {
                    retVal = new PythonUnboxer.LongArrayND.LongArrayNDList(listStorage, info, hashCode, false);
                } else if (info.getType() == DoubleSequenceStorage.class) {
                    retVal = new PythonUnboxer.DoubleArrayND.DoubleArrayNDList(listStorage, info, hashCode, false);
                }
                if (retVal != null)
                    if (node != null) {
                        specialized = new PythonBoxed.ArrayBoxed.ArrayNDBoxed.ListSequence(node, retVal);
                    } else {
                        /*- specialized = new PythonArgumentBoxed.ArgArrayBoxed.ArrayNDBoxed.ListSequence(retVal); */
                        throw TypeException.INSTANCE.message("Not supported yet.");
                    }
            } else if (storage.getStorage() instanceof TupleSequenceStorage) {
                TupleSequenceStorage listStorage = ((TupleSequenceStorage) storage.getStorage());
                info = getTupleInfo(listStorage);
                Class<?> type = info.getType();
                if (type == IntSequenceStorage.class) {
                    retVal = new PythonUnboxer.IntArrayND.IntArrayNDTupleSequence(listStorage, info, hashCode, false);
                } else if (type == LongSequenceStorage.class) {
                    retVal = new PythonUnboxer.LongArrayND.LongArrayNDTupleSequence(listStorage, info, hashCode, false);
                } else if (type == DoubleSequenceStorage.class) {
                    retVal = new PythonUnboxer.DoubleArrayND.DoubleArrayNDTupleSequence(listStorage, info, hashCode, false);
                }
                if (retVal != null)
                    if (node != null) {
                        specialized = new PythonBoxed.ArrayBoxed.ArrayNDBoxed.TupleSequence(node, retVal);
                    } else {
                        /*- specialized = new PythonArgumentBoxed.ArgArrayBoxed.ArrayNDBoxed.TupleSequence(retVal); */
                        throw TypeException.INSTANCE.message("Not supported yet.");
                    }
            }

            if (retVal != null)
                retVal.setChanged(storage.getStorage().isChangedAndReset());

        } else if (val instanceof PTuple) {
            PTuple tuple = (PTuple) val;
            info = getTupleInfo(tuple);
            Class<?> type = info.getType();

            if (type == IntSequenceStorage.class) {
                retVal = new PythonUnboxer.IntArrayND.IntArrayNDTuple(tuple, info, hashCode, false);
            } else if (type == LongSequenceStorage.class) {
                retVal = new PythonUnboxer.LongArrayND.LongArrayNDTuple(tuple, info, hashCode, false);
            } else if (type == DoubleSequenceStorage.class) {
                retVal = new PythonUnboxer.DoubleArrayND.DoubleArrayNDTuple(tuple, info, hashCode, false);
            }
            if (retVal != null)
                if (node != null) {
                    specialized = new PythonBoxed.ArrayBoxed.ArrayNDBoxed.Tuple(node, retVal);
                } else {
                    /*- specialized = new PythonArgumentBoxed.ArgArrayBoxed.ArrayNDBoxed.Tuple(retVal); */
                    throw TypeException.INSTANCE.message("Not supported yet.");
                }
        }

        return specialized;
    }

}
