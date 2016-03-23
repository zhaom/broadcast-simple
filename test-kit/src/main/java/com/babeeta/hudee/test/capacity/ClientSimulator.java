package com.babeeta.hudee.test.capacity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.babeeta.hudee.core.MessageRouting.Acknowledgement;
import com.babeeta.hudee.core.MessageRouting.Credential;
import com.babeeta.hudee.core.MessageRouting.Heartbeat;
import com.babeeta.hudee.core.MessageRouting.HeartbeatInit;
import com.babeeta.hudee.core.MessageRouting.HeartbeatInit.HeartbeatException;
import com.babeeta.hudee.core.MessageRouting.HeartbeatResponse;
import com.babeeta.hudee.core.MessageRouting.Message;
import com.babeeta.hudee.core.MessageRouting.ReserveInterval;
import com.babeeta.hudee.core.MessageRouting.Response;
import com.babeeta.hudee.core.MessageRouting.ServiceBind;
import com.google.protobuf.MessageLite;

@SuppressWarnings("javadoc")
public class ClientSimulator {
	private final String host;
	private final int port;

	private Socket socket = null;

	private final String appId;
	private final String deviceId;
	private final String deviceKey;

	private boolean waitQuitStatus = false;

	public ClientSimulator(String host, int port, String aid, String did,
	        String key) {
		this.host = host;
		this.port = port;
		this.appId = aid;
		this.deviceId = did;
		this.deviceKey = key;
	}

	public void start() throws IOException, InterruptedException {
		socket = new Socket();
		socket.connect(new InetSocketAddress(host, port));

		Thread handlerThread = new Thread(new DataHandler(socket,
		        new DataInputStream(socket.getInputStream()),
		        new DataOutputStream(
		                socket.getOutputStream())),
		        "Handler");
		handlerThread.start();

		handlerThread.join();
		System.exit(0);
	}

	private interface ResponseHandler {
		void onResponse(int tag, MessageLite message) throws Exception;
	}

	private class DataHandler implements Runnable {
		private final DataInputStream in;
		private final DataOutputStream out;
		private final AtomicInteger counter;
		private final Socket socket;
		private final ConcurrentMap<Integer, ResponseHandler> handlerMap = new ConcurrentHashMap<Integer, ResponseHandler>();
		private final ScheduledExecutorService heartbeatService = Executors
		        .newSingleThreadScheduledExecutor();
		private int lastHeartbeat = 0;

		public DataHandler(Socket socket, DataInputStream in,
		        DataOutputStream out) {
			super();
			this.in = in;
			this.out = out;
			this.counter = new AtomicInteger(0);
			this.socket = socket;

		}

		private MessageLite getProtoType(int clazz) {
			switch (clazz) {
			case 2:
				return HeartbeatResponse.getDefaultInstance();
			case 3:
				return ReserveInterval.getDefaultInstance();
			case 129:
				return Message.getDefaultInstance();
			case 136:
				return Credential.getDefaultInstance();
			case 137:
				return ServiceBind.getDefaultInstance();
			case 135:
				return Response.getDefaultInstance();
			default:
				return null;
			}
		}

		private void dispatcher(int tag, int type, byte[] buffer)
		        throws Exception {
			System.out.println("tag:" + tag + ", type:" + type);

			MessageLite prototype = getProtoType(type);
			if (prototype != null) {
				MessageLite message = prototype.newBuilderForType()
				        .mergeFrom(buffer, 0, buffer.length).build();

				ResponseHandler handler = handlerMap.remove(tag);
				if (handler == null) {
					if (type == 129) {
						Message msg = (Message) message;
						System.out
						        .println("=====================================");
						System.out.println("Receviced message[" + msg.getUid()
						        + "]");
						System.out.println(tag);
						System.out
						        .println("-------------------------------------");
						System.out.println(msg.toString());
						System.out
						        .println("=====================================");

						sendAck(tag, msg.getUid());
					} else if (type == 3) {
						ReserveInterval ri = (ReserveInterval) message;
						System.out
						        .println("=====================================");
						System.out.println(ri.getNextInterval());
						System.out
						        .println("=====================================");
						waitQuitStatus = true;
					}
				} else {
					handler.onResponse(tag, message);
				}
			} else {
				System.out.println("Unkown server feed.");
			}
		}

		private void read() throws Exception {
			int tag = in.readInt();
			int type = in.readInt();
			int len = in.readInt();
			byte[] buf = new byte[len];
			in.readFully(buf);

			dispatcher(tag, type, buf);
		}

		private void send(int tag, int cmd, byte[] content) throws IOException {
			synchronized (out) {
				out.writeInt(tag);
				out.writeInt(cmd);
				out.writeInt(content.length);
				out.write(content);
				out.flush();
			}
		}

		private String getTimeStamp() {
			return SimpleDateFormat.getDateTimeInstance().format(new Date());
		}

		private void onHeartbeatResponse(int tag, HeartbeatResponse message) {
			System.out.println("[" + getTimeStamp() + "]T:"
			        + message.getDelay());
			lastHeartbeat = message.getDelay();
			heartbeatService.schedule(new Runnable() {
				@Override
				public void run() {
					try {
						Heartbeat heartbeat = Heartbeat.newBuilder()
						        .setLastDelay(lastHeartbeat).build();
						int tag = counter.getAndIncrement();
						handlerMap.put(tag, new ResponseHandler() {
							@Override
							public void onResponse(int tag, MessageLite message)
							        throws Exception {
								onHeartbeatResponse(tag,
								        (HeartbeatResponse) message);
							}
						});
						send(tag, 1, heartbeat.toByteArray());
					} catch (IOException e) {
						System.out.println("Heartbeat error:" + e.toString());
						try {
							Thread.sleep(1000L);
						} catch (InterruptedException e2) {
							e2.printStackTrace();
						}
						try {
							socket.connect(socket.getRemoteSocketAddress());
						} catch (IOException e1) {
							System.out
							        .println("Error while reconnecting to remote server."
							                + e1.toString());
						}
					}
				}

			}, message.getDelay(), TimeUnit.SECONDS);
		}

		private void sendAck(int messageTag, String messageId)
		        throws IOException {
			Acknowledgement ac = Acknowledgement.newBuilder().setUid(messageId)
			        .build();
			int tag = counter.getAndIncrement();

			handlerMap.put(tag, new ResponseHandler() {
				@Override
				public void onResponse(int tag, MessageLite message)
				        throws Exception {
					Response rsp = (Response) message;
					System.out.println(rsp.getStatus());
				}
			});

			send(tag, 130, ac.toByteArray());
		}

		private void sendDeviceAuth(String deviceId, String deviceKey)
		        throws IOException {
			Credential auth = Credential.newBuilder().setId(deviceId)
			        .setSecureKey(deviceKey).build();

			int tag = counter.getAndIncrement();

			handlerMap.put(tag, new ResponseHandler() {
				@Override
				public void onResponse(int tag, MessageLite message)
				        throws Exception {
					System.out.println("Device auth successful!!!");
				}
			});
			send(tag, 132, auth.toByteArray());
		}

		private void sendHeartbeatInit(String cause, int lastTimeout,
		        String pullInfo)
		        throws IOException {
			HeartbeatInit hbi = null;
			if (pullInfo == null) {
				hbi = HeartbeatInit.newBuilder().setCause(cause)
				        .setLastException(HeartbeatException.valueOf(cause))
				        .setLastTimeout(lastTimeout)
				        .build();
			} else {
				hbi = HeartbeatInit.newBuilder().setCause(cause)
				        .setLastException(HeartbeatException.valueOf(cause))
				        .setLastTimeout(lastTimeout).setPullInfo(pullInfo)
				        .build();
			}
			int tag = counter.getAndIncrement();

			handlerMap.put(tag, new ResponseHandler() {
				@Override
				public void onResponse(int tag, MessageLite message)
				        throws Exception {
					onHeartbeatResponse(tag, (HeartbeatResponse) message);

					sendDeviceAuth(deviceId, deviceKey);
				}
			});
			send(tag, 0, hbi.toByteArray());
		}

		@Override
		public void run() {
			while (!waitQuitStatus) {
				try {
					if (counter.get() == 0) {
						sendHeartbeatInit("none", 0, appId + "." + deviceId);
					}
					read();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			try {
				heartbeatService.shutdownNow();
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Quit......");
		}
	}
}
