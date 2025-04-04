package webserver;

import http.HttpResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class RequestHandlerTest {

    private OutputStream outputStreamToFile(String path) throws IOException {
        return Files.newOutputStream(Paths.get(path));
    }

    @Test
    @DisplayName("회원 가입 올바로 index.html로 리다이렉트 되는지")
    void forwardIndexPage() throws Exception {
        // given
        String testPath = "src/test/resources/response_signup.txt";
        HttpResponse response = new HttpResponse(outputStreamToFile(testPath));

        // when
        response.redirect("/index.html");

    }

    @Test
    @DisplayName("유저 리스트 확인")
    void forwardUserListPage() throws Exception{
        //given
        String testPath = "src/test/resources/response_list.txt";
        HttpResponse response = new HttpResponse(outputStreamToFile(testPath));

        // when
        response.redirect("/user/list.html");
    }

    @Test
    @DisplayName("로그인 확인")
    void forwardLoginPage() throws Exception {
        //given
        String testPath = "src/test/resources/response_login.txt";
        HttpResponse response = new HttpResponse(outputStreamToFile(testPath));

        // when
        response.redirect("/user/login.html");
    }

    @Test
    @DisplayName("로그인 실패 확인")
    void RedirectLoginFailPage() throws Exception {
        //given
        String testPath = "src/test/resources/response_login_failed.txt";
        HttpResponse response = new HttpResponse(outputStreamToFile(testPath));

        // when
        response.redirect("/user/login_failed.html");
    }

}