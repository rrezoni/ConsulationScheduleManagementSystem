package mainpackage;

import java.net.http.WebSocket;
import java.sql.Date;
import java.sql.Timestamp;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class AdminMainScene extends Scene {

	public AdminMainScene(Stage primaryStage) {
		super(new Pane(), 480, 480);

		init(primaryStage);
	}

	private void init(Stage primaryStage) {

		BorderPane mainBorderPane = new BorderPane();
		setRoot(mainBorderPane);

		primaryStage.setX((Screen.getPrimary().getVisualBounds().getWidth() - 480) / 2);
		primaryStage.setY((Screen.getPrimary().getVisualBounds().getHeight() - 480) / 2);

		MenuBar menuBar = new MenuBar();
		mainBorderPane.setTop(menuBar);
		menuBar.getMenus().addAll(new Menu("File"), new Menu("Edit"), new Menu("Help"));

		TabPane mainTabPane = new TabPane();
		mainTabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		mainBorderPane.setCenter(mainTabPane);
		mainTabPane.getTabs().addAll(getRequestsTab(), getStudentsTab(), getProfessorsTab(), getSubjectsTab(),
				getProfessorsSubjects(), getConsultationsTab());

		HBox bottomHBox = new HBox();
		bottomHBox.setStyle("-fx-padding: 10px; -fx-alignment: center-left;");
		mainBorderPane.setBottom(bottomHBox);

		Button logoutButton = new Button("Log Out");
		bottomHBox.getChildren().add(logoutButton);
		logoutButton.setOnAction(e -> {
			WebSocketClient.ws.sendClose(WebSocket.NORMAL_CLOSURE, "Logged Out")
					.thenRun(() -> System.out.println("Logged out")).join();
			primaryStage.setScene(new LoginScene(primaryStage));
			System.gc();
		});

	}

	private Tab getRequestsTab() {

		Tab requestsTab = new Tab("Requests");

		BorderPane requestsBorderPane = new BorderPane();
		requestsTab.setContent(requestsBorderPane);

		TableView<AccountRequest> requestsTableView = new TableView<AccountRequest>();
		requestsBorderPane.setCenter(requestsTableView);

		TableColumn<AccountRequest, String> idTableColumn = new TableColumn<AccountRequest, String>("ID");
		idTableColumn.setCellValueFactory(new PropertyValueFactory<AccountRequest, String>("id"));
		requestsTableView.getColumns().add(idTableColumn);

		TableColumn<AccountRequest, String> sidTableColumn = new TableColumn<AccountRequest, String>("Student ID");
		sidTableColumn.setCellValueFactory(new PropertyValueFactory<AccountRequest, String>("sid"));
		requestsTableView.getColumns().add(sidTableColumn);

		TableColumn<AccountRequest, String> firstNameTableColumn = new TableColumn<AccountRequest, String>(
				"First Name");
		firstNameTableColumn.setCellValueFactory(new PropertyValueFactory<AccountRequest, String>("firstName"));
		requestsTableView.getColumns().add(firstNameTableColumn);

		TableColumn<AccountRequest, String> lastNameTableColumn = new TableColumn<AccountRequest, String>("Last Name");
		lastNameTableColumn.setCellValueFactory(new PropertyValueFactory<AccountRequest, String>("lastName"));
		requestsTableView.getColumns().add(lastNameTableColumn);

		TableColumn<AccountRequest, String> usernameTableColumn = new TableColumn<AccountRequest, String>("Username");
		usernameTableColumn.setCellValueFactory(new PropertyValueFactory<AccountRequest, String>("username"));
		requestsTableView.getColumns().add(usernameTableColumn);

		TableColumn<AccountRequest, String> genderTableColumn = new TableColumn<AccountRequest, String>("Gender");
		genderTableColumn.setCellValueFactory(new PropertyValueFactory<AccountRequest, String>("gender"));
		requestsTableView.getColumns().add(genderTableColumn);

		TableColumn<AccountRequest, Date> birthDateTableColumn = new TableColumn<AccountRequest, Date>("Birthdate");
		birthDateTableColumn.setCellValueFactory(new PropertyValueFactory<AccountRequest, Date>("birthDate"));
		requestsTableView.getColumns().add(birthDateTableColumn);

		TableColumn<AccountRequest, String> departmentTableColumn = new TableColumn<AccountRequest, String>(
				"Department");
		departmentTableColumn.setCellValueFactory(new PropertyValueFactory<AccountRequest, String>("department"));
		requestsTableView.getColumns().add(departmentTableColumn);

		TableColumn<AccountRequest, String> studyYearTableColumn = new TableColumn<AccountRequest, String>(
				"Study Year");
		studyYearTableColumn.setCellValueFactory(new PropertyValueFactory<AccountRequest, String>("studyYear"));
		requestsTableView.getColumns().add(studyYearTableColumn);

		HBox bottomHBox = new HBox();
		bottomHBox.setStyle("-fx-padding: 10px; -fx-spacing: 10px; -fx-alignment: center;");
		requestsBorderPane.setBottom(bottomHBox);

		Button declineButton = new Button("Decline");
		bottomHBox.getChildren().add(declineButton);
		declineButton.setOnAction(e -> {
			if (!requestsTableView.getSelectionModel().isEmpty())
				try {
					TCPClient.pw.println("{\"type\":\"DeclineAccountRequest\",\"id\":\""
							+ requestsTableView.getSelectionModel().getSelectedItem().getId() + "\"}");
					String response = TCPClient.br.readLine();

					JsonObject json = new Gson().fromJson(response, JsonObject.class);

					if (json.get("description").getAsString().equals("Successful"))
						requestsTableView.getItems().remove(requestsTableView.getSelectionModel().getSelectedIndex());

				} catch (Exception e1) {
					e1.printStackTrace();
				}
		});

		Button acceptButton = new Button("Accept");
		bottomHBox.getChildren().add(acceptButton);
		acceptButton.setOnAction(e -> {
			if (!requestsTableView.getSelectionModel().isEmpty())
				try {

					TCPClient.pw.println("{\"type\":\"AcceptAccountRequest\",\"rid\":\""
							+ requestsTableView.getSelectionModel().getSelectedItem().getId() + "\"}");
					String response = TCPClient.br.readLine();

					JsonObject json = new Gson().fromJson(response, JsonObject.class);

					if (json.get("description").getAsString().equals("Successful"))
						requestsTableView.getItems().remove(requestsTableView.getSelectionModel().getSelectedIndex());

				} catch (Exception e1) {
					e1.printStackTrace();
				}
		});

		try {

			TCPClient.pw.println("{\"type\":\"AccountRequestTableRequest\"}");
			String response = TCPClient.br.readLine();

			JsonObject json = new Gson().fromJson(response, JsonObject.class);
			JsonArray accountRequests = json.get("accountrequests").getAsJsonArray();

			for (int i = 0; i < accountRequests.size(); i++) {
				JsonObject accReq = accountRequests.get(i).getAsJsonObject();
				requestsTableView.getItems().add(new AccountRequest(accReq.get("rfirstname").getAsString(),
						accReq.get("rlastname").getAsString(), accReq.get("rid").getAsInt(),
						accReq.get("rsid").getAsString(), accReq.get("rusername").getAsString(),
						accReq.get("rgender").getAsString(), Date.valueOf(accReq.get("rbirthdate").getAsString()),
						accReq.get("rdepartment").getAsString(), accReq.get("ryear").getAsString()));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return requestsTab;
	}

	private Tab getStudentsTab() {

		Tab studentsTab = new Tab("Students");

		BorderPane studentsBorderPane = new BorderPane();
		studentsTab.setContent(studentsBorderPane);

		TableView<Student> studentsTableView = new TableView<Student>();
		studentsBorderPane.setCenter(studentsTableView);

		TableColumn<Student, String> idTableColumn = new TableColumn<Student, String>("ID");
		idTableColumn.setCellValueFactory(new PropertyValueFactory<Student, String>("id"));
		studentsTableView.getColumns().add(idTableColumn);

		TableColumn<Student, String> firstNameTableColumn = new TableColumn<Student, String>("First Name");
		firstNameTableColumn.setCellValueFactory(new PropertyValueFactory<Student, String>("firstName"));
		studentsTableView.getColumns().add(firstNameTableColumn);

		TableColumn<Student, String> lastNameTableColumn = new TableColumn<Student, String>("Last Name");
		lastNameTableColumn.setCellValueFactory(new PropertyValueFactory<Student, String>("lastName"));
		studentsTableView.getColumns().add(lastNameTableColumn);

		TableColumn<Student, String> usernameTableColumn = new TableColumn<Student, String>("Username");
		usernameTableColumn.setCellValueFactory(new PropertyValueFactory<Student, String>("username"));
		studentsTableView.getColumns().add(usernameTableColumn);

		TableColumn<Student, String> genderTableColumn = new TableColumn<Student, String>("Gender");
		genderTableColumn.setCellValueFactory(new PropertyValueFactory<Student, String>("gender"));
		studentsTableView.getColumns().add(genderTableColumn);

		TableColumn<Student, Date> birthDateTableColumn = new TableColumn<Student, Date>("Birthdate");
		birthDateTableColumn.setCellValueFactory(new PropertyValueFactory<Student, Date>("birthDate"));
		studentsTableView.getColumns().add(birthDateTableColumn);

		TableColumn<Student, String> departmentTableColumn = new TableColumn<Student, String>("Department");
		departmentTableColumn.setCellValueFactory(new PropertyValueFactory<Student, String>("department"));
		studentsTableView.getColumns().add(departmentTableColumn);

		TableColumn<Student, String> studyYearTableColumn = new TableColumn<Student, String>("Study Year");
		studyYearTableColumn.setCellValueFactory(new PropertyValueFactory<Student, String>("studyYear"));
		studentsTableView.getColumns().add(studyYearTableColumn);

		HBox bottomHBox = new HBox();
		bottomHBox.setStyle("-fx-padding: 10px; -fx-spacing: 10px; -fx-alignment: center");
		studentsBorderPane.setBottom(bottomHBox);

		Button deleteButton = new Button("Delete");
		bottomHBox.getChildren().add(deleteButton);
		deleteButton.setOnAction(e -> {
			if (!studentsTableView.getSelectionModel().isEmpty()) {
				try {

					TCPClient.pw.println("{\"type\":\"DeleteStudentRequest\",\"sid\":\""
							+ studentsTableView.getSelectionModel().getSelectedItem().getId() + "\"}");
					String response = TCPClient.br.readLine();

					JsonObject json = new Gson().fromJson(response, JsonObject.class);

					if (json.get("description").getAsString().equals("Successful"))
						studentsTableView.getItems().remove(studentsTableView.getSelectionModel().getSelectedIndex());

				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		try {

			TCPClient.pw.println("{\"type\":\"StudentTableRequest\"}");
			String response = TCPClient.br.readLine();

			JsonObject json = new Gson().fromJson(response, JsonObject.class);
			JsonArray students = json.get("students").getAsJsonArray();

			if (json.get("description").getAsString().equals("Successful"))
				for (int i = 0; i < students.size(); i++) {
					JsonObject s = students.get(i).getAsJsonObject();
					studentsTableView.getItems()
							.add(new Student(s.get("sid").getAsString(), s.get("sfirstname").getAsString(),
									s.get("slastname").getAsString(), s.get("susername").getAsString(),
									s.get("spassword").getAsString(), Date.valueOf(s.get("sbirthdate").getAsString()),
									s.get("sgender").getAsString(), s.get("sdepartment").getAsString(),
									s.get("syear").getAsString()));
				}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return studentsTab;
	}

	private Tab getProfessorsTab() {

		Tab professorsTab = new Tab("Professors");

		BorderPane professorsBorderPane = new BorderPane();
		professorsTab.setContent(professorsBorderPane);

		TableView<Professor> professorsTableView = new TableView<Professor>();
		professorsBorderPane.setCenter(professorsTableView);

		TableColumn<Professor, String> idTableColumn = new TableColumn<Professor, String>("ID");
		idTableColumn.setCellValueFactory(new PropertyValueFactory<Professor, String>("id"));
		professorsTableView.getColumns().add(idTableColumn);

		TableColumn<Professor, String> firstNameTableColumn = new TableColumn<Professor, String>("First Name");
		firstNameTableColumn.setCellValueFactory(new PropertyValueFactory<Professor, String>("firstName"));
		professorsTableView.getColumns().add(firstNameTableColumn);

		TableColumn<Professor, String> lastNameTableColumn = new TableColumn<Professor, String>("Last Name");
		lastNameTableColumn.setCellValueFactory(new PropertyValueFactory<Professor, String>("lastName"));
		professorsTableView.getColumns().add(lastNameTableColumn);

		TableColumn<Professor, String> usernameTableColumn = new TableColumn<Professor, String>("Username");
		usernameTableColumn.setCellValueFactory(new PropertyValueFactory<Professor, String>("username"));
		professorsTableView.getColumns().add(usernameTableColumn);

		TableColumn<Professor, String> passwordTableColumn = new TableColumn<Professor, String>("Password");
		passwordTableColumn.setCellValueFactory(new PropertyValueFactory<Professor, String>("password"));
		professorsTableView.getColumns().add(passwordTableColumn);

		TableColumn<Professor, Date> birthDateTableColumn = new TableColumn<Professor, Date>("Birthdate");
		birthDateTableColumn.setCellValueFactory(new PropertyValueFactory<Professor, Date>("birthDate"));
		professorsTableView.getColumns().add(birthDateTableColumn);

		TableColumn<Professor, String> genderTableColumn = new TableColumn<Professor, String>("Gender");
		genderTableColumn.setCellValueFactory(new PropertyValueFactory<Professor, String>("gender"));
		professorsTableView.getColumns().add(genderTableColumn);

		GridPane bottomGridPane = new GridPane();
		bottomGridPane.setStyle("-fx-vgap: 10px; -fx-hgap: 10px; -fx-alignment: center; -fx-padding: 10px;");
		professorsBorderPane.setBottom(bottomGridPane);

		TextField idTextField = new TextField();
		idTextField.setPromptText("ID");
		bottomGridPane.add(idTextField, 0, 0);

		TextField firstNameTextField = new TextField();
		firstNameTextField.setPromptText("First Name");
		bottomGridPane.add(firstNameTextField, 1, 0);

		TextField lastNameTextField = new TextField();
		lastNameTextField.setPromptText("Last Name");
		bottomGridPane.add(lastNameTextField, 2, 0);

		TextField usernameTextField = new TextField();
		usernameTextField.setPromptText("Username");
		bottomGridPane.add(usernameTextField, 0, 1);

		PasswordField passwordField = new PasswordField();
		passwordField.setPromptText("Password");
		bottomGridPane.add(passwordField, 1, 1);

		DatePicker birthDatePicker = new DatePicker();
		birthDatePicker.setPromptText("Birthdate");
		bottomGridPane.add(birthDatePicker, 2, 1);

		TextField genderTextField = new TextField();
		genderTextField.setPromptText("Gender");
		bottomGridPane.add(genderTextField, 0, 2);

		Button addButton = new Button("Add");
		bottomGridPane.add(addButton, 1, 2);
		addButton.setOnAction(e -> {
			try {

				TCPClient.pw.println("{\"type\":\"AddProfessor\",\"id\":\"" + idTextField.getText()
						+ "\",\"firstname\":\"" + firstNameTextField.getText() + "\",\"lastname\":\""
						+ lastNameTextField.getText() + "\",\"username\":\"" + usernameTextField.getText()
						+ "\",\"password\":\"" + passwordField.getText() + "\",\"birthdate\":\""
						+ birthDatePicker.getValue().toString() + "\",\"gender\":\"" + genderTextField.getText()
						+ "\"}");
				String response = TCPClient.br.readLine();

				JsonObject json = new Gson().fromJson(response, JsonObject.class);

				if (json.get("description").getAsString().equals("Successful")) {
					professorsTableView.getItems()
							.add(new Professor(idTextField.getText(), firstNameTextField.getText(),
									lastNameTextField.getText(), usernameTextField.getText(), passwordField.getText(),
									Date.valueOf(birthDatePicker.getValue().toString()), genderTextField.getText()));
					idTextField.clear();
					firstNameTextField.clear();
					lastNameTextField.clear();
					usernameTextField.clear();
					passwordField.clear();
					genderTextField.clear();
				}

			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});

		Button deleteButton = new Button("Delete");
		bottomGridPane.add(deleteButton, 2, 2);
		deleteButton.setOnAction(e -> {
			if (!professorsTableView.getSelectionModel().isEmpty()) {
				try {

					TCPClient.pw.println("{\"type\":\"DeleteProfessorRequest\",\"pid\":\""
							+ professorsTableView.getSelectionModel().getSelectedItem().getId() + "\"}");
					String response = TCPClient.br.readLine();

					JsonObject json = new Gson().fromJson(response, JsonObject.class);

					if (json.get("description").getAsString().equals("Successful"))
						professorsTableView.getItems()
								.remove(professorsTableView.getSelectionModel().getSelectedIndex());

				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		try {

			TCPClient.pw.println("{\"type\":\"ProfessorTableRequest\"}");
			String response = TCPClient.br.readLine();

			JsonObject json = new Gson().fromJson(response, JsonObject.class);
			JsonArray professors = json.get("professors").getAsJsonArray();

			if (json.get("description").getAsString().equals("Successful"))
				for (int i = 0; i < professors.size(); i++) {
					JsonObject professor = professors.get(i).getAsJsonObject();
					professorsTableView.getItems()
							.add(new Professor(professor.get("pid").getAsString(),
									professor.get("pfirstname").getAsString(), professor.get("plastname").getAsString(),
									professor.get("pusername").getAsString(), professor.get("ppassword").getAsString(),
									Date.valueOf(professor.get("pbirthdate").getAsString()),
									professor.get("pgender").getAsString()));
				}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return professorsTab;
	}

	private Tab getSubjectsTab() {

		Tab subjectsTab = new Tab("Subjects");

		BorderPane subjectsBorderPane = new BorderPane();
		subjectsTab.setContent(subjectsBorderPane);

		TableView<Subject> subjectsTableView = new TableView<Subject>();
		subjectsBorderPane.setCenter(subjectsTableView);

		TableColumn<Subject, Integer> idTableColumn = new TableColumn<Subject, Integer>("ID");
		idTableColumn.setCellValueFactory(new PropertyValueFactory<Subject, Integer>("id"));
		subjectsTableView.getColumns().add(idTableColumn);

		TableColumn<Subject, String> nameTableColumn = new TableColumn<Subject, String>("Subject Name");
		nameTableColumn.setCellValueFactory(new PropertyValueFactory<Subject, String>("name"));
		subjectsTableView.getColumns().add(nameTableColumn);

		TableColumn<Subject, String> yearTableColumn = new TableColumn<Subject, String>("Year");
		yearTableColumn.setCellValueFactory(new PropertyValueFactory<Subject, String>("year"));
		subjectsTableView.getColumns().add(yearTableColumn);

		HBox bottomHBox = new HBox();
		bottomHBox.setStyle("-fx-padding: 10px; -fx-spacing: 10px; -fx-alignment: center;");
		subjectsBorderPane.setBottom(bottomHBox);

		TextField nameTextField = new TextField();
		nameTextField.setPromptText("Name");
		bottomHBox.getChildren().add(nameTextField);

		ComboBox<String> yearComboBox = new ComboBox<String>();
		yearComboBox.setPromptText("Year");
		yearComboBox.getItems().addAll("First", "Second", "Third");
		bottomHBox.getChildren().add(yearComboBox);

		Button addButton = new Button("Add");
		bottomHBox.getChildren().add(addButton);
		addButton.setOnAction(e -> {
			try {

				TCPClient.pw.println("{\"type\":\"AddSubjectRequest\",\"subname\":\"" + nameTextField.getText()
						+ "\",\"subyear\":\"" + yearComboBox.getValue() + "\"}");
				String response = TCPClient.br.readLine();

				JsonObject json = new Gson().fromJson(response, JsonObject.class);

				if (json.get("description").getAsString().equals("Successful")) {

					JsonObject s = json.get("subject").getAsJsonObject();

					subjectsTableView.getItems().add(new Subject(s.get("subid").getAsInt(),
							s.get("subname").getAsString(), s.get("subyear").getAsString()));

					nameTextField.clear();
					yearComboBox.setPromptText("Year");
				}

			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});

		Button deleteButton = new Button("Delete");
		bottomHBox.getChildren().add(deleteButton);
		deleteButton.setOnAction(e -> {
			if (!subjectsTableView.getSelectionModel().isEmpty()) {
				try {

					TCPClient.pw.println("{\"type\":\"DeleteSubjectRequest\",\"subid\":\""
							+ subjectsTableView.getSelectionModel().getSelectedItem().getId() + "\"}");
					String response = TCPClient.br.readLine();

					JsonObject json = new Gson().fromJson(response, JsonObject.class);

					if (json.get("description").getAsString().equals("Successful"))
						subjectsTableView.getItems().remove(subjectsTableView.getSelectionModel().getSelectedIndex());

				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		try {

			TCPClient.pw.println("{\"type\":\"SubjectTableRequest\"}");
			String response = TCPClient.br.readLine();

			JsonObject json = new Gson().fromJson(response, JsonObject.class);
			JsonArray subjects = json.get("subjects").getAsJsonArray();

			if (json.get("description").getAsString().equals("Successful"))
				for (int i = 0; i < subjects.size(); i++) {
					JsonObject s = subjects.get(i).getAsJsonObject();
					subjectsTableView.getItems().add(new Subject(s.get("subid").getAsInt(),
							s.get("subname").getAsString(), s.get("subyear").getAsString()));
				}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return subjectsTab;
	}

	private Tab getProfessorsSubjects() {

		Tab professorsSubjectsTab = new Tab("Professors Subjects");

		BorderPane professorsSubjectsBorderPane = new BorderPane();
		professorsSubjectsTab.setContent(professorsSubjectsBorderPane);

		TableView<ProfessorSubject> professorsSubjectsTableView = new TableView<ProfessorSubject>();
		professorsSubjectsBorderPane.setCenter(professorsSubjectsTableView);

		TableColumn<ProfessorSubject, Integer> subIdTableColumn = new TableColumn<ProfessorSubject, Integer>(
				"Subject ID");
		subIdTableColumn.setCellValueFactory(new PropertyValueFactory<ProfessorSubject, Integer>("subId"));
		professorsSubjectsTableView.getColumns().add(subIdTableColumn);

		TableColumn<ProfessorSubject, String> pIdTableColumn = new TableColumn<ProfessorSubject, String>(
				"Professor ID");
		pIdTableColumn.setCellValueFactory(new PropertyValueFactory<ProfessorSubject, String>("profId"));
		professorsSubjectsTableView.getColumns().add(pIdTableColumn);

		HBox bottomHBox = new HBox();
		bottomHBox.setStyle("-fx-padding: 10px; -fx-spacing: 10px; -fx-alignment: center;");
		professorsSubjectsBorderPane.setBottom(bottomHBox);

		TextField subIdTextField = new TextField();
		subIdTextField.setPromptText("Subject ID");
		bottomHBox.getChildren().add(subIdTextField);

		TextField pIdTextField = new TextField();
		pIdTextField.setPromptText("Professor ID");
		bottomHBox.getChildren().add(pIdTextField);

		Button addButon = new Button("Add");
		bottomHBox.getChildren().add(addButon);
		addButon.setOnAction(e -> {
			try {

				TCPClient.pw.println("{\"type\":\"AddProfessorSubjectRequest\",\"subid\":"
						+ Integer.parseInt(subIdTextField.getText()) + ",\"pid\":\"" + pIdTextField.getText() + "\"}");
				String response = TCPClient.br.readLine();

				JsonObject json = new Gson().fromJson(response, JsonObject.class);

				if (json.get("description").getAsString().equals("Successful"))
					professorsSubjectsTableView.getItems().add(
							new ProfessorSubject(Integer.parseInt(subIdTextField.getText()), pIdTextField.getText()));

			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});

		Button deleteButton = new Button("Delete");
		bottomHBox.getChildren().add(deleteButton);
		deleteButton.setOnAction(e -> {
			if (!professorsSubjectsTableView.getSelectionModel().isEmpty()) {
				try {

					TCPClient.pw.println("{\"type\":\"DeleteProfessorSubjectRequest\",\"subid\":"
							+ professorsSubjectsTableView.getSelectionModel().getSelectedItem().getSubId()
							+ ",\"pid\":\""
							+ professorsSubjectsTableView.getSelectionModel().getSelectedItem().getProfId() + "\"}");
					String response = TCPClient.br.readLine();

					JsonObject json = new Gson().fromJson(response, JsonObject.class);

					if (json.get("description").getAsString().equals("Successful"))
						professorsSubjectsTableView.getItems()
								.remove(professorsSubjectsTableView.getSelectionModel().getSelectedIndex());
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		try {

			TCPClient.pw.println("{\"type\":\"ProfessorSubjectTableRequest\"}");
			String response = TCPClient.br.readLine();

			JsonObject json = new Gson().fromJson(response, JsonObject.class);
			JsonArray professorSubjects = json.get("professorsubjects").getAsJsonArray();

			if (json.get("description").getAsString().equals("Successful"))
				for (int i = 0; i < professorSubjects.size(); i++) {
					JsonObject profSub = professorSubjects.get(i).getAsJsonObject();
					professorsSubjectsTableView.getItems().add(
							new ProfessorSubject(profSub.get("subid").getAsInt(), profSub.get("pid").getAsString()));
				}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return professorsSubjectsTab;
	}

	private Tab getConsultationsTab() {

		Tab consultationsTab = new Tab("Consultations");

		BorderPane consultationsBorderPane = new BorderPane();
		consultationsTab.setContent(consultationsBorderPane);

		TableView<Consultation> consultationsTableView = new TableView<Consultation>();
		consultationsBorderPane.setCenter(consultationsTableView);

		TableColumn<Consultation, Integer> cIdTableColumn = new TableColumn<Consultation, Integer>("Consultation ID");
		cIdTableColumn.setCellValueFactory(new PropertyValueFactory<Consultation, Integer>("consId"));
		consultationsTableView.getColumns().add(cIdTableColumn);

		TableColumn<Consultation, Integer> profIdTableColumn = new TableColumn<Consultation, Integer>("Professor ID");
		profIdTableColumn.setCellValueFactory(new PropertyValueFactory<Consultation, Integer>("profId"));
		consultationsTableView.getColumns().add(profIdTableColumn);

		TableColumn<Consultation, Integer> subIdTableColumn = new TableColumn<Consultation, Integer>("Subject ID");
		subIdTableColumn.setCellValueFactory(new PropertyValueFactory<Consultation, Integer>("subId"));
		consultationsTableView.getColumns().add(subIdTableColumn);

		TableColumn<Consultation, Timestamp> consStartTableColumn = new TableColumn<Consultation, Timestamp>(
				"Consultation Start");
		consStartTableColumn.setCellValueFactory(new PropertyValueFactory<Consultation, Timestamp>("consStart"));
		consultationsTableView.getColumns().add(consStartTableColumn);

		TableColumn<Consultation, Timestamp> consEndTableColumn = new TableColumn<Consultation, Timestamp>(
				"Consultation End");
		consEndTableColumn.setCellValueFactory(new PropertyValueFactory<Consultation, Timestamp>("consEnd"));
		consultationsTableView.getColumns().add(consEndTableColumn);

		HBox bottomHBox = new HBox();
		bottomHBox.setStyle("-fx-padding: 10px; -fx-spacing: 10px; -fx-alignment: center;");
		consultationsBorderPane.setBottom(bottomHBox);

		Button deleteButton = new Button("Delete");
		bottomHBox.getChildren().add(deleteButton);
		deleteButton.setOnAction(e -> {
			if (!consultationsTableView.getSelectionModel().isEmpty()) {
				try {

					TCPClient.pw.println("{\"type\":\"DeleteConsultationRequest\",\"cid\":"
							+ consultationsTableView.getSelectionModel().getSelectedItem().getConsId() + "}");
					String response = TCPClient.br.readLine();

					JsonObject json = new Gson().fromJson(response, JsonObject.class);

					if (json.get("description").getAsString().equals("Successful"))
						consultationsTableView.getItems()
								.remove(consultationsTableView.getSelectionModel().getSelectedIndex());

				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		try {

			TCPClient.pw.println("{\"type\":\"ConsultationTableRequest\"}");
			String response = TCPClient.br.readLine();

			JsonObject json = new Gson().fromJson(response, JsonObject.class);
			JsonArray consultations = json.get("consultations").getAsJsonArray();

			if (json.get("description").getAsString().equals("Successful"))
				for (int i = 0; i < consultations.size(); i++) {
					JsonObject c = consultations.get(i).getAsJsonObject();
					consultationsTableView.getItems()
							.add(new Consultation(c.get("cid").getAsInt(), c.get("pid").getAsString(),
									c.get("subid").getAsInt(), Timestamp.valueOf(c.get("cstart").getAsString()),
									Timestamp.valueOf(c.get("cend").getAsString()),
									c.get("cdescription").getAsString()));
				}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return consultationsTab;
	}

}
