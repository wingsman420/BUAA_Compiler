package LLVM.RealValue;

public class CharArray extends RealValue{
    private int length;
    private String value;

    public CharArray(int length){
        super();
        this.length = length;
        value = null;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String getValue(){
        return value;
    }

    public int getLength(){
        return this.length;
    }

    @Override
    public String toString(){
        if (value == null)
        {
            return "zeroinitializer";
        }
        else
        {
            StringBuilder sb = new StringBuilder();
            sb.append('c');
            sb.append('\"');
            for(int i = 1; i < this.length + 1; i++){
                if (i < this.value.length() - 1){
                    sb.append(this.value.charAt(i));
                }
                else
                {
                    sb.append("\\00");
                }
            }
            sb.append('\"');
            return sb.toString();
        }
    }

}
