package ca.mcgill.ecse316.dnsclient;

public class DNSClient {

	// Default flag values
	static int timeout = 5; // Timeout before retransmitting an answered query (in seconds)
	static int maxRetries = 3; // # of retries to retransmit an answered query
	static int port = 53; // UDP port number of the DNS server
	static QueryType queryType = QueryType.A; // Query can be mail server, name server or IP address (type A)
	
	static String dnsServerAddr; // DNS server IPv4 address
	static String domName; // Domain name to query for

	public static void main(String[] args) {
		
		try {
			DNSParser.parse(args);
		}
		catch (Exception e) {
			System.out.println("ERROR\t" + e.getMessage());
			System.exit(1);
		}
		
		System.out.println("timeout : " + timeout);
		System.out.println("max retries : " + maxRetries);
		System.out.println("port : " + port);
		System.out.println("query type: " + queryType.name());
		System.out.println("dns addr: " + dnsServerAddr);
		System.out.println("dom name: " + domName);
		
		
		// Successful output
		System.out.println("DnsClient sending request for " + domName);
		System.out.println("Server: " + dnsServerAddr);
		System.out.println("Request type: " + queryType.name());
	}
}
