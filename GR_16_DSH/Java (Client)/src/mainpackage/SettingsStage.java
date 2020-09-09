package mainpackage;

import javafx.stage.Stage;

public class SettingsStage extends Stage {
	
	public static boolean isOpen;

	public SettingsStage() {
		super();
		
		init();
	}
	
	private void init() {
		
		isOpen = true;
		
		setTitle("Settings");
		setScene(new SettingsScene(this));
		setResizable(false);
		
	}
	
}
