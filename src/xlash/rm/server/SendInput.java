package xlash.rm.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;

import xlash.rm.client.Input;

public class SendInput implements Serializable{
	
	public ArrayList<Integer> key;
	public ArrayList<Integer> mouse;
	public int wheel;
	
	public SendInput(Input input){
		key = new ArrayList<Integer>();
		mouse = new ArrayList<Integer>();
		key.addAll(input.keys);
		mouse.addAll(input.mouses);
		this.wheel = input.wheel;
	}

}
