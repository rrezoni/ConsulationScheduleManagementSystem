package mainpackage;

import java.sql.Timestamp;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Screen;

public class ConsultationAddingScene extends Scene {

	public ConsultationAddingScene(ConsultationAddingStage stage) {
		super(new Pane(), 320, 480);

		init(stage);
	}

	private void init(ConsultationAddingStage stage) {

		getStylesheets().add(getClass().getResource("style.css").toExternalForm());

		BorderPane mainBorderPane = new BorderPane();
		mainBorderPane.getStyleClass().add("background");

		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().addAll(new Menu("File"), new Menu("Edit"), new Menu("Help"));
		mainBorderPane.setTop(menuBar);

		GridPane centerGridPane = new GridPane();
		centerGridPane.setId("ConsultationAddingSceneCenterGridPane");
		mainBorderPane.setCenter(centerGridPane);

		Text title = new Text("Add Consultation");
		title.setStyle("-fx-font-size: 20.0px; -fx-fill: #ffffffaf;");
		centerGridPane.add(title, 0, 0, 2, 1);

		ComboBox<String> subjectComboBox = new ComboBox<String>();
		subjectComboBox.setPromptText("Subject Name");
		centerGridPane.add(subjectComboBox, 0, 1, 2, 1);
		try {

			TCPClient.pw.println("{\"type\":\"ProfessorSubjectNamesRequest\",\"pid\":\"" + LoggedUser.id + "\"}");
			String response = TCPClient.br.readLine();

			JsonObject json = new Gson().fromJson(response, JsonObject.class);

			if (json.get("description").getAsString().equals("Successful")) {
				JsonArray subjects = json.get("subjects").getAsJsonArray();
				for (int i = 0; i < subjects.size(); i++)
					subjectComboBox.getItems().add(subjects.get(i).getAsJsonObject().get("subname").getAsString());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		DatePicker startDatePicker = new DatePicker();
		startDatePicker.setPromptText("Start Date");
		centerGridPane.add(startDatePicker, 0, 2);

		TextField startTimePicker = new TextField();
		startTimePicker.getStyleClass().add("consultationAddingSceneTimePicker");
		startTimePicker.setPromptText("Start Time (hh:mm:ss)");
		centerGridPane.add(startTimePicker, 1, 2);

		DatePicker endDatePicker = new DatePicker();
		endDatePicker.setPromptText("End Date");
		centerGridPane.add(endDatePicker, 0, 3);

		TextField endTimePicker = new TextField();
		endTimePicker.getStyleClass().add("consultationAddingSceneTimePicker");
		endTimePicker.setPromptText("End Time (hh:mm:ss)");
		centerGridPane.add(endTimePicker, 1, 3);

		TextArea descriptionTextArea = new TextArea();
		descriptionTextArea.setId("consultationAddingSceneDescriptionArea");
		descriptionTextArea.setPromptText("Consultation description (optional)");
		centerGridPane.add(descriptionTextArea, 0, 4, 2, 1);

		Button addButton = new Button("Add");
		centerGridPane.add(addButton, 0, 5);
		addButton.setOnAction(e -> {
			try {
				if (!subjectComboBox.getValue().isBlank() && !startDatePicker.getValue().toString().isBlank()
						&& !endDatePicker.getValue().toString().isBlank() && !startTimePicker.getText().isBlank()
						&& !endTimePicker.getText().isBlank()
						&& !Timestamp.valueOf(startDatePicker.getValue().toString() + " " + startTimePicker.getText())
								.after(Timestamp.valueOf(
										endDatePicker.getValue().toString() + " " + endTimePicker.getText()))) {

					TCPClient.pw.println("{\"type\":\"AddConsultationRequest\",\"pid\":\"" + LoggedUser.id
							+ "\",\"subname\":\"" + subjectComboBox.getValue() + "\",\"cstart\":\""
							+ startDatePicker.getValue().toString() + ' ' + startTimePicker.getText() + "\",\"cend\":\""
							+ endDatePicker.getValue().toString() + ' ' + endTimePicker.getText()
							+ "\",\"description\":\"" + descriptionTextArea.getText() + "\"}");
					String response = TCPClient.br.readLine();

					JsonObject json = new Gson().fromJson(response, JsonObject.class);

					if (json.get("description").getAsString().equals("Successful"))
						new Alert(AlertType.INFORMATION) {
							{
								setHeaderText("Success!");
								setContentText("Consultation succesfully added.");
							}
						}.show();
					else if (json.get("description").getAsString().equals("ConsultationError"))
						new Alert(AlertType.ERROR) {
							{
								setHeaderText("Consultation Error");
								setContentText(
										"Consultation should be after today or \na consultation already exists at this time.");
							}
						}.show();
					else {
						new Alert(AlertType.ERROR) {
							{
								setHeaderText("Fields Error");
								setContentText("Fields are not properly filled.");
							}
						}.show();
					}
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});

		stage.setOnCloseRequest(e -> {
			ConsultationAddingStage.isOpen = false;
			System.gc();
		});

		stage.setX((Screen.getPrimary().getVisualBounds().getWidth() - 320) / 2);
		stage.setY((Screen.getPrimary().getVisualBounds().getHeight() - 480) / 2);

		setRoot(mainBorderPane);

	}

}
