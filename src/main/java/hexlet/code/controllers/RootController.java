package hexlet.code.controllers;

import io.javalin.http.Handler;

public final class RootController {
    private static Handler welcome = ctx -> {
        ctx.render("index.html");
    };

    public static Handler getWelcome() {
        return welcome;
    }
}
