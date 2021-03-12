public class BinaryEqual extends Node {
    public BinaryEqual(double left, double right){
        super.l = new Node(left);
        super.r = new Node(right);
        super.value = "==";
    }

    public BinaryEqual(boolean left, boolean right){
        super.l = new Node(left);
        super.r = new Node(right);
        super.value = "==";
    }
}
