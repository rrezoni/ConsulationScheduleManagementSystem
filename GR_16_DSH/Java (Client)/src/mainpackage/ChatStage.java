package mainpackage;

import java.sql.Date;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javafx.stage.Stage;

public class ChatStage extends Stage {

	public ChatStage(String othersUsername) {
		super();

		init(othersUsername);
	}

	private void init(String othersUsername) {

		try {

			User otherUser = null;

			TCPClient.pw.println("{\"type\":\"UserInfoRequest\",\"username\":\"" + othersUsername + "\"}");
			String response = TCPClient.br.readLine();

			JsonObject json = new Gson().fromJson(response, JsonObject.class);

			if (json.get("user").getAsString().equals("Student")) {
				JsonObject userinfo = json.get("userinfo").getAsJsonObject();
				otherUser = new Student(userinfo.get("sid").getAsString(), userinfo.get("sfirstname").getAsString(),
						userinfo.get("slastname").getAsString(), userinfo.get("susername").getAsString(),
						userinfo.get("spassword").getAsString(), Date.valueOf(userinfo.get("sbirthdate").getAsString()),
						userinfo.get("sgender").getAsString(), userinfo.get("sdepartment").getAsString(),
						userinfo.get("syear").getAsString());
				setScene(new ChatScene(this, otherUser));
				setResizable(false);
				setTitle("Chat");
			} else if (json.get("user").getAsString().equals("Professor")) {
				JsonObject userinfo = json.get("userinfo").getAsJsonObject();
				otherUser = new Professor(userinfo.get("pid").getAsString(), userinfo.get("pfirstname").getAsString(),
						userinfo.get("plastname").getAsString(), userinfo.get("pusername").getAsString(),
						userinfo.get("ppassword").getAsString(), Date.valueOf(userinfo.get("pbirthdate").getAsString()),
						userinfo.get("pgender").getAsString());
				setScene(new ChatScene(this, otherUser));
				setResizable(false);
				setTitle("Chat");
			} else {
				this.close();
			}

		} catch (Exception e1) {
			e1.printStackTrace();
			this.close();
		}

	}

}
