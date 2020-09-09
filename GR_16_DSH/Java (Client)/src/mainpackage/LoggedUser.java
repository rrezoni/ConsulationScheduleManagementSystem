package mainpackage;

import java.sql.Date;

public class LoggedUser {
	
	public static String id = null;
	public static String firstName = null;
	public static String lastName = null;
	public static Date birthDate = null;
	public static String gender = null;
	public static String username = null;
	public static String user;
	
	public static void setStudent(String i, String fn, String ln, Date bd, String g, String un) {
		id = i;
		firstName = fn;
		lastName = ln;
		birthDate = bd;
		gender = g;
		username = un;
		user = "Student";
	}
	
	public static void setProfessor(String i, String fn, String ln, Date bd, String g, String un) {
		id = i;
		firstName = fn;
		lastName = ln;
		birthDate = bd;
		gender = g;
		username = un;
		user = "Professor";
	}
	
	public static void setAdmin(String i, String fn, String ln, String un) {
		id = i;
		firstName = fn;
		lastName = ln;
		username = un;
		user = "Admin";
	}
	
	public static void clear() {
		id = null;
		firstName = null;
		lastName = null;
		birthDate = null;
		gender = null;
		username = null;
		user = null;
	}

}
