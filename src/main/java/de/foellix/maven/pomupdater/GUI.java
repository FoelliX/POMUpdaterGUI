package de.foellix.maven.pomupdater;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class GUI extends Application {
	private static final Font BOLD = Font.font("System", FontWeight.BOLD, 12);
	public static Map<String, List<VersionedPOMElement>> map = null;
	public static boolean started = false;

	private GridPane pane;
	private int row;

	@Override
	public void start(Stage stage) {
		this.pane = new GridPane();
		this.pane.setPadding(new Insets(5, 10, 5, 10));
		this.row = 0;

		final ScrollPane scrollPane = new ScrollPane(this.pane);
		scrollPane.setFitToWidth(true);

		final Scene scene = new Scene(scrollPane, 1000, 600);
		scene.getStylesheets().add(this.getClass().getResource("/style.css").toString());

		stage.setTitle("POM Updater GUI [by FoelliX.de]");
		stage.getIcons()
				.add(new Image(this.getClass().getResource("/images/fx_icon_16.png").toString(), 16, 16, false, true));
		stage.getIcons()
				.add(new Image(this.getClass().getResource("/images/fx_icon_32.png").toString(), 32, 32, false, true));
		stage.getIcons()
				.add(new Image(this.getClass().getResource("/images/fx_icon_64.png").toString(), 64, 64, false, true));
		stage.setScene(scene);
		stage.show();

		started = true;

		new Thread(() -> {
			while (map == null) {
				try {
					Thread.sleep(500);
				} catch (final InterruptedException e) {
					System.err.println("Error while waiting for data to be loaded!" + Helper.getExceptionAppendix(e));
				}
			}
			Platform.runLater(() -> load());
		}).start();
	}

	private void load() {
		Label label = new Label("Plugins:");
		label.setFont(BOLD);
		this.pane.add(label, 0, this.row);
		this.pane.add(new Label("Current"), 1, this.row);
		this.pane.add(new Label("Release/Stable"), 2, this.row);
		this.pane.add(new Label("Latest"), 3, this.row);
		this.pane.add(new Label("Update to:"), 4, this.row);
		this.row++;
		addElements(map.get(IOHandler.PLUGINS));

		Separator separator = new Separator();
		separator.setPadding(new Insets(7, 0, 5, 0));
		this.pane.add(separator, 0, this.row, 5, 1);
		this.row++;

		label = new Label("Dependencies:");
		label.setFont(BOLD);
		this.pane.add(label, 0, this.row);
		this.pane.add(new Label("Current"), 1, this.row);
		this.pane.add(new Label("Release/Stable"), 2, this.row);
		this.pane.add(new Label("Latest"), 3, this.row);
		this.pane.add(new Label("Update to:"), 4, this.row);
		this.row++;
		addElements(map.get(IOHandler.DEPENDENCIES));

		separator = new Separator();
		separator.setPadding(new Insets(7, 0, 5, 0));
		this.pane.add(separator, 0, this.row, 5, 1);
		this.row++;

		label = new Label("Variables:");
		label.setFont(BOLD);
		this.pane.add(label, 0, this.row);
		this.pane.add(new Label("Current"), 1, this.row);
		this.pane.add(new Label("Latest"), 3, this.row);
		this.pane.add(new Label("Update to:"), 4, this.row);
		this.row++;
		addVariables();

		separator = new Separator();
		separator.setPadding(new Insets(7, 0, 5, 0));
		this.pane.add(separator, 0, this.row, 5, 1);
		this.row++;

		Font.loadFont(this.getClass().getResource("/FontAwesome.otf").toString(), 15);
		final Button saveBtn = new Button("\uf0c7\tSave & Close");
		saveBtn.getStyleClass().add("defaultBtn");
		saveBtn.setStyle("-fx-font-family: FontAwesome; -fx-font-size: 15px;");
		HBox.setHgrow(saveBtn, Priority.ALWAYS);
		saveBtn.setMaxWidth(Double.MAX_VALUE);
		saveBtn.setOnAction((e) -> IOHandler.getInstance().save());
		this.pane.add(saveBtn, 0, this.row, 5, 1);

		final ColumnConstraints column1 = new ColumnConstraints();
		column1.setPercentWidth(41);
		final ColumnConstraints column2 = new ColumnConstraints();
		column2.setPercentWidth(13);
		final ColumnConstraints column3 = new ColumnConstraints();
		column3.setPercentWidth(20);
		this.pane.getColumnConstraints().addAll(column1, column2, column2, column2, column3);
	}

	private void addElements(List<VersionedPOMElement> list) {
		for (final VersionedPOMElement element : list) {
			this.pane.add(new Label(element.getIdentifier()), 0, this.row);
			this.pane.add(new Label(element.getVersion()), 1, this.row);
			this.pane.add(new Label(element.getRelease()), 2, this.row);
			this.pane.add(new Label(element.getLatest()), 3, this.row);
			final List<String> versions = element.getVersions();
			for (final String variable : IOHandler.getInstance().getVariables().keySet()) {
				versions.add("${" + variable + "}");
			}
			final ChoiceBox<String> choiceBox = new ChoiceBox<>(FXCollections.observableArrayList(versions));
			choiceBox.setValue(element.getVersion());
			choiceBox.setOnAction((e) -> changedVersion(e, element));
			this.pane.add(choiceBox, 4, this.row);
			HBox.setHgrow(choiceBox, Priority.ALWAYS);
			choiceBox.setMaxWidth(Double.MAX_VALUE);
			this.row++;
		}
	}

	private void addVariables() {
		for (final VariableElement variable : IOHandler.getInstance().getVariables().values()) {
			this.pane.add(new Label(variable.getVariable()), 0, this.row);
			this.pane.add(new Label(variable.getVersion()), 1, this.row);
			final List<String> versions = new LinkedList<>();
			for (final VersionedPOMElement element : map.get(IOHandler.PLUGINS)) {
				if (Helper.stripVariableName(element.getVersion()).equals(variable.getVariable())) {
					if (versions.isEmpty()) {
						versions.addAll(element.getVersions());
					} else {
						versions.retainAll(element.getVersions());
					}
				}
			}
			for (final VersionedPOMElement element : map.get(IOHandler.DEPENDENCIES)) {
				if (Helper.stripVariableName(element.getVersion()).equals(variable.getVariable())) {
					if (versions.isEmpty()) {
						versions.addAll(element.getVersions());
					} else {
						versions.retainAll(element.getVersions());
					}
				}
			}
			versions.remove("${" + variable.getVariable() + "}");
			final ChoiceBox<String> choiceBox = new ChoiceBox<>(FXCollections.observableArrayList(versions));
			if (!versions.isEmpty()) {
				this.pane.add(new Label(versions.get(0)), 3, this.row);
			} else {
				choiceBox.setDisable(true);
			}
			choiceBox.setValue(variable.getVersion());
			choiceBox.setOnAction((e) -> changedVersion(e, variable));
			this.pane.add(choiceBox, 4, this.row);
			HBox.setHgrow(choiceBox, Priority.ALWAYS);
			choiceBox.setMaxWidth(Double.MAX_VALUE);
			this.row++;
		}
	}

	private void changedVersion(ActionEvent e, IElement variable) {
		@SuppressWarnings("unchecked")
		final String newVersion = ((ChoiceBox<String>) e.getSource()).getSelectionModel().getSelectedItem();
		variable.updateVersion(newVersion);
	}
}