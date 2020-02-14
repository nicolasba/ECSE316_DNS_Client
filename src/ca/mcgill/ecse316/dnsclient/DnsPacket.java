package ca.mcgill.ecse316.dnsclient;

import java.nio.ByteBuffer;

import java.util.Arrays;
import java.util.Random;

public class DnsPacket {

	ByteBuffer buffer;
	byte[] message;

	DnsPacketHeader header;
	DnsPacketQuestion question;
	DnsPacketAnswer answer;
	DnsPacketAnswer auth;
	DnsPacketAnswer additional;

	// Constructor used for request packets
	public DnsPacket() {

		buffer = ByteBuffer.allocate(1024);
		header = new DnsPacketHeader();
		question = new DnsPacketQuestion();

		// Concatenate byte buffers from header and question section
		buffer.put(header.buffer);
		buffer.put(question.buffer);

		// Get byte[] from byte buffer
		message = Arrays.copyOf(buffer.array(), buffer.position());
	}

	// Constructor used for response packets
	public DnsPacket(ByteBuffer response) {

		// The values from each section will be stored in their corresponding instances
		System.out.println("position before header: " + response.position());
		header = new DnsPacketHeader(response);
		System.out.println("position after header: " + response.position());
		question = new DnsPacketQuestion(response);
		System.out.println("position after question: " + response.position());
		answer = new DnsPacketAnswer(response, 0);
		auth = new DnsPacketAnswer(response, 1);
		additional = new DnsPacketAnswer(response, 2);
	}

	/************************** PACKET HEADER **************************/

	 class DnsPacketHeader {

		ByteBuffer buffer;

		// First field
		int randID;

		// Second field
		int QR; // Message is a query(0) / response(1)
		int OPCODE; // Kind of query (0 is standard)
		int AA; // Server is (1) or not (0) an authority for domain name
		int TC; // Message was truncated
		int RD; // Recursive queries? Yes (1) / No (0)
		int RA; // Recursive queries supported by server ? Yes (1) / No (0)
		int Z; // Does not mean anything
		int RCODE; // Error codes

		// 3rd-6th fields
		int QDCOUNT; // Number of entries in the Question section
		int ANCOUNT; // Number of resource records (RRs) in the Answer section
		int NSCOUNT; // Number of name server RRs in the Authority section
		int ARCOUNT; // Number of RRs in the Additional section

		// Constructor used to instantiate request packet headers
		DnsPacketHeader() {

			// Request packet values
			buffer = ByteBuffer.allocate(12); // A DNS packet header contains 12 8-bit fields
			QR = OPCODE = AA = TC = RA = Z = RCODE = 0;
			RD = 1;
			QDCOUNT = 1;
			ANCOUNT = NSCOUNT = ARCOUNT = 0;

			byte[] tempRandID = new byte[2]; // Id must be random 16-bit number (byte is 8 bits long)
			Random rand = new Random();
			rand.nextBytes(tempRandID); // Generate random 16-bit ID
			randID = Byte.toUnsignedInt(tempRandID[0]) << 8 | Byte.toUnsignedInt(tempRandID[1]);

			byte[] last3Rows = { 0x0, 0x0, 0x0, 0x0, 0x0, 0x0 };

			buffer.put(tempRandID[0]);
			buffer.put(tempRandID[1]);

			buffer.put((byte) 0x01); // 2nd row header
			buffer.put((byte) 0x00);

			buffer.put((byte) 0x00); // QDCOUNT
			buffer.put((byte) 0x01);

			buffer.put(last3Rows); // Other counts

			// Need to flip (to set limit) so that buffers can be concatenated into one
			// final buffer
			buffer.flip();
		}

		// Constructor to instantiate response packet headers (parsing done according to
		// DNS protocol)
		DnsPacketHeader(ByteBuffer response) {

			buffer = response;

			// First field
			randID = buffer.getChar();
//			randID = Byte.toUnsignedInt(buffer.get()) << 8 |  Byte.toUnsignedInt(fields[1]);

			// Second row (Do logical shift right instead of arithmetic)
			byte lefthalf = buffer.get();
			byte righthalf = buffer.get();
			QR = ((0x1 << 7) & lefthalf) >>> 7;
			OPCODE = ((0xF << 3) & lefthalf) >>> 3;
			AA = ((0x1 << 2) & lefthalf) >>> 2;
			TC = ((0x1 << 1) & lefthalf) >>> 1;
			RD = 0x1 & lefthalf;
			RA = ((0x1 << 7) & righthalf) >>> 7;
			Z = ((0x7 << 4) & righthalf) >>> 4;
			RCODE = 0xF & righthalf;

			// 3rd-6th fields
			QDCOUNT = buffer.getChar();
			ANCOUNT = buffer.getChar();
			NSCOUNT = buffer.getChar();
			ARCOUNT = buffer.getChar();
		}

	}

	/************************** PACKET QUESTION SECTION **************************/

	 class DnsPacketQuestion {

		ByteBuffer buffer;

		// Request
		DnsPacketQuestion() {

			buffer = ByteBuffer.allocate(1024);

			// Number of labels in domain name. Ex: 3 labels in "www.mcgill.ca"
			int domNameLabelsLength = DnsClient.domNameLabels.length;

			for (int i = 0; i < domNameLabelsLength; i++) {
				byte[] labelChars = DnsClient.domNameLabels[i].getBytes();
				buffer.put((byte) labelChars.length); // Store number of character in label first
				buffer.put(labelChars); // Store sequence of characters
			}

			buffer.put((byte) 0x0); // End of QNAME

			// Store QTYPE 16-bit code
			switch (DnsClient.queryType) {
			case A:
				buffer.putChar((char) 0x0001);
				break;
			case NS:
				buffer.putChar((char) 0x0002);
				break;
			case MX:
				buffer.putChar((char) 0x000f);
				break;
			}

			// Store QCLASS = 1 (Internet)
			buffer.putChar((char) 0x0001);

			buffer.flip();
		}

		// Response (Ignore the data from this section in response message)
		DnsPacketQuestion(ByteBuffer response) {

			buffer = response;

			// Number of labels in domain name. Ex: 3 labels in "www.mcgill.ca"
			int domNameLabelsLength = DnsClient.domNameLabels.length;

			for (int i = 0; i < domNameLabelsLength; i++) {
				byte[] labelChars = DnsClient.domNameLabels[i].getBytes();
				buffer.get(); // Remove number of characters in label
				buffer.get(new byte[labelChars.length]); // Remove sequence of characters
			}

			buffer.get(); // End of QNAME

			buffer.getChar(); // Remove QTYPE
			buffer.getChar(); // Remove QCLASS
		}

	}

	/**************** PACKET ANSWER (ANS/AUTH/ADD) SECTION ****************/

	 class DnsPacketAnswer {

		ByteBuffer buffer;
		int nbRecords; // # of RRs in this answer
		DnsPacketAnswerRR[] rrs;

		// This section is only relevant for response packets
		DnsPacketAnswer(ByteBuffer response, int answerType) {

			buffer = response;

			if (answerType == 0)
				nbRecords = header.ANCOUNT; // # of RRs in Answer
			else if (answerType == 1)
				nbRecords = header.NSCOUNT; // # of RRs in Authority
			else if (answerType == 2)
				nbRecords = header.ARCOUNT; // # of RRs in Additional

			rrs = new DnsPacketAnswerRR[nbRecords];
			
			for (int i = 0; i < nbRecords; i++) {

				rrs[i] = new DnsPacketAnswerRR();
				parseLabels(response, rrs[i], "NAME");

				rrs[i].TYPE = response.getChar();
				rrs[i].CLASS = response.getChar();
				rrs[i].TTL = ((long) response.getChar()) << 16 | response.getChar();
				rrs[i].RDLENGTH = response.getChar();

				// RDATA for TYPE A is an IPv4 address
				if (rrs[i].TYPE == 0x0001) {

					rrs[i].RDATA += Byte.toUnsignedInt(response.get());
					rrs[i].RDATA += ".";
					rrs[i].RDATA += Byte.toUnsignedInt(response.get());
					rrs[i].RDATA += ".";
					rrs[i].RDATA += Byte.toUnsignedInt(response.get());
					rrs[i].RDATA += ".";
					rrs[i].RDATA += Byte.toUnsignedInt(response.get());

				} else if (rrs[i].TYPE == 0x0002) { // RDATA for NS is a sequence of labels
					parseLabels(response, rrs[i], "RDATA");

				} else if (rrs[i].TYPE == 0x0005) { // RDATA for CNAME is a sequence of labels
					parseLabels(response, rrs[i], "RDATA");

				} else if (rrs[i].TYPE == 0x000F) { // RDATA for MX is a sequence of labels
					rrs[i].PREFERENCE += response.getChar();
					parseLabels(response, rrs[i], "EXCHANGE");
				}
			}
		}

		void parseLabels(ByteBuffer response, DnsPacketAnswerRR rr, String s) {

			// First byte indicates length of label
			byte nbLabelChars = response.get();
			boolean foundPointer = false;
			int positionAfterPointer = 0;

			// As long as we haven't encountered 0x0, we have to keep reading labels
			while (nbLabelChars != 0) {
				
				//Found compression pointer
				if (nbLabelChars < 0) {
					
					//Go back 1 byte to read 2 bytes containing the offset
					response.position(response.position() - 1); 
					
					int offset = response.getChar();
					offset = offset & 0x3FFF;		//Only consider the last 14 bits from the 2 bytes
					
					//Keep track of position after pointer (only after the first pointer encountered, 
					//there can be nested pointers)
					if (!foundPointer)
						positionAfterPointer = response.position(); 
					
					foundPointer = true;
					
					response.position(offset);	//Reposition the buffer to the offset
					nbLabelChars = response.get();
				}

				byte[] labelBytes = new byte[nbLabelChars];
				response.get(labelBytes);

				switch (s) {
				case "NAME":
					// Concatenate labels to NAME field in RR
					rr.NAME += new String(labelBytes);
					rr.NAME += ".";
					break;
				case "RDATA":
					// Concatenate labels to RDATA field in RR
					rr.RDATA += new String(labelBytes);
					rr.RDATA += ".";
					break;
				case "EXCHANGE":
					// Concatenate labels to EXCHANGE field in RR
					rr.EXCHANGE += new String(labelBytes);
					rr.EXCHANGE += ".";
					break;
				}

				nbLabelChars = response.get();
			}
			
			if (foundPointer)
				response.position(positionAfterPointer);

			// Remove last '.'
			switch (s) {
			case "NAME":
				rr.NAME = rr.NAME.substring(0, rr.NAME.length() - 1);
				break;
			case "RDATA":
				rr.RDATA = rr.RDATA.substring(0, rr.RDATA.length() - 1);
				break;
			case "EXCHANGE":
				rr.EXCHANGE = rr.EXCHANGE.substring(0, rr.EXCHANGE.length() - 1);
				break;
			}

		}
	}

	 class DnsPacketAnswerRR {

		String NAME = ""; // Domain name associated with RR
		int TYPE = 0; // Type of data in RDATA (0x1 typeA, 0x0002 NS, 0x0005 CNAME, 0x000f MX)
		int CLASS = 0; // Similar to QCODE in Question section (should be 0x1, if not ERROR)
		long TTL = 0; // How long (in seconds) this RR may be cached
		int RDLENGTH = 0; // Length of RDATA in bytes

		// Describes resource (TypeA: IPv4 address (4 octets), NS and CNAME: sequence of
		// labels,
		// MX: PREFERENCE AND EXCHANGE)
		String RDATA = "";

		// Only relevant for MX TYPE (mail server)
		int PREFERENCE = 0; // Preference given to this RR
		String EXCHANGE = ""; // Domain name of mail server (as sequence of labels)

	}
}
