package com.example.example4.utils;

public class ObjectTest {
	public String Name;
	public int a;
	public ObjectTest(String string, int a) {
		// TODO Auto-generated constructor stub
		this.Name = string;
		this.a =a;
	}
	public static void main(String args[]){
		ObjectTest o1 = new ObjectTest("Anant",1);
		ObjectTest o2 = new ObjectTest("Anant",2);
		ObjectTest o3 = new ObjectTest("Anant",3);
		System.out.println(o1.toString());
		System.out.println(o2.toString());
		System.out.println(o3.toString());
		System.out.println(o1.toString() == o2.toString());
		System.out.println(o1.a);
		System.out.println(o2.a);
		o2.a = o1.a;
		System.out.println(o2.a);
		System.out.println(o3.toString() == o2.toString());
		System.out.println(o3.toString() == o1.toString());
		System.out.println(o1 == o2);
		System.out.println(o3 == o2);
		System.out.println(o3 == o1);
	}
	public String toString(){
		return this.Name;
	}

}
