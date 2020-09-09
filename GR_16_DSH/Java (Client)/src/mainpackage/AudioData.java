package mainpackage;

public class AudioData {
	
	private final String type = "AudioData";
	private String sourceID;
	private String targetID;
	private int length;
	private String audioBytes;
	
	public AudioData(String sourceID, String targetID, int length, String audioBytes) {
		this.sourceID = sourceID;
		this.targetID = targetID;
		this.length = length;
		this.audioBytes = audioBytes;
	}

	public String getType() {
		return type;
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

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getAudioBytes() {
		return audioBytes;
	}

	public void setAudioBytes(String audioBytes) {
		this.audioBytes = audioBytes;
	}
	
}
