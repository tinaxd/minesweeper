package work.tinax.sanemine;

import java.util.Scanner;

public class Main {
	public static void main2(String[] args) {
		var scanner = new Scanner(System.in);
		var b = new Board(16, 16, 32);
		System.out.println(b.showGameState(false));
		while (true) {
			int column = scanner.nextInt();
			int row = scanner.nextInt();
			b.openCell(column, row);
			System.out.println(b.showGameState(false));
			
			if (b.isFailed()) {
				System.out.println("You lost!");
				break;
			}
			
			if (b.gameCleared()) {
				System.out.println("You won!");
				break;
			}
		}
		scanner.close();
	}
}
