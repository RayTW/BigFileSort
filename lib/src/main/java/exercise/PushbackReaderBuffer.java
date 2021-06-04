package exercise;

import java.io.IOException;
import java.io.PushbackReader;

/**
 * 緩充類.
 *
 * @author ray
 */
public class PushbackReaderBuffer {

  private PushbackReader reader;
  private char[] buffer;
  private StringBuilder line;
  private String perviousLine;

  /**
   * 建構.
   *
   * @param reader PushbackReader
   */
  public PushbackReaderBuffer(PushbackReader reader) {
    this.reader = reader;
    buffer = new char[1];
    line = new StringBuilder();
  }

  /** 讀取一行. */
  public String readline() throws IOException {
    try {
      while ((reader.read(buffer)) != -1) {
        if (buffer[0] == '\n') {
          break;
        }
        line.append(buffer[0]);
      }

      perviousLine = line.toString();

      return perviousLine;
    } finally {
      line.setLength(0);
    }
  }

  /** 是否有讀取過的上一筆字串行. */
  public boolean hasPervious() {
    if (perviousLine == null || perviousLine.isEmpty()) {
      return false;
    }
    return true;
  }

  public int perviousInt() {
    return Integer.parseInt(perviousLine);
  }

  public void unread() throws IOException {
    this.reader.unread((perviousLine + "\n").toCharArray());
  }

  public void close() throws IOException {
    this.reader.close();
  }
}
