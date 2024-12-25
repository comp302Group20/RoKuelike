package Domain;

import java.util.Random;

public abstract class Monster implements iOccupier{
	private static Class<?>[] monsterTypes = {ArcherMonster.class, FighterMonster.class, WizardMonster.class};
	protected static int spawnFrequency;
	protected int[] coords;
	protected Hall hall;
	
	public Monster(int[] coords, Hall hall) {
		this.coords = coords;
		this.hall = hall;
	}

	public abstract void attack(Hero hero);
	
	public static void spawnMonsters(Hall hall) throws Exception {
		Random random = new Random();
		for (int i = 0; i < 3; i++) {
			int spawnX = random.nextInt(hall.getXsize());
			int spawnY = random.nextInt(hall.getYsize());
			int trialCount = 0;
			while (hall.getSquares()[spawnX][spawnY].isOccupied() && trialCount<hall.getXsize()*hall.getYsize()) {
				spawnX = random.nextInt(hall.getXsize());
				spawnY = random.nextInt(hall.getYsize());
			}
			if (trialCount == hall.getXsize()*hall.getYsize()) {
				System.out.println("Hall is full, monster can't be spawned");
			}
			int[] coords = {spawnX, spawnY};
			Monster newM = (Monster) monsterTypes[i].getConstructor(int[].class, Hall.class).newInstance(coords, hall);
			newM.place(hall.getSquares()[spawnX][spawnY]);
		}
	}
	
	public void place(Square s) {
		s.placeOccupier(this);
		s.getHall().addMonster(this);
	}
	public int[] getCoord() {
		return coords;
	}
}
