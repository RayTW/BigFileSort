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
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * 主程式.
 *
 * @author ray
 */
public class BigFileSort {

  /**
   * 建立指定筆亂數數值.
   *
   * @param path 存檔路徑
   * @param numberCount 總筆數
   */
  public void createRandomNumerFile(Path path, int numberCount) {
    if (Files.notExists(path.getParent())) {
      try {
        Files.createDirectory(path.getParent());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    Random random = new Random();

    try (PrintWriter writer =
        new PrintWriter(Files.newBufferedWriter(path, Charset.forName("utf-8")))) {

      for (int i = 0; i < numberCount; i++) {
        writer.println(String.valueOf(random.nextInt(Integer.MAX_VALUE)));
      }
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
  public void executeSort(Path source, Path target, int total, int filePieceCount) {
    String tempPath = source.getParent().toString() + "/piece/piece%d.txt";

    reduce(source, tempPath, total / filePieceCount);
    writeFile(tempPath, filePieceCount, target);
  }

  /**
   * 將大檔拆分為多個檔案(數值降冪排序).
   *
   * @param source 來源檔
   * @param tempPath 拆分後的路徑暫存檔
   * @param batch 每個拆分檔案筆數(必須可整除總筆數)
   */
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

          try (PrintWriter writer =
              new PrintWriter(Files.newBufferedWriter(piecePath, Charset.forName("utf-8")))) {
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
  public void writeFile(String tempPath, int filePieceCount, Path target) {
    ArrayList<PushbackReaderBuffer> readers = new ArrayList<>();

    for (int i = 0; i < filePieceCount; i++) {
      try {
        readers.add(
            new PushbackReaderBuffer(
                new PushbackReader(new FileReader(String.format(tempPath, i)), 11)));
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

      // 刪除分割暫存檔
      Path tempFolder = Paths.get(tempPath).getParent();
      Files.list(tempFolder)
          .forEach(
              path -> {
                try {
                  Files.delete(path);
                } catch (IOException e) {
                  e.printStackTrace();
                }
              });
      Files.delete(tempFolder);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * 掃全部檔案找出每個檔案第1個數值並比對其中最小值回傳.
   *
   * @param readers 多個排序過檔案
   */
  private String findMin(List<PushbackReaderBuffer> readers) {
    Integer min = null;
    PushbackReaderBuffer minReader = null;

    for (int i = readers.size() - 1; i >= 0; i--) {
      PushbackReaderBuffer reader = readers.get(i);

      try {
        if (reader.readline().isEmpty()) {
          readers.remove(i).close();
          continue;
        }

        if (min == null || reader.hasPervious() && reader.perviousInt() < min) {
          if (minReader != null) {
            minReader.unread();
          }
          min = reader.perviousInt();
          minReader = reader;
        } else {
          reader.unread();
        }
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
    String root = "/Users/ray/Desktop/bigFile/";
    Path source = Paths.get(root, "numbers.txt");
    Path target = Paths.get(root, "result.txt");
    int totalCount = 1000000; // 總筆數

    // 亂數產生數值
    big.createRandomNumerFile(source, totalCount);

    long time = System.currentTimeMillis();
    big.executeSort(source, target, totalCount, 200);
    System.out.println("time=" + (System.currentTimeMillis() - time) + "ms");
  }
}
