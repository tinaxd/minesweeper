package work.tinax.sanemine;

import java.util.Optional;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class BoardView extends Canvas {
	private Board b;
	private WinLoseAction finalAction;
	private boolean discloseBombs = false;
	private final GraphicsContext gcContext;
	private Optional<Integer> highlightedCell = Optional.empty();
	
	private static final Color CLOSED_COLOR = Color.DARKGRAY;
	private static final Color OPENED_COLOR = Color.DARKGREEN;
	private static final Color FLAGGED_COLOR = Color.ORANGERED;
	private static final Color BACKGROUND_COLOR = Color.LIGHTGRAY;
	private static final Color HIGHLIGHT_COLOR = Color.LIGHTBLUE;
	
	private static final Color LINE_COLOR = Color.ALICEBLUE;
	
	public BoardView(double width, double height, Board b, WinLoseAction act) {
		super(width, height);
		gcContext = this.getGraphicsContext2D();
		
		this.setBoard(b);
		
		addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
			if (onMouseClick(e)) {
				judge();
				drawBoard(gcContext);
			}
		});
		
		addEventHandler(MouseEvent.MOUSE_MOVED, this::onMousePositionChange);
		
		this.setAction(act);
	}
	
	public BoardView(double width, double height) {
		this(width, height, null, null);
	}

	public Board getBoard() {
		return b;
	}

	public void setBoard(Board b) {
		this.b = b;
		judge();
		drawBoard(gcContext);
	}
	
	/**
	 * @param gc
	 */
	private void drawBoard(GraphicsContext gc) {
		final var width = this.getWidth();
		final var height = this.getHeight();
		
		gc.setFill(BACKGROUND_COLOR);
		gc.fillRect(0, 0, width, height);
		
		if (b == null) {
			return;
		}
		
		final var nColumns = b.getWidth();
		final var nRows = b.getHeight();
		
		final var cellWidth = width / nColumns;
		final var cellHeight = height / nRows;
		
		var margin = 5.0;
		
		gc.setStroke(LINE_COLOR);
		gc.setFont(new Font(24));
		for (int x=0; x<nColumns; x++) {
			for (int y=0; y<nRows; y++) {
				var cellIndex = b.fromColumnRow(x, y);
				var initX = (width * x) / nColumns;
				var initY = (height * y) / nRows;
				var drawNumber = false;
				var flagged = false;
				
				var cell = b.getCell(x, y);
				switch (cell.getState()) {
				case CLOSED:
					if (highlightedCell.map(i -> i == cellIndex).orElse(false)) {
						// when cell is highlighted
						gc.setFill(HIGHLIGHT_COLOR);
					} else {
						gc.setFill(CLOSED_COLOR);
					}
					break;
				case FLAGGED:
					gc.setFill(CLOSED_COLOR);
					flagged = true;
					break;
				case OPENED:
					drawNumber = true;
					gc.setFill(OPENED_COLOR);
					break;
				default:
					throw new UnsupportedOperationException();
				}
				
				//System.out.printf("%f %f %f %f\n", initX, initY, cellWidth, cellHeight);
				gc.fillRect(initX, initY, cellWidth-margin, cellHeight-margin);
			
				if (drawNumber) {
					int bombs = cell.getNeighborBombs();
					if (bombs != 0) {
						gc.setFill(Color.PALEVIOLETRED);
						var text = Integer.toString(bombs);
						gc.fillText(text, initX+(cellWidth/3), initY+(cellHeight/2));
					}
				}
				
				if (flagged) {
					gc.setFill(FLAGGED_COLOR);
					var cellWidthM = cellWidth - margin;
					var cellHeightM = cellHeight - margin;
					var flagWidth = cellWidthM / 2.0;
					var flagHeight = cellHeightM / 2.0;
					var initFX = initX + (cellWidthM / 4.0);
					var initFY = initY + (cellHeightM / 4.0);
					gc.fillRect(initFX, initFY, flagWidth, flagHeight);
				}
				
				if (discloseBombs && cell.hasBomb()) {
					gc.setStroke(LINE_COLOR);
					var cellWidthM = cellWidth - margin;
					var cellHeightM = cellHeight - margin;
					var p1x = initX + (cellWidthM / 5.0);
					var p1y = initY + (cellHeightM / 5.0);
					var p2x = initX + (cellWidthM * 4.0 / 5.0);
					var p2y = initY + (cellHeightM * 4.0 / 5.0);
					var p3x = initX + (cellWidthM * 4.0 / 5.0);
					var p3y = initY + (cellHeightM / 5.0);
					var p4x = initX + (cellWidthM / 5.0);
					var p4y = initY + (cellHeightM * 4.0 / 5.0);
					gc.strokeLine(p1x, p1y, p2x, p2y);
					gc.strokeLine(p3x, p3y, p4x, p4y);
				}
			}
		}
	}
	
	private void onMousePositionChange(MouseEvent e) {
		if (b == null) {
			highlightedCell = Optional.empty();
			return;
		}
		
		final var x = e.getSceneX();
		final var y = e.getSceneY();
		
		var res = getIndexFromMouseCord(x, y);
		
		var xIndex = res[0];
		var yIndex = res[1];
		
		if (   xIndex < 0 || xIndex >= b.getWidth()
			|| yIndex < 0 || yIndex >= b.getHeight()) {
			highlightedCell = Optional.empty();
			return;
		}
		
		highlightedCell = Optional.of(b.fromColumnRow(xIndex, yIndex));
		drawBoard(gcContext);
	}
	
	private int[] getIndexFromMouseCord(double x, double y) {
		final var width = this.getWidth();
		final var height = this.getHeight();
		
		final var nColumns = b.getWidth();
		final var nRows = b.getHeight();
		
		final var cellWidth = width / nColumns;
		final var cellHeight = height / nRows;
		
		var xIndex = (int) (x / cellWidth) - 1;
		var yIndex = (int) (y / cellHeight) - 1;
		
		return new int[] {xIndex, yIndex};
	}
	
	private boolean onMouseClick(MouseEvent e) {
		if (b == null) {
			return false;
		}
		
		final var x = e.getSceneX();
		final var y = e.getSceneY();
		System.out.printf("clicked: %f %f\n", x, y);
		
		var res = getIndexFromMouseCord(x, y);
		
		var xIndex = res[0];
		var yIndex = res[1];
		
		if (   xIndex < 0 || xIndex >= b.getWidth()
			|| yIndex < 0 || yIndex >= b.getHeight()) {
			System.out.println("ignore click");
			return false;
		}
		
		if (e.getButton() == MouseButton.PRIMARY) {
			if (b.getCell(xIndex, yIndex).getState() == CellState.FLAGGED) {
				System.out.println("flag block");
			} else {
				System.out.printf("open cell %d %d\n", xIndex, yIndex);
				b.openCell(xIndex, yIndex);
			}
		} else if (e.getButton() == MouseButton.SECONDARY) {
			b.toggleFlag(xIndex, yIndex);
		}
		return true;
	}
	
	private void judge() {
		if (b == null) {
			return;
		}
		
		var act = getAction();
		if (act == null) {
			return;
		}
		
		if (b.isFailed()) {
			act.onLose(this);
		} else if (b.gameCleared()) {
			act.onWin(this);
		}
	}

	public WinLoseAction getAction() {
		return finalAction;
	}

	public void setAction(WinLoseAction action) {
		this.finalAction = action;
	}

	public boolean isDisclosingBombs() {
		return discloseBombs;
	}

	public void discloseBombs(boolean discloseBombs) {
		this.discloseBombs = discloseBombs;
		drawBoard(gcContext);
	}
}
