package Domain;

public class ExtraLife extends Enchantment {

	public ExtraLife(boolean inInventory, int[] location, Hall hall) {
		super(inInventory, location, hall);
	}

	@Override
	public void collectEnchant(Hero hero) {
		useEnchant(hero);
	}

	@Override
	public void useEnchant(Hero hero) {
		hero.incrementLives();
	}
}
