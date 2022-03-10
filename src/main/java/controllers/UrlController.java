package controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.DuplicateKeyException;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UrlController {
    public static Handler listUrls = ctx -> {
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

        ctx.attribute("urls", urls);
        ctx.attribute("page", page);
        ctx.render("urls/index.html");
    };

    public static Handler newUrl = ctx -> {
        Url url = new Url();

        ctx.attribute("url", url);
        ctx.render("urls/index.html");
    };

    public static Handler createUrl = ctx -> {
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

    public static Handler showUrl = ctx -> {
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

}
