package mainpackage;

import javafx.stage.Stage;

public class ConsultationAddingStage extends Stage {
	
	public static boolean isOpen;

	public ConsultationAddingStage() {
		super();
		
		init();
	}
	
	private void init() {
		
		isOpen = true;
		
		setScene(new ConsultationAddingScene(this));
		setResizable(false);
		setTitle("Add Consultation");
		
	}
	
}
