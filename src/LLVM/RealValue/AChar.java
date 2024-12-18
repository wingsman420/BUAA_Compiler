package LLVM.RealValue;

public class AChar extends RealValue{

    private int initValue;

    public AChar(){
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
}
