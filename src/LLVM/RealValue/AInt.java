package LLVM.RealValue;

public class AInt extends RealValue{

    private int initValue;

    public AInt(){
        super();
        initValue = 0;
    }

    public void setInitValue(int initValue){
        this.initValue = initValue;
    }

    public int getInitValue(){
        return initValue;
    }

    @Override
    public String toString(){
        return "";
    }

    @Override
    public int getLength() {
        return 0;
    }

    @Override
    public String toMips() {
        return initValue + "";
    }
}
