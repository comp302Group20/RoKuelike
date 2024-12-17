package Domain;

import Exceptions.ObjectCantBePlacedException;

public class Square{
    private final int xcoor;
    private final int ycoor;
    private boolean isOccupied;
    private iOccupier occupier;
    private Square[] nearbySquares; //index 0: right neighbor, index 1: top, index 2: left, index 3: bottom

    public Square(int xcoor, int ycoor, Square[] nearbySquares){
        this.xcoor = xcoor;
        this.ycoor = ycoor;
        this.nearbySquares = nearbySquares;
    }

    public void placeOccupier(iOccupier occupier){
        if (!isOccupied){
            this.occupier = occupier;
        } else{
            throw new ObjectCantBePlacedException();
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
}