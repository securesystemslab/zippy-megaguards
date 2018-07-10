/*
 * Copyright (c) 2018, Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use in script and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of script code must retain the above copyright notice, this
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
package edu.uci.megaguards.test.truffle;

import static edu.uci.python.test.PythonTests.assertPrints;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import edu.uci.megaguards.test.MGTests;

@FixMethodOrder(MethodSorters.JVM)
public class TruffleForTests {

    private static final String path = MGTests.MGSrcTestPath + File.separator + "for" + File.separator;

    @Test
    public void simple() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test1.py");
        assertPrints("[2, 4, 6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void simple_2() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test2.py");
        assertPrints("[0, 4, 8, 12, 16, 20, 24, 28, 32, 36, 40, 44, 48, 52, 56, 60, 64, 68, 72, 76, " + //
                        "80, 84, 88, 92, 96, 100, 104, 108, 112, 116, 120, 124, 128, 132, 136, 140, 144, 148, 152," + //
                        " 156, 160, 164, 168, 172, 176, 180, 184, 188, 192, 196]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void simple_two_forloops() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        // Note: Data dependence issue c[i]
        Path script = Paths.get(path + "test3.py");
        assertPrints("[8, 10, 12]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void simple_2_two_forloops() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test4.py");
        assertPrints("[8, 10, 12]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void a4d_simple() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test5.py");
        assertPrints("[[[[2, 4, 6], [8, 10, 12]], [[14, 16, 18], [20, 22, 24]]], [[[2, 4, 6], [8, 10, 12]], [[14, 16, 18], [20, 22, 24]]]]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();
    }

    @Test
    public void simple_function_two_forloops() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test6.py");
        assertPrints("[8, 10, 12]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void simple_no_arrays() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test7.py");
        assertPrints("", script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void simple_not() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test8.py");
        assertPrints("[2, 0, 6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void simple_break() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test9.py");
        assertPrints("[1, 4, 1]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void not_simple_range() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test10.py");
        assertPrints("[[2, 4, 6], [2, 4, 6], [2, 4, 6]]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void simple_minus() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test11.py");
        assertPrints("[-2, -4, -6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void simple_if() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test12.py");
        assertPrints("[3, 4, 0]\n[3, 4, 9]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 2, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void simple_int() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test13.py");
        assertPrints("[2, 4, 6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void simple_comp() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test14.py");
        assertPrints("[2, 4, 6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 3, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void simple_comp2() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test15.py");
        assertPrints("[2, 4, 6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 3, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void simple_minMax() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test16.py");
        assertPrints("[1, 2, 3]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void simple_round() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test17.py");
        assertPrints("[2.0, 4.0, 6.0]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void simple_two_len_range() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        // Note: Data dependence issue c[i]
        Path script = Paths.get(path + "test18.py");
        assertPrints("[8, 10, 12]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void simple_two_var_range() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        // Note: Data dependence issue c[i]
        Path script = Paths.get(path + "test19.py");
        assertPrints("[8, 10, 12]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void simple_two_iter_range() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        // Note: Data dependence issue c[i]
        Path script = Paths.get(path + "test20.py");
        assertPrints("[6, 9, 12]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void simple_dd_threshold() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test21.py");
        assertPrints("[2, 4, 0]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 3, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void simple_recycle_tree() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test22.py");
        assertPrints("[8, 10, 12]\n[26, 46, 66]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 2, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void simple_recycle_tree2() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test23.py");
        assertPrints("[8, 10, 12]\n[26, 46, 66]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 2, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void simple_recycle_tree3() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test24.py");
        assertPrints("[8, 10, 12]\n[26.4, 46.4, 66.4]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 2, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void simple_handoff_to_offload() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test25.py");
        Path output = Paths.get(path + "test25.output");
        assertPrints(output, script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void simple_handoff_to_offload2() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test26.py");
        Path output = Paths.get(path + "test26.output");
        assertPrints(output, script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void simple_range_1D() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test27.py");
        assertPrints("[2, 4, 6, 2, 4, 6, 2, 4, 6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void simple_multiranges() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test28.py");
        Path output = Paths.get(path + "test28.output");
        assertPrints(output, script);
        MGTests.verifyMGExecution(script, lastLogSize, 3, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void simple_stepOf2_handoff_to_offload() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test29.py");
        Path output = Paths.get(path + "test29.output");
        assertPrints(output, script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void simple_recycle_tree4() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test31.py");
        assertPrints("[8.0, 10.0, 12.0]\n[26.0, 46.0, 66.0]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 2, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void test32() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test32.py");
        Path output = Paths.get(path + "test32.output");
        assertPrints(output, script);
        MGTests.verifyMGExecution(script, lastLogSize, 2, 0);
        MGTests.releaseMGTruffleOptions();
    }

    @Test
    public void test33() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test33.py");
        Path output = Paths.get(path + "test33.output");
        assertPrints(output, script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();
    }

    @Test
    public void test34() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        // integer overflow
        Path script = Paths.get(path + "test34.py");
        Path output = Paths.get(path + "test34.output");
        assertPrints(output, script);
        MGTests.verifyMGExecution(script, lastLogSize, 6, 0);
        MGTests.releaseMGTruffleOptions();
    }

    @Test
    public void test35() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test35.py");
        Path output = Paths.get(path + "test35.output");
        assertPrints(output, script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();
    }

    @Ignore
    @Test
    public void test36() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test36.py");
        Path output = Paths.get(path + "test36.output");
        assertPrints(output, script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();
    }

    @Test
    public void test37() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        // overflow exception
        Path script = Paths.get(path + "test37.py");
        Path output = Paths.get(path + "test37.output");
        assertPrints(output, script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();
    }

    @Test
    public void test38() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test38.py");
        Path output = Paths.get(path + "test38.output");
        assertPrints(output, script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();
    }

    @Test
    public void test39() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test39.py");
        Path output = Paths.get(path + "test39.output");
        assertPrints(output, script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();
    }

    @Test
    public void test40() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test40.py");
        Path output = Paths.get(path + "test40.output");
        assertPrints(output, script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();
    }

    @Test
    public void test41() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test41.py");
        Path output = Paths.get(path + "test41.output");
        assertPrints(output, script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();
    }

    @Test
    public void test42() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test42.py");
        Path output = Paths.get(path + "test42.output");
        assertPrints(output, script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();
    }

    @Test
    public void test43() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test43.py");
        Path output = Paths.get(path + "test43.output");
        assertPrints(output, script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();
    }

    @Test
    public void test44() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test44.py");
        Path output = Paths.get(path + "test44.output");
        assertPrints(output, script);
        MGTests.verifyMGExecution(script, lastLogSize, 41, 0);
        MGTests.releaseMGTruffleOptions();
    }

    @Test
    public void test45() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test45.py");
        Path output = Paths.get(path + "test45.output");
        assertPrints(output, script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();
    }

    @Test
    public void test46() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test46.py");
        Path output = Paths.get(path + "test46.output");
        assertPrints(output, script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();
    }

}
