package edu.ucdavis;

public class User {
	private String name;
	private String phone;
	private double X;
	private double Y;
	
	public User (String n, String p) {
		name = n;
		phone = p;
		X = Y = 0;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getPhone() {
		return phone;
	}
	public void setX(double x) {
		X = x;
	}
	public double getX() {
		return X;
	}
	public void setY(double y) {
		Y = y;
	}
	public double getY() {
		return Y;
	}
}
