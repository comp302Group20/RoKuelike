package Domain;

import java.util.Random;

public class Reveal extends Enchantment {

	private int duration = 10;
	private Random random = new Random(4);
	
	public Reveal(boolean inInventory, int[] location) {
		super(inInventory, location);
	}

	@Override
	public void collectEnchant(Hero hero) {
		inInventory = true;
		hero.getInventory().put(this);
	}

	@Override
	public void useEnchant(Hero hero) {
		hero.getInventory().use(this);
		Hall hall = hero.getCurrentHall();
		int[] runeLocation = hall.getRuneLocation();
		int startingX, startingY;
		boolean fitsInHall;
		do {
			startingX = runeLocation[0] + random.nextInt(7) - 3;
			startingY = runeLocation[1] + random.nextInt(7) - 3;
			fitsInHall = (startingX >= 0) && (startingY >= 0) && (startingX + 3 < hall.getXsize()) && (startingY + 3 < hall.getYsize());
		} while (!fitsInHall);
		// highlight(startingX, startingY, startingX + 3, startingY + 3) for 10 seconds
	}
}
