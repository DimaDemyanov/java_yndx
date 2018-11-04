
import java.util.List;

class Info {
    private List<String> greenUrls;
    private String url;
    private String title;
    private String annotation;

    Info(List<String> greenUrls, String url, String title, String annotation) {
        this.greenUrls = greenUrls;
        this.url = url;
        this.title = title;
        this.annotation = annotation;
    }
}