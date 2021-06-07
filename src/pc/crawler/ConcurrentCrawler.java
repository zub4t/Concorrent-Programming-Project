package pc.crawler;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import pc.set.ISet;
import pc.set.STMHashSet;
import pc.util.stack.IStack;
import pc.util.stack.Stack;

/**
 * Concurrent crawler.
 *
 */
public class ConcurrentCrawler extends BaseCrawler {

  public static void main(final String[] args) throws IOException {
    final int threads = args.length > 0 ? Integer.parseInt(args[0]) : 4;
    final String rootPath = args.length > 1 ? args[1] : "http://localhost:8123";
    final ConcurrentCrawler cc = new ConcurrentCrawler(threads);
    cc.setVerboseOutput(false);
    cc.crawl(new URL(rootPath));
  }

  private final int numberOfThreads;
  private final ISet<URL> seen;
  private final IStack<URL> toVisit;
  // private final BlockingQueue<URL> toVisit;
  final AtomicInteger rid = new AtomicInteger(0);

  public ConcurrentCrawler(final int threads) throws IOException {
    this.numberOfThreads = threads;
    seen = new STMHashSet<>();
    toVisit = new Stack<>();
    // toVisit = new LinkedBlockingQueue<>();
  }

  @Override
  public void crawl(final URL root) {

    log("Starting at %s", root);
    toVisit.push(root);
    Thread[] runners = new Thread[numberOfThreads];
    for (int i = 0; i < this.numberOfThreads; i++) {
      runners[i] = new Thread(new Runner());
      runners[i].setName("T" + i);
      runners[i].start();
    }
    /*
     * new Thread(new Runnable() {
     * 
     * @Override public void run() { while (true) for (int i = 0; i <
     * numberOfThreads; i++) { // System.out.println(runners[i].getName() + " is " +
     * runners[i].getState());
     * 
     * } } }).start();
     */
    for (int i = 0; i < this.numberOfThreads; i++) {
      try {
        runners[i].join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

  }

  private class Runner implements Runnable {

    @Override
    public void run() {
      do {
        try {

          long t = System.currentTimeMillis();
          URL url = null;
          url = toVisit.pop();
          seen.add(url);

          int lrid = rid.getAndIncrement();
          File htmlContents = download(lrid, url);
          if (htmlContents != null) {
            ArrayList<URL> links = parseLinks(url, htmlContents);
            synchronized (this) {
              for (URL newURL : links) {
                if (!seen.contains(newURL) && !toVisit.contains(newURL)) {
                  toVisit.push(newURL);
                }
              }
            }

            t = System.currentTimeMillis() - t;
            System.out.printf("Done: %d transfers in %d ms (%.2f transfers/s)%n", lrid, t, (1e+03 * lrid) / t);
          //  System.out.println(toVisit.size() + " visits left");
        // System.out.println(seen.size() + " visits done");
            System.out.println(Thread.currentThread().getName() + " " + url);

          }
        } catch (Exception e) {
          e.printStackTrace();
        }

      } while (toVisit.size() != 0);
      System.out.println(Thread.currentThread().getName() + " end !!! ");
    }
  }
}
