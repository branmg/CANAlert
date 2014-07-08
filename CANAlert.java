/* CANAlert by branmg
   
   Description:
   A program that interfaces with the serial port or serial over USB for connecting with the NGT-1 and 
   reading in PGNs from the NMEA2000 network. The program then stores each one in an object for referencing
   at a later point where we can log them and display their message in a GUI.
   
   To DO:
   -Set PGNMessage objects to decifer which part of the input is a PGN Message
   -Get all PGN definitions from the NMEA website.
   -Add options to send PGNs to certain devices on the network.
   -Set up ability to read beginning and last codes of each message.
   -Interface with a prototype monitoring setup to send and display each PGN with it's related definition
    to a webserver or NagiOS setup(Simple)
   -Implement with CANBoat analyzer for analyzing NGT-1 input for PGNs.
    
*/

import jssc.*;
import java.util.Scanner;
import java.util.Arrays;
import java.io.*;
  
public class CANAlert {

   static SerialPort serialPort;
   static PGNMessage[] messageArray = new PGNMessage[30]; //PGNMessage array cnotaining 30 of the past messages.
   static int count = 0;

   public static void main(String[] args) {
      
      serialPort = new SerialPort( getPortName() ); 
      /* The following is an array later to be sent as bytes that clears out the TX list for the NGT-1. This
         is done so that it will begin sending messages from the NGT-1. This is done in CANBoat's actisense-serial.c
         and is learned from reverse engineering the product by the creator of that software released via the GNU GPL
      */   
      String introNGTMessage[] = { "0x11", "0x02", "0x03" };
      
      try {
         serialPort.openPort();//Open port
         serialPort.setParams(9600, 8, 1, 0);//Set params
         
         for ( int x = 0; x < introNGTMessage.length; x++ ) { //Send all introductory NGT-1 messages to the port
            serialPort.writeBytes(introNGTMessage[x].getBytes()); //Write data to the port as Bytes
         }   
         
         System.out.println( serialPort.isCTS() );
      
         int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Masks for receiving char, CTS notify, DSR notify
         serialPort.setEventsMask(mask);//Set masks for listening.
         serialPort.addEventListener(new SerialPortReader());//Add SerialPortEventListener
         serialPort.purgePort(8|4);
      }
      catch (SerialPortException ex) {
         System.out.println(ex);
      }
   }
   
   public static String getPortName() {
      String chosenPort;
      String[] allPorts = SerialPortList.getPortNames(); //Returns a String[] for all port names.
      Scanner kb = new Scanner( System.in );
      if ( allPorts.length == 0 ) {
         System.out.println(" No ports found. Please check for connected serial devices. Exiting..." );
         System.exit( 0 );
      } 
      else {     
         System.out.println("Please choose a port listed below: ");
      }   
      
      for ( String port : allPorts ) {
         System.out.println( port );
      }   
      
      chosenPort = kb.next();
      return chosenPort;
   }   
   
    //Inner class for a SerialPortReader as an ActionListener like Swing. 
   static class SerialPortReader implements SerialPortEventListener { //Similar to Swing's ActionListener, requiring serialEvent.
   
      public void serialEvent(SerialPortEvent event) {
         
         if ( event.isRXCHAR() ) { //If data is available
            if(event.getEventValue() > 7){//Check bytes count in the input buffer
               //Read data, if 8 bytes available 
               try {
                  int[] RXMessage = new int[event.getEventValue()];
                  String RXHex;
                  RXHex = serialPort.readHexString(event.getEventValue()); //Read 8 bytes 
                  for ( int x = 0; x < 8; x++ ) {
                     //Read the byte value as an unsigned int ( bitwise operator 0xFF for 0-255 instead of -127-128.
                     //then typecast it to char.
                     //System.out.print( RXHex );
                     //System.out.print("");
                     
                  } 
                  System.out.println( RXHex );
                  
                  System.out.println();
                  //messageArray[count] = new PGNMessage( RXMessage ); //Print the message then send it to a new PGNMessage obj
                  count++; //Keeping count of the number of PGNMessages we're storing to remember. Will .log them later.
                  if ( count == 30 ) {
                     count = 0;
                  }   
                                
               }
               catch (SerialPortException ex) {
                  System.out.println(ex);
               }
               //catch (UnsupportedEncodingException ex) {
                  //System.out.println(ex);
               //}   
            }
         }
         //Keeping track of CTS and DSR at the moment. NGT-1 and N2k Messages don't require a handshake.
         else if(event.isCTS()){//If CTS line has changed state
            if(event.getEventValue() == 1){//If line is ON
               System.out.println("CTS - ON");
            }
            else {
               System.out.println("CTS - OFF");
            }
         }
         else if(event.isDSR()){///If DSR line has changed state
            if(event.getEventValue() == 1){//If line is ON
               System.out.println("DSR - ON");
            }
            else {
               System.out.println("DSR - OFF");
            }
         }
      }
   }
}