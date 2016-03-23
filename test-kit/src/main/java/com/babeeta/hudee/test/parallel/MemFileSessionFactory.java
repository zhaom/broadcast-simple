package com.babeeta.hudee.test.parallel;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemFileSessionFactory {

	static Logger logger = LoggerFactory.getLogger(MemFileSessionFactory.class
			.getName());

	public static void main(String[] args) {
		for (int i = 0; i < 20000; i++) {
			getNewSession();
		}
	}

	private static AtomicLong didIndex = new AtomicLong(0L);
	private static final int oneLineByte = 142;
	private static ResourceBundle rb = ResourceBundle.getBundle("service");
	private static String fileName = rb.getString("didFilePath");

	public static synchronized Session getNewSession() {
		Session s = new Session();
		s.appId = rb.getString("appId");
		s.appKey = rb.getString("appKey");
		String idAndKeyLine = getBigFileLine(didIndex.getAndIncrement());
		String did = idAndKeyLine.substring(0, 32)
				.replaceAll("\"", "");
		String key = idAndKeyLine.substring(34,67)
				.replaceAll("\"", "").trim();
		String[] ss = new String[] { did, key };
		s.did = ss[0];
		s.secureKey = ss[1];
		logger.info("did:[{}] dkey:[{}]", ss[0],ss[1]);
		
		return s;
	}

	private static String getBigFileLine(long index) {
		try {
			java.io.RandomAccessFile raf = new java.io.RandomAccessFile(
					fileName, "r");
			long totalLen = raf.length();
			//logger.info("文件总长是: " + totalLen);

			if ((index + 1) * oneLineByte / 2 > totalLen) {
				index = totalLen / oneLineByte / 2 - 1;
			}
			java.nio.channels.FileChannel channel = raf.getChannel();
			java.nio.MappedByteBuffer buffer = channel.map(
					FileChannel.MapMode.READ_ONLY, index * oneLineByte / 2,
					oneLineByte);
			byte[] src = new byte[oneLineByte];
			for (int i = 0; i < oneLineByte; i++) {
				src[i] = buffer.get(i);
			}
			ByteArrayInputStream is = new ByteArrayInputStream(src);
			StringBuffer sb = new StringBuffer();

			for (int i = 0; i < is.available(); i++) {
				sb.append((char) is.read());
			}
			sb = sb.delete(0, 1);
			sb = sb.delete(sb.lastIndexOf("\""), sb.length());
			//logger.info(sb.toString());

			buffer.clear();
			channel.close();
			raf.close();
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
