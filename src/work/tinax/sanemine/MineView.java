package work.tinax.sanemine;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MineView extends BorderPane {
	private Board b;
	private BoardView bv;
	private boolean settingShown = false;
	
	public MineView(double width, double height) {
		super();
		b = new Board(16, 16, 40);
		bv = new BoardView(width, height, b, new WinLoseAction() {
			@Override
			public void onWin(BoardView bv) {
				var alert = new Alert(AlertType.INFORMATION);
				alert.setHeaderText(null);
				alert.setTitle("MineSweeper");
				alert.setContentText("‚ ‚È‚½‚ÌŸ‚¿‚Å‚·!");
				alert.showAndWait();
			}

			@Override
			public void onLose(BoardView bv) {
				bv.discloseBombs(true);
				var alert = new Alert(AlertType.WARNING);
				alert.setHeaderText(null);
				alert.setTitle("MineSweeper");
				alert.setContentText("‚ ‚È‚½‚Ì•‰‚¯!");
				alert.showAndWait();
			}
		});
		setCenter(bv);
		
		var newGameButton = new Button("New Game");
		newGameButton.setOnAction(this::onNewGameButtonPressed);
		setTop(newGameButton);
	}
	
	public void onNewGameButtonPressed(ActionEvent e) {
		System.out.println("new game button pressed");
		if (settingShown) {
			System.out.println("ignore");
			return;
		}
		settingShown = true;
		
		var settings = new BoardSettingsView();
		if (b != null) {
			settings.setInitValues(
					Integer.valueOf(b.getWidth()),
					Integer.valueOf(b.getHeight()),
					Integer.valueOf(b.getInitBombs()));
		}
		
		var scene = new Scene(settings);
		var stage = new Stage();
		stage.setScene(scene);
		settings.setCallback(board -> {
			updateBoard(board);
			settingShown = false;
			stage.close();
		});
		stage.setOnCloseRequest(ev -> {
			settingShown = false;
		});
		stage.showAndWait();
	}
	
	void updateBoard(Board b) {
		this.b = b;
		bv.discloseBombs(false);
		bv.setBoard(b);
	}
}
