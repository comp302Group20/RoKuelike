package Domain;

import java.util.Random;

public class FighterMonster extends Monster {
	private static Random random = new Random(2);
	
	public FighterMonster(int[] coords, Hall hall) {
		super(coords, hall);
	}
	
	public static void spawnFighter(Hall hall) {
		int spawnX = random.nextInt(hall.getXsize());
		int spawnY = random.nextInt(hall.getYsize());
		int trialCount = 0;
		while (!hall.getSquares()[spawnX][spawnY].isOccupied() && trialCount<hall.getXsize()*hall.getYsize()) {
			spawnX = random.nextInt(hall.getXsize());
			spawnY = random.nextInt(hall.getYsize());
		}
		if (trialCount == hall.getXsize()*hall.getYsize()) {
			System.out.println("Hall is full, monster can't be spawned");
		}
		int[] coords = {spawnX, spawnY};
		FighterMonster newFM = new FighterMonster(coords, hall);
		newFM.place(hall.getSquares()[spawnX][spawnY]);
	}

	@Override
	public void attack(Hero hero) {
		hero.decrementLives();		
	}
	
	public void moveRandomly() {
		int movingDirection = random.nextInt(4);
		move(movingDirection, 1);
	}
	
	public void move(int movingDirection, int distance) {
		switch (movingDirection) { 
		case 0: //left, x-, A
			if (coords[0] - distance >= 0) {coords[0] -= distance;}
			else {coords[0] = 0;}
			break;
		case 1: //up, y+, W
			if (coords[1] + distance < hall.getYsize()) {coords[1] += distance;}
			else {coords[1] = hall.getYsize()-1;}
			break;
		case 2: //right, x+, D
			if (coords[0] + distance < hall.getXsize()) {coords[0] += distance;}
			else {coords[0] = hall.getXsize() - 1;}
			break;
		case 3: //down, y-, S
			if (coords[1] - distance <= 0) {coords[1] -= distance;}
			else {coords[1] = 0;}
			break;
		}
	}
}
