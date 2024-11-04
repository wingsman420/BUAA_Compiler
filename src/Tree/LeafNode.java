package Tree;

import base.Token;

public class LeafNode extends Node {
    private Token value;

    public LeafNode(Token value) {
        super(value.getType());
        this.value = value;
    }

    public Token getToken()
    {
        return value;
    }

    public String getValue() {
        return value.toString();
    }

    @Override
    public void print(int depth) {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            indent.append("  ");
        }
        System.out.println(indent + getType() + " " + getValue());
    }
}