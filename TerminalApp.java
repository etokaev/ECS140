/*
You can test Chario, Scanner, or Parser separately by adding and removing comments
in the last few lines of the TerminalApp constructor.
*/

import java.io.*;


public class TerminalApp{

   // Data model
   private Chario chario;
   private Scanner scanner;
   private Parser parser;
   public static String argsGlobal[] = null;

   public TerminalApp(String array[]){
      java.util.Scanner reader = new java.util.Scanner(System.in);
      //System.out.print("Enter the input file name: ");
      String filename = array[0];
      FileInputStream stream;
      try{
         stream = new FileInputStream(filename);
     }catch(IOException e){
         System.out.println("Error opening file.");
         return;
      }      
      chario = new Chario(stream);
      //testChario();
      scanner = new Scanner(chario);
      //testScanner();
      parser = new Parser(chario, scanner);
      testParser();
   }

   private void testChario(){
      char ch = chario.getChar();
      while (ch != Chario.EF)
         ch = chario.getChar();
      chario.reportErrors();
   }

   private void testScanner(){
      Token token = scanner.nextToken();
      while (token.code != Token.EOF){
         chario.println(token.toString());
         token = scanner.nextToken();
      }
      chario.reportErrors();
   }

   private void testParser(){
      try{
         parser.parse();
      }
      catch(Exception e){}
      chario.reportErrors();
   }

   public static void main(String args[]){
      //TerminalApp.argsGlobal[0] = args[0];
      //System.out.print(argsGlobal[0]);
      new TerminalApp(args);
   }
}
