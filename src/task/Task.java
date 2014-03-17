package task;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import location.Location;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class Task implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long id;
	private String title;
	private String description;
	private String beginDate;
	private String endDate = dateToString(new Date(Long.MAX_VALUE));
	private List<byte[]> photos;
	private boolean status;
	private double latitude;
	private double longitude;
	private String nameOfPlace;


	
	public Task() {
		this.title = "";
		this.description = "";
		this.status = false;
		this.beginDate = "";
		this.endDate = "";
		this.photos = new ArrayList<byte[]>();
		this.latitude = 0;
		this.longitude = 0;
		this.nameOfPlace = "";
	}
	
	
	public Task(String title, String description) {
		super();
		this.title = title;
		this.description = description;
		this.status = false;
		this.beginDate = "";
		this.endDate = "";
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
		Log.i("lol","done");
		if (d == 1) this.status = true;
		else this.status = false;
	}
	
	public int done() {
		return status ? 1 : 0;
	}

	public Date getBeginDate() {
		return stringToDate(beginDate);
	}
	
	public String getBeginDateInString() {
		return beginDate;
	}
	
	public void setBeginDate(String beginDate) {
		this.beginDate = dateToString(stringToDate(beginDate));
	}

	public void setBeginDate(Date beginDate) {
		this.beginDate = dateToString(beginDate);
	}
	
	public String getEndDateInString() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = dateToString(endDate);
	}
	
	public void setEndDate(String endDate) {
		this.endDate = dateToString(stringToDate(endDate));
	}
	
	private GregorianCalendar getStartDate() {
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(stringToDate(beginDate)); 
		return (GregorianCalendar)cal;
	}
	
	private GregorianCalendar getEndDate() {
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(stringToDate(endDate)); 
		return (GregorianCalendar)cal;
	}
	
	public int getStartDay() {
		return getStartDate().get(Calendar.DAY_OF_MONTH);
	}
	
	public int getStartMonth() {
		return getStartDate().get(Calendar.MONTH);
	}
	
	public int getStartYear(){
		return getStartDate().get(Calendar.YEAR);
	}
	
	public int getEndDay() {
		return getEndDate().get(Calendar.DAY_OF_MONTH);
	}
	
	public int getEndMonth() {
		return getEndDate().get(Calendar.MONTH);
	}
	
	public int getEndYear(){
		return getEndDate().get(Calendar.YEAR);
	}

	public List<byte[]> getPhotos() {
		return photos;
	}

	public void setPhotos(List<byte[]> photos) {
		this.photos = photos;
	}
	
	public byte[] getPhotoInBytes(int i) {
		return this.photos.get(i);
	}
	
	public void setPhoto(int i, byte[] photo) {
		this.photos.set(i, photo);
	}
	
	public Drawable getDrawable(int i) {
		Bitmap bitMapImage = BitmapFactory.decodeByteArray(photos.get(i), 0, photos.get(i).length);
		return ((Drawable)new BitmapDrawable(bitMapImage));
	}
	
	public Bitmap getBitmap(int i){
		return BitmapFactory.decodeByteArray(photos.get(i), 0, photos.get(i).length);
	}
	
	public String dateToString(Date date) {
		SimpleDateFormat  dateFormat = new SimpleDateFormat("yyyy-MM-dd");     
		 String datetime = dateFormat.format(date);
		 return datetime;
	}
	
	public Date stringToDate(String dt) { 
		SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd");   
		Date date = new Date();
		try {
			date = format.parse(dt);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		return date;
	}
	
}
