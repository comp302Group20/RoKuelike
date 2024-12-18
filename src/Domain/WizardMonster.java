package Domain;

import java.util.Random;

public class WizardMonster extends Monster {
	private static Random random = new Random(0);
	
	public WizardMonster(int[] coords, Hall hall) {
		super(coords, hall);
	}

	public static void spawnWizard(Hall hall) {
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
		WizardMonster newWM = new WizardMonster(coords, hall);
		newWM.place(hall.getSquares()[spawnX][spawnY]);
	}

	@Override
	public void attack(Hero hero) {
		int[] prevRuneLocation = hall.getRuneLocation();
		((HallObj) hall.getSquares()[prevRuneLocation[0]][prevRuneLocation[1]].getOccupier()).setContainsRune(false);
		
		int objWithRuneIndex = random.nextInt(hall.getSquaresWithObjects().size());
        Square squareContainingRune = hall.getSquaresWithObjects().get(objWithRuneIndex);
        ((HallObj) squareContainingRune.getOccupier()).setContainsRune(true);
        hall.setRuneLocation(new int[] {squareContainingRune.getXcoor(), squareContainingRune.getYcoor()});
	}

}
