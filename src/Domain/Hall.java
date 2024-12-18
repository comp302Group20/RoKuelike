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
            squares = new Square[xsize][ysize];
            for (int i = 0; i < xsize; i++){
                for (int j = 0; j < ysize; j++) {
                    Square[] nearby = new Square[4];
                    if (i > 0) {
                        nearby[0] = squares[i-1][j];
                    } else {
                        nearby[0] = null;
                    }
                    if (j > 0) {
                        nearby[1] = squares[i][j-1];
                    } else {
                        nearby[1] = null;
                    }
                    if (i < xsize-1) { //assigning neighbors before creating said neighbors may be problematic. Test later
                        nearby[2] = squares[i+1][j];
                    } else {
                        nearby[2] = null;
                    }
                    if (j < ysize-1) { //assigning neighbors before creating said neighbors may be problematic. Test later
                        nearby[3] = squares[i][j+1];
                    } else {
                        nearby[3] = null;
                    }
                    squares[i][j] = new Square(i, j, nearby, this);
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