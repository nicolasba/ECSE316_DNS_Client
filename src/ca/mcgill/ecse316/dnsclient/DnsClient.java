package ca.mcgill.ecse316.dnsclient;

public class DnsClient {
	
	// Default flag values
	static int timeout = 5; // Timeout before retransmitting an answered query (in seconds)
	static int maxRetries = 3; // # of retries to retransmit an answered query
	static int port = 53; // UDP port number of the DNS server
	static QueryType queryType = QueryType.A; // Query can be mail server, name server or IP address (type A)
	
	static DnsPacket packet;
	
	static String dnsServerAddr; // DNS server IPv4 address
	static byte[] dnsServerAddrBytes; //DNS server IPv4 address without '.'
	static String domName; // Domain name to query for
	static String[] domNameLabels;

	public static void main(String[] args) {
		
		dnsServerAddr = "";
		dnsServerAddrBytes = new byte[4];
		domName = "";
		
		try {
			DnsParser.parse(args);
			packet = new DnsPacket();
			SocketClient.manageSocket(packet.message);
		}
		catch (Exception e) {
			System.out.println("ERROR\t" + e.getMessage());
			System.exit(1);
		}
				
		System.out.println("hex dump request packet:");
		printHexDump(packet.message);
		
		byte [] id = {(byte) 0xFF, (byte) 0xFF};
		int id2 = Byte.toUnsignedInt(id[0]) << 8 | Byte.toUnsignedInt(id[1]);
		System.out.println(String.format("id: 0x%08X", id2));
		
		byte b = (byte) 0xFF;
		int d = (int) b;
		int c = Byte.toUnsignedInt(b);
		System.out.println(String.format("b: 0x%08X",  c << 8));
	
		System.out.println(c);
		
		System.out.println("timeout : " + timeout);
		System.out.println("max retries : " + maxRetries);
		System.out.println("port : " + port);
		System.out.println("query type: " + queryType.name());
		System.out.println("dns addr: " + dnsServerAddr);
		System.out.println("dom name: " + domName);
		
		System.out.print("dom name labels: " );
		for (String i : domNameLabels)
			System.out.print(i + " ");
		System.out.println();
		
		// Successful output
		System.out.println("DnsClient sending request for " + domName);
		System.out.println("Server: " + dnsServerAddr);
		System.out.println("Request type: " + queryType.name());
	}
	
	public static void printHexDump(byte [] data) {
		
		System.out.println("-------------");
		System.out.println("Hex dump :");
		for (int i = 0; i < data.length; i++) {
			System.out.print(String.format("0x%02X ", data[i]));
			
			if ((i+1) % 10 == 0)
				System.out.println();
		}
		System.out.println();
		System.out.println("-------------");
		System.out.println();
		
	}
}
