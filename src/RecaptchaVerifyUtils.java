import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

public class RecaptchaVerifyUtils {

    public static final String SITE_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static RecaptchaVerificationStatus verify(String gRecaptchaResponse) {
        String parameters =
            "secret=" + RecaptchaConstants.SECRET_KEY +
            "&response=" + gRecaptchaResponse;

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(SITE_VERIFY_URL))
            .header("User-Agent", "Mozilla/5.0")
            .header("Accept-Language", "en-US,en;q=0.5")
            .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            .POST(HttpRequest.BodyPublishers.ofString(parameters))
            .build();

        try {
            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject jsonObject = new Gson().fromJson(httpResponse.body(), JsonObject.class);
            boolean success = jsonObject.get("success").getAsBoolean();
            if (success) {
                return RecaptchaVerificationStatus.VERIFIED;
            }
        } catch (Exception exception) {
            System.out.println("Error while verifying recaptcha response.");
            System.out.println(exception.getMessage());
        }

        return RecaptchaVerificationStatus.UNVERIFIED;
    }

}
