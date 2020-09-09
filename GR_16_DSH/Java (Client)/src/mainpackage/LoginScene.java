package mainpackage;

import java.sql.Date;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class LoginScene extends Scene {

	public LoginScene(Stage primaryStage) {
		super(new BorderPane(), 320, 640);

		init(primaryStage);
	}

	private void init(Stage primaryStage) {

		TCPClient.initSocket();

		getStylesheets().add(getClass().getResource("style.css").toExternalForm());

		BorderPane mainBorderPane = new BorderPane();
		mainBorderPane.getStyleClass().add("background");
		setRoot(mainBorderPane);

		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().addAll(new Menu("File"), new Menu("Edit"), new Menu("Help"));
		mainBorderPane.setTop(menuBar);

		primaryStage.setX((Screen.getPrimary().getVisualBounds().getWidth() - 320) / 2);
		primaryStage.setY((Screen.getPrimary().getVisualBounds().getHeight() - 640) / 2);

		GridPane loginGridPane = new GridPane();
		loginGridPane.setId("loginGridPane");
		mainBorderPane.setCenter(loginGridPane);

		HBox profileImageHBox = new HBox();
		profileImageHBox.setId("profileImageHBox");
		ImageView profileImageView = new ImageView(new Image(
				getClass().getResource("Default_Profile_Picture.png").toExternalForm(), 100, 100, true, true));
		profileImageHBox.getChildren().add(profileImageView);
		loginGridPane.add(profileImageHBox, 0, 0, 2, 1);

		Text welcomeText = new Text("Welcome");
		welcomeText.setId("welcomeText");
		loginGridPane.add(welcomeText, 0, 1, 2, 1);

		TextField usernameField = new TextField();
		usernameField.setId("usernameField");
		usernameField.getStyleClass().add("inputField");
		usernameField.setPromptText("Username");
		loginGridPane.add(usernameField, 0, 2, 2, 1);

		PasswordField passwordField = new PasswordField();
		passwordField.setId("passwordField");
		passwordField.getStyleClass().add("inputField");
		passwordField.setPromptText("Password");
		loginGridPane.add(passwordField, 0, 3, 2, 1);
		passwordField.setOnKeyPressed(e -> {
			if (e.getCode().equals(KeyCode.ENTER)) {
				login(primaryStage, usernameField.getText(), passwordField.getText());
				System.gc();
			}
		});

		HBox loginButtonHBox = new HBox();
		loginButtonHBox.setId("loginButtonHBox");
		Button loginButton = new Button("Log In");
		loginButton.setId("loginButton");
		loginButtonHBox.getChildren().add(loginButton);
		loginGridPane.add(loginButtonHBox, 0, 4, 2, 1);
		loginButton.setOnAction(e -> {
			login(primaryStage, usernameField.getText(), passwordField.getText());
			System.gc();
		});

		Label signupLabel = new Label("Don't have an account?");
		signupLabel.setTextFill(Paint.valueOf("#000000af"));
		loginGridPane.add(signupLabel, 0, 5, 1, 1);

		HBox signupButtonHBox = new HBox();
		signupButtonHBox.setId("signupButtonHBox");

		Button signupButton = new Button("Sign Up");
		signupButton.setId("signupButton");
		signupButtonHBox.getChildren().add(signupButton);
		loginGridPane.add(signupButtonHBox, 1, 5, 1, 1);
		signupButton.setOnAction(e -> {
			primaryStage.setScene(new SignupScene(primaryStage));
			System.gc();
		});

		HBox bottomHBox = new HBox();
		bottomHBox.getStyleClass().add("bottomHBox");

		Text copyrightText = new Text("Copyright © 2020 Fiek - All Rights Reserved");
		copyrightText.getStyleClass().add("copyrightText");
		bottomHBox.getChildren().add(copyrightText);
		mainBorderPane.setBottom(bottomHBox);

	}

	private void login(Stage primaryStage, String username, String password) {
		if (username.isBlank() || password.isBlank())
			new Alert(AlertType.ERROR) {
				{
					setResizable(false);
					setHeaderText("Fields not filled!");
					setContentText("Please fill all fields.");
				}
			}.show();

		else {

			try {
				TCPClient.pw.println("{\"type\":\"LoginRequest\",\"username\":\"" + username + "\",\"password\":\""
						+ password + "\"}");
				String response = TCPClient.br.readLine();
				JsonObject json = new Gson().fromJson(response, JsonObject.class);

				if (json.get("type").getAsString().equals("LoginResponse")
						&& json.get("description").getAsString().equals("Successful")) {
					JsonObject info = json.get("info").getAsJsonObject();
					if (json.get("user").getAsString().equals("Student")) {
						LoggedUser.setStudent(info.get("sid").getAsString(), info.get("sfirstname").getAsString(),
								info.get("slastname").getAsString(),
								Date.valueOf(info.get("sbirthdate").getAsString().substring(0, 10)),
								info.get("sgender").getAsString(), info.get("susername").getAsString());
						primaryStage.setScene(new MainScene(primaryStage));
					} else if (json.get("user").getAsString().equals("Professor")) {
						LoggedUser.setProfessor(info.get("pid").getAsString(), info.get("pfirstname").getAsString(),
								info.get("plastname").getAsString(),
								Date.valueOf(info.get("pbirthdate").getAsString().substring(0, 10)),
								info.get("pgender").getAsString(), info.get("pusername").getAsString());
						primaryStage.setScene(new MainScene(primaryStage));
					} else if (json.get("user").getAsString().equals("Admin")) {
						LoggedUser.setAdmin(info.get("aid").getAsString(), info.get("afirstname").getAsString(),
								info.get("alastname").getAsString(), info.get("ausername").getAsString());
						primaryStage.setScene(new AdminMainScene(primaryStage));
					}
					System.gc();
				} else {
					new Alert(AlertType.ERROR) {
						{
							setHeaderText("Wrong Username/ID or Password!");
							setContentText("Please check your username/id and password.");
							setResizable(false);
						}
					}.show();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		primaryStage.setOnCloseRequest(e ->

		{
			System.gc();
			Platform.exit();
			System.exit(0);
		});

	}

}
