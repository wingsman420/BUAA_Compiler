package Tree;

import java.util.ArrayList;
import java.util.List;

public abstract class Node {
    private String type;

    public Node(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public abstract void print(int depth); // 用于打印树的抽象方法
}
