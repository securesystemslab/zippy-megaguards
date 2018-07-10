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
package edu.uci.megaguards.test.parallel;

import static edu.uci.python.test.PythonTests.assertPrints;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assume.assumeTrue;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import edu.uci.megaguards.MGOptions;
import edu.uci.megaguards.test.MGTests;

@FixMethodOrder(MethodSorters.JVM)
public class DDTests {

    private static final String path = MGTests.MGSrcTestPath + File.separator + "dd" + File.separator;

    @Test
    public void independent() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test1.py");
        assertPrints("[2, 4, 6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 1);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void dd_two_variables() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test2.py");
        assertPrints("[[2, 2, 3], [1, 4, 3], [1, 2, 6]]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 1);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void const_variable() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test3.py");
        assertPrints("[2, 4, 6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 1);

        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void const_variable_dependent() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test4.py");
        assertPrints("[2, 4, 4]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGParallelOptions();
    }

    @Test
    public void dd_two_loops() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test5.py");
        assertPrints("[[2, 4, 6], [2, 4, 6], [2, 4, 6]]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 1);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void const_variable_ignore_dependent() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test6.py");
        assertPrints("", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 1);
        MGTests.releaseMGParallelOptions();
    }

    @Test
    public void atomic() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test7.py");
        assertPrints("6.0\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 3);
        MGTests.releaseMGParallelOptions();
    }

    @Test
    public void independent2() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test8.py");
        assertPrints("[[2, 4, 6], [2, 4, 6], [2, 4, 6]]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 1);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void atomic_2() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test9.py");
        assertPrints("[2, 4, 6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 3, 0);
        MGTests.releaseMGParallelOptions();
    }

    @Test
    public void atomic_subscript_neg_index() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        assumeTrue(MGOptions.testTrowable);
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test10.py");
        assertPrints("[6, 12, 18]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 2, 0);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void constant() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test11.py");
        assertPrints("[2, 4, 6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 1);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void privatization_1() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test12.py");
        assertPrints("[2, 4, 6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 0);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void privatization_2() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test13.py");
        assertPrints("[2, 4, 6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 0);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void privatization_3() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test14.py");
        assertPrints("7\n[2, 4, 6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 0);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void privatization_4() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test15.py");
        assertPrints("[2, 4, 6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 0);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void privatization_5() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test16.py");
        assertPrints("0\n[2, 4, 6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 0);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void privatization_6() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test17.py");
        assertPrints("[2, 4, 6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 1);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void dd_test18() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test18.py");
        assertPrints("[10, 14, 22]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 2, 0);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void dd_test19() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test19.py");
        assertPrints("[30.0, 120.0, 480.0]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 2, 0);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void dd_test20() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test20.py");
        assertPrints("[10, 10, 10]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 2, 0);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void dd_test21() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test21.py");
        assertPrints("[10, 10, 10]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 2, 0);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void dd_test22() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test22.py");
        assertPrints("[189]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 1, 0);
        MGTests.releaseMGParallelOptions();

    }

}
