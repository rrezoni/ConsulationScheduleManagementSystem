package mainpackage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Base64;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.w3c.dom.Document;

import com.google.gson.Gson;

import javafx.animation.Interpolator;
import javafx.animation.Transition;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.util.Duration;

public class CallScene extends Scene {

	private TimerTask myCameraTask;
	private Thread microphoneThread;

	public CallScene(ChatStage chatStage, User otherUser) {
		super(new Pane(), 480, 640);

		try {
			init(chatStage, otherUser);
		} catch (Exception e) {
			e.printStackTrace();
			chatStage.close();
		}
	}

	private void init(ChatStage chatStage, User otherUser) throws Exception {

		UDPClient.initDatagramSocket();

		File confFile = new File(getClass().getResource("conf.xml").getPath());
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(confFile);
		doc.getDocumentElement().normalize();

		String serverIP = doc.getElementsByTagName("ip").item(0).getTextContent();
		int serverPort = Integer.parseInt(doc.getElementsByTagName("port").item(0).getTextContent());

		getStylesheets().add(getClass().getResource("style.css").toExternalForm());

		BorderPane mainBorderPane = new BorderPane();
		mainBorderPane.getStyleClass().add("background");

		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().addAll(new Menu("File"), new Menu("Edit"), new Menu("Help"));
		mainBorderPane.setTop(menuBar);

		BorderPane rightBorderPane = new BorderPane();
		rightBorderPane.setId("callSceneRightBorderPane");
		mainBorderPane.setRight(rightBorderPane);

		HBox rightBorderPaneTopHBox = new HBox();
		rightBorderPaneTopHBox.setId("callSceneRightBorderPaneTopHBox");
		rightBorderPane.setTop(rightBorderPaneTopHBox);

		ImageView profileImageView = new ImageView(
				new Image(getClass().getResource("Default_Profile_Picture.png").toExternalForm(), 60, 60, true, true));
		rightBorderPaneTopHBox.getChildren().add(profileImageView);

		Text otherUsersNameText = new Text(otherUser.getFirstName());
		otherUsersNameText.setId("callSceneOtherUsersNameText");
		rightBorderPaneTopHBox.getChildren().add(otherUsersNameText);

		HBox rightBorderPaneBottomHBox = new HBox();
		rightBorderPaneBottomHBox.setId("callSceneRightBorderPaneBottomHBox");
		rightBorderPane.setBottom(rightBorderPaneBottomHBox);

		VBox callScrollPaneVBox = new VBox();
		callScrollPaneVBox.setId("callSceneScrollPaneVBox");
		rightBorderPane.setCenter(callScrollPaneVBox);

		ChatScrollPane callScrollPane = new ChatScrollPane();
		callScrollPane.setId("callSceneScrollPane");
		callScrollPaneVBox.getChildren().add(callScrollPane);

		TextArea textArea = new TextArea();
		textArea.setId("callSceneTextArea");
		rightBorderPaneBottomHBox.getChildren().add(textArea);
		textArea.setOnKeyPressed(e -> {
			if (!textArea.getText().isBlank() && e.getCode().equals(KeyCode.ENTER)) {
				WebSocketClient.ws.sendText(
						new Gson().toJson(new TextData(textArea.getText().substring(0, textArea.getText().length() - 1),
								LoggedUser.id, otherUser.getId(), LoggedUser.username)),
						true);
				callScrollPane.addMyText(textArea.getText().substring(0, textArea.getText().length() - 1), true);
				textArea.clear();
			}
		});

		Button sendButton = new Button("Send");
		rightBorderPaneBottomHBox.getChildren().add(sendButton);
		sendButton.setOnAction(e -> {
			if (!textArea.getText().isBlank()) {
				WebSocketClient.ws.sendText(
						new Gson().toJson(new TextData(textArea.getText(), LoggedUser.id, otherUser.getId(), LoggedUser.username)), true);
				callScrollPane.addMyText(textArea.getText(), true);
				textArea.clear();
			}
		});

		BorderPane centerBorderPane = new BorderPane();
		mainBorderPane.setCenter(centerBorderPane);

		VBox centerBorderPaneCenterVBox = new VBox();
		centerBorderPaneCenterVBox.setId("callSceneCenterBorderPaneCenterVBox");
		centerBorderPane.setCenter(centerBorderPaneCenterVBox);

		ImageView othersCameraView = new ImageView();
		othersCameraView.setPreserveRatio(true);
		othersCameraView.setFitWidth(320);
		othersCameraView.setFitHeight(240);
		centerBorderPaneCenterVBox.getChildren().add(othersCameraView);

		ImageView myCameraView = new ImageView();
		myCameraView.setPreserveRatio(true);
		myCameraView.setFitWidth(320);
		myCameraView.setFitHeight(240);
		centerBorderPaneCenterVBox.getChildren().add(myCameraView);

		HBox centerBorderPaneBottomHBox = new HBox();
		centerBorderPaneBottomHBox.setId("callSceneCenterBorderPaneBottomHBox");
		centerBorderPane.setBottom(centerBorderPaneBottomHBox);

		Button stopMicButton = new Button();
		if (UDPClient.usingMic)
			stopMicButton.setText("Stop Mic");
		else
			stopMicButton.setText("Start Mic");
		centerBorderPaneBottomHBox.getChildren().add(stopMicButton);

		Button stopCallButton = new Button("Stop");
		centerBorderPaneBottomHBox.getChildren().add(stopCallButton);

		Button stopCameraButton = new Button();
		centerBorderPaneBottomHBox.getChildren().add(stopCameraButton);

		rightBorderPane.setOnMouseEntered(e -> {
			new Transition() {
				double currentPaneWidth = rightBorderPane.getWidth();
				double currentCameraViewWidth = myCameraView.getFitWidth();
				{
					setCycleDuration(Duration.millis(250));
					setInterpolator(Interpolator.EASE_BOTH);
				}

				@Override
				protected void interpolate(double frac) {
					rightBorderPane.setPrefWidth(currentPaneWidth + (240 - currentPaneWidth) * frac);
					myCameraView.setFitWidth(220 + (currentCameraViewWidth - 220) * (1 - frac));
					othersCameraView.setFitWidth(220 + (currentCameraViewWidth - 220) * (1 - frac));
				}

			}.play();
		});
		rightBorderPane.setOnMouseExited(e -> {
			new Transition() {
				double currentPaneWidth = rightBorderPane.getWidth();
				double currentCameraViewWidth = myCameraView.getFitWidth();
				{
					setCycleDuration(Duration.millis(250));
					setInterpolator(Interpolator.EASE_BOTH);
				}

				@Override
				protected void interpolate(double frac) {
					rightBorderPane.setPrefWidth(140 + (currentPaneWidth - 140) * (1 - frac));
					myCameraView.setFitWidth(currentCameraViewWidth + (320 - currentCameraViewWidth) * frac);
					othersCameraView.setFitWidth(currentCameraViewWidth + (320 - currentCameraViewWidth) * frac);
				}
			}.play();
		});

		Timer othersTextsTimer = new Timer();
		TimerTask othersTextsTimerTask = new TimerTask() {
			@Override
			public void run() {
				Platform.runLater(() -> {
					try {
						for (String text : WebSocketClient.receivedDataHashMap.get("TextData").get(otherUser.getId()))
							callScrollPane.addOthersText(text, true);
						WebSocketClient.receivedDataHashMap.get("TextData").get(otherUser.getId()).clear();
					} catch (Exception e) {
					}
				});
			}

		};

		AudioFormat format = new AudioFormat(8000.0f, 8, 1, true, true);
		DataLine.Info microphoneInfo = new DataLine.Info(TargetDataLine.class, format);
		TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(microphoneInfo);
		class MicrophoneRunnable implements Runnable {

			@Override
			public void run() {
				while (true) {
					if (!UDPClient.usingMic)
						break;
					try {
						int numOfBytesRead;
						byte[] bytesRead = new byte[microphone.getBufferSize() / 5];
						numOfBytesRead = microphone.read(bytesRead, 0, bytesRead.length);

						AudioData audioDataToSend = new AudioData(LoggedUser.id, otherUser.getId(), numOfBytesRead,
								Base64.getEncoder().encodeToString(bytesRead));
						byte[] jsonBytesToSend = new Gson().toJson(audioDataToSend).getBytes();
						UDPClient.ds.send(new DatagramPacket(jsonBytesToSend, jsonBytesToSend.length,
								InetAddress.getByName(serverIP), serverPort));

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}
		;
		microphoneThread = new Thread(new MicrophoneRunnable());
		microphone.open(format);
		microphone.start();

		DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, format);
		SourceDataLine speaker = (SourceDataLine) AudioSystem.getLine(speakerInfo);
		Thread speakerThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					if (!UDPClient.onACall)
						break;
					try {
						if (!UDPClient.receivedAudioData.isEmpty()) {
							AudioData audioData = new Gson().fromJson(UDPClient.receivedAudioData.get(0),
									AudioData.class);
							speaker.write(Base64.getDecoder().decode(audioData.getAudioBytes()), 0,
									audioData.getLength());
							UDPClient.receivedAudioData.remove(0);
						} else
							Thread.sleep(1);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		});
		speaker.open(format);
		speaker.start();

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		VideoCapture myCameraCapture = new VideoCapture();
		Timer myCameraTimer = new Timer();
		class MyCameraTimerTask extends TimerTask {

			@Override
			public void run() {
				try {

					Mat frameMat = new Mat();
					if (myCameraCapture.read(frameMat)) {
						Imgproc.resize(frameMat, frameMat, new Size(160, 120));
						Core.flip(frameMat, frameMat, 1);
						MatOfByte frameMatOfByte = new MatOfByte();
						Imgcodecs.imencode(".jpg", frameMat, frameMatOfByte);
						Image myCameraImage = new Image(new ByteArrayInputStream(frameMatOfByte.toArray()));
						myCameraView.setImage(myCameraImage);

						VideoFrame frameToSend = new VideoFrame(LoggedUser.id, otherUser.id,
								Base64.getEncoder().encodeToString(frameMatOfByte.toArray()));
						byte[] jsonBytesToSend = new Gson().toJson(frameToSend).getBytes();
						UDPClient.ds.send(new DatagramPacket(jsonBytesToSend, jsonBytesToSend.length,
								InetAddress.getByName(serverIP), serverPort));
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}

		Timer othersCameraTimer = new Timer();
		TimerTask othersCameraTask = new TimerTask() {
			@Override
			public void run() {
				Platform.runLater(() -> {
					try {
						if (UDPClient.receivedVideoData.size() > 5) {
							Image othersCameraImage = new Image(new ByteArrayInputStream(
									Base64.getDecoder().decode(UDPClient.receivedVideoData.removeLast().getBytes())));
							othersCameraView.setImage(othersCameraImage);
						}
					} catch (Exception e) {
					}
				});
			}
		};

		othersTextsTimer.scheduleAtFixedRate(othersTextsTimerTask, 0, 1000);

		microphoneThread.start();

		speakerThread.start();

		try {
			if (myCameraCapture.open(0) && UDPClient.usingCamera) {
				myCameraTask = new MyCameraTimerTask();
				myCameraTimer.scheduleAtFixedRate(myCameraTask, 0, 33);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		othersCameraTimer.scheduleAtFixedRate(othersCameraTask, 0, 33);

		if (UDPClient.usingCamera) {
			stopCameraButton.setText("Stop Cam");
		} else {
			myCameraView.setImage(new Image(getClass().getResource("Default_Profile_Picture.png").toExternalForm(), 320,
					320, true, true));
			othersCameraView.setImage(new Image(getClass().getResource("Default_Profile_Picture.png").toExternalForm(),
					320, 320, true, true));
			stopCameraButton.setText("Start Cam");
		}

		stopMicButton.setOnAction(e -> {
			UDPClient.usingMic = !UDPClient.usingMic;
			if (!UDPClient.usingMic) {
				microphone.stop();
				microphone.close();
				stopMicButton.setText("Unmute");
			} else {
				try {
					microphone.open(format);
					microphone.start();
					stopMicButton.setText("Mute");
					microphoneThread = new Thread(new MicrophoneRunnable());
					microphoneThread.start();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		stopCallButton.setOnAction(e -> {
			UDPClient.close();
			speaker.stop();
			speaker.close();
			microphone.stop();
			microphone.close();
			try {
				myCameraTask.cancel();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			myCameraTimer.cancel();
			myCameraCapture.release();
			othersTextsTimerTask.cancel();
			othersTextsTimer.cancel();
			othersTextsTimer.purge();
			othersCameraTask.cancel();
			othersCameraTimer.cancel();
			othersCameraTimer.purge();
			myCameraCapture.release();
			chatStage.setScene(new ChatScene(chatStage, otherUser));
			System.gc();
		});

		stopCameraButton.setOnAction(e -> {
			UDPClient.usingCamera = !UDPClient.usingCamera;
			if (!UDPClient.usingCamera) {
				myCameraTask.cancel();
				myCameraCapture.release();
				myCameraView.setImage(new Image(getClass().getResource("Default_Profile_Picture.png").toString(), 240,
						240, true, true));
				stopCameraButton.setText("Start Cam");
			} else {
				try {
					if (myCameraCapture.open(0)) {
						myCameraTask = new MyCameraTimerTask();
						myCameraTimer.scheduleAtFixedRate(myCameraTask, 0, 33);
						stopCameraButton.setText("Stop Cam");
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		chatStage.setOnCloseRequest(e -> {
			UDPClient.close();
			speaker.stop();
			speaker.close();
			microphone.stop();
			microphone.close();
			try {
				myCameraTask.cancel();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			myCameraTimer.cancel();
			myCameraCapture.release();
			othersTextsTimerTask.cancel();
			othersTextsTimer.cancel();
			othersTextsTimer.purge();
			othersCameraTimer.cancel();
			othersCameraTimer.purge();
			myCameraCapture.release();
			MainScene.chatStages.remove(otherUser.username);
			System.gc();
		});

		chatStage.setX((Screen.getPrimary().getVisualBounds().getWidth() - 480) / 2);
		chatStage.setY((Screen.getPrimary().getVisualBounds().getHeight() - 640) / 2);

		setRoot(mainBorderPane);

	}

}
