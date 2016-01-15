package nl.huygensing.demoloader;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

public class DemoServer extends AbstractHandler {

  private FlatSqLiteLoader loader;

  public DemoServer(String path) {
    File watchDir = new File(path);
    loader = new FlatSqLiteLoader(watchDir);
  }

  private void startDaemon() throws IOException, SQLException {
    if(!FlatSqLiteLoader.signal) {
      loader.startDaemon();
    }
  }

  private void stopDaemon() throws SQLException {
    loader.stopDaemon();
  }

  @Override
  public void handle(String s, Request baseRequest, HttpServletRequest request,
                     HttpServletResponse response) throws IOException, ServletException {

    switch (baseRequest.getPathInfo()) {
      case "/": writeIndex(baseRequest, response); break;
      case "/table":
        try {
          writeTable(baseRequest, response);
        } catch (SQLException e) {
          e.printStackTrace();
        }
        break;
      default: writeNotFound(baseRequest, response); break;
    }

  }

  private void writeTable(Request baseRequest, HttpServletResponse response) throws IOException, SQLException {
    response.setContentType("text/html;charset=utf-8");
    response.setStatus(HttpServletResponse.SC_OK);
    baseRequest.setHandled(true);
    PrintWriter w = response.getWriter();
    w.println("<div>" + new Date() + "</div>");
    Connection connection = DriverManager.getConnection("jdbc:sqlite:demo.db");
    Statement statement = connection.createStatement();
    ResultSet rs = statement.executeQuery("SELECT * FROM quads");
    w.println("<table>");
    while(rs.next()) {
      w.println("<tr>");
      w.println("<td>" + rs.getString(2) + "</td>");
      w.println("<td>" + rs.getString(2) + "</td>");
      w.println("<td>" + rs.getString(2) + "</td>");
      w.println("<td>" + rs.getString(2) + "</td>");
      w.println("<td>" + rs.getString(2) + "</td>");
      w.println("</tr>");
    }
    w.println("</table>");
    statement.close();
    connection.close();
  }

  private void writeNotFound(Request baseRequest, HttpServletResponse response) throws IOException {
    response.setContentType("text/html;charset=utf-8");
    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    baseRequest.setHandled(true);
    response.getWriter().println("404 not found");
  }

  private void writeIndex(Request baseRequest, HttpServletResponse response) throws IOException {
    response.setContentType("text/html;charset=utf-8");
    response.setStatus(HttpServletResponse.SC_OK);
    baseRequest.setHandled(true);
    PrintWriter w = response.getWriter();

    w.println("<html><head><script src='https://code.jquery.com/jquery-1.12.0.min.js'></script></head><body>");
    w.println("<h1>SQLite table contents</h1>");
    w.println("<div id='table'></div>");
    w.println("<script>");
    w.println("function poll() { " +
        "$.ajax('/table', { success: function(resp) { $('#table').html(resp) }});" +
        "setTimeout(poll, 1000); }"
    );
    w.println("function startSync() { $.ajax('/startdaemon'); }");
    w.println("function stopSync() { $.ajax('/stopdaemon'); }");
    w.println("poll();");
    w.println("</script>");
    w.println("</body></html>");
  }



  public static void main(String[] args) throws Exception
  {
    FlatSqLiteLoader.setupTables();
    DemoServer demoServer = new DemoServer(args[0]);
    demoServer.startDaemon();
    Server server = new Server(8180);
    server.setHandler(demoServer);

    server.start();
    server.join();
  }
}
