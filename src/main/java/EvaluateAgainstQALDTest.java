import evaluation.EvaluationResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

class EvaluateAgainstQALDTest {

  public static void main(String[] args) throws IOException, ClassNotFoundException {
    FileInputStream fi = new FileInputStream(new File("evaluationResultObject.txt"));
    ObjectInputStream oi = new ObjectInputStream(fi);

    // Read objects
    EvaluationResult evaluationResult1 = (EvaluationResult) oi.readObject();

    System.out.println(evaluationResult1.toString());

    oi.close();
    fi.close();
  }
}