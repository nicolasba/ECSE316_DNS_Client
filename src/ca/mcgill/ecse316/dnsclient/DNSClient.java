package ca.mcgill.ecse316.dnsclient;

import java.util.Arrays;

public class DNSClient {

	// Default flag values
	static int timeout = 5; // Timeout before retransmitting an answered query (in seconds)
	static int maxRetries = 3; // # of retries to retransmit an answered query
	static int port = 53; // UDP port number of the DNS server
	static QueryType queryType = QueryType.TypeA; // Query can be mail server, name server or IP address (type A)
	
	static String dnsServerAddr; // DNS server IPv4 address
	static String domName; // Domain name to query for

	public static void main(String[] args) {
		
		try {
			parse(args);
		}
		catch (Exception e) {
			System.out.println("ERROR\t" + e.getMessage());
			System.exit(1);
		}
		
		System.out.println("timeout : " + timeout);
		System.out.println("max retries : " + maxRetries);
		System.out.println("port : " + port);
		System.out.println("query type: " + queryType.name());
		
		
	}

	public static void parse(String[] args) throws Exception {

		String[] tempDnsAddr;
		
		if (args.length < 2)
			throw new InputSyntaxException("Insufficient number of arguments");

		// Second to last argument is DNS server IPv4 address
		dnsServerAddr = args[args.length - 2];
		
		//First character should be '@'
		if (dnsServerAddr.charAt(0) != '@')
			throw new InputSyntaxException("Incorrect DNS server address, missing @");
		dnsServerAddr = dnsServerAddr.substring(1);		//Get rid of '@'
			
		tempDnsAddr = dnsServerAddr.split("\\.");
		
		// DNS server address must be of the form a.b.c.d
		if (tempDnsAddr.length != 4)
			throw new InputSyntaxException("Incorrect DNS server address");
		
		for (int i = 0; i < 4; i++) {
			try {
				Integer.valueOf(tempDnsAddr[i]);				
			}
			catch (Exception e) {
				throw new InputSyntaxException("Incorrect DNS server address");
			}
		}

		// Last argument is domain name
		domName = args[args.length - 1];
		
		// Parse optional flags
		if (args.length > 2) 
			parseFlags(Arrays.copyOfRange(args, 0, args.length -2));
			
	}

	public static void parseFlags(String[] flags) throws Exception{
		
		int i = 0;
		
		while (i < flags.length) {
			
			String flag = flags[i];
			
			switch (flag) {
			
			case "-t" :
				if (!(i + 1 < flags.length)) throw new InputSyntaxException("Missing timeout value");
				try { timeout = Integer.valueOf(flags[i+1]); } 
				catch (Exception e) { throw new InputSyntaxException("Invalid timeout value"); }
				i += 2;
				break;
				
			case "-r" :
				if (!(i + 1 < flags.length)) throw new InputSyntaxException("Missing max retries value");
				try { maxRetries = Integer.valueOf(flags[i+1]); } 
				catch (Exception e) { throw new InputSyntaxException("Invalid max retries value"); }
				i += 2;
				break;
				
			case "-p" :
				if (!(i + 1 < flags.length)) throw new InputSyntaxException("Missing port value");
				try { port = Integer.valueOf(flags[i+1]); } 
				catch (Exception e) { throw new InputSyntaxException("Invalid port value"); }
				i += 2;
				break;
			
			case "-mx":
				if (queryType == QueryType.NameServer)
					 throw new InputSyntaxException("Only one of the mx/ns flags might be used");	
				queryType = QueryType.MailServer;
				i++;
				break;
			
			case "-ns":
				if (queryType == QueryType.MailServer)
					 throw new InputSyntaxException("Only one of the mx/ns flags might be used");	
				queryType = QueryType.NameServer;
				i++;
				break;
				
			default:
				throw new InputSyntaxException("Undefined flag(s)");
			}
		}
	}

}
