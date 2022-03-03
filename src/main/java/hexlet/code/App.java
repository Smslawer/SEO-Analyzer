package hexlet.code;

import io.javalin.Javalin;
import io.javalin.core.JavalinConfig;


public class App {
    public static void main(String[] args) {
        System.out.println(returnHello());

        Javalin app = getApp();
        app.get("/", ctx -> ctx.result("Hello World"));
        app.start(getPort());
    }

    public static String returnHello() {
        return "Hello";
    }

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "5000");
        return Integer.parseInt(port);
    }

    public static Javalin getApp() {
        Javalin app = Javalin.create(JavalinConfig::enableDevLogging);

        app.before(ctx -> ctx.attribute("ctx", ctx));

        return app;
    }
}
