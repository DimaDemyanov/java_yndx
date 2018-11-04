import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Searcher {
    private final static String SEARCH_URL = "https://yandex.ru/search";
    private final static String SEARCH_PARAM = "text";
    private final static String PAGES_TAG = "li.serp-item";
    private final static String GREENURL_TAG = ".typo_type_greenurl a.path__item";
    private final static String PAGE_TAG = "a.link_theme_normal";
    private final static String ANNOTATIONS_TAG = ".text-container";
    private final static String ATTRIBUTE_UTL = "href";
    private final static String INPUT_FILE = "in";
    private final static String OUTPUT_FILE = "out.json";
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(INPUT_FILE), StandardCharsets.UTF_8)
        );
        Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create();
        List<List<Info>> resultDocuments = new ArrayList<>();
        while(reader.ready()) {
            String target = reader.readLine();
            System.out.println(target);
            Document document = Jsoup.connect(SEARCH_URL)
//                    .proxy("156.237.192.228", 	1080)
                    .data(SEARCH_PARAM, target)
                    .get();
            List<Element> elements = document.select(PAGES_TAG);
            List<Info> resultInfos = new ArrayList<>();
            for (Element element : elements) {
                List<String> greenUrls = element
                        .select(GREENURL_TAG)
                        .stream()
                        .map(Element::text)
                        .collect(Collectors.toList());
                Element page = element.selectFirst(PAGE_TAG);
                String title = page.text();
                String url = page.attr(ATTRIBUTE_UTL);
                Elements annotations = element.select(ANNOTATIONS_TAG);
                resultInfos.add(new Info(greenUrls, url, title,
                        CollectionUtils.isEmpty(annotations)
                        ? null
                        : annotations.first().text()));
            }
            resultDocuments.add(resultInfos);
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE))) {
            gson.toJson(resultDocuments, writer);
        }

    }
}