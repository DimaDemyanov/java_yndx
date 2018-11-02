
import java.util.List;

public class Info {
    private List<String> greenUrls;
    private String url;
    private String title;
    private String annotation;

    public Info(List<String> greenUrls, String url, String title, String annotation) {
        this.greenUrls = greenUrls;
        this.url = url;
        this.title = title;
        this.annotation = annotation;
    }
}