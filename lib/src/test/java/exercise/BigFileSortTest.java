package exercise;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import org.junit.Test;

/**
 * 測試類.
 *
 * @author ray
 */
public class BigFileSortTest {
  @Test
  public void testSomeLibraryMethod() {

    String s = "Hell\no World";

    System.out.println("s="+s);
    
    // create a new StringReader
    StringReader sr = new StringReader(s);

    // create a new PushBack reader based on our string reader
    PushbackReader pr = new PushbackReader(sr, 20);

    try {
      // read the first five chars
      for (int i = 0; i < 5; i++) {
        char c = (char) pr.read();
        System.out.print("" + c);
      }

      // change line
      System.out.println();

      // create a new array to unread
      char[] cbuf = "Hell\n".toCharArray();

      // unread into cbuf
      pr.unread(cbuf);

      // read five chars, which is what we unread from cbuf
      for (int i = 0; i < 12; i++) {
        char c = (char) pr.read();
        System.out.print("" + c);
      }

      // close the stream
      pr.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }
}
