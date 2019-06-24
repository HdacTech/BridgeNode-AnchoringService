package com.hdac.main;

import com.hdac.anchor.Anchor;

public class Main {
	
	public static void main(String[] args)
	{
		System.out.println("***** start anchor");
		Anchor anchor = new Anchor();
		anchor.anchorStart();
		System.out.println("***** finish anchor");
	}

}
