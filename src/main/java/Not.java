public class Not extends Node{
    public Not(boolean expression){
        super.l = null;
        super.r = new Node(expression);
        super.value = "!!";
    }
}