package Domain;

public class HallObj implements iOccupier{

    private boolean containsRune;
    private Square location;

	@Override
	public void place(Square s) {
		location = s;
	}

	public void setContainsRune(boolean containsRune) {
		this.containsRune = containsRune;
	}
}