package LLVM.classes.module;

import LLVM.classes.Value;

import java.io.Serializable;

public abstract class GlobalValue extends Value {

    public boolean isFunction;

    public String name;

    public GlobalValue(int id, boolean isFunction, String name) {
        super(id);
        this.isFunction = isFunction;
        this.name = name;
    }

    public boolean isFunction() {
        return isFunction;
    }

    public String getName() {
        return name;
    }

    @Override
    public abstract String toString();
}
