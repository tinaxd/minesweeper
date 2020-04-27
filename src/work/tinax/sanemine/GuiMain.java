package work.tinax.sanemine;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GuiMain extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		var mv = new MineView(700, 700);
		var scene = new Scene(mv, 800, 800);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
}
