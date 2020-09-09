package mainpackage;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class ChatScrollPane extends ScrollPane {

	private VBox chatScrollPaneContent;

	public ChatScrollPane() {
		init();
	}

	private void init() {

		setId("chatSceneScrollPane");

		chatScrollPaneContent = new VBox();
		chatScrollPaneContent.setId("chatSceneScrollPaneContent");
		setFitToWidth(true);
		chatScrollPaneContent.setFillWidth(true);
		setContent(chatScrollPaneContent);
		chatScrollPaneContent.heightProperty().addListener(new ChangeListener<Object>() {
			@Override
			public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {
				setVvalue(1);
			}
		});

	}

	public void addOthersText(String text, boolean callScene) {
		chatScrollPaneContent.getChildren().add(new OthersTextHBox(text, callScene));
	}

	public void addMyText(String text, boolean callScene) {
		chatScrollPaneContent.getChildren().add(new MyTextHBox(text, callScene));
	}

	public class OthersTextHBox extends HBox {

		public OthersTextHBox(String text, boolean callScene) {
			init(text, callScene);
		}

		private void init(String othersText, boolean callScene) {

			setAlignment(Pos.CENTER_LEFT);

			HBox othersTextHBox = new HBox();
			othersTextHBox.setId("chatSceneOthersTextHBox");
			getChildren().add(othersTextHBox);

			Text text = new Text(othersText);
			othersTextHBox.getChildren().add(text);
			if (!callScene)
				if (text.getLayoutBounds().getWidth() + 10 < ChatScrollPane.this.getWidth())
					text.wrappingWidthProperty().bind(ChatScrollPane.this.widthProperty()
							.subtract(ChatScrollPane.this.getWidth() - text.getLayoutBounds().getWidth()));
				else
					text.wrappingWidthProperty().bind(ChatScrollPane.this.widthProperty().subtract(50));
			else
				text.wrappingWidthProperty().bind(ChatScrollPane.this.widthProperty().subtract(50));
		}

	}

	public class MyTextHBox extends HBox {

		public MyTextHBox(String text, boolean callScene) {
			init(text, callScene);
		}

		private void init(String myText, boolean callScene) {

			setAlignment(Pos.CENTER_RIGHT);

			HBox myTextHBox = new HBox();
			myTextHBox.setId("chatSceneMyTextHBox");
			getChildren().add(myTextHBox);

			Text text = new Text(myText);
			myTextHBox.getChildren().add(text);
			if (!callScene)
				if (text.getLayoutBounds().getWidth() + 10 < ChatScrollPane.this.getWidth())
					text.wrappingWidthProperty().bind(ChatScrollPane.this.widthProperty()
							.subtract(ChatScrollPane.this.getWidth() - text.getLayoutBounds().getWidth()));
				else
					text.wrappingWidthProperty().bind(ChatScrollPane.this.widthProperty().subtract(50));
			else
				text.wrappingWidthProperty().bind(ChatScrollPane.this.widthProperty().subtract(50));
		}

	}

}
