//package mainpackage;
//
//import java.io.File;
//import java.io.IOException;
//import java.net.Inet6Address;
//import java.net.InetAddress;
//import java.net.NetworkInterface;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//import java.util.Collections;
//
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
//
//import org.w3c.dom.Document;
//import org.xml.sax.SAXException;
//
//public final class DBConnection {
//
//	public static Connection getConnection(String user, String password)
//			throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException {
//
//		File confFile = new File(WebSocketClient.class.getResource("conf.xml").getPath());
//		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//		Document doc = dBuilder.parse(confFile);
//		doc.getDocumentElement().normalize();
//
//		Class.forName("com.mysql.cj.jdbc.Driver");
//
//		return DriverManager.getConnection("jdbc:mysql://" + doc.getElementsByTagName("ip").item(0).getTextContent()
//				+ "/dsh_database?user=" + user + "&password=" + password);
//	}
//
//	public static String getHostName() {
//
//		String hostAddress = null;
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
//					hostAddress = inetAddress.getHostAddress();
//					break outer;
//				}
//			}
//			return hostAddress;
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		}
//	}
//
//}
