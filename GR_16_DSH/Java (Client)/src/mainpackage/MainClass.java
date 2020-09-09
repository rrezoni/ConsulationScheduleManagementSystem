package mainpackage;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class MainClass extends Application {

	public static void main(String[] args) {
		
		launch(args);

	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		primaryStage.setScene(new LoginScene(primaryStage));
		primaryStage.setResizable(false);
		primaryStage.setOnCloseRequest(e -> {
			Platform.exit();
			System.exit(0);
		});
		primaryStage.setTitle("Consultations");
		primaryStage.show();

	}

}
