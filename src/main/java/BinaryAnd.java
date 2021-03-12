public class BinaryAnd extends Node {
    public BinaryAnd(boolean left, boolean right){
        super.l = new Node(left);
        super.r = new Node(right);
        super.value = "&&";
    }
}
