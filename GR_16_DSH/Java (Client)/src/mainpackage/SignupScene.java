package mainpackage;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class SignupScene extends Scene {

	public SignupScene(Stage primaryStage) {
		super(new Pane(), 320, 640);

		init(primaryStage);
	}

	private void init(Stage primaryStage) {

		getStylesheets().add(getClass().getResource("style.css").toExternalForm());

		BorderPane mainBorderPane = new BorderPane();
		mainBorderPane.getStyleClass().add("background");

		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().addAll(new Menu("File"), new Menu("Edit"), new Menu("Help"));
		mainBorderPane.setTop(menuBar);

		BorderPane centerBorderPane = new BorderPane();
		mainBorderPane.setCenter(centerBorderPane);

		VBox centerBorderPaneTopVBox = new VBox();
		centerBorderPaneTopVBox.setId("signupSceneCenterBorderPaneTopVBox");
		centerBorderPane.setTop(centerBorderPaneTopVBox);

		Button backButton = new Button("Back");
		centerBorderPaneTopVBox.getChildren().add(backButton);
		backButton.setOnAction(e -> {
			primaryStage.setScene(new LoginScene(primaryStage));
			System.gc();
		});

		GridPane mainGridPane = new GridPane();
		mainGridPane.setId("signupSceneMainGridPane");
		centerBorderPane.setCenter(mainGridPane);

		HBox titleHBox = new HBox();
		titleHBox.setId("signupSceneTitleHBox");
		mainGridPane.add(titleHBox, 0, 0, 2, 1);

		Text titleText = new Text("Request your Account");
		titleText.setId("signupSceneTitleText");
		titleHBox.getChildren().add(titleText);

		TextField firstNameTextField = new TextField();
		firstNameTextField.setPromptText("First Name");
		firstNameTextField.getStyleClass().add("inputField");
		mainGridPane.add(firstNameTextField, 0, 1, 2, 1);

		TextField lastNameTextField = new TextField();
		lastNameTextField.setPromptText("Last Name");
		lastNameTextField.getStyleClass().add("inputField");
		mainGridPane.add(lastNameTextField, 0, 2, 2, 1);

		mainGridPane.add(new Label("Birth Date:") {
			{
				setTextFill(Paint.valueOf("#000000af"));
			}
		}, 0, 3, 1, 1);

		DatePicker birthDatePicker = new DatePicker();
		mainGridPane.add(birthDatePicker, 1, 3, 1, 1);

		mainGridPane.add(new Label("Gender: ") {
			{
				setTextFill(Paint.valueOf("#000000af"));
			}
		}, 0, 4, 1, 1);

		HBox genderHBox = new HBox();
		genderHBox.setId("signupSceneGenderHBox");
		mainGridPane.add(genderHBox, 1, 4, 1, 1);

		ToggleGroup genderToggleGroup = new ToggleGroup();
		RadioButton femaleButton = new RadioButton("Female");
		genderHBox.getChildren().add(femaleButton);
		femaleButton.setToggleGroup(genderToggleGroup);
		RadioButton maleButton = new RadioButton("Male");
		genderHBox.getChildren().add(maleButton);
		maleButton.setToggleGroup(genderToggleGroup);

		TextField usernameTextField = new TextField();
		usernameTextField.setPromptText("Username");
		usernameTextField.getStyleClass().add("inputField");
		mainGridPane.add(usernameTextField, 0, 5, 2, 1);

		TextField idTextField = new TextField();
		idTextField.setPromptText("ID");
		idTextField.getStyleClass().add("inputField");
		mainGridPane.add(idTextField, 0, 6, 2, 1);

		TextField departmentTextField = new TextField();
		departmentTextField.setPromptText("Department");
		departmentTextField.getStyleClass().add("inputField");
		mainGridPane.add(departmentTextField, 0, 7, 2, 1);

		mainGridPane.add(new Label("Study Year:") {
			{
				setFill(Paint.valueOf("#000000af"));
			}
		}, 0, 8, 1, 1);

		ComboBox<String> studyYearComboBox = new ComboBox<String>();
		studyYearComboBox.setId("signupSceneStudyYeadComboBox");
		studyYearComboBox.getItems().addAll("First", "Second", "Third");
		mainGridPane.add(studyYearComboBox, 1, 8, 1, 1);

		PasswordField passwordField = new PasswordField();
		passwordField.setPromptText("Password");
		passwordField.getStyleClass().add("inputField");
		mainGridPane.add(passwordField, 0, 9, 2, 1);

		PasswordField confirmPasswordField = new PasswordField();
		confirmPasswordField.setPromptText("Confirm Password");
		confirmPasswordField.getStyleClass().add("inputField");
		mainGridPane.add(confirmPasswordField, 0, 10, 2, 1);

		HBox requestButtonHBox = new HBox();
		requestButtonHBox.setId("signupSceneRequestButtonHBox");
		mainGridPane.add(requestButtonHBox, 0, 11, 2, 1);

		Button requestButton = new Button("Request");
		requestButtonHBox.getChildren().add(requestButton);
		requestButton.setOnAction(e -> {

			if (firstNameTextField.getText().isBlank() || lastNameTextField.getText().isBlank()
					|| birthDatePicker.getValue() == null || genderToggleGroup.getSelectedToggle() == null
					|| usernameTextField.getText().isBlank() || idTextField.getText().isBlank()
					|| passwordField.getText().isBlank() || confirmPasswordField.getText().isBlank()
					|| !passwordField.getText().contentEquals(confirmPasswordField.getText()))

				new Alert(AlertType.ERROR) {
					{
						setResizable(false);
						setHeaderText("Fields not filled or passwords don't match!");
						setContentText("Please fill all fields and check passwords.");
					}
				}.show();

			else {

				try {
					TCPClient.pw.println("{\"type\":\"SignupRequest\",\"firstname\":\"" + firstNameTextField.getText()
							+ "\",\"lastname\":\"" + lastNameTextField.getText() + "\",\"birthdate\":\""
							+ birthDatePicker.getValue().toString() + "\",\"gender\":\""
							+ (genderToggleGroup.getSelectedToggle().toString() == "Female" ? 'F' : 'M')
							+ "\",\"username\":\"" + usernameTextField.getText() + "\",\"id\":\""
							+ idTextField.getText() + "\",\"password\":\"" + passwordField.getText()
							+ "\",\"studyyear\":\"" + studyYearComboBox.getSelectionModel().getSelectedItem()
							+ "\",\"department\":\"" + departmentTextField.getText() + "\"}");
					String response = TCPClient.br.readLine();

					JsonObject json = new Gson().fromJson(response, JsonObject.class);
					if (json.get("description").getAsString().equals("Successful")) {
						new Alert(AlertType.INFORMATION) {
							{
								setResizable(false);
								setHeaderText("Success!");
								setContentText("Account has been requested.");
							}
						}.show();
						primaryStage.setScene(new LoginScene(primaryStage));
						System.gc();
					} else
						new Alert(AlertType.ERROR) {
							{
								setResizable(false);
								setHeaderText("Failed!");
								setContentText("An error happened in the server.");
							}
						}.show();

				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}

		});

		HBox bottomHBox = new HBox();
		bottomHBox.getStyleClass().add("bottomHBox");

		Text copyrightText = new Text("Copyright © 2020 Fiek - All Rights Reserved");
		copyrightText.getStyleClass().add("copyrightText");
		bottomHBox.getChildren().add(copyrightText);
		mainBorderPane.setBottom(bottomHBox);

		setRoot(mainBorderPane);

	}

}
