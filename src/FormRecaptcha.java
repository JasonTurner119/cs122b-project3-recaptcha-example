import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

@WebServlet(name = "FormReCaptcha", urlPatterns = "/form-recaptcha")
public class FormRecaptcha extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String DATABASE_USER = "mytestuser";
    private static final String DATABASE_PASSWORD = "My6$Password";
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/moviedb";
    
    private static final String G_RECAPTCHA_RESPONSE_PARAMETER_NAME = "g-recaptcha-response";
    private static final String SEARCHED_STAR_NAME_PARAMETER_NAME = "name";

    private static final String GET_STARS_SQL = "SELECT * FROM stars WHERE name LIKE ?";
    private static final int GET_STARS_SQL_STAR_NAME_INDEX = 1;
    private static final String STAR_NAME_SQL_COLUMN_NAME = "stars.name";
    private static final String STAR_ID_SQL_COLUMN_NAME = "stars.id";
    
    private static final String CONTENT_TYPE = "text/html";
    
    Connection databaseConnection;
    
    @Override
    public void init(ServletConfig config) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            databaseConnection = DriverManager.getConnection(
                DATABASE_URL,
                DATABASE_USER,
                DATABASE_PASSWORD
            );
        } catch (SQLException | ClassNotFoundException exception) {
            System.out.println("Error connecting to database.");
            System.out.println(exception.getMessage());
        }
    }

    @Override
    public void doPost(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException {

        String gRecaptchaResponse = request.getParameter(G_RECAPTCHA_RESPONSE_PARAMETER_NAME);

        RecaptchaVerificationStatus verification = RecaptchaVerifyUtils.verify(gRecaptchaResponse);
        if (verification != RecaptchaVerificationStatus.VERIFIED) {
            sendRecaptchaError(response);
            return;
        }

        String starName = request.getParameter(SEARCHED_STAR_NAME_PARAMETER_NAME);
        performStarsQuery(starName, response);
        
    }
    
    private void performStarsQuery(
        String starName,
        HttpServletResponse response
    ) throws IOException {
        try (PreparedStatement statement = databaseConnection.prepareStatement(GET_STARS_SQL)) {
            statement.setString(GET_STARS_SQL_STAR_NAME_INDEX, starName);
            ResultSet resultSet = statement.executeQuery();
            sendStarsList(response, resultSet);
        } catch (Exception exception) {
            sendExceptionError(response, exception);
        }
    }
    
    private static void sendRecaptchaError(HttpServletResponse response) throws IOException {
        response.setContentType(CONTENT_TYPE);
        response.getWriter().println(getErrorHtml("Recaptcha Verification Error"));
    }
    
    private static void sendExceptionError(
        HttpServletResponse response,
        Exception exception
    ) throws IOException {
        response.setContentType(CONTENT_TYPE);
        response.getWriter().println(getErrorHtml(exception.getMessage()));
    }
    
    private static void sendStarsList(
        HttpServletResponse response,
        ResultSet resultSet
    ) throws IOException, SQLException {
        response.setContentType(CONTENT_TYPE);
        response.getWriter().println(getHtmlForStarsList(resultSet));
    }

    private static String getErrorHtml(String message) {
        return
            "<html>" +
            "<head><title> Error </title></head>" +
            "<body>" +
            "<p>error: " + message + "</p>" +
            "</body>" +
            "</html>";
        
    }
    
    private static String getHtmlForStarsList(ResultSet resultSet) throws SQLException {
        StringBuilder html = new StringBuilder();
        html.append(getStarTableLeadingHtml());
        while (resultSet.next()) {
			html.append(getStarTableRowHtml(resultSet));
        }
        html.append(getStarTableTrailingHtml());
        return html.toString();
    }
    
    private static String getStarTableRowHtml(ResultSet resultSet) throws SQLException {
        String starId = resultSet.getString(STAR_ID_SQL_COLUMN_NAME);
        String starName = resultSet.getString(STAR_NAME_SQL_COLUMN_NAME);
        return String.format("<tr><td> %s </td><td> %s </td></tr>", starId, starName);
    }
    
    private static String getStarTableLeadingHtml() {
        return
            "<html>" +
            "<head><title> MovieDB: Found Records </title></head>" +
            "<body>" +
            "<h1> MovieDB: Found Records </h1>" +
            "<table border>" +
            "<tr><td> ID </td><td> Name </td></tr>";
    }
    
    private static String getStarTableTrailingHtml() {
        return "</table></body></html>";
    }
    
}
