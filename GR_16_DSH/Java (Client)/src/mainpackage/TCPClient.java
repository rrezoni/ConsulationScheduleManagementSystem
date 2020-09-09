package mainpackage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public final class TCPClient {

	public static Socket socket;
	public static PrintWriter pw;
	public static BufferedReader br;

	public static void initSocket() {

		try {

			File confFile = new File(TCPClient.class.getResource("conf.xml").getPath());
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(confFile);
			doc.getDocumentElement().normalize();
			String host = doc.getElementsByTagName("ip").item(1).getTextContent();
			int port = Integer.parseInt(doc.getElementsByTagName("port").item(1).getTextContent());

			socket = new Socket(host, port);

			pw = new PrintWriter(socket.getOutputStream(), true);
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static void close() {
		try {
			socket.close();
			pw.close();
			br.close();
			socket = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
