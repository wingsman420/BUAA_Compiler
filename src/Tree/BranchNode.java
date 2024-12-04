package Tree;

import java.util.ArrayList;
import java.util.List;

public class BranchNode extends Node {
    private List<Node> children = new ArrayList<>();
    private BranchNode parent;

    public BranchNode(String type,BranchNode parent) {
        super(type);
        this.parent = parent;
    }

    public BranchNode getParent() {
        return parent;
    }

    public void addChild(Node child) {
        children.add(child);
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setParent(BranchNode parent) {
        this.parent = parent;
    }

    @Override
    public void print(int depth) {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            indent.append("  ");
        }
        System.out.println(indent + getType());
        for (Node child : children) {
            child.print(depth + 1);
        }
    }
}
