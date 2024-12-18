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
		switch (movingDirection) {
		case 0:
			coords[0]--;
			break;
		case 1:
			coords[1]--;
			break;
		case 2:
			coords[0]++;
			break;
		case 3:
			coords[1]++;
			break;
		}
	}
	
	public void move(int movingDirection) {
		switch (movingDirection) {
		case 0:
			coords[0]--;
			break;
		case 1:
			coords[1]--;
			break;
		case 2:
			coords[0]++;
			break;
		case 3:
			coords[1]++;
			break;
		}
	}
}
