package Domain;

public class ExtraLife extends Enchantment {

	public ExtraLife(boolean inInventory, int[] location) {
		super(inInventory, location);
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
