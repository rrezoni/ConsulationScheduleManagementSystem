package mainpackage;

import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.Gson;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Screen;

public class ChatScene extends Scene {

	public ChatScene(ChatStage chatStage, User otherUser) {
		super(new Pane(), 480, 480);

		init(chatStage, otherUser);
	}

	private void init(ChatStage chatStage, User otherUser) {

		getStylesheets().add(getClass().getResource("style.css").toExternalForm());

		BorderPane mainBorderPane = new BorderPane();
		mainBorderPane.getStyleClass().add("background");

		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().addAll(new Menu("File"), new Menu("Edit"), new Menu("Help"));
		mainBorderPane.setTop(menuBar);

		VBox mainVBox = new VBox();
		mainVBox.setId("chatSceneMainVBox");
		mainBorderPane.setCenter(mainVBox);

		BorderPane topBorderPane = new BorderPane();
		topBorderPane.setId("chatSceneTopBorderPane");
		mainVBox.getChildren().add(topBorderPane);

		GridPane topBorderPaneLeftGridPane = new GridPane();
		topBorderPaneLeftGridPane.setId("chatSceneTopBorderPaneLeftGridPane");
		topBorderPane.setLeft(topBorderPaneLeftGridPane);

		ImageView profileImageView = new ImageView(
				new Image(getClass().getResource("Default_Profile_Picture.png").toExternalForm(), 60, 60, true, true));
		topBorderPaneLeftGridPane.add(profileImageView, 0, 0, 1, 2);

		Text otherUsersNameText = new Text(otherUser.getFirstName());
		otherUsersNameText.setId("chatSceneOtherUsersNameText");
		topBorderPaneLeftGridPane.add(otherUsersNameText, 1, 0);

		Text otherUsersEmailText = new Text(otherUser.getUsername() + ", " + otherUser.getId());
		otherUsersEmailText.setId("chatSceneOtherUsersEmailText");
		topBorderPaneLeftGridPane.add(otherUsersEmailText, 1, 1);

		HBox topBorderPaneRightHBox = new HBox();
		topBorderPaneRightHBox.setId("chatSceneTopBorderPaneRightHBox");
		topBorderPane.setRight(topBorderPaneRightHBox);

		Button voiceButton = new Button("");
		voiceButton.setId("chatSceneVoiceButton");
		topBorderPaneRightHBox.getChildren().add(voiceButton);

		Button cameraButton = new Button();
		cameraButton.setId("chatSceneCameraButton");
		topBorderPaneRightHBox.getChildren().add(cameraButton);

		VBox chatScrollPaneVBox = new VBox();
		chatScrollPaneVBox.setId("chatSceneScrollPaneVBox");
		mainVBox.getChildren().add(chatScrollPaneVBox);

		ChatScrollPane chatScrollPane = new ChatScrollPane();
		chatScrollPaneVBox.getChildren().add(chatScrollPane);

		HBox chatBottomHBox = new HBox();
		chatBottomHBox.setId("chatSceneBottomHBox");
		mainBorderPane.setBottom(chatBottomHBox);

		TextArea chatTextArea = new TextArea();
		chatTextArea.setId("chatSceneTextArea");
		chatBottomHBox.getChildren().add(chatTextArea);
		chatTextArea.setOnKeyPressed(e -> {
			if (!chatTextArea.getText().isBlank() && e.getCode().equals(KeyCode.ENTER)) {
				WebSocketClient.ws.sendText(new Gson()
						.toJson(new TextData(chatTextArea.getText().substring(0, chatTextArea.getText().length() - 1),
								LoggedUser.id, otherUser.getId(), LoggedUser.username)),
						true);
				chatScrollPane.addMyText(chatTextArea.getText().substring(0, chatTextArea.getText().length() - 1),
						false);
				chatTextArea.clear();
			}
		});

		Button sendButton = new Button("Send");
		chatBottomHBox.getChildren().add(sendButton);
		sendButton.setOnAction(e -> {
			if (!chatTextArea.getText().isBlank()) {
				WebSocketClient.ws.sendText(new Gson().toJson(
						new TextData(chatTextArea.getText(), LoggedUser.id, otherUser.getId(), LoggedUser.username)),
						true);
				chatScrollPane.addMyText(chatTextArea.getText(), false);
				chatTextArea.clear();
			}
		});

		Timer othersTextsTimer = new Timer();
		TimerTask othersTextsTimerTask = new TimerTask() {
			@Override
			public void run() {
				Platform.runLater(() -> {
					try {
						for (String text : WebSocketClient.receivedDataHashMap.get("TextData").get(otherUser.getId()))
							chatScrollPane.addOthersText(text, false);
						WebSocketClient.receivedDataHashMap.get("TextData").get(otherUser.getId()).clear();
					} catch (Exception e) {
					}
				});
			}

		};
		othersTextsTimer.scheduleAtFixedRate(othersTextsTimerTask, 0, 1000);

		voiceButton.setOnAction(e -> {
//			WebSocketClient.ws.sendText("{\"type\":\"VoiceCallRequest\",\"sourceID\":\"" + LoggedUser.id
//					+ "\",\"targetID\":\"" + otherUser.getId() + "\"}", true);
			if (!UDPClient.onACall) {
				UDPClient.usingCamera = false;
				UDPClient.usingMic = true;
				othersTextsTimerTask.cancel();
				othersTextsTimer.cancel();
				othersTextsTimer.purge();
				chatStage.setScene(new CallScene(chatStage, otherUser));
				System.gc();
			}
		});

		cameraButton.setOnAction(e -> {
//			WebSocketClient.ws.sendText("{\"type\":\"VideoCallRequest\",\"sourceID\":\"" + LoggedUser.id
//					+ "\",\"targetID\":\"" + otherUser.getId() + "\"}", true);
			if (!UDPClient.onACall) {
				UDPClient.usingCamera = true;
				UDPClient.usingMic = true;
				othersTextsTimerTask.cancel();
				othersTextsTimer.cancel();
				othersTextsTimer.purge();
				chatStage.setScene(new CallScene(chatStage, otherUser));
				System.gc();
			}
		});

		chatStage.setOnCloseRequest(e -> {
			othersTextsTimerTask.cancel();
			othersTextsTimer.cancel();
			othersTextsTimer.purge();
			MainScene.chatStages.remove(otherUser.username);
			System.gc();
		});

		chatStage.setX((Screen.getPrimary().getVisualBounds().getWidth() - 480) / 2);
		chatStage.setY((Screen.getPrimary().getVisualBounds().getHeight() - 480) / 2);

		setRoot(mainBorderPane);

	}

}
