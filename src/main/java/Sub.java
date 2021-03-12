public class Sub extends Node {
    public Sub(double left, double right){
        super.l = new Node(left);
        super.r = new Node(right);
        super.value = '-';
    }
}
