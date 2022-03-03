package hexlet.code;

import io.javalin.Javalin;
import io.javalin.core.JavalinConfig;


public class App {
    public static void main(String[] args) {
        System.out.println(returnHello());

        Javalin app = getApp();
        app.get("/", ctx -> ctx.result("Hello World"));
        app.start(5000);
    }

    public static String returnHello() {
        return "Hello";
    }

    public static Javalin getApp() {
        Javalin app = Javalin.create(JavalinConfig::enableDevLogging);

        app.before(ctx -> ctx.attribute("ctx", ctx));

        return app;
    }
}
