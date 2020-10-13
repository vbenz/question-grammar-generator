package grammar.read.questions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TreeLexiconNode {

    HashMap<String, TreeLexiconNode> children;

    HashMap<String, ResultQA> map;

    public TreeLexiconNode() {
        children = new HashMap<String, TreeLexiconNode>();
        map = new HashMap<String, ResultQA>();
    }

    public List<ResultQA> lookup(String[] tokenized_candidate, int i) {

        List<ResultQA> list = new ArrayList<ResultQA>();
        TreeLexiconNode child;

        //System.out.print(map+"\n");
        if (i < tokenized_candidate.length) {
         //System.out.print("Checking: "+tokenized_candidate[i]+"\n");

            if (children.containsKey(tokenized_candidate[i])) {

                //System.out.print("Yes, it contains: "+tokenized_candidate[i]+"\n");
                child = children.get(tokenized_candidate[i]);
                 this.print(children);

                list.addAll(child.lookup(tokenized_candidate, i + 1));
            }
        }

        for (String entry : map.keySet()) {
            list.add(new ResultQA(entry, map.get(entry).getUri(), map.get(entry).getType()));
        }

        return list;
    }

    public List<ResultQA> lookup(String[] tokenized_candidate, int i, double threshold) {
        List<ResultQA> list = new ArrayList<ResultQA>();
        TreeLexiconNode child;

        // System.out.print(map+"\n");
        double normLD;

        if (i < tokenized_candidate.length) {
            // System.out.print("Checking: "+tokenized_candidate[i]+"\n");

            for (String entry : children.keySet()) {
                //System.out.println("entry:"+entry);
                //System.out.println("tokenized_candidate:"+tokenized_candidate[i]);
                normLD = Levenshtein.normalizedDistance(entry, tokenized_candidate[i]);

                if (normLD < threshold) {

                    // System.out.print(entry +" "+tokenized_candidate[i]+":"+normLD+"\n");
                    child = children.get(entry);
                    list.addAll(child.lookup(tokenized_candidate, i + 1, threshold));
                }

            }
        }

        for (String entry : map.keySet()) {
            list.add(new ResultQA(entry, map.get(entry).getUri(), map.get(entry).getType()));
        }

        return list;
    }

    public void insert(String entry, String[] tokenized_entry, String uri, String type, int i) {
        {

            TreeLexiconNode child = null;

            if (i < tokenized_entry.length) {

                if (children.containsKey(tokenized_entry[i])) {
                    child = children.get(tokenized_entry[i]);
                    child.insert(entry, tokenized_entry, uri, type, i + 1);
                } else {
                    child = new TreeLexiconNode();
                    child.insert(entry, tokenized_entry, uri, type, i + 1);
                    children.put(tokenized_entry[i], child);
                }

            }
            if (tokenized_entry.length == i) {
                map.put(entry, new ResultQA(entry, uri, type));
                // System.out.print("Inserting: "+entry+" "+uri+"\n");
            }

        }

    }

    @Override
    public String toString() {
        return "TreeLexiconNode{" + "children=" + children + ", map=" + map + '}';
    }

    private void print(HashMap<String, TreeLexiconNode> children) {
        /*HashMap<String, Result> map=child.map;
        for(String key:map.keySet()) {
            Result result=map.get(key);
            System.out.println(key);
             System.out.println(result.toString());
        }
        
       
        for(String key:children.keySet()) {
            TreeLexiconNode treeLexiconNode=children.get(key);
            System.out.println(key);
             System.out.println(treeLexiconNode.toString());
        }*/
    }
    
}
