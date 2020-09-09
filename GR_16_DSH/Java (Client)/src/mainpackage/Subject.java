package mainpackage;

public class Subject {

	private int id;
	private String name;
	private String year;

	public Subject(int id, String name, String year) {
		super();
		this.id = id;
		this.name = name;
		this.year = year;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

}
