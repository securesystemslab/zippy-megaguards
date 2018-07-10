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
package edu.uci.megaguards.test.parallel;

import static edu.uci.python.test.PythonTests.assertPrints;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assume.assumeTrue;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import edu.uci.megaguards.MGOptions;
import edu.uci.megaguards.test.MGTests;

@FixMethodOrder(MethodSorters.JVM)
public class FunCallTests {

    private static final String path = MGTests.MGSrcTestPath + File.separator + "funcall" + File.separator;

    @Test
    public void funcall_test1() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test1.py");
        assertPrints("[2, 4, 6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 1);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void funcall_test2() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        // Note: Data dependence issue c[i]
        Path script = Paths.get(path + "test2.py");
        assertPrints("[8, 10, 12]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 1);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void funcall_test3() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test3.py");
        assertPrints("[-2, -4, -6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 1);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void funcall_test4() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test4.py");
        assertPrints("[-2, -4, -6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 1);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void funcall_test5() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test5.py");
        assertPrints("[2, 4, 6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 1);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void funcall_test6() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test6.py");
        assertPrints("[2, 4, 6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 1);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void funcall_test7() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test7.py");
        assertPrints("[3, 6, 9]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 1);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void funcall_test8() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test8.py");
        assertPrints("[3, 6, 9]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 1);
        MGTests.releaseMGParallelOptions();

    }

    @Ignore
    @Test
    public void funcall_test9() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test9.py");
        assertPrints("[None, None, None]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 0);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void funcall_test10() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test10.py");
        assertPrints("[None, None, 9]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 0);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void funcall_test11() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test11.py");
        assertPrints("[None, None, 9]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 0);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void funcall_test12() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test12.py");
        assertPrints("[2, 6, 9]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 1);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void funcall_test13() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test13.py");
        assertPrints("[128, 128, 128]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 1);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void funcall_test14() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test14.py");
        assertPrints("[10, 12, 22]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 2);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void funcall_test15() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        MGOptions.Backend.allowInAccurateMathFunctions = false;
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test15.py");
        assertPrints("[11.46189, 7.31809, 5.78588, 3.82733, 2.52068, 0.07568, 6.73360, 1.33559, 2.93451, 6.06472, -5.25474, -3.11574, 6.14582, 4.14164, -4.37739, -5.00485, -2.32297, ]\n", script);
        MGOptions.Backend.allowInAccurateMathFunctions = true;
        MGTests.verifyMGExecution(script, lastLogSize, 0, 0);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void funcall_test16() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test16.py");
        assertPrints("[2, 4, 6]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 1);
        MGTests.releaseMGParallelOptions();

    }

}
