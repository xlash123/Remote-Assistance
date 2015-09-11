package xlash.rm.client;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.Serializable;
import java.util.ArrayList;

public class Input implements KeyListener, MouseListener, MouseWheelListener{
	
	public ArrayList<Integer> keys = new ArrayList<Integer>();
	public ArrayList<Integer> mouses = new ArrayList<Integer>();
	
	public boolean[] key = new boolean[256];
	public boolean[] mouse = new boolean[3];
	public int wheel;
	
	public boolean isKeyPressed(int key){
		return this.key[key];
	}
	
	public boolean isMouseClicked(int mouse){
		return this.mouse[mouse-1];
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		mouse[arg0.getButton()-1] = true;
		if(Window.client != null) mouses.add(arg0.getButton());
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		mouse[arg0.getButton()-1] = false;
		if(Window.client != null) mouses.add(-(arg0.getButton()));
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		key[arg0.getKeyCode()] = true;
		if(Window.client != null) keys.add(arg0.getKeyCode());
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		key[arg0.getKeyCode()] = false;
		if(Window.client != null) keys.add(-arg0.getKeyCode());
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		wheel = (int) arg0.getPreciseWheelRotation();
	}

}
