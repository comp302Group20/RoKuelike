package Domain;

public class Main{
    public static void main(String[] args){
        System.out.println("Test");
        Hall hall = new Hall("Air", 6, 6);
        Square sq = hall.getSquares()[0][0];
    		   System.out.println("x coor:" + sq.getXcoor() + "y coor:" + sq.getYcoor());
    		   System.out.println("Right neighbor: x coor:" + sq.getNearbySquares()[0].getXcoor() + "y coor:" + sq.getNearbySquares()[0].getYcoor());
    		   System.out.println("Up neighbor: x coor:" + sq.getNearbySquares()[1].getXcoor() + "y coor:" + sq.getNearbySquares()[1].getYcoor());
    		   System.out.println("Left neighbor: x coor:" + sq.getNearbySquares()[2].getXcoor() + "y coor:" + sq.getNearbySquares()[2].getYcoor());
    		   System.out.println("Down neighbor: x coor:" + sq.getNearbySquares()[3].getXcoor() + "y coor:" + sq.getNearbySquares()[3].getYcoor());
        hall.placeOccupierToHall(3, 3, new HallObj());
        System.out.println(hall.getSquaresWithObjects().getFirst().getOccupier());
        try {
			Monster.spawnMonsters(hall);
		} catch (Exception e) {
			e.printStackTrace();
		}
        for (var el : hall.getMonsters()) {
        	System.out.println(el.getClass().getName() + " x:" + el.coords[0] + " y:" + el.coords[1]);
        }
    }
}