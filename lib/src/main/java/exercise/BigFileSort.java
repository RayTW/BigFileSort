package exercise;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.PushbackReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * 主程式.
 *
 * @author ray
 */
public class BigFileSort {
  public boolean someLibraryMethod() {
    return true;
  }

  /**
   * 建立n筆亂數數值.
   *
   * @param path 存檔路徑
   * @param numberCount 總筆數
   */
  public void createRandomNumerFile(Path path, int numberCount) {
    try {
      Files.write(
          path,
          new Iterable<String>() {

            @Override
            public Iterator<String> iterator() {
              return new Iterator<String>() {
                int count = 0;
                Random random = new Random();

                @Override
                public boolean hasNext() {
                  return count < numberCount;
                }

                @Override
                public String next() {
                  count++;
                  return String.valueOf(random.nextInt(Integer.MAX_VALUE));
                }
              };
            }
          });
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * 排序.
   *
   * @param source 未排序檔案路徑
   * @param target 排序過檔案路徑
   */
  public void sort(Path source, Path target, int total, int filePieceCount) {
    String tempPath = source.getParent().toString() + "/piece/piece%d.txt";

    reduce(source, tempPath, total / filePieceCount);
    writeSortedFile(tempPath, filePieceCount, target);
  }

  private void reduce(Path source, String tempPath, int batch) {
    try {
      Path temp = Paths.get(tempPath);

      if (Files.notExists(temp.getParent())) {
        Files.createDirectory(temp.getParent());
      }

      try (Scanner scanner = new Scanner(source)) {
        int piece = 0;

        // 每500萬筆數值排序後寫到1個檔案裡，共寫入200個檔
        while (scanner.hasNext()) {
          Path piecePath = Paths.get(String.format(tempPath, piece));
          int[] numbers = new int[batch];

          for (int i = 0; scanner.hasNext() && i < batch; i++) {
            numbers[i] = scanner.nextInt();
          }
          Arrays.sort(numbers);

          try (PrintWriter writer = new PrintWriter(piecePath.toString(), "utf-8")) {
            for (int n : numbers) {
              writer.println(String.valueOf(n));
            }
          } catch (IOException e) {
            e.printStackTrace();
          }

          piece++;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * 將已排序過的數字寫入一個檔案.
   *
   * @param tempPath 分散暫存的數字檔
   * @param filePieceCount 分部檔案數
   * @param target 已排序的檔案
   */
  public void writeSortedFile(String tempPath, int filePieceCount, Path target) {
    ArrayList<PushbackReader> readers = new ArrayList<>();

    for (int i = 0; i < filePieceCount; i++) {
      try {
        readers.add(new PushbackReader(new FileReader(String.format(tempPath, i)), 11));
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }
    String min = "";

    try (PrintWriter writer =
        new PrintWriter(Files.newBufferedWriter(target, Charset.forName("utf-8")))) {
      // 從200個檔案裡取每個檔案首筆數值，再從中比出最小的數值寫檔
      while (!(min = findMin(readers)).isEmpty()) {
        writer.println(min);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private String findMin(List<PushbackReader> readers) {
    int min = Integer.MAX_VALUE;
    StringBuilder buf = new StringBuilder();
    char[] c = new char[1];

    for (int i = readers.size() - 1; i >= 0; i--) {
      PushbackReader reader = readers.get(i);

      try {
        while ((reader.read(c)) != -1) {
          if (c[0] == '\n') {
            break;
          }
          buf.append(c[0]);
        }
        if (buf.length() == 0) {
          readers.remove(i).close();
          continue;
        }

        int n = Integer.parseInt(buf.toString());

        if (n < min) {
          min = n;
        } else {
          reader.unread(buf.append('\n').toString().toCharArray());
        }
        buf.setLength(0);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return readers.isEmpty() ? "" : String.valueOf(min);
  }

  /**
   * main.
   *
   * @param args 參數
   */
  public static void main(String[] args) {
    BigFileSort big = new BigFileSort();

    Path source = Paths.get("/Users/ray/Desktop/test/numbers1.txt");
    Path target = Paths.get("/Users/ray/Desktop/test/numbersSorted.txt");

    //    big.createRandomNumerFile(source, 10000);
    big.sort(source, target, 10000, 200);
  }
}
