package com.babeeta.hudee.test.jinli;

import com.babeeta.hudee.core.MessageRouting;
import com.babeeta.hudee.core.MessageRouting.*;
import com.babeeta.hudee.core.MessageRouting.HeartbeatInit.HeartbeatException;
import com.google.protobuf.MessageLite;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("javadoc")
public class ClientSimulator {

    public static final Logger logger = LoggerFactory.getLogger(ClientSimulator.class);

	private final String host;
	private final int port;
	private final String appId;
	private final String appKey;
	private final String imei;

	private Socket socket = null;

	private String deviceId = null;
	private String deviceKey = null;
	private String clientId = null;

	private boolean inListenStatus = false;
	private boolean waitQuitStatus = false;

	public ClientSimulator(String host, int port, String appId, String appKey,
                           String imei) {
		this.host = host;
		this.port = port;
		this.appId = appId;
		this.appKey = appKey;
		this.imei = imei;

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
		private final ScheduledExecutorService pushService = Executors
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
                logger.info("No suitable lite for this message, type {[]}",clazz);
				return null;
			}
		}

		private void dispatcher(int tag, int type, byte[] buffer)
		        throws Exception {
			logger.info("Read tag:" + tag + ", type:" + type);

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

		private void sendServiceBind(String appId)
		        throws IOException {
			ServiceBind serviceBind = ServiceBind.newBuilder()
			        .setApplicationId(appId).build();
			int tag = counter.getAndIncrement();
            logger.info("Send ServiceBind with appid [{}]",appId);
			handlerMap.put(tag, new ResponseHandler() {
				@Override
				public void onResponse(int tag, MessageLite message)
				        throws Exception {
					ServiceBind bind = (ServiceBind) message;
					logger.info("Bind successful!!! aid=["
                            + bind.getApplicationId() + "], cid=["
                            + bind.getClientId() + "]");
					clientId = bind.getClientId();
					//inListenStatus = true;
                    logger.info("Sleep 10 Seconds!!!!!");
                    Thread.sleep(10000L);
                    sendServiceBind(bind.getApplicationId(),clientId);
				}
			});
			send(tag, 133, serviceBind.toByteArray());
		}

        private void sendServiceBind(String appId, String cId) throws IOException{
            ServiceBind serviceBind = ServiceBind.newBuilder().setApplicationId(appId).setClientId(cId).build();
            int tag = counter.getAndIncrement();
            logger.info("Send ServiceBind with appid [{}] and cid [{}]",appId,cId);
            handlerMap.put(tag, new ResponseHandler() {
                @Override
                public void onResponse(int tag, MessageLite message)
                        throws Exception {
                    ServiceBind bind = (ServiceBind) message;
                    logger.info("Bind successful!!! aid=["
                            + bind.getApplicationId() + "], cid=["
                            + bind.getClientId() + "]");
                    clientId = bind.getClientId();
                    sendDeviceAuthWithPullInfo();

                }
            });
            send(tag, 133, serviceBind.toByteArray());

        }

		private void sendServiceUnbind()
		        throws IOException {
			ServiceBind serviceUnbind = ServiceBind.newBuilder()
			        .setApplicationId(appId).setClientId(clientId).build();
			int tag = counter.getAndIncrement();

			handlerMap.put(tag, new ResponseHandler() {
				@Override
				public void onResponse(int tag, MessageLite message)
				        throws Exception {
					// disconnect, exit
					waitQuitStatus = true;
				}
			});
			send(tag, 134, serviceUnbind.toByteArray());
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
					logger.info("Ack response,status [{}]",rsp.getStatus());
					sendServiceUnbind();
				}
			});

			send(tag, 130, ac.toByteArray());
		}

		private void sendDeviceAuth(String deviceId, String deviceKey)
		        throws IOException {
			Credential auth = Credential.newBuilder().setId(deviceId)
			        .setSecureKey(deviceKey).build();
            logger.info("Send DeviceAuth with did:["+deviceId+"] and key:["+deviceKey+"].");
			int tag = counter.getAndIncrement();

			handlerMap.put(tag, new ResponseHandler() {
				@Override
				public void onResponse(int tag, MessageLite message)
				        throws Exception {
                    Response rsp = (Response) message;
					logger.info("Device auth successful,response status [{}], workmode [{}]",rsp.getStatus(),rsp.getWorkMode());
					sendServiceBind(appId);
                    inListenStatus = true;
				}
			});
			send(tag, 132, auth.toByteArray());
		}

        private void sendDeviceAuthWithPullInfo() throws IOException {
            Credential authWithPull = Credential.newBuilder().setId(deviceId).setSecureKey(deviceKey).setPullInfo(appId+"."+deviceId).build();
            logger.info("Send DeviceAuth with did [{}],dKey [{}] and pullinfo [{}]",new String[]{deviceId,deviceKey,appId+"."+deviceId});
            int tag = counter.getAndDecrement();
            handlerMap.put(tag,new ResponseHandler() {
                @Override
                public void onResponse(int tag, MessageLite message) throws Exception {
                    try{
                    ReserveInterval rsp = (ReserveInterval) message;
                    logger.info("Device auth successful,appId [{}], interval [{}]",rsp.getAid(),rsp.getNextInterval());

                    logger.info("has no message,so now exit!");
                   //waitQuitStatus = true;
                    //inListenStatus = true;
                    }catch (Exception ex){

                    }
                    //inListenStatus = true;
                }
            });
            send(tag,132,authWithPull.toByteArray());
        }

		private void sendDeviceRegister(String imei) throws IOException {
			DeviceRegister deviceRegister = DeviceRegister.newBuilder()
			        .setImei(imei).build();
            logger.info("Send DeviceRegister with imei:"+imei);
			int tag = counter.getAndIncrement();

			handlerMap.put(tag, new ResponseHandler() {
				@Override
				public void onResponse(int tag, MessageLite message)
				        throws Exception {
					Credential credential = (Credential) message;
					deviceId = credential.getId();
					deviceKey = credential.getSecureKey();
					logger.info("DeviceRegister response Did [{}] dKey [{}]",deviceId, deviceKey);

					sendDeviceAuth(deviceId, deviceKey);
				}
			});
			send(tag, 131, deviceRegister.toByteArray());
		}

		private void sendHeartbeatInit(String cause, int lastTimeout)
		        throws IOException {
			HeartbeatInit hbi = HeartbeatInit.newBuilder().setCause(cause)
			        .setLastException(HeartbeatException.valueOf(cause))
			        .setLastTimeout(lastTimeout)
			        .build();
			int tag = counter.getAndIncrement();

			handlerMap.put(tag, new ResponseHandler() {
				@Override
				public void onResponse(int tag, MessageLite message)
				        throws Exception {
					onHeartbeatResponse(tag, (HeartbeatResponse) message);
				}
			});
			send(tag, 0, hbi.toByteArray());
		}

		private String sendMessage(String content) throws ParseException,
		        IOException {
            String pattern = "{\"life\" : \"%d\", \"payload\" : \"%s\"}";
            String request = String.format(pattern, 86400, content);
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost post = new HttpPost("/1/api/" + appId + "/push/broadcast");
			byte[] token = (appId + ":" + appKey).getBytes("utf-8");
			String authorization = "Basic "
			        + new String(Base64.encodeBase64(token), "utf-8");
			System.out.println("authorization = " + authorization);
			post.addHeader("Authorization", authorization);
            post.addHeader("Content-type","application/json");
			post.setEntity(new ByteArrayEntity(request.getBytes("utf-8")));

			HttpResponse response = httpClient
			        .execute(new HttpHost(host, 8080), post);

			if (response.getStatusLine().getStatusCode() == 200) {

				String id = EntityUtils.toString(response.getEntity());
				return id;
			}
			return null;
		}

		@Override
		public void run() {
			while (!waitQuitStatus) {
				try {
					if (counter.get() == 0) {
						//sendHeartbeatInit("none", 0);
						sendDeviceRegister(imei);
					}
					if (inListenStatus) {
						pushService.schedule(new Runnable() {
							@Override
							public void run() {
								String id = null;
								try {
									id = sendMessage("push message at "
									        + new Date().toString());
									logger.info("Pushed message [" + id
									        + "] to simulator.");
                                    Thread.sleep(5*60*1000);
                                    sendDeviceAuthWithPullInfo();
                                    Thread.sleep(60*1000);
								} catch (ParseException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								} catch (InterruptedException ex){
                                    ex.printStackTrace();
                                }

                                logger.info("now should shutdown!!!");
                                waitQuitStatus = true;
							}
						}, 60, TimeUnit.SECONDS);

                        logger.info("============================================");
                        logger.info("broadcast one message for appid:[{}]",appId);
                        logger.info("now waiting for about 10 minutes to get message.");
                        logger.info("If want to get message,please confirm schedule info manully first.");

					}
					read();
				}catch (EOFException ex){
                    logger.error("Read exception,time out!");
                }catch (Exception e) {
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
