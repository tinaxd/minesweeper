package work.tinax.sanemine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MineAI {
	private Board board;
	private boolean logEnabled;
	private boolean firstTime = true;
	private int assumeNestLevel = 0;
	
	public MineAI(Board board) {
		this.board = board;
	}
	
	public void setLogging(boolean enabled) {
		logEnabled = enabled;
	}
	
	public boolean getLogging() {
		return logEnabled;
	}
	
	private static final Direction[] ALL_DIRECTIONS =
			new Direction[] {Direction.LEFTUP, Direction.UP, Direction.RIGHTUP, Direction.LEFT, Direction.RIGHT, Direction.LEFTDOWN, Direction.DOWN, Direction.RIGHTDOWN};
	
	// if not any open, return empty list
	public List<Integer> nextStep(boolean logging, AICallback cb) {		
		if (firstTime) {
			firstTime = false;
			var ret = new ArrayList<Integer>();
			ret.add(openAny());
			return ret;
		}
		
		int cells = board.getTotalCells();
		var done = new ArrayList<Integer>();
		boolean ok = false;
		for (int i=0; i<cells; i++) {
			// Search for opened cells..
			var cell = board.getCell(i);
			
			// Skip flagged cells.
			if (cell.isFlagged()) {
				continue;
			}
			
			// We cannot get any information from closed cells.
			if (cell.isClosed()) {
				continue;
			}
			
			// We can solve puzzle by utilizing neighbor bomb cell hints.
			if (cell.isOpened()) {
				int bombsAround = cell.getNeighborBombs();
//				if (bombsAround == 0) {
//					continue;
//				}
				
				// Gather neighbor cells.
				int fi = i;
				var neighbors = Arrays.asList(ALL_DIRECTIONS).stream().map(d -> board.getCellIndex(fi, d)).filter(k -> k != -1).map(k -> board.getCell(k)).collect(Collectors.toList());
				int closedCellsAround = 0;
				int flaggedCellsAround = 0;
				//int openedCellsAround = 0;
				for (var neighbor : neighbors) {
					switch (neighbor.getState()) {
					case OPENED:
						//openedCellsAround++;
						break;
					case CLOSED:
						closedCellsAround++;
						break;
					case FLAGGED:
						flaggedCellsAround++;
						break;
					}
				}
				
				if (bombsAround == flaggedCellsAround) {
					if (closedCellsAround > 0) {
						// Open all remaining cells.
						if (logEnabled) {
							System.out.printf("Open cells around %d by logic.\n", i);
						}
						neighbors.stream().filter(c -> c.isClosed()).forEach(c -> c.setState(CellState.OPENED));
						done = null; // TODO:
						ok = true;
						break;
					} else {
						continue;
					}
				} else if (bombsAround > flaggedCellsAround) {
					if (closedCellsAround == bombsAround - flaggedCellsAround) {
						// Flag all remaining cells.
						if (logEnabled) {
							System.out.printf("Flag cells around %d by logic.\n", i);
						}
						neighbors.stream().filter(c -> c.isClosed()).forEach(c -> c.setState(CellState.FLAGGED));
						done = null; // TODO:
						ok = true;
						break;
					} else {
						continue;
					}
				} else {
					throw new AIReasoningException(String.format("bombsAround < flaggedCellsAround @ %d", i));
				}
			}
		}
		
		if (ok) {
			return done;
		}
		
		// Start assume!
		int cells1 = board.getTotalCells();
		for (int i=0; i<cells1; i++) {
			var cell = board.getCell(i);
			if (!cell.isClosed()) {
				// closed only
				continue;
			}
			
			boolean surroundedByOpen = false;
			for (var dir : ALL_DIRECTIONS) {
				int nextIndex = board.getCellIndex(i, dir);
				if (nextIndex < 0) continue;
				if (board.getCell(nextIndex).isOpened()) {
					surroundedByOpen = true;
				}
			}
			if (!surroundedByOpen) {
				// Surrounded By Open only
				continue;
			}
			
			if (logging) {
				System.out.printf("ASSUME closed cell as flagged (entering level %d) %d\n", assumeNestLevel+1, i);
			}
			var newBoard = board.clone();
			newBoard.getCell(i).setState(CellState.FLAGGED);
			try {
				if (solveAll(newBoard, logging, cb, assumeNestLevel+1)) {
					board.replaceWith(newBoard);
					return null;
				}
			} catch (AIReasoningException e) {
				if (logging) {
					System.out.println(e.getMessage());
					System.out.printf("ASSUME %d failed (back to level %d)\n", i, assumeNestLevel);
				}
			}
		}
		
		// Random strategy...
		if (done != null && done.isEmpty()) {
			System.err.println(board.showGameState(true));
			throw new AIReasoningException("NO LOGIC");
			// No clue. Just random.
//			var ret = new ArrayList<Integer>();
//			ret.add(openAny());
//			return ret;
		}
		
		return done;
	}
	
	public static boolean solveAll(Board board, boolean logging, AICallback cb) {
		return solveAll(board, logging, cb, 0);
	}
	
	public static boolean solveAll(Board board, boolean logging, AICallback cb, int nestLevel) {
		var ai = new MineAI(board);
		ai.assumeNestLevel = nestLevel;
		if (nestLevel > 0) {
			ai.firstTime = false;
		}
		ai.setLogging(logging);
		cb.beforeStart(board);
		int step = 0;
		while (true) {
			ai.nextStep(logging, cb);
			if (!cb.onStep(board, step++, ai.assumeNestLevel)) {
				return false;
			}
			if (board.gameCleared()) {
				return true;
			} else if (board.isFailed()) {
				return false;
			}
		}
	}
	
	// returns opened cell index or -1.
	public int openAny() {
		int cells = board.getTotalCells();
		for (int i=0; i<cells; i++) {
			if (board.getCell(i).isClosed()) {
				if (logEnabled) {
					System.out.printf("RANDOM Open cell %d\n", i);
				}
				board.openCell(i);
				return i;
			}
		}
		return -1;
	}
}
