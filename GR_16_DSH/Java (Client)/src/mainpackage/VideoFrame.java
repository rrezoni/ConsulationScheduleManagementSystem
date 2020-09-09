package mainpackage;

public class VideoFrame {
	
	private final String type = "VideoFrame";
	private String sourceID;
	private String targetID;
	private String frameBytes;
	
	public VideoFrame(String sourceID, String targetID, String frameBytes) {
		this.sourceID = sourceID;
		this.targetID = targetID;
		this.frameBytes = frameBytes;
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

	public String getFrameBytes() {
		return frameBytes;
	}

	public void setFrameBytes(String frameBytes) {
		this.frameBytes = frameBytes;
	}
	
}
