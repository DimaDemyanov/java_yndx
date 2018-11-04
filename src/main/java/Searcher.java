import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.collections4.CollectionUtils;
import org.jsoup.Connection;
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
    private final static String ATTRIBUTE_URL = "href";
    private final static String DEFAULT_OUTPUT_FILE = "out.json";

    private static Document getHTML(String target, CommandLine cmd) throws IOException {
        Connection connection;
        if (cmd.hasOption("proxy")) {
            String[] proxy = cmd.getOptionValue("proxy").split(":");
            if(proxy.length < 2)
                throw new IllegalArgumentException("Incorrect proxy");
            connection = Jsoup.connect(SEARCH_URL).proxy(proxy[0], Integer.parseInt(proxy[1]));
        } else
            connection = Jsoup.connect(SEARCH_URL);
        return connection
                .data(SEARCH_PARAM, target)
                .get();
    }

    private static List<Info> getInfos(String target, CommandLine cmd) throws IOException {
        Document document = getHTML(target, cmd);
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
            String url = page.attr(ATTRIBUTE_URL);
            Elements annotations = element.select(ANNOTATIONS_TAG);
            resultInfos.add(new Info(greenUrls, url, title,
                    CollectionUtils.isEmpty(annotations)
                            ? null
                            : annotations.first().text()));
        }
        return resultInfos;
    }

    public static void main(String[] args) throws IOException, ParseException {
        CommandLine cmd = new CliParser().parse(args);
        if(cmd.hasOption("help")){
            System.out.println("searcher.jar [-i {inputfilename}(required)] [-o {outputfilename}] [-p {proxy adress X.X.X.X:X}]");
            return;
        }
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(cmd.getOptionValue("input")), StandardCharsets.UTF_8
                )
        );
        Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create();
        List<List<Info>> resultDocuments = new ArrayList<>();
        while(reader.ready()) {
            String target = reader.readLine();
            resultDocuments.add(getInfos(target, cmd));
        }
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter( cmd.hasOption("output")?cmd.getOptionValue("output"):DEFAULT_OUTPUT_FILE))
        ) {
            gson.toJson(resultDocuments, writer);
        }

    }
}