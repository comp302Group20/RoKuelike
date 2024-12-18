package Domain;

public class ExtraTime extends Enchantment {

	private static int timeAdded = 5;
	
	public ExtraTime(boolean inInventory, int[] location) {
		super(inInventory, location);
	}

	@Override
	public void collectEnchant(Hero hero) {
		useEnchant(hero);
	}

	@Override
	public void useEnchant(Hero hero) {
		hero.changeTime(timeAdded);
	}
	
}
