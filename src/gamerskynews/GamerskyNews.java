package gamerskynews;

import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.model.ConsolePageModelPipeline;
import us.codecraft.webmagic.model.OOSpider;
import us.codecraft.webmagic.model.annotation.ExtractBy;
import us.codecraft.webmagic.model.annotation.ExtractByUrl;
import us.codecraft.webmagic.model.annotation.HelpUrl;
import us.codecraft.webmagic.model.annotation.TargetUrl;

/**
 * ʹ��ע�ⷽʽ��ȡ�����ǿյ�������ҳ����������
 * ������ҳ����Ϊhttp://www.gamersky.com/news/*.shtml��ʽ
 */

@TargetUrl("http://www.gamersky.com/news/\\d{1,6}/*[^jpg|png|gif|#]+")
@HelpUrl("http://www.gamersky.com/news")
public class GamerskyNews {

    @ExtractByUrl
    private String url;

    @ExtractBy(value = "//div[@class='Mid2L_tit']/h1/text()", notNull = true)
    private String title;

    @ExtractBy(value = "//div[@class='Mid2L_con']/allText()", notNull = true)
    private String content;

    public static void main(String[] args) {
        OOSpider.create(Site.me().setSleepTime(0).setRetrySleepTime(3), new ConsolePageModelPipeline(),
                GamerskyNews.class).addUrl("http://www.gamersky.com").run();
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

}