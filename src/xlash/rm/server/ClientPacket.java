package xlash.rm.server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import javax.imageio.ImageIO;

public class ClientPacket implements Serializable{
	
	public byte[] byteArray;
	public byte[] sound;
	
	public ClientPacket(BufferedImage img, ByteArrayOutputStream baos) throws IOException{
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ImageIO.write(img, "jpg", byteArrayOutputStream);
        this.byteArray = byteArrayOutputStream.toByteArray();
        this.sound = baos.toByteArray();
	}

}
