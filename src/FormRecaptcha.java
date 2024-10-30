import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name = "FormReCaptcha", urlPatterns = "/form-recaptcha")
public class FormRecaptcha extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String loginUser = "mytestuser";
    private static final String loginPasswd = "My6$Password";
    private static final String loginUrl = "jdbc:mysql://localhost:3306/moviedbexample";
    
    private static final String G_RECAPTCHA_RESPONSE_PARAMETER_NAME = "g-recaptcha-response";
    private static final String SEARCHED_STAR_NAME_PARAMETER_NAME = "name";

    private static final String GET_STARS_SQL = "SELECT * FROM stars WHERE name LIKE ?";
    
    Connection databaseConnection;
    
    @Override
    public void init(ServletConfig config) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            databaseConnection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        } catch (SQLException | ClassNotFoundException exception) {
            System.out.println("Error connecting to database.");
            System.out.println(exception.getMessage());
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String gRecaptchaResponse = request.getParameter(G_RECAPTCHA_RESPONSE_PARAMETER_NAME);
        System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);

        RecaptchaVerificationStatus verification = RecaptchaVerifyUtils.verify(gRecaptchaResponse);
        if (verification != RecaptchaVerificationStatus.VERIFIED) {
            out.println(
                getErrorHtml("Recaptcha Verification Error")
            );
            return;
        }

        try (PreparedStatement statement = databaseConnection.prepareStatement(GET_STARS_SQL)) {

            String name = request.getParameter(SEARCHED_STAR_NAME_PARAMETER_NAME);
            statement.setString(1, name);

            ResultSet resultSet = statement.executeQuery();

            out.println(getLeadingHtml());

            while (resultSet.next()) {
                out.println(
                    getStarTableRowHtml(resultSet)
                );
            }

            out.println(getTrailingHtml());

        } catch (Exception exception) {
            out.println(
                getErrorHtml(exception.getMessage())
            );
        }
        
    }

    private static String getErrorHtml(String message) {
        return
            "<html>" +
            "<head><title>Error</title></head>" +
            "<body>" +
            "<p>error: " + message + "</p>" +
            "</body>" +
            "</html>";
        
    }
    
    private static String getStarTableRowHtml(ResultSet resultSet) throws SQLException {
        String starId = resultSet.getString("id");
        String starName = resultSet.getString("name");
        return String.format("<tr><td>%s</td><td>%s</td></tr>", starId, starName);
    }
    
    private static String getLeadingHtml() {
        return
            "<html><head><title> MovieDB: Found Records </title></head>" +
            "<body><h1> MovieDB: Found Records </h1>" +
            "<table border>" +
            "<tr><td> ID </td><td> Name </td></tr>";
    }
    
    private static String getTrailingHtml() {
        return "</table></body></html>";
    }
    
}
