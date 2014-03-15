package task;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.util.Log;

public class Serializer {
    public static byte[] serialize(Object obj) throws IOException {
    	Log.i("jest","YYYYYYYY");
        /*ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(obj);
        if (b.toByteArray() == null) Log.i("jest","ser null");
        else Log.i("jest","ser not null ocb?");
        return b.toByteArray();*/
    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	ObjectOutputStream out = null;
    	byte[] yourBytes;
    	try {
    	  out = new ObjectOutputStream(bos);   
    	  out.writeObject(obj);
    	  yourBytes = bos.toByteArray();
    	} finally {
    	  try {
    	    if (out != null) {
    	      out.close();
    	    }
    	  } catch (IOException ex) {
    	    // ignore close exception
    	  }
    	  try {
    	    bos.close();
    	  } catch (IOException ex) {
    	    // ignore close exception
    	  }
    	}
    	return yourBytes;
    }

    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
       /* ByteArrayInputStream b = new ByteArrayInputStream(bytes);
        ObjectInputStream o = new ObjectInputStream(b);
        return o.readObject();*/
    	ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
    	ObjectInputStream in = null;
    	Object o;
    	try {
    	  in = new ObjectInputStream(bis);
    	  o = in.readObject(); 
    	} finally {
    	  try {
    	    bis.close();
    	  } catch (IOException ex) {
    	    // ignore close exception
    	  }
    	  try {
    	    if (in != null) {
    	      in.close();
    	    }
    	  } catch (IOException ex) {
    	    // ignore close exception
    	  }
    	}
    	return o;
    }
}

