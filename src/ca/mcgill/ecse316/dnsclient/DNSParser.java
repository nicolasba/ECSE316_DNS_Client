package ca.mcgill.ecse316.dnsclient;

import java.util.Arrays;

public class DNSParser {
	
	public static void parse(String[] args) throws Exception {

		String[] tempDnsAddr;
		
		if (args.length < 2)
			throw new InputSyntaxException("Insufficient number of arguments");

		// Second to last argument is DNS server IPv4 address
		DNSClient.dnsServerAddr = args[args.length - 2];
		
		//First character should be '@'
		if (DNSClient.dnsServerAddr.charAt(0) != '@')
			throw new InputSyntaxException("Incorrect DNS server address");
		DNSClient.dnsServerAddr = DNSClient.dnsServerAddr.substring(1);		//Get rid of '@'
			
		tempDnsAddr = DNSClient.dnsServerAddr.split("\\.");
		
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
		DNSClient.domName = args[args.length - 1];
		
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
				try { DNSClient.timeout = Integer.valueOf(flags[i+1]); } 
				catch (Exception e) { throw new InputSyntaxException("Invalid timeout value"); }
				i += 2;
				break;
				
			case "-r" :
				if (!(i + 1 < flags.length)) throw new InputSyntaxException("Missing max retries value");
				try { DNSClient.maxRetries = Integer.valueOf(flags[i+1]); } 
				catch (Exception e) { throw new InputSyntaxException("Invalid max retries value"); }
				i += 2;
				break;
				
			case "-p" :
				if (!(i + 1 < flags.length)) throw new InputSyntaxException("Missing port value");
				try { DNSClient.port = Integer.valueOf(flags[i+1]); } 
				catch (Exception e) { throw new InputSyntaxException("Invalid port value"); }
				i += 2;
				break;
			
			case "-mx":
				if (DNSClient.queryType == QueryType.NS)
					 throw new InputSyntaxException("Only one of the mx/ns flags might be used");	
				DNSClient.queryType = QueryType.MX;
				i++;
				break;
			
			case "-ns":
				if (DNSClient.queryType == QueryType.MX)
					 throw new InputSyntaxException("Only one of the mx/ns flags might be used");	
				DNSClient.queryType = QueryType.NS;
				i++;
				break;
				
			default:
				throw new InputSyntaxException("Undefined flag(s)");
			}
		}
	}

}
