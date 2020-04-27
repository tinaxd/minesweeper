package work.tinax.sanemine;

public class Cell {
	private boolean hasBomb;
	private CellState state;
	
	private int neighborBombs;
	
	public Cell(boolean hasBomb, CellState state) {
		this.setHasBomb(hasBomb);
		this.setState(state);
	}
	
	public Cell(boolean hasBomb) {
		this(hasBomb, CellState.CLOSED);
	}

	public boolean hasBomb() {
		return hasBomb;
	}

	public void setHasBomb(boolean hasBomb) {
		this.hasBomb = hasBomb;
	}

	public CellState getState() {
		return state;
	}

	public void setState(CellState state) {
		this.state = state;
	}

	public int getNeighborBombs() {
		return neighborBombs;
	}

	public void setNeighborBombs(int neighborBombs) {
		this.neighborBombs = neighborBombs;
	}
	
	public boolean isOpened() {
		return state == CellState.OPENED;
	}
	
	public boolean isClosed() {
		return state == CellState.CLOSED;
	}
	
	public boolean isFlagged() {
		return state == CellState.FLAGGED;
	}
}
