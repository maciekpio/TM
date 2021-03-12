public class Add extends Node {
    public Add(double left, double right){
        super.l = new Node(left);
        super.r = new Node(right);
        super.value = '+';
    }
}
