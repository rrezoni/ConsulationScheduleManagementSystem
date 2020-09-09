package mainpackage;

import java.sql.Date;

public class Professor extends User {

	public Professor() {
		super();
	}

	public Professor(String id, String firstName, String lastName, String username, String password, Date birthDate,
			String gender) {
		super(id, firstName, lastName, username, password, birthDate, gender);
	}

}
