package gamerskynews;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Json;
import us.codecraft.xsoup.Xsoup;

/**
 * 通过实现PageProcessor方式爬取游民星空新闻网页http://www.gamersky.com/news/的新闻标题与概览
 * 由于是jsonp请求方式, 所以返回的数据需要经过处理, 才能提取数据
 * 因为每个页面有很多相同Html结构的新闻标题与概览项, 所以每次爬取的内容都是List<String>类型,
 * 再通过自定义Pipeline输出自己想要的格式
 */

public class GamerskyNewsPageProcessor implements PageProcessor {

    private static int pages = 2215;  //新闻页面总页数

    // 自定义User-Agent
    private static String userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:44.0) Gecko/20100101 Firefox/45.0";

    // Sleep time 时间要足够长才可以读取数据, 否则发生read time out
    private Site site = Site.me().setRetryTimes(3).setSleepTime(1).setRetrySleepTime(500).setCycleRetryTimes(3)
            .setUserAgent(userAgent);

    @Override
    public Site getSite() {
        return site;
    }

    @Override
    public void process(Page page) {
        Json json = page.getJson().removePadding("other");
        String rawText = json.jsonPath("$.body").toString();
        page.putField("url", getResult(rawText, "//a[@class='tt']/@href"));
        page.putField("title", getResult(rawText, "//a[@class='tt']/text()"));
        page.putField("content", getResult(rawText, "//div[@class='txt']/text()"));
        page.putField("category", getResult(rawText, "//a[@class='dh']/text()"));

        int totalPages = Integer.valueOf(json.jsonPath("$.totalPages").toString()); //获取totalPages
        // 如果当前获得的总页数大于现在的页数, 则添加request url
        if (totalPages > pages) {
            for (int i = 1; i <= totalPages - pages; i++) {
                String leftPart = "http://db2.gamersky.com/LabelJsonpAjax.aspx?callback=other&jsondata=%7B%22type%22%3A%22updatenodelabel%22%2C%22nodeId%22%3A%2211007%22%2C%22page%22%3A";
                String rightPart = "%7D";
                String url = leftPart + String.valueOf(i) + rightPart;
                page.addTargetRequest(url);
            }
            // 更新pages
            pages = totalPages;
        }

    }

    private String makePlainTextToHtml(String text) {
        text = "<body>" + text + "</body>";
        text = "<html>" + text + "</html>";
        return text;
    }

    private Document parsedHtmlByJsoup(String html) {
        Document document = Jsoup.parse(html);
        return document;
    }

    private List<String> getResult(String rawText, String xpath) {
        String htmlText = makePlainTextToHtml(rawText);
        Document document = parsedHtmlByJsoup(htmlText);
        List<String> result = Xsoup.compile(xpath).evaluate(document).list();
        return result;
    }

    private static void addUrls(Spider spider) {
        for (int i = 1; i <= pages; i++) {
            String leftPart = "http://db2.gamersky.com/LabelJsonpAjax.aspx?callback=other&jsondata=%7B%22type%22%3A%22updatenodelabel%22%2C%22nodeId%22%3A%2211007%22%2C%22page%22%3A";
            String rightPart = "%7D";
            String url = leftPart + String.valueOf(i) + rightPart;
            spider.addUrl(url);
        }

    }

    public static void main(String[] args) {

        Spider gamerskyNewsSpider = Spider.create(new GamerskyNewsPageProcessor());
        addUrls(gamerskyNewsSpider);
        // 线程数多于1容易造成read time out, 如果第二个线程启动时有间隔时间, 可以解决
        gamerskyNewsSpider.thread(1).addPipeline(new ConsolePipeline()).addPipeline(new GamerskyNewsPipeline());
        gamerskyNewsSpider.run();

    }
}