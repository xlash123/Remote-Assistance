package xlash.rm.common;

import java.io.IOException;
import java.io.Serializable;

public class PicturePacket implements Serializable{
	
	public int[] newPixels;
	
	public PicturePacket(int[] newPixels) throws IOException{
		this.newPixels = newPixels;
	}

}
