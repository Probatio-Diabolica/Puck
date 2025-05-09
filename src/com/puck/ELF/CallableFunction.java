package com.puck.ELF;

import java.util.List;

public interface CallableFunction {
    Object call(Interpreter interpreter, List<Object> args);
    Object call(FileInterpreter interpreter, List<Object> args);
    int ParamLength();
}
