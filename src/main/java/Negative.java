public class Negative extends Node {
    public Negative(double number){
        super.l = null;
        super.r = new Node(number);
        super.value = "-";
    }
}
