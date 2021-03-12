public class Multi extends Node {
    public Multi(double left, double right){
        super.l = new Node(left);
        super.r = new Node(right);
        super.value = '*';
    }
}
