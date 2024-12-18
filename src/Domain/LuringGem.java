package Domain;

import java.util.ArrayList;

public class LuringGem extends Enchantment {

	//private static int duration; ???There is no duration in project but should we include it? 
	
	public LuringGem(boolean inInventory, int[] location) {
		super(inInventory, location);
	}

	@Override
	public void collectEnchant(Hero hero) {
		inInventory = true;
		hero.getInventory().put(this);
	}

	@Override
	public void useEnchant(Hero hero) {
		char direction = 'c';
		//direction = input(direction);
		int intDirection = -1;
		switch (direction) {
		case 'A':
			intDirection = 0;
			break;
		case 'W':
			intDirection = 1;
			break;
		case 'D':
			intDirection = 2;
			break;
		case 'S':
			intDirection = 3;
			break;
		}
		
		ArrayList<Monster> allMonsters = hero.getCurrentHall().getMonsters();
		for (Monster m : allMonsters) {
			if (m instanceof FighterMonster) {
				((FighterMonster) m).move(intDirection);
			}
		}
	}

}
