package nl.huygensing.demoloader;

import nl.huygensing.rdfpatch.RdfPatchInstruction;
import nl.huygensing.rdfpatch.RdfPatchInstructionHandler;
import nl.huygensing.rdfpatch.RdfPatchParser;
import nl.huygensing.rdfpatch.SimpleQuad;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Comparator;

public class FlatSqLiteLoader implements RdfPatchInstructionHandler, Runnable {
  private final File watchDir;
  private Connection connection;
  public static boolean signal = false;

  public FlatSqLiteLoader(File file) {
    this.watchDir = file;
  }

  public static void main(String[] args) throws SQLException, IOException {
    setupTables();
    new FlatSqLiteLoader(new File(args[0])).startDaemon();
  }

  public void startDaemon() throws SQLException, IOException {
    if(!watchDir.exists() || !watchDir.isDirectory()) {
      throw new RuntimeException("not a valid dir: " + watchDir.getAbsolutePath());
    }

    signal = true;
    new Thread(this).start();
  }

  @Override
  public void run() {
    System.out.println("run invoked");
    try {
      connection = DriverManager.getConnection("jdbc:sqlite:demo.db");
      connection.setAutoCommit(false);
      while (signal) {
        File[] files = watchDir.listFiles();
        // Sort the list of files by name (should be a UNIX timestamp)
        Arrays.sort(files, new Comparator<File>() {
          @Override
          public int compare(File file, File t1) {
            return Integer.parseInt(file.getName()) - Integer.parseInt(t1.getName());
          }
        });

        for (File f : files) {
          System.err.println(f.getName());
          RdfPatchParser parser = new RdfPatchParser(new FileInputStream(f), this);
          parser.parse();
          connection.commit();
          f.delete();
        }

        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {

        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void stopDaemon() throws SQLException {
    signal = false;
    connection.close();
  }

  public static void setupTables() throws SQLException {
    Connection connection = DriverManager.getConnection("jdbc:sqlite:demo.db");
    Statement statement = connection.createStatement();
    statement.setQueryTimeout(30);

    ResultSet rs1 = statement.executeQuery("SELECT count(name) as cnt FROM sqlite_master WHERE type='table' AND name='quads'");
    rs1.next();
    int count = rs1.getInt("cnt");
    if(count == 0) {
      System.out.println("Creating table quads");
      statement.executeUpdate("create table quads (id string, subject string, predicate string, object string, graphctx string)");
    } else {
      System.out.println("Table quads exist");
    }



    statement.close();
    connection.close();
  }

  @Override
  public void handleInstruction(RdfPatchInstruction instruction)  {
    RdfPatchInstruction.Type operation = instruction.getType();
    switch (operation) {
      case ADD: addQuad(instruction.getSimpleQuad()); break;
      case DELETE: deleteQuad(instruction.getSimpleQuad()); break;
      case COMMENT:
      default: System.out.println("Ignoring comment " + instruction.getCommentLine());
    }
  }

  private void deleteQuad(SimpleQuad quad) {
//    System.out.println("Deleting quad: ");
//    quad.dump();
    try {
      PreparedStatement ps = connection.prepareStatement("DELETE FROM quads WHERE id=?");
      ps.setString(1, makeId(quad));
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private void addQuad(SimpleQuad quad) {
//    System.out.println("Adding quad: ");
//    quad.dump();
    try {
      PreparedStatement ps = connection.prepareStatement("INSERT INTO quads VALUES (?, ?, ?, ?, ?)");
      ps.setString(1, makeId(quad));
      ps.setString(2, quad.getSubject().getLabel());
      ps.setString(3, quad.getPredicate().getLabel());
      ps.setString(4, quad.getObject().getLabel());
      ps.setString(5, quad.getGraphCtx().getLabel());
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private String makeId(SimpleQuad quad) {
    return quad.getSubject().getLabel().hashCode() + "|" +
        quad.getPredicate().getLabel().hashCode() + "|" +
        quad.getPredicate().getLabel().hashCode() + "|" +
        quad.getGraphCtx().getLabel().hashCode();

  }


}
