import jssc.SerialPort;

public class PGNMessage {

   protected int ourCode;
   protected String hexString;

   public PGNMessage( String hexInput ) {
      hexString = hexInput;
   }   
   

   
   public static void main( String[] args ) {
      int Escape = 0x10;
      int StartOfText = 0x02;
      int EndOfText = 0x03;
      int N2kData = 0x93;
      int BEMCMD = 0xA0;
      char EscapeChar = (char)Escape;
      int StartOfTextChar = (char)StartOfText;
      int EndOfTextChar = (char)EndOfText;
      int N2kDataChar = (char)N2kData;
      System.out.println("" + StartOfTextChar + " " + EndOfTextChar + " " + EscapeChar + " " + N2kDataChar);
   }   
}