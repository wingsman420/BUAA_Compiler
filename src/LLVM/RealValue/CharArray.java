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
            for(int i = 0; i < this.length; i++){
                if (i < this.value.length()){
                    switch (this.value.charAt(i)){
                        case 0: sb.append("\\00"); break;
                        case 7: sb.append("\\07"); break;
                        case 8: sb.append("\\08"); break;
                        case 9: sb.append("\\09"); break;
                        case 10: sb.append("\\0A"); break;
                        case 11: sb.append("\\0B"); break;
                        case 12: sb.append("\\0C"); break;
                        case 34: sb.append("\\22"); break;
                        case 39: sb.append("\\27"); break;
                        case 92: sb.append("\\5C"); break;
                        default: sb.append(this.value.charAt(i));;
                    }
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
