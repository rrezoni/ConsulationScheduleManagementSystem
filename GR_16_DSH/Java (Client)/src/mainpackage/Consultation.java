package mainpackage;

import java.sql.Timestamp;

public class Consultation {

	private int consId;
	private String profId;
	private int subId;
	private Timestamp consStart;
	private Timestamp consEnd;
	private String profFullName;
	private String subName;
	private String subYear;
	private String description;

	public Consultation(int consId, String profId, int subId, Timestamp consStart, Timestamp consEnd,
			String description) {
		super();
		this.consId = consId;
		this.profId = profId;
		this.subId = subId;
		this.consStart = consStart;
		this.consEnd = consEnd;
		this.setDescription(description);
	}

	public Consultation(int consId, String profId, int subId, Timestamp consStart, Timestamp consEnd,
			String description, String profFullName, String subName, String subYear) {
		this(consId, profId, subId, consStart, consEnd, description);
		this.profFullName = profFullName;
		this.subName = subName;
		this.subYear = subYear;
	}

	public String getProfFullName() {
		return profFullName;
	}

	public void setProfFullName(String profFullName) {
		this.profFullName = profFullName;
	}

	public String getSubName() {
		return subName;
	}

	public void setSubName(String subName) {
		this.subName = subName;
	}

	public int getConsId() {
		return consId;
	}

	public void setConsId(int consId) {
		this.consId = consId;
	}

	public String getProfId() {
		return profId;
	}

	public void setProfId(String profId) {
		this.profId = profId;
	}

	public int getSubId() {
		return subId;
	}

	public void setSubId(int subId) {
		this.subId = subId;
	}

	public Timestamp getConsStart() {
		return consStart;
	}

	public void setConsStart(Timestamp consStart) {
		this.consStart = consStart;
	}

	public Timestamp getConsEnd() {
		return consEnd;
	}

	public void setConsEnd(Timestamp consEnd) {
		this.consEnd = consEnd;
	}

	public String getSubYear() {
		return subYear;
	}

	public void setSubYear(String subYear) {
		this.subYear = subYear;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
