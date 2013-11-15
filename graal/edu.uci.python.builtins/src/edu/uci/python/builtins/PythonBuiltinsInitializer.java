/*
 * Copyright (c) 2013, Regents of the University of California
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
package edu.uci.python.builtins;

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.modules.*;
import edu.uci.python.runtime.standardtypes.*;

public class PythonBuiltinsInitializer {

    public static PythonContext initialize() {
        PythonBuiltinsContainer.getInstance().setDefaultBuiltins(new PythonDefaultBuiltins());
        PythonBuiltinsContainer.getInstance().setArrayModuleBuiltins(new ArrayModuleBuiltins());
        PythonBuiltinsContainer.getInstance().setBisectModuleBuiltins(new BisectModuleBuiltins());
        PythonBuiltinsContainer.getInstance().setTimeModuleBuiltins(new TimeModuleBuiltins());
        PythonBuiltinsContainer.getInstance().setListBuiltins(new ListBuiltins());
        PythonBuiltinsContainer.getInstance().setStringBuiltins(new StringBuiltins());
        PythonBuiltinsContainer.getInstance().setDictionaryBuiltins(new DictionaryBuiltins());

        PythonContext context = new PythonContext(new PythonOptions(), new PythonBuiltinsLookup());
        context.getPythonBuiltinsLookup().addModule("array", new ArrayModule(context, PythonBuiltinsContainer.getInstance().getArrayModuleBuiltins()));
        context.getPythonBuiltinsLookup().addModule("bisect", new BisectModule(context, PythonBuiltinsContainer.getInstance().getBisectModuleBuiltins()));
        context.getPythonBuiltinsLookup().addModule("time", new TimeModule(context, PythonBuiltinsContainer.getInstance().getTimeModuleBuiltins()));

        return context;
    }
}
