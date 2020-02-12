package ca.mcgill.ecse316.dnsclient;

import java.util.Arrays;
import java.util.Random;


public class DnsPacket {
	
	private DnsPacketHeader header = new DnsPacketHeader();
	
	public DnsPacketHeader make() {	
		
		return header;
	}
	
	
	private class DnsPacketHeader{		
		
		char fields[];			
		
		//First field
		int randID;			
		
		//Second field
		int QR;					// Message is a query(0) / response(1)
		int OPCODE;				// Kind of query (0 is standard)
		int AA;					// Server is (1) or not (0) an authority for domain name
		int TC;					// Message was truncated
		int RD;					// Recursive queries? Yes (1) / No (0)
		int RA;					// Recursive queries supported by server ? Yes (1) / No (0)
		int Z;					// Does not mean anything
		int RCODE;				// Error codes
		
		int QR_pos = 15;
		int OPCODE_pos = 11;			
		int AA_pos = 10;
		int TC_pos = 9;
		int RD_pos = 8;
		int RA_pos = 7;
		int Z_pos = 4;
		int RCODE_pos = 0;
		
		//3rd-6th fields
		int QDCOUNT;			// Number of entries in the Question section
		int ANCOUNT;			// Number of resource records (RRs) in the Answer section
		int NSCOUNT;			// Number of name server RRs in the Authority section
		int ARCOUNT;			// Number of RRs in the Additional section
		
		// Constructor used to instantiate request packet headers
		DnsPacketHeader(){
			
			//Request packet values
			fields = new char[6];				// A DNS packet header contains 6 16-bit field
			QR = OPCODE = AA = TC = RA = Z = RCODE = 0;
			RD = 1;
			QDCOUNT = 1;
			ANCOUNT = NSCOUNT = ARCOUNT = 0;
			
			byte[] tempRandID = new byte[2];	// Id must be random 16-bit number (byte is 8 bits long)
			Random rand = new Random();
			rand.nextBytes(tempRandID);			// Generate random 16-bit ID
			
			
			randID = tempRandID[0] << 8 | tempRandID[1];
			fields[0] = (char) randID;
			fields[1] = (char) (QR << QR_pos | OPCODE << OPCODE_pos | AA << AA_pos | TC << TC_pos
					| RD << RD_pos | RA << RA_pos | Z << Z_pos | RCODE << RCODE_pos);
			fields[2] = (char) QDCOUNT;
			fields[3] = (char) ANCOUNT;
			fields[4] = (char) NSCOUNT;
			fields[5] = (char) ARCOUNT;
		}
		
		//Constructor to instantiate response packet headers (parsing done according to DNS protocol)
		DnsPacketHeader(char[] response_fields){
			
			fields = Arrays.copyOf(response_fields, response_fields.length);
			
			//First field
			randID = fields[0];
			
			//Second field
			QR = ((0x1 << QR_pos) & fields[1]) >> QR_pos;
			OPCODE = ((0xF << OPCODE_pos) & fields[1]) >> OPCODE_pos;
			AA = ((0x1 << AA_pos) & fields[1]) >> AA_pos;
			TC = ((0x1 << TC_pos) & fields[1]) >> TC_pos;
			RD = ((0x1 << RD_pos) & fields[1]) >> RD_pos;
			RA = ((0x1 << RA_pos) & fields[1]) >> RA_pos;
			Z = ((0x7 << Z_pos) & fields[1]) >> Z_pos;
			RCODE = ((0xF << RCODE_pos) & fields[1]) >> RCODE_pos;
			
			//3rd-6th fields
			QDCOUNT = fields[2];
			ANCOUNT = fields[3];
			NSCOUNT = fields[4];
			ARCOUNT = fields[5];
		}
		
	}

}
