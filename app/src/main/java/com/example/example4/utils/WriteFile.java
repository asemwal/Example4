package com.example.example4.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class WriteFile {
	private File FileName;
	private File DirName;
    private PrintWriter out;
    private boolean status = false;
    public boolean openFile(String FileName){
    	status = false;
    	try {
    		this.FileName = new File(FileName);
    		this.FileName.createNewFile();
			out = new PrintWriter(this.FileName);
			status = true;
    	} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    	return status;
    }
    
    public boolean writeLine(StringBuffer br){
    	out.println(br.toString());
    	out.flush();
    	return true;
    }
	
	

}
