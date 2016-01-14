package nl.huygensing.rdfpatch;


import org.semanticweb.yars.nx.parser.ParseException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RdfPatchParser {

  private final InputStream stream;
  private final RdfPatchInstructionHandler handler;

  public RdfPatchParser(InputStream is, RdfPatchInstructionHandler handler) {
    this.stream = is;
    this.handler = handler;
  }

  public void parse() throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(this.stream));
    String line;
    long lineNumber = 1;
    while((line = br.readLine()) != null) {
      try {
        RdfPatchInstruction rdfPatchInstruction = new RdfPatchInstruction(line);
        lineNumber++;
        handler.handleInstruction(rdfPatchInstruction);
      } catch (ParseException | IOException e) {
        throw new IOException("RdfPatch parse exception at line: " + lineNumber, e);
      }
    }
    br.close();
    stream.close();

  }

  public static void main(String args[]) throws ParseException, IOException {
    RdfPatchInstructionHandler handler = new RdfPatchInstructionHandler() {
      @Override
      public void handleInstruction(RdfPatchInstruction instruction) {
        instruction.dump();
      }
    };

    RdfPatchParser rdfPatchParser = new RdfPatchParser(new FileInputStream(args[0]), handler);
    rdfPatchParser.parse();
  }


}
