package gamerskynews;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import org.apache.http.annotation.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.utils.FilePersistentBase;

@ThreadSafe
public class GamerskyNewsPipeline extends FilePersistentBase implements Pipeline {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private int count = 0;

    public GamerskyNewsPipeline() {

        setPath("./data/gamersky/");
    }

    public GamerskyNewsPipeline(String path) {

        setPath(path);
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        String path = this.path + PATH_SEPERATOR + task.getUUID() + PATH_SEPERATOR;

        try {
            PrintWriter printWriter = new PrintWriter(
                    new OutputStreamWriter(new FileOutputStream(getFile(path + "gamerskynews.txt"), true), "UTF-8"));
            printWriter.println("PAGE:\t" + resultItems.getRequest().getUrl() + "\r\n");
            for (int i = 0; i <= resultItems.getAll().entrySet().size(); i++) {
                printWriter.println(++count);
                for (Map.Entry<String, Object> entry : resultItems.getAll().entrySet()) {
                    if (entry.getValue() instanceof List<?>) {
                        List<?> list = (List<?>) entry.getValue();
                        printWriter.print(entry.getKey() + ":\t");
                        printWriter.println(list.get(i).toString().trim());
                    } else {
                        printWriter.println(entry.getKey() + ":\t" + entry.getValue().toString().trim());
                    }
                }
                printWriter.println("");
            }
            printWriter.close();

        } catch (IOException e) {
            logger.warn("Write data to file error", e);
        }

    }

}
