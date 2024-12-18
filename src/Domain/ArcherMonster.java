package Domain;

import java.util.Random;

public class ArcherMonster extends Monster {
	private static Random random = new Random(1);
	
	public ArcherMonster(int[] coords, Hall hall) {
		super(coords, hall);
	}
	
	public static void spawnArcher(Hall hall) {
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
		ArcherMonster newAM = new ArcherMonster(coords, hall);
		newAM.place(hall.getSquares()[spawnX][spawnY]);
	}
	
	@Override
	public void attack(Hero hero) {
		Arrow arrow;
		int deltaX = hero.getX() - this.coords[0];
		int deltaY = hero.getY() - this.coords[1];
		double[] dcoords = {(double) (coords[0]), (double) coords[1]};
		if (deltaY == 0) {
			arrow = new Arrow(dcoords, new double[] {0,1});
		} else {
			double direction = deltaX/deltaY;
			arrow = new Arrow(dcoords, new double[] {1, 1/direction});
		}
	}

	private class Arrow{
		public Arrow(double[] coords, double[] direction) {
			location = coords;
			this.direction = direction;
			distanceTravelled = 0;
		}

		double[] location;
		double[] direction;
		double distanceTravelled;
		
		void moveArrow() {
			location[0] = location[0] + direction[0];
			location[1] = location[1] + direction[1];
			distanceTravelled += Math.pow(direction[0]*direction[0] + direction[1]*direction[1], 0.5);
			if (distanceTravelled > 4) {
				//destroy arrow
			}
		}
	}
}
