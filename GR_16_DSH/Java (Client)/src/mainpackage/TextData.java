package mainpackage;

public class TextData {

	private final String type = "TextData";
	private String text;
	private String sourceID;
	private String targetID;
	private String sourceUsername;

	public TextData(String text, String sourceID, String targetID, String sourceUsername) {
		this.text = text;
		this.sourceID = sourceID;
		this.targetID = targetID;
		this.sourceUsername = sourceUsername;
	}

	public String getType() {
		return type;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getSourceID() {
		return sourceID;
	}

	public void setSourceID(String sourceID) {
		this.sourceID = sourceID;
	}

	public String getTargetID() {
		return targetID;
	}

	public void setTargetID(String targetID) {
		this.targetID = targetID;
	}

	public String getSourceUsername() {
		return sourceUsername;
	}

	public void setSourceUsername(String sourceUsername) {
		this.sourceUsername = sourceUsername;
	}

}
