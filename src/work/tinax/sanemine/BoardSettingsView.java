package work.tinax.sanemine;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class BoardSettingsView extends VBox {
	private final IntegerBox width;
	private final IntegerBox height;
	private final IntegerBox bombs;
	private final Button okButton;
	private Consumer<Board> callback;
	
	public BoardSettingsView() {
		width = new IntegerBox("â°ïù");
		height = new IntegerBox("ècïù");
		bombs = new IntegerBox("îöíeÇÃêî");
		okButton = new Button("OK");
		getChildren().addAll(width, height, bombs, okButton);
		okButton.setOnAction(this::onOkButtonPressed);
	}
	
	public void setInitValues(Integer width, Integer height, Integer bombs) {
		if (width != null) {
			this.width.setInitValue(Integer.valueOf(width));
		}
		if (height != null) {
			this.height.setInitValue(Integer.valueOf(height));
		}
		if (bombs != null) {
			this.bombs.setInitValue(Integer.valueOf(bombs));
		}
	}
	
	public void setCallback(Consumer<Board> callback) {
		this.callback = callback;
	}
	
	public void onOkButtonPressed(ActionEvent e) {
		var b = ask();
		if (b != null && callback != null) {
			callback.accept(b);
		}
	}
	
	public Board ask() {
		try {
			int w = width.collect().get();
			int h = height.collect().get();
			int b = bombs.collect().get();
			return new Board(w, h, b);
		} catch (NoSuchElementException e) {
			var alert = new Alert(AlertType.ERROR);
			alert.setHeaderText(null);
			alert.setContentText("ì¸óÕílÇ™ïsê≥Ç≈Ç∑");
			alert.showAndWait();
			return null;
		}
	}
}

class IntegerBox extends HBox {
	private final Label label;
	private final TextField input;
	
	public IntegerBox(String askText) {
		label = new Label(askText);
		input = new TextField();
		this.getChildren().addAll(label, input);
	}
	
	public void setInitValue(int v) {
		input.setText(Integer.toString(v));
	}
	
	public Optional<Integer> collect() {
		String u = input.getText();
		try {
			int i = Integer.parseInt(u);
			return Optional.of(Integer.valueOf(i));
		} catch (NumberFormatException e) {
			return Optional.empty();
		}
	}
}
