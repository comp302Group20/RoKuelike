package Domain;

public class Hero {
	private int x;
    private int y;
    private int livesCount;
    private int remainingTime;
    private Inventory inventory;
    private Hall currentHall;
    private boolean isVisibleToArchers;
	
    public boolean isVisibleToArchers() {
		return isVisibleToArchers;
	}

	public void setVisibleToArchers(boolean isVisibleToArchers) {
		this.isVisibleToArchers = isVisibleToArchers;
	}

	public int getY() {
		return y;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void decrementLives() {
		livesCount--;
	}
	public void incrementLives() {
		livesCount++;
	}
	
	public int getRemainingTime() {
		return remainingTime;
	}
	
	public void changeTime(int seconds) {
		remainingTime += seconds;
	}

	public Inventory getInventory() {
		return inventory;
	}

	public Hall getCurrentHall() {
		return currentHall;
	}

	public void setCurrentHall(Hall currentHall) {
		this.currentHall = currentHall;
	}

	public int getLivesCount() {
		return livesCount;
	}

}
