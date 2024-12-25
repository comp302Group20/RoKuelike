package Domain;

import java.util.Random;

public abstract class Enchantment implements iOccupier{
	
	private static Class<?> [] enchantTypes = {ExtraTime.class, ExtraLife.class, Reveal.class, CloakOfProtection.class, LuringGem.class};
	private static final Random random = new Random(3);
	protected boolean inInventory;
	protected Hall hall;
	protected int[] location;
	
	public Enchantment(boolean inInventory, int[] location, Hall hall) {
		this.inInventory = inInventory;
		this.location = location;
		this.hall = hall;
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
		if (trialCount >= hall.getXsize()*hall.getYsize()) {
			System.out.println("Hall is full, enchantment can't be spawned");
		}
		int[] coords = {spawnX, spawnY};
		Enchantment newEnchant = (Enchantment) enchantTypes[randomEnchant].getConstructor(boolean.class, int[].class, Hall.class).newInstance(false, coords, hall);
		hall.placeOccupierToHall(spawnX, spawnY, newEnchant);
	}
	
	@Override
	public void place(Square s) {
		s.placeOccupier(this);
	}
}
