package work.tinax.sanemine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Board {
	private int width;
	private int height;
	private int initBombs;
	private List<Cell> cells;
	private boolean failed = false;
	
	public Board(int width, int height, int nBombs) {
		if (width <= 0 || height <= 0 || nBombs < 0 || nBombs >= width * height) {
			throw new IllegalArgumentException();
		}
		this.width = width;
		this.height = height;
		this.initBombs = nBombs;
		setupCells(width, height, nBombs);
		buildNeighborMap();
	}
	
	private void setupCells(int width, int height, int nBombs) {
		var nCells = width * height;
		cells = new ArrayList<Cell>();
		for (int i=0; i<nCells; i++) {
			cells.add(new Cell(false));
		}
		
		var bombIndices = new ArrayList<Integer>();
		for (int i=0; i<nCells; i++) {
			bombIndices.add(i);
		}
		
		var random = new Random();
		for (int i=0; i<nBombs; i++) {
			var rand = random.nextInt(bombIndices.size());
			var index = bombIndices.get(rand);
			bombIndices.remove(rand);
			cells.get(index).setHasBomb(true);
		}
	}
	
	// return -1 on error.
	private int getCellIndex(int base, Direction direction) {
		var cr = fromIndex(base);
		var column = cr[0];
		var row = cr[1];
		switch (direction) {
		case LEFTUP:
			if (column <= 0 || row <= 0) return -1;
			return fromColumnRow(column-1, row-1);
		case UP:
			if (row <= 0) return -1;
			return fromColumnRow(column, row-1);
		case RIGHTUP:
			if (column >= width-1 || row <= 0) return -1;
			return fromColumnRow(column+1, row-1);
		case LEFT:
			if (column <= 0) return -1;
			return fromColumnRow(column-1, row);
		case RIGHT:
			if (column >= width-1) return -1;
			return fromColumnRow(column+1, row);
		case LEFTDOWN:
			if (column <= 0 || row >= height-1) return -1;
			return fromColumnRow(column-1, row+1);
		case DOWN:
			if (row >= height-1) return -1;
			return fromColumnRow(column, row+1);
		case RIGHTDOWN:
			if (column >= width-1 || row >= height-1) return -1;
			return fromColumnRow(column+1, row+1);
		default:
			throw new UnsupportedOperationException();
		}
	}
	
	public int[] fromIndex(int index) {
		int column = index % width;
		int row = index / height;
		return new int[] {column, row};
	}
	
	public int fromColumnRow(int column, int row) {
		return column + row * width;
	}
	
	private void buildNeighborMap() {
		for (int i=0; i<width*height; i++) {
			var dirs = new Direction[] {Direction.LEFTUP, Direction.UP, Direction.RIGHTUP, Direction.LEFT, Direction.RIGHT, Direction.LEFTDOWN, Direction.DOWN, Direction.RIGHTDOWN};
			int bombs = 0;
			for (var dir : dirs) {
				var index = getCellIndex(i, dir);
				if (index != -1 && cells.get(index).hasBomb()) {
					bombs++;
				}
			}
			cells.get(i).setNeighborBombs(bombs);
		}
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getInitBombs() {
		return initBombs;
	}
	
	@Override
	public String toString() {
		return showGameState(true);
	}
	
	public String showGameState(boolean discloseBombs) {
		var sb = new StringBuilder();
		for (int i=0; i<width*height; i++) {
			if (i != 0 && i % width == 0) {
				sb.append('\n');
			}
			
			sb.append(charOfCell(cells.get(i), discloseBombs));
			sb.append(' ');
		}
		return sb.toString();
	}
	
	@SuppressWarnings("unused")
	private static char charOfCell(Cell c) {
		return charOfCell(c, true);
	}
	
	private static char charOfCell(Cell c, boolean discloseBomb) {
		if (discloseBomb && c.hasBomb()) {
			return '+';
		}
		if (c.isOpened()) {
			if (c.getNeighborBombs() == 0) {
				return ' ';
			} else {
				return Integer.toString(c.getNeighborBombs()).charAt(0);
			}
		}
		
		return 'O';
	}
	
	public void openCell(int column, int row) {
		openCell(fromColumnRow(column, row));
	}
	
	public void openCell(int index) {
		var cell = cells.get(index);
		if (cell.hasBomb()) {
			failed = true;
			return;
		}
		
		openCell4(index);
	}
	
	private void openCell4(int index) {
		var cell = cells.get(index);
		if (cell.hasBomb() || cell.isOpened() || cell.isFlagged()) {
			// do not disclose.
			return;
		}
		
		cell.setState(CellState.OPENED);
		
		if (cell.getNeighborBombs() > 0) {
			// disclose this cell, but not neighbor cells.
			return;
		}
		
		var dirs = new Direction[] {Direction.UP, Direction.LEFT, Direction.RIGHT, Direction.DOWN};
		for (var dir : dirs) {
			var nextIndex = getCellIndex(index, dir);
			if (nextIndex != -1) {
				openCell4(nextIndex);
			}
		}
	}

	public boolean isFailed() {
		return failed;
	}
	
	public boolean gameCleared() {
		for (var cell : cells) {
			if ((!cell.hasBomb() && !cell.isOpened()) || (cell.hasBomb() && cell.isOpened())) {
				return false;
			}
		}
		return true;
	}
	
	public Cell getCell(int index) {
		return cells.get(index);
	}
	
	public Cell getCell(int column, int row) {
		return getCell(fromColumnRow(column, row));
	}
	
	public void toggleFlag(int index) {
		switch (cells.get(index).getState()) {
		case OPENED:
			return;
		case CLOSED:
			cells.get(index).setState(CellState.FLAGGED);
			break;
		case FLAGGED:
			cells.get(index).setState(CellState.CLOSED);
			break;
		default:
			throw new UnsupportedOperationException();
		}
	}

	public void toggleFlag(int column, int row) {
		toggleFlag(fromColumnRow(column, row));
	}
}
