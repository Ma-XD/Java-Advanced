package info.kgeorgiy.ja.dziubenko.crawler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final ExecutorService downloadService;
    private final ExecutorService extractorService;

    /**
     * Constructs implementation of {@link Crawler}.
     *
     * @param downloader  allows to download pages and extract links from them.
     * @param downloaders maximum number of simultaneously downloaded pages.
     * @param extractors  maximum number of pages from which links are simultaneously extracted.
     * @param perHost     maximum number of pages that can be simultaneously loaded from one host.
     */
    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = Objects.requireNonNull(downloader);
        downloadService = Executors.newFixedThreadPool(downloaders);
        extractorService = Executors.newFixedThreadPool(extractors);
    }

    private void testDownloadParameters(String url, int depth) {
        Objects.requireNonNull(url, "Expected not null URL");
        if (depth < 1) {
            throw new IllegalArgumentException("Expected depth > 0, actual=" + depth);
        }
    }

    @Override
    public Result download(String url, int depth) {
        testDownloadParameters(url, depth);

        RecursiveDownloader recDownloader = new RecursiveDownloader();
        recDownloader.download(url, depth);

        return new Result(recDownloader.getSuccess(), recDownloader.getErrors());
    }

    @Override
    public void close() {
        downloadService.shutdownNow();
        extractorService.shutdownNow();
    }

    private class RecursiveDownloader {
        private final Set<String> visited = ConcurrentHashMap.newKeySet();
        private final Set<String> phaseLinks = ConcurrentHashMap.newKeySet();
        private final Map<String, IOException> errors = new ConcurrentHashMap<>();
        private final Phaser phaser = new Phaser();

        public List<String> getSuccess() {
            return visited.stream()
                    .filter(page -> !errors.containsKey(page))
                    .toList();
        }

        public Map<String, IOException> getErrors() {
            return Map.copyOf(errors);
        }

        private void clear() {
            phaseLinks.clear();
            visited.clear();
            errors.clear();
        }

        public void download(String url, int depth) {
            clear();
            phaser.register();
            visited.add(url);
            phaseLinks.add(url);

            for (int phase = depth; phase > 0; phase--) {
                List<String> links = new ArrayList<>(phaseLinks);
                phaseLinks.clear();
                final int phaseDepth = phase;
                links.forEach(link -> downloadLink(phaseDepth, link));
                phaser.arriveAndAwaitAdvance();
            }
        }

        private void downloadLink(int depth, String url) {
            Callable<Void> task = () -> {
                Document document = downloader.download(url);
                if (depth > 1) {
                    extractLinks(document, url);
                }
                return null;
            };
            downloadService.submit(checkedRunnable(task, url));
        }

        private void extractLinks(Document document, String url) {
            Callable<Void> task = () -> {
                document.extractLinks()
                        .stream()
                        .filter(visited::add)
                        .forEach(phaseLinks::add);
                return null;
            };
            extractorService.submit(checkedRunnable(task, url));
        }

        private Runnable checkedRunnable(Callable<Void> task, String url) {
            phaser.register();
            return () -> {
                try {
                    task.call();
                } catch (IOException e) {
                    errors.put(url, e);
                } catch (Exception e) {
                    System.err.println("Download error: " + e.getMessage());
                } finally {
                    phaser.arriveAndDeregister();
                }
            };
        }
    }

    /**
     * Starts download on passed arguments
     *
     * @param args url [depth [downloads [extractors [perHost]]]]
     */
    public static void main(String[] args) {
        if (args == null || args.length < 1 || args.length > 5) {
            System.err.println("Invalid number of arguments");
            return;
        }

        String url = args[0];
        int depth, downloaders, extractors, perHost;
        try {
            depth = getParameter(args, 1);
            downloaders = getParameter(args, 2);
            extractors = getParameter(args, 3);
            perHost = getParameter(args, 4);
        } catch (NumberFormatException e) {
            System.err.println("Parameters are not integer. Run with args: url [depth [downloads [extractors [perHost]]]]");
            return;
        }

        try (Crawler crawler = new WebCrawler(new CachingDownloader(100), downloaders, extractors, perHost)) {
            Result result = crawler.download(url, depth);
            System.out.println("Success: " + result.getDownloaded() + "\n"
                    + "Error: " + result.getErrors());
        } catch (IOException | IllegalArgumentException | NullPointerException e) {
            System.err.println(e.getMessage());
        }
    }

    private static int getParameter(String[] args, int index) {
        if (args.length <= index) {
            return 1;
        }
        return Integer.parseInt(args[index]);
    }
}
