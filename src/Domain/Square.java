package Domain;

public class Square{
    private final int xcoor;
    private final int ycoor;
    private boolean isOccupied;
    private iOccupier occupier;
    private Square[] nearbySquares; //index 0: right neighbor, index 1: top, index 2: left, index 3: bottom
    private Hall hall;

    public Square(int xcoor, int ycoor, Square[] nearbySquares, Hall hall){
        this.xcoor = xcoor;
        this.ycoor = ycoor;
        this.nearbySquares = nearbySquares;
        this.hall = hall;
    }

    public void placeOccupier(iOccupier occupier){
        if (!isOccupied){
            this.occupier = occupier;
        } else{
            System.out.println("Object can't be placed here");
        }
    }

    public int getXcoor(){
        return xcoor;
    }
    public int getYcoor(){
        return ycoor;
    }
    public Square[] getNearbySquares(){
        return nearbySquares;
    }

    public iOccupier getOccupier(){
        return occupier;
    }

	public boolean isOccupied() {
		return isOccupied;
	}

	public Hall getHall() {
		return hall;
	}
}