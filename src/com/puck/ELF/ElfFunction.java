package com.puck.ELF;
import java.util.List;

import com.puck.ELF.Expr.Statement;

public class ElfFunction implements CallableFunction{

    private final Statement.Function declaration;

    ElfFunction(Statement.Function declaration)
    {
        this.declaration = declaration;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        PuckEnv env = new PuckEnv(interpreter.globals);
        for(int i = 0; i<declaration.params.size();i++){
            env.define(declaration.params.get(i).lexem,arguments.get(i));
        }

        interpreter.executeBLock(declaration.body,environment);
        return null;
    }

    @Override
    public Object call(FileInterpreter interpreter, List<Object> args) {
        PuckEnv env = new PuckEnv(interpreter.globals);
        for(int i = 0; i<declaration.params.size();i++){
            env.define(declaration.params.get(i).lexem,arguments.get(i));
        }

        interpreter.executeBLock(declaration.body,environment);
        return null;
    }

    @Override
    public int ParamLength() {
        return declaration.params.size();
    }
    
    public String toString(){
        return "<fn " + declaration.name.lexem + ">";
    }
    
}
