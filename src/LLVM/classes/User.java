package LLVM.classes;

public abstract class User extends Value{

    protected double value;

    public User(int id){
        super(id);
    }

    @Override
    public abstract String toString();

    public double getValue(){
        return value;
    }

    public void setValue(double value){
        this.value = value;
    }
}
