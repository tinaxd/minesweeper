package work.tinax.sanemine;

public class Main {
	public static void main(String[] args) {
		if (args.length > 0 && "gui".equals(args[0])) {
			GuiMain.guiMain(args);
			return;
		}
		
//		var scanner = new Scanner(System.in);
//		var b = new Board(16, 16, 32);
//		System.out.println(b.showGameState(false));
//		while (true) {
//			int column = scanner.nextInt();
//			int row = scanner.nextInt();
//			b.openCell(column, row);
//			System.out.println(b.showGameState(false));
//			
//			if (b.isFailed()) {
//				System.out.println("You lost!");
//				break;
//			}
//			
//			if (b.gameCleared()) {
//				System.out.println("You won!");
//				break;
//			}
//		}
//		scanner.close();
		
		// AI Test
		var b = new Board(16, 16, 32);
		boolean result = MineAI.solveAll(b, true, new AICallback() {

			@Override
			public void beforeStart(Board board) {
				System.out.println(board.showGameState(true));
			}

			@Override
			public boolean onStep(Board board, int currentStep, int nestLevel) {
				System.out.printf("[Step %d (nest=%d)]\n", currentStep, nestLevel);
				//System.out.println(board.showGameState(true));
				return true;
			}
			
		});
		
		if (result) {
			System.out.println(b.showGameState(true));
			System.out.println("Finish!");
		} else {
			System.out.println(b.showGameState(true));
			System.out.println("Failed...");
		}
	}
}
