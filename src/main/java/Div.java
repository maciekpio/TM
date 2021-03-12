public class Div extends Node {
    public Div(double left, double right){
        super.l = new Node(left);
        super.r = new Node(right);
        super.value = '/';
    }
}
