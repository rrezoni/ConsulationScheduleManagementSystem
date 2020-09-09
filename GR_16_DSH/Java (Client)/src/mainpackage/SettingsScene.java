package mainpackage;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Screen;

public class SettingsScene extends Scene {

	public SettingsScene(SettingsStage stage) {
		super(new Pane(), 320, 480);

		init(stage);
	}

	private void init(SettingsStage stage) {

		getStylesheets().add(getClass().getResource("style.css").toExternalForm());

		BorderPane mainBorderPane = new BorderPane();
		mainBorderPane.getStyleClass().add("background");

		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().addAll(new Menu("File"), new Menu("Edit"), new Menu("Help"));
		mainBorderPane.setTop(menuBar);

		GridPane centerGridPane = new GridPane();
		centerGridPane.setId("settingsSceneMainGridPane");
		mainBorderPane.setCenter(centerGridPane);

		HBox titleHBox = new HBox();
		titleHBox.setId("settingsSceneTitleHBox");
		centerGridPane.add(titleHBox, 0, 0, 2, 1);

		Text title = new Text("Settings");
		title.setId("settingsSceneTitleText");
		titleHBox.getChildren().add(title);

		PasswordField currentPasswordField = new PasswordField();
		currentPasswordField.getStyleClass().add("settingsSceneInputField");
		currentPasswordField.setStyle("-fx-pref-width: 200px;");
		currentPasswordField.setPromptText("Current Password");
		centerGridPane.add(currentPasswordField, 0, 1, 2, 1);

		TextField newUsernameTextField = new TextField();
		newUsernameTextField.getStyleClass().add("settingsSceneInputField");
		newUsernameTextField.setStyle("-fx-pref-width: 100px;");
		newUsernameTextField.setPromptText("New Username");
		centerGridPane.add(newUsernameTextField, 0, 2, 1, 1);

		Button changeUsernameButton = new Button("Change Username");
		centerGridPane.add(changeUsernameButton, 1, 2, 1, 1);
		changeUsernameButton.setOnAction(e -> {
			if (!currentPasswordField.getText().isBlank() && !newUsernameTextField.getText().isBlank())
				try {

					TCPClient.pw.println("{\"type\":\"ChangeUsernameRequest\",\"givenpassword\":\""
							+ currentPasswordField.getText() + "\",\"id\":\"" + LoggedUser.id + "\",\"newusername\":\""
							+ newUsernameTextField.getText() + "\",\"user\":\"" + LoggedUser.user + "\"}");
					String response = TCPClient.br.readLine();

					JsonObject json = new Gson().fromJson(response, JsonObject.class);

					if (json.get("description").getAsString().equals("Successful"))
						new Alert(AlertType.INFORMATION) {
							{
								setHeaderText("Success!");
								setContentText("Username was successfully changed.");
							}
						}.show();
					else if (json.get("description").getAsString().equals("WrongPassword"))
						new Alert(AlertType.ERROR) {
							{
								setHeaderText("Wrong Password!");
								setContentText("The given password is wrong.");
							}
						}.show();
					else if (json.get("description").getAsString().equals("Failed"))
						new Alert(AlertType.ERROR) {
							{
								setHeaderText("An Error Happened.");
								setContentText("The username maybe exists. Try another username.");
							}
						}.show();
						
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			else
				new Alert(AlertType.ERROR) {
					{
						setHeaderText("Field Error!");
						setContentText("Please fill all fields.");
					}
				}.show();
		});

		PasswordField newPasswordField = new PasswordField();
		newPasswordField.getStyleClass().add("settingsSceneInputField");
		newPasswordField.setStyle("-fx-pref-width: 100px;");
		newPasswordField.setPromptText("New Password");
		centerGridPane.add(newPasswordField, 0, 3, 1, 1);

		Button changePasswordButton = new Button("Change Password");
		centerGridPane.add(changePasswordButton, 1, 3, 1, 1);
		changePasswordButton.setOnAction(e -> {

			if (!currentPasswordField.getText().isBlank() && !newPasswordField.getText().isBlank())
				try {

					TCPClient.pw.println("{\"type\":\"ChangePasswordRequest\",\"givenpassword\":\""
							+ currentPasswordField.getText() + "\",\"id\":\"" + LoggedUser.id + "\",\"newpassword\":\""
							+ newPasswordField.getText() + "\",\"user\":\"" + LoggedUser.user + "\"}");
					String response = TCPClient.br.readLine();

					JsonObject json = new Gson().fromJson(response, JsonObject.class);

					if (json.get("description").getAsString().equals("Successful"))
						new Alert(AlertType.INFORMATION) {
							{
								setHeaderText("Success!");
								setContentText("Password was successfully changed.");
							}
						}.show();
					else if (json.get("description").getAsString().equals("WrongPassword"))
						new Alert(AlertType.ERROR) {
							{
								setHeaderText("Wrong Password!");
								setContentText("The given password is wrong.");
							}
						}.show();
					else if (json.get("description").getAsString().equals("Failed"))
						new Alert(AlertType.ERROR) {
							{
								setHeaderText("An Error Happened.");
								setContentText("Try again later.");
							}
						}.show();

				} catch (Exception e1) {
					e1.printStackTrace();
				}
			else
				new Alert(AlertType.ERROR) {
					{
						setHeaderText("Field Error!");
						setContentText("Please fill all fields.");
					}
				}.show();
		});

		Button doneButton = new Button("Done");
		centerGridPane.add(doneButton, 1, 4, 1, 1);
		doneButton.setOnAction(e -> {
			SettingsStage.isOpen = false;
			System.gc();
			stage.close();
		});

		stage.setOnCloseRequest(e -> {
			SettingsStage.isOpen = false;
			System.gc();
		});

		stage.setX((Screen.getPrimary().getVisualBounds().getWidth() - 320) / 2);
		stage.setY((Screen.getPrimary().getVisualBounds().getHeight() - 480) / 2);

		setRoot(mainBorderPane);

	}

}
