package nl.huygensing.rdfpatch;

import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;

import java.io.IOException;

public class SimpleQuad {
  public Node getSubject() {
    return subject;
  }

  public Node getPredicate() {
    return predicate;
  }

  public Node getObject() {
    return object;
  }

  public Node getGraphCtx() {
    return graphCtx;
  }

  private Node subject;
  private Node predicate;
  private Node object;
  private Node graphCtx;

  public SimpleQuad(Node[] nodes) throws IOException {
    if(nodes.length != 4) {
      throw new IOException("Illegal amount of nodes for SimpleQuad: " + nodes.length);
    }
    subject = nodes[0];
    predicate = nodes[1];
    object = nodes[2];
    graphCtx = nodes[3];
  }
  
  public void dump() {
    System.out.println("Subj: " + subject.getLabel() + " (" + subject.getClass().getSimpleName() + ")");
    System.out.println("Pred: " + predicate.getLabel() + " (" + predicate.getClass().getSimpleName() + ")");
    System.out.println("Obj: " +  object.getLabel() + " (" + object.getClass().getSimpleName() + ")");
    if(object instanceof Literal) {
      System.out.println("Literal datatype: " + ((Literal) object).getDatatype());
      System.out.println("Literal lang: " + ((Literal) object).getLanguageTag());
    }

    System.out.println("GraphCtx:" +   graphCtx.getLabel());
  }


}
