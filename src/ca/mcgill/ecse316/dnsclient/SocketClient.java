package ca.mcgill.ecse316.dnsclient;

import java.net.*;
import static ca.mcgill.ecse316.dnsclient.DnsClient.*;

public class SocketClient {

	public static byte[] sendPacket(byte[] sendData) throws Exception {

		byte[] receiveData = new byte[1024];

		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress dnsServerIPAddr = InetAddress.getByAddress(dnsServerAddrBytes);

		clientSocket.setSoTimeout(timeout * 1000);		//Set timeout in ms
		System.out.println("dns server ip addr: " + dnsServerIPAddr);

		DatagramPacket requestPacket = new DatagramPacket(sendData, sendData.length, dnsServerIPAddr, port);
		clientSocket.send(requestPacket);

		DatagramPacket responsePacket = new DatagramPacket(receiveData, receiveData.length);
		clientSocket.receive(responsePacket);

		System.out.println("hello " + responsePacket.getAddress());
		System.out.println("Server says: " + new String(responsePacket.getData()));
		
		clientSocket.close();
		return responsePacket.getData();
	}
}
