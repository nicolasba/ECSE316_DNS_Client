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
				printHexDump(responseData);
				responsePacket = new DnsPacket(ByteBuffer.wrap(responseData)); // Will throw exception if no response
				didReceive = true; // If we get to this point, we received a response
			} catch (SocketTimeoutException timeoutEx) {

				if (nbAttempts + 1 == maxRetries) {
					System.out.println("ERROR	No response: Maximum number of retries [" + maxRetries + "] exceeded");
					System.exit(1);
				}

			} catch (Exception e) {
				System.out.println("ERROR\t" + e.getMessage());
//				System.exit(1);
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
			System.out.print("ERROR\t Name server was unable to interpret the query");
			break;
		case 0x2:
			System.out.print("ERROR\t Server failure");
			break;
		case 0x3:
			System.out.print("ERROR\t Domain name referenced in the query does not exist");
			break;
		case 0x4:
			System.out.print("ERROR\t Name server does not support the request kind of query");
			break;
		case 0x5:
			System.out.print("ERROR\t Name server refuses to perform the requested operation for policy reasons");
			break;
		}

		if (responsePacket.header.RCODE != 0)
			System.out.println(":\tRCODE = " + responsePacket.header.RCODE);

		printAnswerContent(responsePacket.answer, "Answer");
		printAnswerContent(responsePacket.additional, "Additional");
	}

	public static void printAnswerContent(DnsPacketAnswer answer, String s) {

		int nbRecords = 0;
		boolean unsupportedType = false;

		if (s.equals("Answer"))
			nbRecords = responsePacket.header.ANCOUNT;
		else if (s.equals("Additional"))
			nbRecords = responsePacket.header.ARCOUNT;

		// Answer records
		System.out.println("*** " + s + " Section (" + nbRecords + " records) ***");

		for (int i = 0; i < nbRecords; i++) {

			unsupportedType = false;

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
			default:
				System.out.print("ERROR\tResource record with unsupported type: ");
				System.out.println(String.format("0x%02X ", answer.rrs[i].TYPE));
				unsupportedType = true;
			}

			//Only print the following for supported type responses
			if (!unsupportedType) {
				// Seconds can cache
				System.out.print(answer.rrs[i].TTL + "s\t");

				// Auth/nonauth
				if (responsePacket.header.AA == 0x1)
					System.out.println("auth");
				else
					System.out.println("nonauth");
			}
		}

		if (nbRecords == 0)
			System.out.println("NOT FOUND");
		System.out.println();
	}

	public static void printHexDump(byte[] data) {

		System.out.println("-------------");
		System.out.println("Hex dump :");
		for (int i = 0; i < data.length; i++) {

			if (i % 10 == 0)
				System.out.print(i + ": ");

			System.out.print(String.format("0x%02X ", data[i]));

			if ((i + 1) % 10 == 0) {

				System.out.print("\t");

				for (int j = i - 9; j < i + 1; j++) {
					System.out.print((char) data[j]);
				}
				
				System.out.println();
			}
		}
		System.out.println();
		System.out.println("-------------");
		System.out.println();

	}
}
