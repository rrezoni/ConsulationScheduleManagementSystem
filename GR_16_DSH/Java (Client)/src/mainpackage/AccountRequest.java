package mainpackage;

import java.sql.Date;

public class AccountRequest {

	private String firstName;
	private String lastName;
	private int id;
	private String sid;
	private String username;
	private String gender;
	private Date birthDate;
	private String department;
	private String studyYear;

	public AccountRequest(String firstName, String lastName, int id, String sid, String username, String gender, Date birthDate,
			String department, String studyYear) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.id = id;
		this.sid = sid;
		this.username = username;
		this.gender = gender;
		this.birthDate = birthDate;
		this.department = department;
		this.studyYear = studyYear;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getStudyYear() {
		return studyYear;
	}

	public void setStudyYear(String studyYear) {
		this.studyYear = studyYear;
	}

}
