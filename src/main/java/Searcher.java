import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Searcher {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("in"));
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        List<List<Info>> resultDocuments = new ArrayList<>();
        while(reader.ready()) {
            String target = reader.readLine();
            System.out.println(target);
            Document document = Jsoup.connect("https://yandex.ru/yandsearch")
                    .data("text", target)
                    .get();
            List<Element> elements = document.select("li.serp-item");
            List<Info> resultInfos = new ArrayList<>();
            for (Element element : elements) {
                List<String> strs = element
                        .select(".typo_type_greenurl a.path__item")
                        .stream()
                        .map(Element::text)
                        .collect(Collectors.toList());
                String title = element.selectFirst("a.link_theme_normal").text();
                String url = element.selectFirst("a.link_theme_normal").attr("href");
                Elements annotations = element.select(".text-container");
                if (!annotations.isEmpty()) {
                    resultInfos.add(new Info(strs, url, title, annotations.first().text()));
                } else {
                    resultInfos.add(new Info(strs, url, title, null));
                }
            }
            resultDocuments.add(resultInfos);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("out.json"))) {
            gson.toJson(resultDocuments, writer);
        }

    }
}