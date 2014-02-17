package location;

public class Location {

	private float latitude;
	private float longitude;
	private String nameOfPlace;
	
	public Location () {
		this.latitude = 0;
		this.longitude = 0;
		this.nameOfPlace = "";
	}
	
	public Location(String nameOfPlace) {
		super();
		this.nameOfPlace = nameOfPlace;
	}

	public Location(float latitude, float longitude) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
		this.nameOfPlace = "";
	}

	public Location(float latitude, float longitude, String nameOfPlace) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
		this.nameOfPlace = nameOfPlace;
	}

	public float getLatitude() {
		return latitude;
	}

	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}

	public float getLongitude() {
		return longitude;
	}

	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}

	public String getNameOfPlace() {
		return nameOfPlace;
	}

	public void setNameOfPlace(String nameOfPlace) {
		this.nameOfPlace = nameOfPlace;
	}
	
	}
