public class BinaryOr extends Node {
    public BinaryOr(boolean left, boolean right){
        super.l = new Node(left);
        super.r = new Node(right);
        super.value = "||";
    }
}
