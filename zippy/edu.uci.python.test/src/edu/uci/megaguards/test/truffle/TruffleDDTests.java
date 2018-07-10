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
package edu.uci.megaguards.test.truffle;

import static edu.uci.python.test.PythonTests.assertPrints;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import edu.uci.megaguards.test.MGTests;

@FixMethodOrder(MethodSorters.JVM)
public class TruffleDDTests {

    private static final String path = MGTests.MGSrcTestPath + File.separator + "dd" + File.separator;

    @Test
    public void independent() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test1.py");
        assertPrints("[2, 4, 6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void dd_two_variables() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test2.py");
        assertPrints("[[2, 2, 3], [1, 4, 3], [1, 2, 6]]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void const_variable() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test3.py");
        assertPrints("[2, 4, 6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);

        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void const_variable_dependent() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test4.py");
        assertPrints("[2, 4, 4]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();
    }

    @Test
    public void dd_two_loops() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test5.py");
        assertPrints("[[2, 4, 6], [2, 4, 6], [2, 4, 6]]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void const_variable_ignore_dependent() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test6.py");
        assertPrints("", script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();
    }

    @Test
    public void atomic() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test7.py");
        assertPrints("6.0\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 3, 0);
        MGTests.releaseMGTruffleOptions();
    }

    @Test
    public void independent2() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test8.py");
        assertPrints("[[2, 4, 6], [2, 4, 6], [2, 4, 6]]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void atomic_2() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test9.py");
        assertPrints("[2, 4, 6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 3, 0);
        MGTests.releaseMGTruffleOptions();
    }

    @Test
    public void atomic_subscript_neg_index() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test10.py");
        assertPrints("[6, 12, 18]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 2, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void constant() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test11.py");
        assertPrints("[2, 4, 6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void privatization_1() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test12.py");
        assertPrints("[2, 4, 6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void privatization_2() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test13.py");
        assertPrints("[2, 4, 6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void privatization_3() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test14.py");
        assertPrints("7\n[2, 4, 6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void privatization_4() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test15.py");
        assertPrints("[2, 4, 6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void privatization_5() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test16.py");
        assertPrints("0\n[2, 4, 6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void privatization_6() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test17.py");
        assertPrints("[2, 4, 6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void dd_test18() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test18.py");
        assertPrints("[10, 14, 22]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 2, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void dd_test19() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test19.py");
        assertPrints("[30.0, 120.0, 480.0]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 2, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void dd_test20() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test20.py");
        assertPrints("[10, 10, 10]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 2, 0);
        MGTests.releaseMGTruffleOptions();

    }

    @Test
    public void dd_test21() {
        MGTests.setMGTruffleOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test21.py");
        assertPrints("[10, 10, 10]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 2, 0);
        MGTests.releaseMGTruffleOptions();

    }

}
