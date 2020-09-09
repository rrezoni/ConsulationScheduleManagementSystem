package mainpackage;

import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import com.google.gson.Gson;

public final class UDPClient {

	public static DatagramSocket ds;
	public static LinkedList<String> receivedVideoData;
	public static LinkedList<String> receivedAudioData;
	public static boolean onACall;
	public static boolean usingCamera;
	public static boolean usingMic;

	public static void initDatagramSocket() {

		receivedVideoData = new LinkedList<String>();
		receivedAudioData = new LinkedList<String>();
		onACall = true;

		try {

			File confFile = new File(WebSocketClient.class.getResource("conf.xml").getPath());
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(confFile);
			doc.getDocumentElement().normalize();

			ds = new DatagramSocket();
			ds.setReceiveBufferSize(65536);
			ds.setSendBufferSize(65536);
			String connectString = "{\"type\":\"Connect\",\"sourceID\":\"" + LoggedUser.id + "\"}";
			ds.send(new DatagramPacket(connectString.getBytes(), connectString.length(),
					InetAddress.getByName(doc.getElementsByTagName("ip").item(0).getTextContent()),
					Integer.parseInt(doc.getElementsByTagName("port").item(0).getTextContent())));

			Thread datagramThread = new Thread(new Runnable() {

				@Override
				public void run() {

					while (onACall) {

						try {

							byte[] receivedBytes = new byte[65536];
							DatagramPacket receivedPacket = new DatagramPacket(receivedBytes, 65536);
							ds.receive(receivedPacket);

							if (new String(receivedBytes).contains("\"type\":\"VideoFrame\"")) {
								VideoFrame videoFrame = new Gson().fromJson(new String(receivedBytes).trim(),
										VideoFrame.class);
								receivedVideoData.addFirst(videoFrame.getFrameBytes());
							} else if (new String(receivedBytes).contains("\"type\":\"AudioData\""))
								receivedAudioData.add(new String(receivedBytes).trim());

						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				}

			});
			datagramThread.start();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void close() {
		ds = null;
		receivedAudioData = null;
		receivedVideoData = null;
		onACall = false;
		usingCamera = false;
		usingMic = false;
	}

//	public static InetAddress getLocalAddress() {
//
//		InetAddress localAddress = null;
//		try {
//
//			outer: for (NetworkInterface nw : Collections.list(NetworkInterface.getNetworkInterfaces())) {
//				if (nw.isLoopback())
//					continue;
//				if (!nw.isUp())
//					continue;
//				if (nw.isVirtual())
//					continue;
//				for (InetAddress inetAddress : Collections.list(nw.getInetAddresses())) {
//					if (inetAddress instanceof Inet6Address)
//						continue;
//					localAddress = inetAddress;
//					break outer;
//				}
//			}
//			return localAddress;
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		}
//	}

}
