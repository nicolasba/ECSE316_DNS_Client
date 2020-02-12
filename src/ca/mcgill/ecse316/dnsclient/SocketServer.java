package ca.mcgill.ecse316.dnsclient;

import java.net.*;
import java.io.*;

import static ca.mcgill.ecse316.dnsclient.DnsClient.*;
import static ca.mcgill.ecse316.dnsclient.SocketClient.*;


public class SocketServer {
    public static void serverSocket() throws Exception {
        DatagramSocket serverSocket = new DatagramSocket(port);
       // serverSocket.setSoTimeout(timeout);

        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];

        while(true){
            DatagramPacket receivePacket =
                    new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);

            String sentence = new String(receivePacket.getData());
            System.out.println("server sentence is: " +sentence);


            //ip and port of sender
            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();

            String capitalizedSentence = sentence.toUpperCase();
            sendData = capitalizedSentence.getBytes();

            //what to send to client
            DatagramPacket sendPacket =
                    new DatagramPacket(sendData, sendData.length, IPAddress, port);
            //write to socket
            serverSocket.send(sendPacket);
        }
    }
}
