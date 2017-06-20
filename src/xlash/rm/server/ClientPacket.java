package xlash.rm.server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import javax.imageio.ImageIO;

public class ClientPacket implements Serializable{
	
	public byte[] byteArray;
	
	public ClientPacket(BufferedImage img) throws IOException{
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ImageIO.write(img, "jpg", byteArrayOutputStream);
        this.byteArray = byteArrayOutputStream.toByteArray();
	}

}
