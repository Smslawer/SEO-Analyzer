package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import io.ebean.DuplicateKeyException;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class UrlController {
    private static Handler listUrls = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
        int rowsPerPage = 10;
        int offset = (page - 1) * rowsPerPage;

        PagedList<Url> pagedUrls = new QUrl()
                .setFirstRow(offset)
                .setMaxRows(rowsPerPage)
                .orderBy()
                .id.asc()
                .findPagedList();

        List<Url> urls = pagedUrls.getList();

        Map<Integer, UrlCheck> checks = new QUrlCheck()
                .url.id.asMapKey()
                .orderBy()
                .createdAt.desc()
                .findMap();

        ctx.attribute("urls", urls);
        ctx.attribute("checks", checks);
        ctx.attribute("page", page);
        ctx.render("urls/index.html");
    };

    private static Handler createUrl = ctx -> {
        String url = ctx.formParam("url");

        Url urlToAdd;

        try {
            URL newUrl = new URL(url);
            String finalUrl = newUrl.getPort() == -1
                    ? String.format("%s://%s", newUrl.getProtocol(), newUrl.getHost())
                    : String.format("%s://%s:%d", newUrl.getProtocol(), newUrl.getHost(), newUrl.getPort());
            urlToAdd = new Url(finalUrl);
            urlToAdd.save();
        } catch (MalformedURLException | DuplicateKeyException e) {
            String msg = e instanceof DuplicateKeyException ? "Страница уже существует" : "Некорректный URL";
            ctx.status(422);
            ctx.sessionAttribute("flash-type", "danger");
            ctx.sessionAttribute("flash", msg);
            ctx.render("index.html");
            return;
        }

        urlToAdd.save();

        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.sessionAttribute("flash-type", "success");
        ctx.redirect("/urls");
    };

    private static Handler showUrl = ctx -> {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (url == null) {
            throw new NotFoundResponse();
        }

        ctx.attribute("url", url);
        ctx.render("urls/show.html");
    };

    private static Handler runChecks = ctx -> {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();
        try {
            HttpResponse<String> response = Unirest
                    .get(url.getName())
                    .asString();

            String body = response.getBody();
            Document doc = Jsoup.parse(body);
            Element h1Element = doc.selectFirst("h1");
            Element descriptionElement = doc.selectFirst("meta[name=description]");

            UrlCheck urlCheck = new UrlCheck();
            urlCheck.setUrl(url);
            urlCheck.setStatusCode(response.getStatus());
            urlCheck.setTitle(doc.title());
            urlCheck.setH1(h1Element == null ? "" : h1Element.text());
            urlCheck.setDescription(descriptionElement == null ? "" : descriptionElement.attr("content"));
            urlCheck.save();
        } catch (UnirestException e) {
            ctx.sessionAttribute("flash-type", "danger");
            ctx.sessionAttribute("flash", "Страница не существует");
            ctx.redirect("/urls/" + id);
            return;
        }

        ctx.sessionAttribute("flash-type", "success");
        ctx.sessionAttribute("flash", "Страница успешно проверена");
        ctx.redirect("/urls/" + id);
    };

    //Методы созданы т.к. чекстайл ругался на то, что лябмды должны быть прайват и иметь вызывающие методы
    public static Handler getListUrls() {
        return listUrls;
    }

    public static Handler getCreateUrl() {
        return createUrl;
    }

    public static Handler getShowUrl() {
        return showUrl;
    }

    public static Handler getRunChecks() {
        return runChecks;
    }
}
