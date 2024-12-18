package Domain;

public abstract class Monster implements iOccupier{
	protected static int spawnFrequency;
	protected int[] coords;
	protected Hall hall;
	
	public Monster(int[] coords, Hall hall) {
		this.coords = coords;
		this.hall = hall;
	}

	public abstract void attack(Hero hero);
	
	public void place(Square s) {
		s.placeOccupier(this);
		s.getHall().addMonster(this);
	}
	public int[] getCoord() {
		return coords;
	}
}
