package LLVM.classes.module;

import LLVM.classes.Value;

public class Argument extends Value {
    private String name;
    private String type;
    private boolean isArray;

    public Argument(int id, String name, String type, boolean isArray) {
        super(id);
        this.name = name;
        this.type = type;
        this.isArray = isArray;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public boolean isArray() {
        return isArray;
    }


    @Override
    public String toString() {
        if (type.equals("INTTK")) {
            if (isArray) {
                return "i32* %" + id;
            }
            else
            {
                return "i32 %" + id;
            }
        }
        else
        {
            if (isArray) {
                return "i8* %" + id;
            }
            else
            {
                return "i8 %" + id;
            }
        }
    }
}
