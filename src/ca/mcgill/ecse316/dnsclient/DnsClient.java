package ca.mcgill.ecse316.dnsclient;

import java.nio.ByteBuffer;

public class DnsClient {

	// Default flag values
	static int timeout = 5; // Timeout before retransmitting an answered query (in seconds)
	static int maxRetries = 3; // # of retries to retransmit an answered query
	static int port = 53; // UDP port number of the DNS server
	static QueryType queryType = QueryType.A; // Query can be mail server, name server or IP address (type A)

	static DnsPacket requestPacket;
	static DnsPacket responsePacket;

	static String dnsServerAddr; // DNS server IPv4 address
	static byte[] dnsServerAddrBytes; // DNS server IPv4 address without '.'
	static String domName; // Domain name to query for
	static String[] domNameLabels;

	public static void main(String[] args) {

		dnsServerAddr = "";
		dnsServerAddrBytes = new byte[4];
		domName = "";
		byte[] responseData = new byte[1024];

		try {
			DnsParser.parse(args);
			requestPacket = new DnsPacket();
			responseData = SocketClient.sendPacket(requestPacket.message);
			responsePacket = new DnsPacket(ByteBuffer.wrap(responseData));
		} catch (Exception e) {
			System.out.println("ERROR\t" + e.getMessage());
			System.exit(1);
		}

		System.out.println("\nHex dump request packet:");
		printHexDump(requestPacket.message);

		System.out.println("\nHex dump response packet:");
		printHexDump(responseData);

		System.out.println("id: " + responsePacket.header.randID);
		System.out.println("QR: " + responsePacket.header.QR);
		System.out.println("OPCODE: " + responsePacket.header.OPCODE);
		System.out.println("AA: " + responsePacket.header.AA);
		System.out.println("TC: " + responsePacket.header.TC);
		System.out.println("RD: " + responsePacket.header.RD);
		System.out.println("RA: " + responsePacket.header.RA);
		System.out.println("Z: " + responsePacket.header.Z);
		System.out.println("RCODE: " + responsePacket.header.RCODE);
		System.out.println("QDCOUNT: " + responsePacket.header.QDCOUNT);
		System.out.println("ANCOUNT: " + responsePacket.header.ANCOUNT);
		System.out.println("NSCOUNT: " + responsePacket.header.NSCOUNT);
		System.out.println("ARCOUNT: " + responsePacket.header.ARCOUNT);

		System.out.println();
		System.out.println();

		System.out.println("Answer: ");

		for (int i = 0; i < responsePacket.answer.rrs.length; i++) {
			System.out.println("NAME: " + responsePacket.answer.rrs[i].NAME);
			System.out.println("TYPE: " + responsePacket.answer.rrs[i].TYPE);
			System.out.println("CLASS: " + responsePacket.answer.rrs[i].CLASS);
			System.out.println("TTL: " + responsePacket.answer.rrs[i].TTL);
			System.out.println("RDLENGTH: " + responsePacket.answer.rrs[i].RDLENGTH);
			System.out.println("RDATA: " + responsePacket.answer.rrs[i].RDATA);
			System.out.println("PREFERENCE: " + responsePacket.answer.rrs[i].PREFERENCE);
			System.out.println("EXCHANGE: " + responsePacket.answer.rrs[i].EXCHANGE);
			System.out.println();
		}

		System.out.println("\nAuthority: ");

		for (int i = 0; i < responsePacket.auth.rrs.length; i++) {
			System.out.println("NAME: " + responsePacket.auth.rrs[i].NAME);
			System.out.println("TYPE: " + responsePacket.auth.rrs[i].TYPE);
			System.out.println("CLASS: " + responsePacket.auth.rrs[i].CLASS);
			System.out.println("TTL: " + responsePacket.auth.rrs[i].TTL);
			System.out.println("RDLENGTH: " + responsePacket.auth.rrs[i].RDLENGTH);
			System.out.println("RDATA: " + responsePacket.auth.rrs[i].RDATA);
			System.out.println("PREFERENCE: " + responsePacket.auth.rrs[i].PREFERENCE);
			System.out.println("EXCHANGE: " + responsePacket.auth.rrs[i].EXCHANGE);
			System.out.println();
		}

		System.out.println("\nAdditional: ");
		for (int i = 0; i < responsePacket.additional.rrs.length; i++) {
			System.out.println("NAME: " + responsePacket.additional.rrs[i].NAME);
			System.out.println("TYPE: " + responsePacket.additional.rrs[i].TYPE);
			System.out.println("CLASS: " + responsePacket.additional.rrs[i].CLASS);
			System.out.println("TTL: " + responsePacket.additional.rrs[i].TTL);
			System.out.println("RDLENGTH: " + responsePacket.additional.rrs[i].RDLENGTH);
			System.out.println("RDATA: " + responsePacket.additional.rrs[i].RDATA);
			System.out.println("PREFERENCE: " + responsePacket.additional.rrs[i].PREFERENCE);
			System.out.println("EXCHANGE: " + responsePacket.additional.rrs[i].EXCHANGE);
			System.out.println();
		}

//		System.out.println("timeout : " + timeout);
//		System.out.println("max retries : " + maxRetries);
//		System.out.println("port : " + port);
//		System.out.println("query type: " + queryType.name());
//		System.out.println("dns addr: " + dnsServerAddr);
//		System.out.println("dom name: " + domName);

//		System.out.print("dom name labels: " );
//		for (String i : domNameLabels)
//			System.out.print(i + " ");
//		System.out.println();

		// Successful output
//		System.out.println("DnsClient sending request for " + domName);
//		System.out.println("Server: " + dnsServerAddr);
//		System.out.println("Request type: " + queryType.name());
	}

	public static void printHexDump(byte[] data) {

		System.out.println("-------------");
		System.out.println("Hex dump :");
		for (int i = 0; i < data.length; i++) {
			System.out.print(String.format("0x%02X ", data[i]));

			if ((i + 1) % 10 == 0)
				System.out.println();
		}
		System.out.println();
		System.out.println("-------------");
		System.out.println();

	}
}
