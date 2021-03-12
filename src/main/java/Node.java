import java.util.Objects;

public class Node {
    Node l;
    Node r;
    Object value;

    public Node(Object value){
        this.value = value;
        this.l = null;
        this.r = null;
    }

    public Node(Object value, Object left, Object right){
        this.value = value;
        this.l = new Node(left);
        this.r = new Node(right);
    }

    public Node(){}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(this.l, node.l) &&
                Objects.equals(this.r, node.r) &&
                Objects.equals(this.value, node.value);
    }
}
