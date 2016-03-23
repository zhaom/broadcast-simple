package com.babeeta.hudee.test.parallel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SomeTest {

	public static void main(String[] args) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date d = new Date();
		String sd = df.format(d);
		System.out.println(sd);

	}
}
