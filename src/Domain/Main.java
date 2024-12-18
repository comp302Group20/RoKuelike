package Domain;

public class Main{
    public static void main(String[] args){
        System.out.println("Test");
        Hall hall = new Hall("Air", 6, 6);
        System.out.println(hall.getElement());
        System.out.println(hall.getXsize());
        System.out.println(hall.getSquares().length);
        System.out.println(hall.getSquares()[0].length);
        hall.placeOccupierToHall(3, 3, new HallObj());
        System.out.println(hall.getSquaresWithObjects().size());
    }
}