package com.puck.ELF;

import java.util.HashMap;
import java.util.Map;

public class PuckEnv {
    final PuckEnv enclosing;
    
    private final Map<String, Object> values = new HashMap<>();

    PuckEnv() {
        enclosing = null;
    }

    PuckEnv(PuckEnv enclosing) {
        this.enclosing = enclosing;
    }

    Object get(Token name) {
        if (values.containsKey(name.lexem)) {
            return values.get(name.lexem);
        }
        if (enclosing != null) return enclosing.get(name);
        throw new RuntimeError(name,
                "Undefined variable '" + name.lexem + "'.");
    }

    void define(String name, Object value) {
        values.put(name, value);
    }

    void assign(Token name, Object value) {
        if (values.containsKey(name.lexem)) {
            values.put(name.lexem, value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name,
                "Undefined variable '" + name.lexem + "'.");
    }
}
