package mainpackage;

public class ProfessorSubject {
	
	private int subId;
	private String profId;
	
	public ProfessorSubject(int subId, String profId) {
		super();
		this.subId = subId;
		this.profId = profId;
	}

	public int getSubId() {
		return subId;
	}

	public void setSubId(int subId) {
		this.subId = subId;
	}

	public String getProfId() {
		return profId;
	}

	public void setpId(String profId) {
		this.profId = profId;
	}
	
}
