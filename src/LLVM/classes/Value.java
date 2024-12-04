package LLVM.classes;

public abstract class Value {

    protected int id;

    public Value(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public abstract String toString();

}
