package Domain;

public class Square implements iLocation{
    private final int xcoor;
    private final int ycoor;
    private boolean isOccupied;
    private iOccupier occupier;
    private iLocation[] nearbySquares; //index 0: right neighbor, index 1: top, index 2: left, index 3: bottom
    private Hall hall;

    public Square(int xcoor, int ycoor, Hall hall){
        this.xcoor = xcoor;
        this.ycoor = ycoor;
        this.nearbySquares = new iLocation[4];
        this.hall = hall;
    }

    public void placeOccupier(iOccupier occupier){
        if (!isOccupied){
            this.occupier = occupier;
            isOccupied = true;
        } else{
            System.out.println("Object can't be placed here");
        }
    }

    public void setNeighbor(iLocation sq, int i) {
    	if (i > 3) {System.out.println("No such neighbor"); return;}
    	nearbySquares[i] = sq;
    }
    
    public int getXcoor(){
        return xcoor;
    }
    public int getYcoor(){
        return ycoor;
    }
    public iLocation[] getNearbySquares(){
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