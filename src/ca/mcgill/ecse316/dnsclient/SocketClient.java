package ca.mcgill.ecse316.dnsclient;

import java.net.*;

public class SocketClient {

	public static byte[] sendPacket(byte[] sendData) throws Exception {

		byte[] receiveData = new byte[1024];

		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress dnsServerIPAddr = InetAddress.getByAddress(DnsClient.dnsServerAddrBytes);

		clientSocket.setSoTimeout(DnsClient.timeout * 1000);		//Set timeout in ms
//		System.out.println("dns server ip addr: " + dnsServerIPAddr);

		DatagramPacket requestPacket = new DatagramPacket(sendData, sendData.length, dnsServerIPAddr, DnsClient.port);
		clientSocket.send(requestPacket);

		DnsClient.responseTime = System.currentTimeMillis();
		DatagramPacket responsePacket = new DatagramPacket(receiveData, receiveData.length);
		clientSocket.receive(responsePacket);
		DnsClient.responseTime = (System.currentTimeMillis() - DnsClient.responseTime) / 1000.0;

//		System.out.println("hello " + responsePacket.getAddress());
//		System.out.println("Server says: " + new String(responsePacket.getData()));
		
		clientSocket.close();
		return responsePacket.getData();
	}
}
