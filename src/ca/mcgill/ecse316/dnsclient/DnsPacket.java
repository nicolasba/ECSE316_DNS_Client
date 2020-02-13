package ca.mcgill.ecse316.dnsclient;

import java.nio.ByteBuffer;

import java.util.Arrays;
import java.util.Random;

public class DnsPacket {

	ByteBuffer buffer;
	byte[] message;

	private DnsPacketHeader header;
	private DnsPacketQuestion question;
	private DnsPacketAnswer answer;
	private DnsPacketAnswer auth;
	private DnsPacketAnswer additional;

	// Constructor used for request packets
	public DnsPacket() {

		buffer = ByteBuffer.allocate(1024);
		header = new DnsPacketHeader();
		question = new DnsPacketQuestion();

		//Concatenate byte buffers from header and question section
		buffer.put(header.buffer);
		buffer.put(question.buffer);
		
		//Get byte[] from byte buffer
		message = Arrays.copyOf(buffer.array(), buffer.position());
	}

	// Constructor used for response packets
	public DnsPacket(ByteBuffer response) {

		header = new DnsPacketHeader(response);
		// Test position changes TODO !!!!!!
		question = new DnsPacketQuestion(response);
	}
	
	/************************** PACKET HEADER **************************/

	private class DnsPacketHeader {

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
	
	private class DnsPacketQuestion {

		ByteBuffer buffer;

		// Request
		public DnsPacketQuestion() {

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

		// Request (Ignore the data from this section in response message)
		public DnsPacketQuestion(ByteBuffer response) {

			buffer = response;

			// Number of labels in domain name. Ex: 3 labels in "www.mcgill.ca"
			int domNameLabelsLength = DnsClient.domNameLabels.length;

			for (int i = 0; i < domNameLabelsLength; i++) {
				byte[] labelChars = DnsClient.domNameLabels[i].getBytes();
				buffer.get(); // Remove number of character in label
				for (int j = 0; j < labelChars.length; j++)
					buffer.get(); // Remove sequence of characters
			}

			buffer.get(); // End of QNAME

			buffer.getChar(); 	//Remove QTYPE
			buffer.getChar();	//Remove QCLASS
		}

	}

	private class DnsPacketAnswer {

		ByteBuffer buffer;
	}
}
