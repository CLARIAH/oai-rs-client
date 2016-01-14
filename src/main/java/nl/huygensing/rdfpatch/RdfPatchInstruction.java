package nl.huygensing.rdfpatch;

import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;

import java.io.IOException;

public class RdfPatchInstruction {
  public enum Type {ADD, COMMENT, DELETE};
  private Type type;
  private SimpleQuad simpleQuad;

  private String commentLine;

  public RdfPatchInstruction(String rdfPatchLine) throws ParseException, IOException {
    String instruction = rdfPatchLine.substring(0, 1);
    switch (instruction) {
      case "+": this.type = Type.ADD; break;
      case "-": this.type = Type.DELETE; break;
      case "#": this.type = Type.COMMENT; break;
      default: throw new IOException("Unsupported instruction: " + instruction);
    }

    if(type != Type.COMMENT) {
      this.simpleQuad = new SimpleQuad(NxParser.parseNodes(rdfPatchLine.substring(2, rdfPatchLine.length())));
    } else {
      commentLine = rdfPatchLine.substring(1, rdfPatchLine.length()).trim();
    }
  }

  public SimpleQuad getSimpleQuad() {
    return simpleQuad;
  }

  public String getCommentLine() {
    return commentLine;
  }


  public Type getType() {
    return type;
  }



  public void dump() {
    System.out.println(type);
    switch (type) {
      case COMMENT: System.out.println(commentLine); break;
      default: simpleQuad.dump();
    }
    System.out.println();
  }
}
