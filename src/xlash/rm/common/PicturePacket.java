package xlash.rm.common;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import javax.imageio.ImageIO;

public class PicturePacket implements Serializable{
	
	public byte[] byteArray;
	
	public PicturePacket(BufferedImage img) throws IOException{
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ImageIO.write(img, "jpg", byteArrayOutputStream);
        this.byteArray = byteArrayOutputStream.toByteArray();
	}

}
