import org.junit.Test;

import Controller.GameController;
import Domain.ArcherMonster;
import Domain.Hero;
import UI.BuildModePanel.CellType;
import UI.BuildModePanel.PlacedObject;
import UI.BuildModePanel;
import UI.GamePanel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.Before;


public class Archer_update_test {
	
	private GamePanel gp;
	private ArcherMonster archer;
	
	@Before
	public void init() {
		BuildModePanel.CellType[][] g = null; 
		PlacedObject[][] p = null; 
		GameController ct = null;
		Hero h = Hero.getInstance(3, 5, 1, 1);
		gp = new GamePanel(g, p, ct, h);
		archer = new ArcherMonster(3, 4, h, new CellType[10][10], gp);
	}
	
	@Test
	public void shootArrowsOnTime() { //Ensures that the archer shoots arrows if the hero has no cloak and more than a second has passed
		gp.setCloakActive(false);
		try {
			TimeUnit.MILLISECONDS.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		archer.update();
		assertTrue(System.currentTimeMillis() - archer.getLastShot() <= 1000);
	}
	
	@Test
	public void dontShootIfCloaked() { //Ensures that the archer monster doesn't shoot arrows if the hero has an active cloak of protection
		gp.setCloakActive(true);
		try {
			TimeUnit.MILLISECONDS.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		archer.update();
		assertTrue(System.currentTimeMillis() - archer.getLastShot() > 1000);
	}
	
	@Test
	public void checkLocation() { //Checks to make sure the archer monster doesn't move
		archer.update();
		assertEquals(3, archer.getX());
		assertEquals(4, archer.getY());
	}
	
	@Test
	public void dontShootIfFar() { //Ensures that the archer monster doesn't shoot if the hero is move than 4 cells away
		BuildModePanel.CellType[][] g = null; 
		PlacedObject[][] p = null; 
		GameController ct = null;
		Hero h = Hero.getInstance(300, 700, 1, 1);
		gp = new GamePanel(g, p, ct, h);
		archer = new ArcherMonster(300, 100, h, new CellType[1000][1000], gp);
		
		gp.setCloakActive(false);
		try {
			TimeUnit.MILLISECONDS.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		archer.update();
		
		assertTrue(System.currentTimeMillis() - archer.getLastShot() > 1000);
	}
	
	
	
}
