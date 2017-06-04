package com.example.example4.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class ReadFile {
	private File FileName;
	private File DirName;
    private BufferedReader br;
    private InputStreamReader isr;
    private boolean status = false;
    private String  line = "";
    public String readLine() throws Exception{
    	try {
    		if((line= br.readLine()) != null){
    			return line;
    		}
    		else {
    			return "EOF";
     		}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
 		}
    	catch(NullPointerException npe){
    		//throw new NullPointerException();

    	}
    	return line;
    }
    public boolean openFile(String FileName){
		//line = "1c:aa:07:b0:74:cd=1,73.0,3.6366666666666667|3,73.0,3.6366666666666667|8,69.0,20.98|10,68.0,22.706666666666667|11,65.0, 22.190000000000001|12,70.0,9.2300000000000004|13,73.0,3.6366666666666667|14,67.0,9.8100000000000005|15,69.0,19.199999999999999|";
    	status = false;
		if(true){

			try {
				br = new BufferedReader(new FileReader(FileName));
				status = true;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    	return status;
    }
	public String getLine() {
		// TODO Auto-generated method stub
		return line.toString();
	}
}
