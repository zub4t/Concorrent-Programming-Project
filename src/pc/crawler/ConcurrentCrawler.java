package pc.crawler;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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
    seen.add(root);
    Thread[] runners = new Thread[numberOfThreads];
    for (int i = 0; i < this.numberOfThreads; i++) {
      runners[i] = new Thread(new Runner());
      runners[i].setName("T" + i);
      runners[i].start();
    }
    for (int i = 0; i < this.numberOfThreads; i++) {
      try {
        runners[i].join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    System.out.println(rid.decrementAndGet() + " visits done");
  }

  private class Runner implements Runnable {

    @Override
    public void run() {
      do {
        try {

          long t = System.currentTimeMillis();
          URL url = null;
          url = toVisit.pop();
          int lrid = rid.getAndIncrement();
          File htmlContents = download(lrid, url);
          if (htmlContents != null) {
            ArrayList<URL> links = parseLinks(url, htmlContents);

            for (URL newURL : links) {
              if (seen.add(newURL)) {
                toVisit.push(newURL);
              }
            }

            t = System.currentTimeMillis() - t;
          //  System.out.printf("Done: %d transfers in %d ms (%.2f transfers/s)%n", lrid, t, (1e+03 * lrid) / t);

          }
        } catch (Exception e) {
          e.printStackTrace();
        }

      } while (toVisit.size() != 0);
      System.out.println(Thread.currentThread().getName() + " end !!! ");
    }
  }
}
