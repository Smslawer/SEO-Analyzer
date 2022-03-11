package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.DB;
import io.ebean.Transaction;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public final class AppTest {

    @Test
    void testInit() {
        assertThat(true).isEqualTo(true);
    }

    private static Javalin app;
    private static String baseUrl;
    private static Url url;
    private static Transaction transaction;
    private static MockWebServer mockWebServer;
    private static String mockHtml;

    @BeforeAll
    public static void beforeAll() {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;

        url = new Url("https://github.com");
        url.save();
    }

    @AfterAll
    public static void afterAll() {
        app.stop();
    }

    @BeforeEach
    void beforeEach() {
        transaction = DB.beginTransaction();
        mockWebServer = new MockWebServer();
    }

    @AfterEach
    void afterEach() throws IOException {
        transaction.rollback();
        mockWebServer.shutdown();
    }

    @Nested
    class RootTest {

        @Test
        void testIndex() {
            HttpResponse<String> response = Unirest.get(baseUrl).asString();
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getBody()).contains("Анализатор страниц");
        }
    }

    @Nested
    class UrlTest {

        @Test
        void testIndex() {
            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls")
                    .asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).contains(url.getName());
        }

        @Test
        void testShow() {
            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls/" + url.getId())
                    .asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).contains(url.getName());
        }

        @Test
        void testCreate() {
            String inputName = "https://mail.yandex.ru";
            HttpResponse<String> responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", inputName)
                    .asEmpty();

            assertThat(responsePost.getStatus()).isEqualTo(302);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls")
                    .asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).contains(inputName);
            assertThat(body).contains("Страница успешно добавлена");

            Url actualUrl = new QUrl()
                    .name.equalTo(inputName)
                    .findOne();

            assertThat(actualUrl).isNotNull();
            assertThat(actualUrl.getName()).isEqualTo(inputName);
        }

        @Test
        void newIncorrectUrlTest() {
            HttpResponse<String> response = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", "github.com")
                    .asString();

            String body = response.getBody();

            Url urlIncorrect = new QUrl()
                    .name.equalTo("github.com")
                    .findOne();

            assertThat(response.getStatus()).isEqualTo(422);
            assertThat(body).contains("Некорректный URL");
            assertThat(urlIncorrect).isNull();
        }

        @Test
        void newExistingUrlTest() {
            HttpResponse<String> response = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", "https://github.com")
                    .asString();

            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(422);
            assertThat(body).contains("Страница уже существует");
        }

        @Test
        void mockTest() throws IOException {
            mockHtml = Files.readString(Paths.get("src/test/resources/fakePage.html")
                    .toAbsolutePath().normalize());
            mockWebServer.enqueue(new MockResponse().setBody(mockHtml));
            mockWebServer.start();

            String mockUrl = mockWebServer.url("").toString();
            String editedMockUrl = mockUrl.substring(0, mockUrl.length() - 1);

            Unirest.post(baseUrl + "/urls")
                    .field("url", mockUrl)
                    .asEmpty();

            Url testUrl = new QUrl()
                    .name.equalTo(editedMockUrl)
                    .findOne();

            Unirest.post(baseUrl + "/urls/" + testUrl.getId() + "/checks")
                    .asString();

            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls/" + testUrl.getId())
                    .asString();

            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).contains("Страница успешно проверена");
            assertThat(body).contains("TestDescription");
            assertThat(body).contains("TestH1");
            assertThat(body).contains("TestTitle");
        }
    }

}
