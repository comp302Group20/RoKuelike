package Domain;

import java.util.HashMap;

public class Inventory {
	private HashMap<Enchantment, Integer> enchantCounts = new HashMap<Enchantment, Integer>();
	private Inventory instance = new Inventory();
	
	private Inventory() {}
	
	public void put(Enchantment e) {
		if (enchantCounts.containsKey(e)) {
			int newCount = enchantCounts.get(e) + 1;
			enchantCounts.put(e, newCount);
		} else {
			enchantCounts.put(e, 1);
		}
	}

	public void use(Enchantment e) {
		if (!enchantCounts.containsKey(e) || enchantCounts.get(e) <= 0) {
			System.out.println("No such enchantment in inventory");
		} else {
			int newCount = enchantCounts.get(e) - 1;
			enchantCounts.put(e, newCount);
		}
	}
	
	public Inventory getInstance() {
		return instance;
	}

}
