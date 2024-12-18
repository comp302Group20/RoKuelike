package Domain;

public class CloakOfProtection extends Enchantment {
	
	private static int duration = 20;
	
	public CloakOfProtection(boolean inInventory, int[] location) {
		super(inInventory, location);
	}

	@Override
	public void collectEnchant(Hero hero) {
		inInventory = true;
		hero.getInventory().put(this);
	}

	@Override
	public void useEnchant(Hero hero) {
		hero.setVisibleToArchers(false);
		// reverse 20 seconds later
	}

}
