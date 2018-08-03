package com.rtmap.driver.scan;

/**
 * Goods��Ϊʵ���࣬������ʹ��
 * 
 * @author Administrator
 * 
 */
public class Barcode {

	private int barcodeid;
	private String name; // ��Ʒ��
	private String barcode; // ����
	private int count; // ����

	public void setBarcodeid(int barcodeid) {
		this.barcodeid = barcodeid;
	}

	public Barcode(String name, String barcode, int count) {
		super();
		this.name = name;
		this.barcode = barcode;
		this.count = count;
	}

	public Barcode() {
		super();
	}

	public int getBarcodeid() {
		return barcodeid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "��Ʒ����" + this.name + ",���룺 " + this.barcode;
	}

}
