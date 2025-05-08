package LLVM.RealValue;

import java.util.ArrayList;

public class IntArray extends RealValue{

    private int length;
    private ArrayList<Integer> list;

    public IntArray(int length){
        super();
        this.length = length;
        list = new ArrayList<>();
    }

    public void addToList(int a)
    {
        list.add(a);
    }

    public void addToList(ArrayList<Integer> a)
    {
        list.addAll(a);
    }

    public ArrayList<Integer> getList()
    {
        return list;
    }

    public int getLength(){
        return this.length;
    }

    @Override
    public String toMips() {
        if (list.isEmpty())
        {
            return "0:" + length;
        }
        else
        {
            StringBuilder str = new StringBuilder();
            for (int i = 0; i < length; i++)
            {
                if (i != 0)
                {
                    str.append(", ");
                }
                if (i < list.size())
                {
                    str.append(list.get(i));
                }
                else
                {
                    str.append("0");
                }
            }
            return str.toString();
        }
    }

    @Override
    public String toString(){
        if (list.isEmpty())
        {
            return "zeroinitializer";
        }
        else
        {
            StringBuilder str = new StringBuilder();
            str.append("[");
            for (int i = 0; i < length; i++)
            {
                if (i != 0)
                {
                    str.append(", ");
                }
                str.append("i32 ");
                if (i < list.size())
                {
                    str.append(list.get(i).toString());
                }
                else
                {
                    str.append("0");
                }
            }
            str.append("]");
            return str.toString();
        }
    }
}
