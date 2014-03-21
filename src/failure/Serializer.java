package failure;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import utils.Utils;

public class Serializer {
    public static byte[] serialize(Object obj) throws IOException {
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
    		  Utils.loge("Deserialization problem");
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

