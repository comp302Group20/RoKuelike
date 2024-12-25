package Domain;

import java.util.ArrayList;
import java.util.Random;

public class Hall{

    enum Element{
        EARTH(6),
        AIR(9),
        WATER(13),
        FIRE(17);

        public final int minObjNo;

        private Element(int num){
            minObjNo = num;
        }
    }

    private Element element;
    private int xsize;
    private int ysize;
    private int hallTime; //int may change depending on how we implement time
    private int objectCount;
    private Square[][] squares;
    private ArrayList<Square> squaresWithObjects;
    private int[] runeLocation;
    private ArrayList<Monster> monsters;

    public Hall(String elementName, int xsize, int ysize) {
        element = Element.valueOf(elementName.toUpperCase());
        if (xsize*ysize>element.minObjNo && xsize>0){
            this.xsize = xsize;
            this.ysize = ysize;
            squaresWithObjects = new ArrayList<Square>();
            monsters = new ArrayList<Monster>();
            squares = new Square[xsize][ysize];
            for (int i = 0; i < xsize; i++){
                for (int j = 0; j < ysize; j++) {
                	squares[i][j] = new Square(i, j, this);
                }
            }
            Square current;
            for (int i = 0; i < xsize; i++){
                for (int j = 0; j < ysize; j++) {
                	current = squares[i][j];
                    if (i > 0) {
                    	Square leftNeighbor = squares[i-1][j];
                        current.setNeighbor(leftNeighbor, 0);
                        leftNeighbor.setNeighbor(current, 2);
                    } else {
                        current.setNeighbor(new Wall(), 0);
                    }
                    if (j > 0) {
                        Square upNeighbor = squares[i][j-1];
                        current.setNeighbor(upNeighbor, 1);
                        upNeighbor.setNeighbor(current, 3);
                    } else {
                        current.setNeighbor(new Wall(), 1);
                    }
                    if (i < xsize-1) { 
                        Square rightNeighbor = squares[i+1][j];
                        current.setNeighbor(rightNeighbor, 2);
                        rightNeighbor.setNeighbor(current, 0);
                    } else {
                        current.setNeighbor(new Wall(), 2);
                    }
                    if (j < ysize-1) { 
                    	Square downNeighbor = squares[i][j+1];
                        current.setNeighbor(downNeighbor, 3);
                        downNeighbor.setNeighbor(current, 1);
                    } else {
                    	current.setNeighbor(new Wall(), 3);
                    }
                }
            }
        }
    }

    public void placeOccupierToHall(int x, int y, iOccupier objToPlace){
        squares[x][y].placeOccupier(objToPlace);
        squaresWithObjects.add(squares[x][y]);
    }

    public void finalizeBuiltHall(){
        if (objectCount >= element.minObjNo){
            hallTime = objectCount*5;
            
            Random random = new Random();
            int objWithRuneIndex = random.nextInt(squaresWithObjects.size());
            Square squareContainingRune = squaresWithObjects.get(objWithRuneIndex);
            ((HallObj) squareContainingRune.getOccupier()).setContainsRune(true);
            runeLocation = new int[] {squareContainingRune.getXcoor(), squareContainingRune.getYcoor()};
        } else {
            System.out.println("More objects need to be placed");
        }
    }

	public Element getElement() {
		return element;
	}

	public int getXsize() {
		return xsize;
	}

	public int getYsize() {
		return ysize;
	}

	public int getHallTime() {
		return hallTime;
	}

	public int getObjectCount() {
		return objectCount;
	}

	public Square[][] getSquares() {
		return squares;
	}

	public ArrayList<Square> getSquaresWithObjects() {
		return squaresWithObjects;
	}

	public int[] getRuneLocation() {
		return runeLocation;
	}

	public void setRuneLocation(int[] runeLocation) {
		this.runeLocation = runeLocation;
	}

	public ArrayList<Monster> getMonsters() {
		return monsters;
	}

	public void addMonster(Monster monster) {
		monsters.add(monster);
	}
    
}