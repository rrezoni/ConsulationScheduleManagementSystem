package mainpackage;

import java.sql.Date;

public class Student extends User {

	private String department;
	private String studyYear;

	public Student() {
		super();
	}

	public Student(String id, String firstName, String lastName, String username, String password, Date birthDate,
			String gender, String department, String studyYear) {
		super(id, firstName, lastName, username, password, birthDate, gender);
		this.department = department;
		this.studyYear = studyYear;
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
