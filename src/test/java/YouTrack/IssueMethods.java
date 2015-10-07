package YouTrack;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Cookies;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.*;
import static com.jayway.restassured.matcher.RestAssuredMatchers.*;
//import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public class IssueMethods {
    Cookies cookies;

    @Before
    public void setUp() throws Exception {
        RestAssured.baseURI = "https://gorest.myjetbrains.com/youtrack/rest/";

        Response r = given().
                param("login", "oksana.granovska@gmail.com").
                param("password", "GoIT-GoAQA2").
        when().
                post("/user/login");

        cookies = r.getDetailedCookies();
    }

    private String createTestIssue() {
        Response response =
                given().
                        cookies(cookies).
                        param("project", "GAQA2").
                        param("summary", "Test summary").
                        param("description", "Test Desc").
                when().
                        put("/issue");

        String location = response.getHeader("Location");
        String issueId = location.substring(location.lastIndexOf('/') + 1);
        return issueId;
    }

    @Test
    public void testCreateNewIssue() throws Exception {
        given().
                cookies(cookies).
                param("project", "GAQA2").
                param("summary", "Short summary").
                param("description", "Desc").
        when().
                put("/issue").
        then().
                statusCode(201);
    }

    @Test()
    public void testGetIssue() throws Exception {
        String issueId = createTestIssue();

        given().
                cookies(cookies).
        when().
                get("/issue/" + issueId).
        then().
                statusCode(200).
                body("issue.@id", equalTo(issueId)).
                body("issue.field.find {it.@name == 'summary'}.value", equalTo("Test summary")).
                body("issue.field.find {it.@name == 'description'}.value", equalTo("Test Desc"));
    }

    @Test
    public void testDeleteIssue() throws Exception {
        String issueId = createTestIssue();

        given().
                cookies(cookies).
        when().
                delete("/issue/" + issueId).
        then().
                statusCode(200);

    }

    @Test
    public void testIssueExists() throws Exception {
        String issueId = createTestIssue();

        given().
                cookies(cookies).
        when().
                get("/issue/" + issueId + "/exists").
        then().
                statusCode(200);

    }

    @Test
    public void testIssueNotExists() throws Exception {
        given().
                cookies(cookies).
        when().
                get("/issue/BOOLSHIT/exists").
        then().
                statusCode(404);
    }

    @Test
    public void testGetNumberOfIssues() throws Exception {
        Response r =
        given().
                cookies(cookies).
                param("callback", "fun").
                param("filter", 5).
        when().
                get("/issue/count").
        then().
                statusCode(200).
        extract().response();

        System.out.print(r.asString());

        Integer issuesNumber = Integer.parseInt(r.asString().replaceAll("[\\D]", ""));
        System.out.print(issuesNumber);
        assertThat(issuesNumber, greaterThanOrEqualTo(10));
    }
}
