package Domain;

import java.util.Random;

public abstract class Enchantment {
	
	private static Class<?> [] enchantTypes = {ExtraTime.class, ExtraLife.class, Reveal.class, CloakOfProtection.class, LuringGem.class};
	private static final Random random = new Random(3);
	protected boolean inInventory;
	protected int[] location;
	
	public Enchantment(boolean inInventory, int[] location) {
		this.inInventory = inInventory;
		this.location = location;
	}
	
	public abstract void collectEnchant(Hero hero);
	public abstract void useEnchant(Hero hero);
	
	public static void spawnEnchants(Hall hall) throws Exception {
		int spawnX = random.nextInt(hall.getXsize());
		int spawnY = random.nextInt(hall.getYsize());
		int randomEnchant = random.nextInt(enchantTypes.length);
		int trialCount = 0;
		while (!hall.getSquares()[spawnX][spawnY].isOccupied() && trialCount<hall.getXsize()*hall.getYsize()) {
			spawnX = random.nextInt(hall.getXsize());
			spawnY = random.nextInt(hall.getYsize());
		}
		if (trialCount == hall.getXsize()*hall.getYsize()) {
			System.out.println("Hall is full, monster can't be spawned");
		}
		int[] coords = {spawnX, spawnY};
		Enchantment newEnchant = (Enchantment) enchantTypes[randomEnchant].getConstructor(boolean.class, int[].class).newInstance(false, coords);
	}

}
