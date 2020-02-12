package ca.mcgill.ecse316.dnsclient;

import java.net.*;
import java.io.*;

import static ca.mcgill.ecse316.dnsclient.DNSClient.*;
import static ca.mcgill.ecse316.dnsclient.SocketServer.*;

public class SocketClient {

    public static void manageSocket() throws Exception {
        DatagramSocket clientSocket =  new DatagramSocket();
        InetAddress ipAddress = InetAddress.getByName(domName);
        InetAddress localIp = InetAddress.getLocalHost();


        InetAddress temp = InetAddress.getByName(dnsServerAddr);

        clientSocket.setSoTimeout(5);
        //clientSocket.setSoTimeout(timeout);
//        System.out.println(clientSocket);
//        System.out.println(ipAddress);

        byte[] sendData = new byte[1024];
        byte[] receiveData = new byte[1024];

//////////////////////////////////////////////////////////////////////////////////////////
        System.out.println("tmp: " +temp);
//        BufferedReader inFromUser =
//                new BufferedReader(new InputStreamReader(System.in));
//        String sentence = inFromUser.readLine();
//        sendData = sentence.getBytes(); //write what you want to send
         String sentence = "test i just want this program to work";
         sendData = sentence.getBytes();
///////////////////////////////////////////////////////////////////////////////////////////////////////////


        DatagramPacket sendIt = new DatagramPacket(sendData, sendData.length, temp, port);
        clientSocket.send(sendIt);
        System.out.println("send it is: " + sendIt);

       // clientSocket.close();

        //problem here
        DatagramPacket receive1 = new DatagramPacket(receiveData,receiveData.length);
        //DatagramSocket serverSocket = new DatagramSocket(port);

//     serverSocket.setSoTimeout(5);
//        try {
//            serverSocket();
//        } catch (Exception e) {
//            System.out.println(e);
//        }

        try{
            clientSocket.receive(receive1);
        } catch (Exception e) {
            System.out.println(e);
        }
//        serverSocket.close();
        System.out.println("hello "+ receive1.getAddress());



//        clientSocket.receive(receive1);
////////////////////
        String newSentence = new String(receive1.getData());

        System.out.println("Server says: " + newSentence);
        clientSocket.close();

    }
}
