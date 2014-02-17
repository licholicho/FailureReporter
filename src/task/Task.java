package task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import location.Location;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class Task {

	private long id;
	private String title;
	private String description;
	private Location location;
	private Date beginDate;
	private Date endDate;
	private List<byte[]> photos;
	private boolean status;
	
	
	public Task() {
		this.title = "";
		this.description = "";
		this.location = new Location();
		this.status = false;
		this.beginDate = new Date();
		this.endDate = new Date();
		this.photos = new ArrayList<byte[]>();
	}
	
	public Task(String title, String description, Location location) {
		super();
		this.title = title;
		this.description = description;
		this.location = location;
		this.status = false;
		this.beginDate = new Date();
		this.endDate = new Date();
		this.photos = new ArrayList<byte[]>();
	}

	public Task(String title, String description) {
		super();
		this.title = title;
		this.description = description;
		this.status = false;
		this.location = new Location();
		this.beginDate = new Date();
		this.endDate = new Date();
		this.photos = new ArrayList<byte[]>();
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
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
		if (d == 1) this.status = true;
		else this.status = false;
	}
	
	public int done() {
		return status ? 1 : 0;
	}

	public Date getBeginDate() {
		return beginDate;
	}
	
	public void setBeginDate(String beginDate) {
		this.beginDate = stringToDate(beginDate);
	}

	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public void setEndDate(String endDate) {
		this.endDate = stringToDate(endDate);
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
