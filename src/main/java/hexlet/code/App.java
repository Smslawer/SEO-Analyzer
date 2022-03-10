package hexlet.code;

import controllers.RootController;
import controllers.UrlController;
import io.javalin.Javalin;
import io.javalin.core.JavalinConfig;
import io.javalin.plugin.rendering.template.JavalinThymeleaf;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;


public class App {
    public static void main(String[] args) {
        System.out.println(returnHello());

        Javalin app = getApp();
        app.start(getPort());
    }

    public static String returnHello() {
        return "Hello";
    }

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "5050");
        return Integer.parseInt(port);
    }

    public static TemplateEngine getTemplateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();

        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/templates/");

        templateEngine.addTemplateResolver(templateResolver);
        templateEngine.addDialect(new LayoutDialect());
        templateEngine.addDialect(new Java8TimeDialect());

        return templateEngine;
    }

    public static void addRouts(Javalin app) {
        app.get("/", RootController.welcome);

        app.routes(() -> {
            path("urls", () -> {
                get(UrlController.listUrls);
                post(UrlController.createUrl);
                get("new", UrlController.newUrl);
                path("{id}", () -> {
                    get(UrlController.showUrl);
                });
            });
        });
    }

    private static String getMode() {
        return System.getenv().getOrDefault("APP_ENV", "development");
    }

    private static boolean isProduction() {
        return getMode().equals("production");
    }

    public static Javalin getApp() {
        Javalin app = Javalin.create(config -> {
            if (!isProduction()) {
                config.enableDevLogging();
            }
            config.enableWebjars();
            JavalinThymeleaf.configure(getTemplateEngine());
        });

        addRouts(app);

        app.before(ctx -> ctx.attribute("ctx", ctx));

        return app;
    }
}
