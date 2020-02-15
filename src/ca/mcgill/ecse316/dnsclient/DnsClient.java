package ca.mcgill.ecse316.dnsclient;

import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import ca.mcgill.ecse316.dnsclient.DnsPacket.DnsPacketAnswer;

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

	static double responseTime;
	static int nbAttempts; // Keep track of the # of request attempts
	static boolean didReceive = false;

	public static void main(String[] args) {

		dnsServerAddr = "";
		dnsServerAddrBytes = new byte[4];
		domName = "";
		byte[] responseData = new byte[1024];

		// Parse user input
		try {
			DnsParser.parse(args);
		} catch (Exception e) {
			System.out.println("ERROR\t" + e.getMessage());
			System.exit(1);
		}

		System.out.println("DnsClient sending request for " + domName);
		System.out.println("Server: " + dnsServerAddr);
		System.out.println("Request type: " + queryType.name());
		System.out.println();

		// Send packets as long as there is no response and max retries are not exceeded
		for (nbAttempts = 0; nbAttempts < maxRetries && !didReceive; nbAttempts++) {

			System.out.println("Attempt " + (nbAttempts + 1) + " out of " + maxRetries);
			System.out.println("Sending request ...");

			try {
				requestPacket = new DnsPacket();
				responseData = SocketClient.sendPacket(requestPacket.message);
				responsePacket = new DnsPacket(ByteBuffer.wrap(responseData)); // Will throw exception if no response
				didReceive = true; // If we get to this point, we received a response
			} catch (SocketTimeoutException timeoutEx) {

				if (nbAttempts + 1 == maxRetries) {
					System.out.println("ERROR	No response: Maximum number of retries [" + maxRetries + "] exceeded");
					System.exit(1);
				}

			} catch (Exception e) {
				System.out.println("ERROR\t" + e.getMessage());
				System.exit(1);
			}

		}

		String attempts = (nbAttempts < 2) ? "attempt" : "attempts";
		System.out.printf("Response received after %.2f seconds (" + nbAttempts + " " + attempts + ")\n", responseTime);
		System.out.println();

		// RA : Print error if server can't recurse queries
		if (responsePacket.header.RA == 0)
			System.out.println("ERROR\t This server does not support recursive queries");

		// RCODE: Errors
		switch (responsePacket.header.RCODE) {
		case 0x1:
			System.out.println("ERROR\t Name server was unable to interpret the query");
			break;
		case 0x2:
			System.out.println("ERROR\t Server failure");
			break;
		case 0x3:
			System.out.println("ERROR\t Domain name referenced in the query does not exist");
			break;
		case 0x4:
			System.out.println("ERROR\t Name server does not support the requested kind of query");
			break;
		case 0x5:
			System.out.println("ERROR\t Name server refuses to perform the requested operation for policy reasons");
			break;
		}

		printAnswerContent(responsePacket.answer, "Answer");
		printAnswerContent(responsePacket.additional, "Additional");

		System.out.println("\nHex dump request packet:");
		printHexDump(requestPacket.message);

		System.out.println("\nHex dump response packet:");
		printHexDump(responseData);

		System.out.println("timeout : " + timeout);
		System.out.println("max retries : " + maxRetries);
		System.out.println("port : " + port);
		System.out.println("query type: " + queryType.name());
		System.out.println("dns addr: " + dnsServerAddr);
		System.out.println("dom name: " + domName);

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
	}

	public static void printAnswerContent(DnsPacketAnswer answer, String s) {

		int nbRecords = 0;

		if (s.equals("Answer"))
			nbRecords = responsePacket.header.ANCOUNT;
		else if (s.equals("Additional"))
			nbRecords = responsePacket.header.ARCOUNT;

		// Answer records
		System.out.println("*** " + s + " Section (" + nbRecords + " records) ***");

		for (int i = 0; i < nbRecords; i++) {

			switch (answer.rrs[i].TYPE) {
			case 0x0001:
				System.out.print("IP\t" + answer.rrs[i].RDATA + "\t");
				break;
			case 0x0002:
				System.out.print("NS\t" + answer.rrs[i].RDATA + "\t");
				break;
			case 0x0005:
				System.out.print("CNAME\t" + answer.rrs[i].RDATA + "\t");
				break;
			case 0x000f:
				System.out.print("MX\t" + answer.rrs[i].EXCHANGE + "\t\t" + answer.rrs[i].PREFERENCE + "\t");
				break;
			}

			// Seconds can cache
			System.out.print(answer.rrs[i].TTL + "s\t");

			// Auth/nonauth
			if (responsePacket.header.AA == 0x1)
				System.out.println("auth");
			else
				System.out.println("nonauth");
		}

		if (nbRecords == 0)
			System.out.println("NOT FOUND");
		System.out.println();
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
