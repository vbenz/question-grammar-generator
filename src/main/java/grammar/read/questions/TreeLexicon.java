package grammar.read.questions;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class TreeLexicon {

    TreeLexiconNode Root;

    public TreeLexicon() {
        Root = new TreeLexiconNode();
    }

    public void insert(String entry, String uri, String type) {
        String[] tokenized_entry = entry.split("\\s+");
        Root.insert(entry, tokenized_entry, uri, type, 0);
    }

    public List<ResultQA> lookup(String candidate) {
        ArrayList<ResultQA> list = new ArrayList<ResultQA>();

        String[] tokenized_array = candidate.split("\\s+");

        for (int i = 0; i < tokenized_array.length; i++) {
            list.addAll(Root.lookup(tokenized_array, i));
        }

        return list;
    }

    public List<ResultQA> lookup(String candidate, double threshold) {
        ArrayList<ResultQA> list = new ArrayList<ResultQA>();

        String[] tokenized_array = candidate.split("\\s+");

        for (int i = 0; i < tokenized_array.length; i++) {
            List<ResultQA> results=Root.lookup(tokenized_array, i, threshold);
            /*for (Result result: results){
                 System.out.println("...............");
                System.out.println(result.toString());
            }*/
            list.addAll(results);
        }

        return list;
    }

}
