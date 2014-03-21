package failure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import utils.Utils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class Failure implements Serializable {

	private static final long serialVersionUID = 1L;
	private long id;
	private String title;
	private String description;
	private String beginDate;
	private String endDate;
	private boolean status;
	private double latitude;
	private double longitude;
	private String nameOfPlace;
	private List<byte[]> photos;
	/*private byte[] photo1;
	private byte[] photo2;
	private byte[] photo3;
*/
	public Failure() {
		this.title = "";
		this.description = "";
		this.status = false;
		this.beginDate = "";
		this.endDate = Utils.dateToString(new Date(Long.MAX_VALUE));
		this.photos = new ArrayList<byte[]>();
		this.latitude = 0;
		this.longitude = 0;
		this.nameOfPlace = "";
	}

	public Failure(String title, String description) {
		super();
		this.title = title;
		this.description = description;
		this.status = false;
		this.beginDate = "";
		this.endDate = Utils.dateToString(new Date(Long.MAX_VALUE));
		this.photos = new ArrayList<byte[]>();
		this.latitude = 0;
		this.longitude = 0;
		this.nameOfPlace = "";
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void setLatitude(String latitude) {
		if (latitude.matches("-?\\d+(\\.\\d*)?"))
			this.latitude = Double.valueOf(latitude);
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public void setLongitude(String longitude) {
		if (longitude.matches("-?\\d+(\\.\\d*)?"))
			this.longitude = Double.valueOf(longitude);
	}

	public String getNameOfPlace() {
		return nameOfPlace;
	}

	public void setNameOfPlace(String nameOfPlace) {
		this.nameOfPlace = nameOfPlace;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isDone() {
		return status;
	}

	public void setDone(boolean done) {
		this.status = done;
	}

	public void setDone(int d) {
		if (d == 1)
			this.status = true;
		else
			this.status = false;
	}

	public int done() {
		return status ? 1 : 0;
	}

	public Date getBeginDate() {
		return Utils.stringToDate(beginDate);
	}

	public String getBeginDateInString() {
		return beginDate;
	}

	public void setBeginDate(String beginDate) {
		this.beginDate = Utils.dateToString(Utils.stringToDate(beginDate));
	}

	public void setBeginDate(Date beginDate) {
		this.beginDate = Utils.dateToString(beginDate);
	}

	public String getEndDateInString() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = Utils.dateToString(endDate);
	}

	public void setEndDate(String endDate) {
		this.endDate = Utils.dateToString(Utils.stringToDate(endDate));
	}

	private GregorianCalendar getStartDate() {
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(Utils.stringToDate(beginDate));
		return (GregorianCalendar) cal;
	}

	private GregorianCalendar getEndDate() {
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(Utils.stringToDate(endDate));
		return (GregorianCalendar) cal;
	}

	public int getStartDay() {
		return getStartDate().get(Calendar.DAY_OF_MONTH);
	}

	public int getStartMonth() {
		return getStartDate().get(Calendar.MONTH);
	}

	public int getStartYear() {
		return getStartDate().get(Calendar.YEAR);
	}

	public int getEndDay() {
		return getEndDate().get(Calendar.DAY_OF_MONTH);
	}

	public int getEndMonth() {
		return getEndDate().get(Calendar.MONTH);
	}

	public int getEndYear() {
		return getEndDate().get(Calendar.YEAR);
	}

	public List<byte[]> getPhotos() {
		/*/-------------
		List<byte[]> photos = new ArrayList<byte[]>();
		if (photo1 != null)
			photos.add(0, photo1);
		if (photo2 != null)
			photos.add(1, photo2);	
		if (photo3 != null)
			photos.add(2, photo3);
		*/
		return photos;
	}
	
	public void setPhotosEmpty(){
		this.photos.clear();
	}

	public void setPhotos(List<byte[]> photos) {
		/*----------
		switch(photos.size()) {
		case 0:
			break;
		case 1:
			photo1 = photos.get(0);
			photo2 = null;
			photo3 = null;
			break;
		case 2:
			photo1 = photos.get(0);
			photo2 = photos.get(1);
			photo3 = null;
			break;
		case 3:
			photo1 = photos.get(0);
			photo2 = photos.get(1);
			photo3 = photos.get(2);
			break;
		}
		*/
		this.photos = photos;
	}

	public byte[] getPhotoInBytes(int i) {
		/*byte[] photo = null;
		switch(i) {
			case 0:
				photo = photo1;
				break;
			case 1:
				photo = photo2;
				break;
			case 2:
				photo = photo3;
				break;
		}
		return photo;*/
		return this.photos.get(i);
	}

	public void setPhoto(int i, byte[] photo) {
		/*switch(i) {
		case 0:
			photo1 = photo;
			break;
		case 1:
			photo2 = photo;
			break;
		case 2:
			photo3 = photo;
			break;
	}*/
		this.photos.set(i, photo);
	}

	public void addPhoto(byte[] photo) {
		this.photos.add(photo);
	}
	
	public void addPhoto(int i, byte[] photo) {
		/*switch(i) {
		case 0:
			photo1 = photo;
			break;
		case 1:
			photo2 = photo;
			break;
		case 2:
			photo3 = photo;
			break;
	}*/
		this.photos.add(i, photo);
	}

	public Drawable getDrawable(int i) {
		Bitmap bitMapImage = BitmapFactory.decodeByteArray(photos.get(i), 0,
				photos.get(i).length);
		return ((Drawable) new BitmapDrawable(bitMapImage));
	}

	public Bitmap getBitmap(int i) {
		return BitmapFactory.decodeByteArray(photos.get(i), 0,
				photos.get(i).length);
	}

/*
	private void writeObject(ObjectOutputStream o) throws IOException {

		o.defaultWriteObject();
		if (!photos.isEmpty()) {
			o.writeObject(photos);
			}
	}

	private void readObject(ObjectInputStream o) throws IOException,
			ClassNotFoundException {

		o.defaultReadObject();
		photos = (List<byte[]>) o.readObject();
	}
*/
}
