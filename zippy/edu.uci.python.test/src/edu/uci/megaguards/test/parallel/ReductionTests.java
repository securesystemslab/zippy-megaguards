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
import org.junit.Test;
import org.junit.runners.MethodSorters;

import edu.uci.megaguards.test.MGTests;

@FixMethodOrder(MethodSorters.JVM)
public class ReductionTests {

    private static final String path = MGTests.MGSrcTestPath + File.separator + "reduction" + File.separator;

    @Test
    public void map_test1() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test1.py");
        assertPrints("[5, 7, 9, 11, 13, 15, 17, 19, 21, 23]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 1);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void map_test2() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test2.py");
        assertPrints("[5, 7, 9, 11, 13, 15, 17, 19, 21, 23]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 1);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void reduce_test3() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test3.py");
        assertPrints("5050\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 1);
        MGTests.releaseMGParallelOptions();
    }

    @Test
    public void reduce_test4() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test4.py");
        assertPrints("2001000\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 1);
        MGTests.releaseMGParallelOptions();
    }

    @Test
    public void reduce_test5() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test5.py");
        assertPrints("8390656\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 1);
        MGTests.releaseMGParallelOptions();
    }

    @Test
    public void reduce_test6() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test6.py");
        assertPrints("40320\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 1);
        MGTests.releaseMGParallelOptions();
    }

    @Test
    public void reduce_test7() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test7.py");
        assertPrints("2006997\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 0);
        MGTests.releaseMGParallelOptions();
    }

    @Test
    public void reduce_test8() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test8.py");
        assertPrints("2006997\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 1);
        MGTests.releaseMGParallelOptions();
    }

    @Test
    public void reduce_test9() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test9.py");
        assertPrints("5347\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 1);
        MGTests.releaseMGParallelOptions();
    }

    @Test
    public void reduce_test10() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test10.py");
        assertPrints("5355\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 1);
        MGTests.releaseMGParallelOptions();
    }

    @Test
    public void reduce_test11() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test11.py");
        assertPrints("5508310771200\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 1);
        MGTests.releaseMGParallelOptions();
    }

    @Test
    public void map_test12() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test12.py");
        assertPrints("[5, 7, 13, 23, 37, 55, 77, 103, 133, 167]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 1);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void map_test13() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test13.py");
        assertPrints("[5.0, 7.0, 13.0, 23.0, 37.0, 55.0, 77.0, 103.0, 133.0, 167.0]\n", script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 1);
        MGTests.releaseMGParallelOptions();

    }

    @Test
    public void map_test14() {
        assumeTrue(MGTests.isOpenCLDeviceAvailable());
        MGTests.setMGParallelOptions();
        int lastLogSize = MGTests.lastLogSize();
        Path script = Paths.get(path + "test14.py");
        assertPrints("[5, 6, 7, 8, 9, 10, 11, 12, 13, 14]\n" +
                        "[5, 6, 7, 8, 9, 10, 11, 12, 13, 14]\n" +
                        "[5, 6, 9, 14, 21, 30, 41, 54, 69, 86]\n" +
                        "[5.0, 11.0, 29.0, 59.0, 101.0, 155.0, 221.0, 299.0, 389.0, 491.0, 605.0, 731.0, 869.0, 1019.0, 1181.0, 1355.0, 1541.0, 1739.0, 1949.0, 2171.0]\n",
                        script);
        MGTests.verifyMGExecution(script, lastLogSize, 0, 4);
        MGTests.releaseMGParallelOptions();

    }

}
