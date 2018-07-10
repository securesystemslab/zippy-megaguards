![zippy-logo-200-rounded.jpg](http://ssllab.org/zippy_logo.jpeg)
# ZipPy+MegaGuards #

### ZipPy ###

|                 | Graal CE JVM   |
| :------------------: |:-------------:|
| Linux Ubuntu 16.04  | [![Build Status](https://badges.herokuapp.com/travis/securesystemslab/zippy-megaguards?env=ZIPPY_JDK_TYPE=GRAALJVM_LINUX&label=Graal%20JVM)](https://travis-ci.org/securesystemslab/zippy-megaguards)

> These builds are without MegaGuards since Travis CI does not support GPU testing

ZipPy is a fast and lightweight [Python 3](https://www.python.org/) implementation built using the
[Truffle](http://openjdk.java.net/projects/graal/) framework. ZipPy leverages the underlying Java JIT compiler and compiles Python programs to highly optimized machine code at runtime.

ZipPy is currently maintained by [Secure Systems and Software Laboratory](http://ssllab.org) at the ​[University of California, Irvine](http://www.uci.edu/).

### MegaGuards ###


MegaGuards is a guards optimization and transparent heterogenous computing system that built on top of
[Truffle](https://github.com/oracle/graal) framework. MegaGuards analyze and execute AST on Truffle
and OpenCL. Currently, *MegaGuards works only on Linux x86 64-Bit and has been tested on Ubuntu x86 64-Bit 16.04.*


MegaGuards is currently maintained by [Secure Systems and Software Laboratory](http://ssllab.org) at the ​[University of California, Irvine](http://www.uci.edu/).

## Getting ZipPy+MegaGuards for Ubuntu 16.04 x86 64-Bit:

### Option 1: Using our interactive script (using `curl`, to install `sudo apt-get install curl`):

        $ mkdir megaguards && cd megaguards
        $ python -c "$(curl -fsSL https://raw.github.com/securesystemslab/zippy-megaguards/getting-zippy-megaguards.py)"

> Some prerequisites require `sudo` privilege to be installed.

### Option 2: Manually:

1. Create a working directory (`$MEGAGUARDS_ROOT`)

        $ mkdir megaguards && cd megaguards
        $ export MEGAGUARDS_ROOT=$PWD

2. Download [JDK with JVMCI 8 v0.46](http://www.oracle.com/technetwork/oracle-labs/program-languages/downloads/index.html) and decompress it.

        $ tar -xzf labsjdk-8u172-jvmci-0.46-linux-amd64.tar.gz

3. Install system dependencies:

        $ sudo apt-get install build-essential
        $ sudo apt-get install git wget curl
        $ sudo apt-get install ocl-icd-opencl-dev clinfo

4. Install GPU and/or CPU drivers (that includes OpenCL icd) from the appropriate vendor based on your system specs.

  - NVIDIA:

        https://www.geforce.com/drivers

        OR

            $ sudo add-apt-repository ppa:graphics-drivers
            $ sudo apt-get update
            $ sudo apt-get install nvidia-390

  - AMD:

        https://support.amd.com/en-us/download

  - Intel:

        https://software.intel.com/en-us/articles/opencl-drivers

7. Clone mxtool:

        $ cd $MEGAGUARDS_ROOT
        $ git clone https://github.com/graalvm/mx.git

8. Append the `mx` build tool directory to your `PATH`.

        $ export PATH=$MEGAGUARDS_ROOT/mx:$PATH

9. Clone ZipPy+MegaGuards:

        $ git clone https://github.com/securesystemslab/zippy-megaguards.git

10. Create a file `$MEGAGUARDS_ROOT/zippy-megaguards/mx.zippy/env` and add JDK path

        JAVA_HOME=/path/to/labsjdk1.8.0_172-jvmci-0.46 ## replace path with correct one. ##
        DEFAULT_VM=server
        DEFAULT_DYNAMIC_IMPORTS=truffle/compiler
        ZIPPY_MUST_USE_GRAAL=1


> For more information please visit the [ZipPy Wiki](https://github.com/securesystemslab/zippy/wiki).


### Build:

    $ cd $MEGAGUARDS_ROOT/zippy-megaguards
    $ mx build

### Check hardware and system compatibility:

    $ cd $MEGAGUARDS_ROOT/zippy-megaguards
    $ mx mg --init
    $ mx mg --clinfo


Please try to solve any `error` that our check script encounter.

### Test all the installed tools:

    $ mx mg --simple-example


### Run a Python program:

To enable MegaGuards use:

`--mg-target=truffle` to execute using a guardless Truffle AST on Graal.

`--mg-target=gpu` to execute on a GPU OpenCL device.

`--mg-target=cpu` to execute on a CPU OpenCL device.

`--mg-target` to execute using our adaptive OpenCL device selection.

    $ cd $MEGAGUARDS_ROOT/zippy-megaguards
    $ mx python <file.py> --mg-target=gpu

### Test:

    $ cd $MEGAGUARDS_ROOT/zippy-megaguards
    $ mx junit
    $ mx junit-mg


### Notes:

- ZipPy is still under development and not all language features are available.
- ZipPy require few runs to reach optimal performance results.
