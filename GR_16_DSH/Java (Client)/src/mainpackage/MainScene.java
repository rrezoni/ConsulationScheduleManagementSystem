package mainpackage;

import java.net.http.WebSocket;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainScene extends Scene {

	public static HashMap<String, ChatStage> chatStages;
	public static SettingsStage settingsStage;
	public static ConsultationAddingStage consultationAddingStage;

	public MainScene(Stage primaryStage) {
		super(new Pane(), 960, 640);

		init(primaryStage);
	}

	@SuppressWarnings("unchecked")
	private void init(Stage primaryStage) {

		TCPClient.initSocket();
		WebSocketClient.initWebSocket();

		getStylesheets().add(getClass().getResource("style.css").toExternalForm());

		chatStages = new HashMap<String, ChatStage>();

		BorderPane mainBorderPane = new BorderPane();
		mainBorderPane.getStyleClass().add("background");

		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().addAll(new Menu("File"), new Menu("Edit"), new Menu("Help"));
		mainBorderPane.setTop(menuBar);

		BorderPane leftBorderPane = new BorderPane();
		leftBorderPane.setId("mainSceneLeftBorderPane");
		mainBorderPane.setLeft(leftBorderPane);
		leftBorderPane.setOnMouseEntered(e -> {
			double currentWidth = leftBorderPane.getWidth();
			new Transition() {
				{
					setCycleDuration(Duration.millis(250));
					setInterpolator(Interpolator.EASE_BOTH);
				}

				@Override
				protected void interpolate(double frac) {
					leftBorderPane.setPrefWidth(currentWidth + (200 - currentWidth) * frac);
				}
			}.play();
		});
		leftBorderPane.setOnMouseExited(e -> {
			double currentWidth = leftBorderPane.getWidth();
			new Transition() {
				{
					setCycleDuration(Duration.millis(250));
					setInterpolator(Interpolator.EASE_BOTH);
				}

				@Override
				protected void interpolate(double frac) {
					leftBorderPane.setPrefWidth(100 + (currentWidth - 100) * (1 - frac));
				}
			}.play();
		});

		VBox leftBorderPaneTopVBox = new VBox();
		leftBorderPaneTopVBox.setId("mainSceneLeftBorderPaneTopVBox");
		leftBorderPane.setTop(leftBorderPaneTopVBox);

		ImageView profileImageView = new ImageView(
				new Image(getClass().getResource("Default_Profile_Picture.png").toExternalForm(), 80, 80, true, true));
		leftBorderPaneTopVBox.getChildren().add(profileImageView);

		Text usersNameText = new Text(LoggedUser.firstName);
		usersNameText.setId("mainSceneUsersNameText");
		leftBorderPaneTopVBox.getChildren().add(usersNameText);

		VBox leftBorderPaneCenterVBox = new VBox();
		leftBorderPaneCenterVBox.setId("mainSceneLeftBorderPaneCenterVBox");
		leftBorderPane.setCenter(leftBorderPaneCenterVBox);

		Text otherUsersText = new Text("Online (0)");
		otherUsersText.setStyle("-fx-font-size: small; -fx-fill: #000000af;");
		leftBorderPaneCenterVBox.getChildren().add(otherUsersText);

		ListView<String> otherUsersListView = new ListView<String>();
		otherUsersListView.setId("mainSceneOtherUsersListView");
		otherUsersListView.setOnMouseClicked(e -> {
			if (e.getButton().equals(MouseButton.PRIMARY))
				if (e.getClickCount() == 2)
					if (!otherUsersListView.getSelectionModel().isEmpty())
						if (!chatStages.containsKey(otherUsersListView.getSelectionModel().getSelectedItem()))
							chatStages.put(otherUsersListView.getSelectionModel().getSelectedItem(),
									new ChatStage(otherUsersListView.getSelectionModel().getSelectedItem()) {
										{
											show();
										}
									});
		});
		leftBorderPaneCenterVBox.getChildren().add(otherUsersListView);

		Timer onlineUsersTimer = new Timer();
		TimerTask onlineUsersTimerTask = new TimerTask() {

			@Override
			public void run() {

				Platform.runLater(() -> {
					if (WebSocketClient.onlineUsers.size() != 0) {
						otherUsersListView.getItems().clear();
						otherUsersText.setText("Online (" + WebSocketClient.onlineUsers.size() + ')');

						for (String username : WebSocketClient.onlineUsers)
							otherUsersListView.getItems().add(username);

					}
				});
			}
		};

		VBox leftBorderPaneBottomVBox = new VBox();
		leftBorderPaneBottomVBox.setId("mainSceneLeftBorderPaneBottomVBox");
		leftBorderPane.setBottom(leftBorderPaneBottomVBox);

		Button settingsButton = new Button("Settings");
		leftBorderPaneBottomVBox.getChildren().add(settingsButton);
		settingsButton.setOnAction(e -> {
			if (!SettingsStage.isOpen) {
				settingsStage = new SettingsStage();
				settingsStage.show();
			}
		});

		Button logoutButton = new Button("Log Out");
		leftBorderPaneBottomVBox.getChildren().add(logoutButton);
		logoutButton.setOnAction(e -> {
			try {
				settingsStage.close();
			} catch (Exception e1) {
			}
			onlineUsersTimerTask.cancel();
			onlineUsersTimer.cancel();
			onlineUsersTimer.purge();
			WebSocketClient.ws.sendClose(WebSocket.NORMAL_CLOSURE, "Logged Out")
					.thenRun(() -> System.out.println("Logged out")).join();
			LoggedUser.clear();
			WebSocketClient.close();
			UDPClient.close();
			for (String key : chatStages.keySet()) {
				chatStages.get(key).close();
			}
			chatStages.clear();
			chatStages = null;
			try {
				settingsStage.close();
				settingsStage = null;
				consultationAddingStage.close();
				consultationAddingStage = null;
			} catch (Exception e1) {
			}
			primaryStage.setScene(new LoginScene(primaryStage));
			System.gc();
		});

		BorderPane centerBorderPane = new BorderPane();
		centerBorderPane.setId("mainSceneCenterBorderPane");
		mainBorderPane.setCenter(centerBorderPane);

		centerBorderPane.setCenter(getConsultationsTableView());

		BorderPane optionsBorderPane = new BorderPane();
		optionsBorderPane.setId("mainSceneOptionsBorderPane");
		centerBorderPane.setTop(optionsBorderPane);

		if (LoggedUser.user == "Professor") {
			HBox leftOptionsHBox = new HBox();
			optionsBorderPane.setLeft(leftOptionsHBox);

			Button addButton = new Button("Add");
			addButton.setId("mainSceneAddButton");
			leftOptionsHBox.getChildren().add(addButton);
			addButton.setOnAction(e -> {
				if (!ConsultationAddingStage.isOpen) {
					consultationAddingStage = new ConsultationAddingStage();
					consultationAddingStage.show();
				}
			});

			Button deleteButton = new Button("Delete");
			deleteButton.setId("mainSceneDeleteButton");
			leftOptionsHBox.getChildren().add(deleteButton);
			deleteButton.setOnAction(e -> {
				if (!((TableView<Consultation>) centerBorderPane.getCenter()).getSelectionModel().isEmpty()
						&& LoggedUser.id.equals(((TableView<Consultation>) centerBorderPane.getCenter())
								.getSelectionModel().getSelectedItem().getProfId()))
					try {

						TCPClient.pw.println("{\"type\":\"DeleteConsultation\",\"cid\":"
								+ ((TableView<Consultation>) centerBorderPane.getCenter()).getSelectionModel()
										.getSelectedItem().getConsId()
								+ ",\"pid\":\""
								+ ((TableView<Consultation>) centerBorderPane.getCenter()).getSelectionModel()
										.getSelectedItem().getProfId()
								+ "\",\"subid\":" + ((TableView<Consultation>) centerBorderPane.getCenter())
										.getSelectionModel().getSelectedItem().getSubId()
								+ "}");
						String response = TCPClient.br.readLine();
						JsonObject json = new Gson().fromJson(response, JsonObject.class);

						if (json.get("description").getAsString().equals("Failed"))
							new Alert(AlertType.ERROR) {
								{
									setHeaderText("Error");
									setContentText("An error happened on the server side.");
								}
							}.show();
						else if (json.get("description").getAsString().equals("Successful"))
							((TableView<Consultation>) centerBorderPane.getCenter()).getItems()
									.remove(((TableView<Consultation>) centerBorderPane.getCenter()).getSelectionModel()
											.getSelectedIndex());

					} catch (Exception e1) {
						e1.printStackTrace();
					}
				else
					new Alert(AlertType.INFORMATION) {
						{
							setHeaderText("Delete Information");
							setContentText("You can only delete your consultations.");
						}
					}.show();
			});
		}

		HBox rightOptionsHBox = new HBox();
		optionsBorderPane.setRight(rightOptionsHBox);

		Button refreshButton = new Button("Refresh");
		rightOptionsHBox.getChildren().add(refreshButton);
		refreshButton.setOnAction(e -> {
			centerBorderPane.setCenter(getConsultationsTableView());
		});

		HBox bottomHBox = new HBox();
		bottomHBox.getStyleClass().add("bottomHBox");
		Text copyrightText = new Text("Copyright © 2020 Fiek - All Rights Reserved");
		copyrightText.getStyleClass().add("copyrightText");
		bottomHBox.getChildren().add(copyrightText);
		mainBorderPane.setBottom(bottomHBox);

		primaryStage.setOnCloseRequest(e -> {
			onlineUsersTimerTask.cancel();
			onlineUsersTimer.cancel();
			onlineUsersTimer.purge();
			WebSocketClient.ws.sendClose(WebSocket.NORMAL_CLOSURE, "Logged Out")
					.thenRun(() -> System.out.println("Logged out")).join();
			LoggedUser.clear();
			WebSocketClient.close();
			UDPClient.close();
			for (String key : chatStages.keySet()) {
				chatStages.get(key).close();
			}
			chatStages.clear();
			chatStages = null;
			primaryStage.setScene(new LoginScene(primaryStage));
			settingsStage.close();
			settingsStage = null;
			consultationAddingStage.close();
			consultationAddingStage = null;
			System.gc();
			Platform.exit();
			System.exit(0);
		});

		primaryStage.setX((Screen.getPrimary().getVisualBounds().getWidth() - 960) / 2);
		primaryStage.setY((Screen.getPrimary().getVisualBounds().getHeight() - 640) / 2);

		setRoot(mainBorderPane);

		onlineUsersTimer.scheduleAtFixedRate(onlineUsersTimerTask, 0, 5000);

	}

	private TableView<Consultation> getConsultationsTableView() {

		TableView<Consultation> consultationsTableView = new TableView<Consultation>();
		consultationsTableView.setId("mainSceneConsultationsTableView");
		if (consultationsTableView.getItems().isEmpty())
			consultationsTableView.setPlaceholder(new StackPane() {
				{
					setStyle("-fx-background-color: #ffffffaf;");
					getChildren().add(new Label("No Consultations") {
						{
							setStyle("-fx-text-fill: #000000af");
						}
					});
				}
			});

		TableColumn<Consultation, String> profFullNameTableColumn = new TableColumn<Consultation, String>(
				"Professor Name");
		profFullNameTableColumn.setCellValueFactory(new PropertyValueFactory<Consultation, String>("profFullName"));
		consultationsTableView.getColumns().add(profFullNameTableColumn);

		TableColumn<Consultation, String> subNameTableColumn = new TableColumn<Consultation, String>("Subject Name");
		subNameTableColumn.setCellValueFactory(new PropertyValueFactory<Consultation, String>("subName"));
		consultationsTableView.getColumns().add(subNameTableColumn);

		TableColumn<Consultation, Timestamp> consStartTableColumn = new TableColumn<Consultation, Timestamp>(
				"Consultation Start");
		consStartTableColumn.setCellValueFactory(new PropertyValueFactory<Consultation, Timestamp>("consStart"));
		consultationsTableView.getColumns().add(consStartTableColumn);

		TableColumn<Consultation, Timestamp> consEndTableColumn = new TableColumn<Consultation, Timestamp>(
				"Consultation End");
		consEndTableColumn.setCellValueFactory(new PropertyValueFactory<Consultation, Timestamp>("consEnd"));
		consultationsTableView.getColumns().add(consEndTableColumn);

		TableColumn<Consultation, String> subYearTableColumn = new TableColumn<Consultation, String>("Subject Year");
		subYearTableColumn.setCellValueFactory(new PropertyValueFactory<Consultation, String>("subYear"));
		consultationsTableView.getColumns().add(subYearTableColumn);

		TableColumn<Consultation, String> descriptionTableColumn = new TableColumn<Consultation, String>("Description");
		descriptionTableColumn.setCellValueFactory(new PropertyValueFactory<Consultation, String>("description"));
		consultationsTableView.getColumns().add(descriptionTableColumn);

		try {

			TCPClient.pw.println("{\"type\":\"ConsultationsTableRequest\"}");
			String response = TCPClient.br.readLine();
			JsonObject json = new Gson().fromJson(response, JsonObject.class);
			JsonArray consultations = json.get("consultations").getAsJsonArray();
			for (int i = 0; i < consultations.size(); i++) {
				JsonObject temp = consultations.get(i).getAsJsonObject();
				consultationsTableView.getItems()
						.add(new Consultation(temp.get("cid").getAsInt(), temp.get("pid").getAsString(),
								temp.get("subid").getAsInt(), Timestamp.valueOf(temp.get("cstart").getAsString()),
								Timestamp.valueOf(temp.get("cend").getAsString()),
								temp.get("cdescription").getAsString(),
								temp.get("pfirstname").getAsString() + ' ' + temp.get("plastname").getAsString(),
								temp.get("subname").getAsString(), temp.get("subyear").getAsString()));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return consultationsTableView;
	}

}
