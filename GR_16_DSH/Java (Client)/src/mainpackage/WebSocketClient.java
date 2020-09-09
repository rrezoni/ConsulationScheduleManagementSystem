package mainpackage;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletionStage;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javafx.application.Platform;

public final class WebSocketClient {

	public static WebSocket ws = null;
	public static HashMap<String, HashMap<String, ArrayList<String>>> receivedDataHashMap;
	public static ArrayList<String> onlineUsers;

	public static void initWebSocket() {

		try {
			File confFile = new File(WebSocketClient.class.getResource("conf.xml").getPath());
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(confFile);
			doc.getDocumentElement().normalize();

			ws = HttpClient.newHttpClient().newWebSocketBuilder()
					.buildAsync(
							URI.create("ws://" + doc.getElementsByTagName("ip").item(0).getTextContent() + ':'
									+ doc.getElementsByTagName("port").item(0).getTextContent() + '/'),
							new WebSocket.Listener() {

								@Override
								public void onOpen(WebSocket webSocket) {

									receivedDataHashMap = new HashMap<String, HashMap<String, ArrayList<String>>>();
									receivedDataHashMap.put("TextData", new HashMap<String, ArrayList<String>>());

									onlineUsers = new ArrayList<String>();

									Listener.super.onOpen(webSocket);
								}

								@Override
								public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
									if (data.toString().contains("\"type\":\"TextData\"")) {
										TextData textData = new Gson().fromJson(data.toString(), TextData.class);
										if (!receivedDataHashMap.get("TextData").containsKey(textData.getSourceID()))
											receivedDataHashMap.get("TextData").put(textData.getSourceID(),
													new ArrayList<String>());
										receivedDataHashMap.get("TextData").get(textData.getSourceID())
												.add(textData.getText());
										String othersUsername = textData.getSourceUsername();
										if (!MainScene.chatStages.containsKey(othersUsername)
												&& !othersUsername.isBlank()) {
											Platform.runLater(() -> MainScene.chatStages.put(othersUsername,
													new ChatStage(othersUsername) {
														{
															show();
														}
													}));
										}
									} else if (data.toString().contains("\"type\":\"OnlineUsers\"")) {
										onlineUsers.clear();
										JsonObject onlineUsersJson = new Gson().fromJson(data.toString(),
												JsonObject.class);
										JsonArray onlineUsersJsonArray = onlineUsersJson.get("onlineUsers")
												.getAsJsonArray();
										for (int i = 0; i < onlineUsersJsonArray.size(); i++)
											onlineUsers.add(onlineUsersJsonArray.get(i).getAsString());
									} // else if (data.toString().contains("\"type\":\"VideoCallRequest\"")) {
//										System.out.println("Video Call");
//										new Alert(AlertType.CONFIRMATION) {
//											{
//												setTitle("Video Call");
//												setContentText("Someone is video calling you.");
//											}
//										}.show();
//									} else if (data.toString().contains("\"type\":\"VoiceCallRequest\"")) {
//										System.out.println("Voice Call");
//										new Alert(AlertType.CONFIRMATION) {
//											{
//												setTitle("Video Call");
//												setContentText("Someone is voice calling you.");
//											}
//										}.show();
//									}

									return Listener.super.onText(webSocket, data, last);
								}

							})
					.join();

			WebSocketClient.ws.sendText("{\"type\":\"Connect\",\"sourceID\":\"" + LoggedUser.id + "\"}", true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void close() {
		ws.abort();
		ws = null;
		receivedDataHashMap.clear();
		receivedDataHashMap = null;
		onlineUsers.clear();
		onlineUsers = null;
	}

}
