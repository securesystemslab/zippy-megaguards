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

import java.util.ArrayList;
import java.util.List;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

import edu.uci.megaguards.MGOptions;
import edu.uci.megaguards.analysis.exception.TypeException;
import edu.uci.megaguards.log.MGLog;
import edu.uci.megaguards.object.ArrayInfo;
import edu.uci.megaguards.object.DataType;
import edu.uci.megaguards.unbox.Unboxer;
import edu.uci.python.runtime.sequence.PList;
import edu.uci.python.runtime.sequence.PTuple;
import edu.uci.python.runtime.sequence.storage.DoubleSequenceStorage;
import edu.uci.python.runtime.sequence.storage.IntSequenceStorage;
import edu.uci.python.runtime.sequence.storage.ListSequenceStorage;
import edu.uci.python.runtime.sequence.storage.LongSequenceStorage;
import edu.uci.python.runtime.sequence.storage.SequenceStorage;
import edu.uci.python.runtime.sequence.storage.TupleSequenceStorage;

public abstract class PythonUnboxer extends Unboxer {

    public PythonUnboxer(Object value, ArrayInfo info, DataType kind, int OriginHashCode, boolean ignore) {
        super(value, info, kind, OriginHashCode, ignore);
    }

    public static class LongArray1D extends PythonUnboxer {

        public LongArray1D(Object value, ArrayInfo info, int OriginHashCode, boolean ignore) {
            super(value, info, DataType.LongArray, OriginHashCode, ignore);
        }

        @Override
        public Object unbox() throws TypeException {
            return value;
        }

        @Override
        public int getTypeSize() {
            return 8;
        }
    }

    public static class IntArray1D extends PythonUnboxer {

        public IntArray1D(Object value, ArrayInfo info, int OriginHashCode, boolean ignore) {
            super(value, info, DataType.IntArray, OriginHashCode, ignore);
        }

        @Override
        public Object unbox() throws TypeException {
            return value;
        }

        @Override
        public int getTypeSize() {
            return 4;
        }
    }

    public static class DoubleArray1D extends PythonUnboxer {

        public DoubleArray1D(Object value, ArrayInfo info, int OriginHashCode, boolean ignore) {
            super(value, info, DataType.DoubleArray, OriginHashCode, ignore);
        }

        @Override
        public Object unbox() throws TypeException {
            return value;
        }

        @Override
        public int getTypeSize() {
            return 8;
        }
    }

    public abstract static class LongArrayND extends PythonUnboxer {

        public LongArrayND(Object value, ArrayInfo info, int OriginHashCode, boolean ignore) {
            super(value, info, DataType.LongArray, OriginHashCode, ignore);
        }

        @TruffleBoundary
        private static void longRecursiveProbingArrays(ListSequenceStorage listStorage, ArrayInfo info, int currentDim, long[][] retVal, int j) {
            int length = listStorage.length();
            if (length > 0) {
                SequenceStorage ith = listStorage.getInternalListArray()[0].getStorage();
                if (ith instanceof ListSequenceStorage) {
                    int dim = currentDim + 1;
                    int localSize = info.getSize(dim, info.getDim() - 1);
                    for (int i = 0; i < length; i++) {
                        ith = listStorage.getInternalListArray()[i].getStorage();
                        longRecursiveProbingArrays((ListSequenceStorage) ith, info, dim, retVal, i * localSize + j);
                    }
                } else
                    for (int i = 0; i < length; i++) {
                        ith = listStorage.getInternalListArray()[i].getStorage();
                        retVal[i + j] = ((LongSequenceStorage) ith).getInternalLongArray();
                    }
            }
        }

        @TruffleBoundary
        private static List<long[]> longRecursiveProbingArrays(PTuple tuple) {
            List<long[]> arrays = new ArrayList<>();
            int length = tuple.len();
            for (int i = 0; i < length; i++) {
                Object ith = tuple.getArray()[i];
                if (ith instanceof PTuple)
                    arrays.addAll(longRecursiveProbingArrays((PTuple) ith));
                else
                    arrays.add(((LongSequenceStorage) ((PList) ith).getStorage()).getInternalLongArray());
            }
            return arrays;
        }

        public static class LongArrayNDList extends LongArrayND {

            public LongArrayNDList(Object value, ArrayInfo info, int OriginHashCode, boolean ignore) {
                super(value, info, OriginHashCode, ignore);
            }

            @TruffleBoundary
            public static long[][] longConvertTo2dim(ListSequenceStorage listStorage, ArrayInfo info) {
                if (MGOptions.ReuseStorage)
                    if (UNBOXED.containsKey(listStorage.getInternalListArray().hashCode())) {
                        if ((MGOptions.Log.TraceUnboxing))
                            MGLog.printlnTagged("long storage reused");
                        return (long[][]) UNBOXED.get(listStorage.getInternalListArray().hashCode());
                    }

                long s = System.currentTimeMillis();
                int dims = info.getDim();
                long[][] retVal = null;
                int size = info.getSize(0, dims - 1);
                retVal = new long[size][0];
                longRecursiveProbingArrays(listStorage, info, 0, retVal, 0);
                if ((MGOptions.Log.TraceUnboxing))
                    MGLog.printlnTagged("List: long[]..[] to long[" + size + "][" + info.getSize(dims - 1) + "] (" + (System.currentTimeMillis() - s) + " ms) ");

                if (MGOptions.ReuseStorage)
                    UNBOXED.put(listStorage.getInternalListArray().hashCode(), retVal);
                return retVal;
            }

            @Override
            public Object unbox() throws TypeException {
                if (value instanceof ListSequenceStorage) {
                    return longConvertTo2dim((ListSequenceStorage) value, info);
                }
                throw TypeException.INSTANCE.message("Couldn't unbox value: " + value.getClass());

            }

        }

        public static class LongArrayNDTuple extends LongArrayND {

            public LongArrayNDTuple(Object value, ArrayInfo info, int OriginHashCode, boolean ignore) {
                super(value, info, OriginHashCode, ignore);
            }

            @TruffleBoundary
            public static long[][] longConvertTo2dim(PTuple tuple) {
                if (MGOptions.ReuseStorage)
                    if (UNBOXED.containsKey(tuple.getArray().hashCode())) {
                        if ((MGOptions.Log.TraceUnboxing))
                            MGLog.printlnTagged("Long storage reused");
                        return (long[][]) UNBOXED.get(tuple.getArray().hashCode());
                    }
                long s = System.currentTimeMillis();
                List<long[]> arrays = new ArrayList<>();
                arrays.addAll(longRecursiveProbingArrays(tuple));
                int size = arrays.size();
                long[][] ret = arrays.toArray(new long[size][0]);
                if ((MGOptions.Log.TraceUnboxing))
                    MGLog.printlnTagged("Tuple: long[]..[] to long[" + size + "][..] (" + (System.currentTimeMillis() - s) + " ms) ");
                if (MGOptions.ReuseStorage)
                    UNBOXED.put(tuple.getArray().hashCode(), ret);
                return ret;
            }

            @Override
            public Object unbox() throws TypeException {
                if (value instanceof PTuple) {
                    return longConvertTo2dim((PTuple) value);
                }
                throw TypeException.INSTANCE.message("Couldn't unbox value: " + value.getClass());

            }

        }

        public static class LongArrayNDTupleSequence extends LongArrayND {

            public LongArrayNDTupleSequence(Object value, ArrayInfo info, int OriginHashCode, boolean ignore) {
                super(value, info, OriginHashCode, ignore);
            }

            @TruffleBoundary
            public static long[][] longConvertTo2dim(TupleSequenceStorage listStorage) {
                if (MGOptions.ReuseStorage)
                    if (UNBOXED.containsKey(listStorage.getInternalPTupleArray().hashCode())) {
                        if ((MGOptions.Log.TraceUnboxing))
                            MGLog.printlnTagged("Long storage reused");
                        return (long[][]) UNBOXED.get(listStorage.getInternalPTupleArray().hashCode());
                    }
                long s = System.currentTimeMillis();
                List<long[]> arrays = new ArrayList<>();
                int length = listStorage.length();
                for (int i = 0; i < length; i++) {
                    PTuple ith = listStorage.getInternalPTupleArray()[i];
                    arrays.addAll(longRecursiveProbingArrays(ith));
                }

                int size = arrays.size();
                long[][] ret = arrays.toArray(new long[size][0]);
                if ((MGOptions.Log.TraceUnboxing))
                    MGLog.printlnTagged("Tuple: long[]..[] to long[" + size + "][..] (" + (System.currentTimeMillis() - s) + " ms) ");
                if (MGOptions.ReuseStorage)
                    UNBOXED.put(listStorage.getInternalPTupleArray().hashCode(), ret);
                return ret;
            }

            @Override
            public Object unbox() throws TypeException {
                if (value instanceof TupleSequenceStorage) {
                    return longConvertTo2dim((TupleSequenceStorage) value);
                }
                throw TypeException.INSTANCE.message("Couldn't unbox value: " + value.getClass());

            }

        }

        @Override
        public int getTypeSize() {
            return 8;
        }
    }

    public abstract static class IntArrayND extends PythonUnboxer {

        public IntArrayND(Object value, ArrayInfo info, int OriginHashCode, boolean ignore) {
            super(value, info, DataType.IntArray, OriginHashCode, ignore);
        }

        @TruffleBoundary
        private static List<int[]> intRecursiveProbingArrays(PTuple tuple) {
            List<int[]> arrays = new ArrayList<>();
            int length = tuple.len();
            for (int i = 0; i < length; i++) {
                Object ith = tuple.getArray()[i];
                if (ith instanceof PTuple)
                    arrays.addAll(intRecursiveProbingArrays((PTuple) ith));
                else
                    arrays.add(((IntSequenceStorage) ((PList) ith).getStorage()).getInternalIntArray());
            }
            return arrays;
        }

        public static class IntArrayNDList extends IntArrayND {

            public IntArrayNDList(Object value, ArrayInfo info, int OriginHashCode, boolean ignore) {
                super(value, info, OriginHashCode, ignore);
            }

            @TruffleBoundary
            private static void intRecursiveProbingArrays(ListSequenceStorage listStorage, ArrayInfo info, int currentDim, int[][] retVal, int j) {
                int length = listStorage.length();
                if (length > 0) {
                    SequenceStorage ith = listStorage.getInternalListArray()[0].getStorage();
                    if (ith instanceof ListSequenceStorage) {
                        int dim = currentDim + 1;
                        int localSize = info.getSize(dim, info.getDim() - 1);
                        for (int i = 0; i < length; i++) {
                            ith = listStorage.getInternalListArray()[i].getStorage();
                            intRecursiveProbingArrays((ListSequenceStorage) ith, info, dim, retVal, i * localSize + j);
                        }
                    } else
                        for (int i = 0; i < length; i++) {
                            ith = listStorage.getInternalListArray()[i].getStorage();
                            retVal[i + j] = ((IntSequenceStorage) ith).getInternalIntArray();
                        }
                }
            }

            @TruffleBoundary
            public static int[][] intConvertTo2dim(ListSequenceStorage listStorage, ArrayInfo info) {
                if (MGOptions.ReuseStorage)
                    if (UNBOXED.containsKey(listStorage.getInternalListArray().hashCode())) {
                        if ((MGOptions.Log.TraceUnboxing))
                            MGLog.printlnTagged("Int storage reused");
                        return (int[][]) UNBOXED.get(listStorage.getInternalListArray().hashCode());
                    }

                long s = System.currentTimeMillis();
                int dims = info.getDim();
                int[][] retVal = null;
                int size = info.getSize(0, dims - 1);
                retVal = new int[size][0];
                intRecursiveProbingArrays(listStorage, info, 0, retVal, 0);
                if ((MGOptions.Log.TraceUnboxing))
                    MGLog.printlnTagged("List: int[]..[] to int[" + size + "][" + info.getSize(dims - 1) + "] (" + (System.currentTimeMillis() - s) + " ms) ");

                if (MGOptions.ReuseStorage)
                    UNBOXED.put(listStorage.getInternalListArray().hashCode(), retVal);
                return retVal;
            }

            @Override
            public Object unbox() throws TypeException {
                if (value instanceof ListSequenceStorage) {
                    return intConvertTo2dim((ListSequenceStorage) value, info);
                }
                throw TypeException.INSTANCE.message("Couldn't unbox value: " + value.getClass());
            }
        }

        public static class IntArrayNDTuple extends IntArrayND {

            public IntArrayNDTuple(Object value, ArrayInfo info, int OriginHashCode, boolean ignore) {
                super(value, info, OriginHashCode, ignore);
            }

            @TruffleBoundary
            public static int[][] intConvertTo2dim(PTuple tuple) {
                if (MGOptions.ReuseStorage)
                    if (UNBOXED.containsKey(tuple.getArray().hashCode())) {
                        if ((MGOptions.Log.TraceUnboxing))
                            MGLog.printlnTagged("int storage reused");
                        return (int[][]) UNBOXED.get(tuple.getArray().hashCode());
                    }
                long s = System.currentTimeMillis();
                List<int[]> arrays = new ArrayList<>();
                arrays.addAll(intRecursiveProbingArrays(tuple));
                int size = arrays.size();
                int[][] ret = arrays.toArray(new int[size][0]);
                if ((MGOptions.Log.TraceUnboxing))
                    MGLog.printlnTagged("Tuple: int[]..[] to int[" + size + "][..] (" + (System.currentTimeMillis() - s) + " ms) ");
                if (MGOptions.ReuseStorage)
                    UNBOXED.put(tuple.getArray().hashCode(), ret);
                return ret;
            }

            @Override
            public Object unbox() throws TypeException {
                if (value instanceof PTuple) {
                    return intConvertTo2dim((PTuple) value);
                }
                throw TypeException.INSTANCE.message("Couldn't unbox value: " + value.getClass());

            }

        }

        public static class IntArrayNDTupleSequence extends IntArrayND {

            public IntArrayNDTupleSequence(Object value, ArrayInfo info, int OriginHashCode, boolean ignore) {
                super(value, info, OriginHashCode, ignore);
            }

            @TruffleBoundary
            public static int[][] intConvertTo2dim(TupleSequenceStorage listStorage) {
                if (MGOptions.ReuseStorage)
                    if (UNBOXED.containsKey(listStorage.getInternalPTupleArray().hashCode())) {
                        if ((MGOptions.Log.TraceUnboxing))
                            MGLog.printlnTagged("int storage reused");
                        return (int[][]) UNBOXED.get(listStorage.getInternalPTupleArray().hashCode());
                    }
                long s = System.currentTimeMillis();
                List<int[]> arrays = new ArrayList<>();
                int length = listStorage.length();
                for (int i = 0; i < length; i++) {
                    PTuple ith = listStorage.getInternalPTupleArray()[i];
                    arrays.addAll(intRecursiveProbingArrays(ith));
                }

                int size = arrays.size();
                int[][] ret = arrays.toArray(new int[size][0]);
                if ((MGOptions.Log.TraceUnboxing))
                    MGLog.printlnTagged("Tuple: int[]..[] to int[" + size + "][..] (" + (System.currentTimeMillis() - s) + " ms) ");
                if (MGOptions.ReuseStorage)
                    UNBOXED.put(listStorage.getInternalPTupleArray().hashCode(), ret);
                return ret;
            }

            @Override
            public Object unbox() throws TypeException {
                if (value instanceof TupleSequenceStorage) {
                    return intConvertTo2dim((TupleSequenceStorage) value);
                }
                throw TypeException.INSTANCE.message("Couldn't unbox value: " + value.getClass());

            }
        }

        @Override
        public int getTypeSize() {
            return 4;
        }

    }

    public abstract static class DoubleArrayND extends PythonUnboxer {

        public DoubleArrayND(Object value, ArrayInfo info, int OriginHashCode, boolean ignore) {
            super(value, info, DataType.DoubleArray, OriginHashCode, ignore);
        }

        @TruffleBoundary
        private static List<double[]> doubleRecursiveProbingArrays(PTuple tuple) {
            List<double[]> arrays = new ArrayList<>();
            int length = tuple.len();
            for (int i = 0; i < length; i++) {
                Object ith = tuple.getArray()[i];
                if (ith instanceof PTuple)
                    arrays.addAll(doubleRecursiveProbingArrays((PTuple) ith));
                else
                    arrays.add(((DoubleSequenceStorage) ((PList) ith).getStorage()).getInternalDoubleArray());
            }
            return arrays;
        }

        public static class DoubleArrayNDList extends DoubleArrayND {

            public DoubleArrayNDList(Object value, ArrayInfo info, int OriginHashCode, boolean ignore) {
                super(value, info, OriginHashCode, ignore);
            }

            @TruffleBoundary
            private static void doubleRecursiveProbingArrays(ListSequenceStorage listStorage, ArrayInfo info, int currentDim, double[][] retVal, int j) {
                int length = listStorage.length();
                if (length > 0) {
                    SequenceStorage ith = listStorage.getInternalListArray()[0].getStorage();
                    if (ith instanceof ListSequenceStorage) {
                        int dim = currentDim + 1;
                        int localSize = info.getSize(dim, info.getDim() - 1);
                        for (int i = 0; i < length; i++) {
                            ith = listStorage.getInternalListArray()[i].getStorage();
                            doubleRecursiveProbingArrays((ListSequenceStorage) ith, info, dim, retVal, i * localSize + j);
                        }
                    } else
                        for (int i = 0; i < length; i++) {
                            ith = listStorage.getInternalListArray()[i].getStorage();
                            retVal[i + j] = ((DoubleSequenceStorage) ith).getInternalDoubleArray();
                        }
                }
            }

            @TruffleBoundary
            public static double[][] doubleConvertTo2dim(ListSequenceStorage listStorage, ArrayInfo info) {
                if (MGOptions.ReuseStorage)
                    if (UNBOXED.containsKey(listStorage.getInternalListArray().hashCode())) {
                        if ((MGOptions.Log.TraceUnboxing))
                            MGLog.printlnTagged("Double storage reused");
                        return (double[][]) UNBOXED.get(listStorage.getInternalListArray().hashCode());
                    }

                long s = System.currentTimeMillis();
                int dims = info.getDim();
                double[][] retVal = null;
                int size = info.getSize(0, dims - 1);
                retVal = new double[size][0];
                doubleRecursiveProbingArrays(listStorage, info, 0, retVal, 0);
                if ((MGOptions.Log.TraceUnboxing))
                    MGLog.printlnTagged("List: double[]..[] to double[" + size + "][" + info.getSize(dims - 1) + "] (" + (System.currentTimeMillis() - s) + " ms) ");

                if (MGOptions.ReuseStorage)
                    UNBOXED.put(listStorage.getInternalListArray().hashCode(), retVal);
                return retVal;
            }

            @Override
            public Object unbox() throws TypeException {
                if (value instanceof ListSequenceStorage) {
                    return doubleConvertTo2dim((ListSequenceStorage) value, info);
                }
                throw TypeException.INSTANCE.message("Couldn't unbox value: " + value.getClass());

            }
        }

        public static class DoubleArrayNDTuple extends DoubleArrayND {

            public DoubleArrayNDTuple(Object value, ArrayInfo info, int OriginHashCode, boolean ignore) {
                super(value, info, OriginHashCode, ignore);
            }

            @TruffleBoundary
            public static double[][] doubleConvertTo2dim(PTuple tuple) {
                if (MGOptions.ReuseStorage)
                    if (UNBOXED.containsKey(tuple.getArray().hashCode())) {
                        if ((MGOptions.Log.TraceUnboxing))
                            MGLog.printlnTagged("Double storage reused");
                        return (double[][]) UNBOXED.get(tuple.getArray().hashCode());
                    }
                long s = System.currentTimeMillis();
                List<double[]> arrays = new ArrayList<>();
                arrays.addAll(doubleRecursiveProbingArrays(tuple));
                int size = arrays.size();
                double[][] ret = arrays.toArray(new double[size][0]);
                if ((MGOptions.Log.TraceUnboxing))
                    MGLog.printlnTagged("Tuple: double[]..[] to double[" + size + "][..] (" + (System.currentTimeMillis() - s) + " ms) ");
                if (MGOptions.ReuseStorage)
                    UNBOXED.put(tuple.getArray().hashCode(), ret);
                return ret;
            }

            @Override
            public Object unbox() throws TypeException {
                if (value instanceof PTuple) {
                    return doubleConvertTo2dim((PTuple) value);
                }
                throw TypeException.INSTANCE.message("Couldn't unbox value: " + value.getClass());

            }

        }

        public static class DoubleArrayNDTupleSequence extends DoubleArrayND {

            public DoubleArrayNDTupleSequence(Object value, ArrayInfo info, int OriginHashCode, boolean ignore) {
                super(value, info, OriginHashCode, ignore);
            }

            @TruffleBoundary
            public static double[][] doubleConvertTo2dim(TupleSequenceStorage listStorage) {
                if (MGOptions.ReuseStorage)
                    if (UNBOXED.containsKey(listStorage.getInternalPTupleArray().hashCode())) {
                        if ((MGOptions.Log.TraceUnboxing))
                            MGLog.printlnTagged("Double storage reused");
                        return (double[][]) UNBOXED.get(listStorage.getInternalPTupleArray().hashCode());
                    }
                long s = System.currentTimeMillis();
                List<double[]> arrays = new ArrayList<>();
                int length = listStorage.length();
                for (int i = 0; i < length; i++) {
                    PTuple ith = listStorage.getInternalPTupleArray()[i];
                    arrays.addAll(doubleRecursiveProbingArrays(ith));
                }

                int size = arrays.size();
                double[][] ret = arrays.toArray(new double[size][0]);
                if ((MGOptions.Log.TraceUnboxing))
                    MGLog.printlnTagged("Tuple: double[]..[] to double[" + size + "][..] (" + (System.currentTimeMillis() - s) + " ms) ");

                if (MGOptions.ReuseStorage)
                    UNBOXED.put(listStorage.getInternalPTupleArray().hashCode(), ret);
                return ret;
            }

            @Override
            public Object unbox() throws TypeException {
                if (value instanceof TupleSequenceStorage) {
                    return doubleConvertTo2dim((TupleSequenceStorage) value);
                }
                throw TypeException.INSTANCE.message("Couldn't unbox value: " + value.getClass());

            }

        }

        @Override
        public int getTypeSize() {
            return 8;
        }

    }

}
