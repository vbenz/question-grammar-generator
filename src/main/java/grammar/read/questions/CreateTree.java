/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grammar.read.questions;


import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author elahi
 */
@Deprecated
public class CreateTree {

    private TreeLexicon treeLexicon = new TreeLexicon();
    private List<Tupple> inputTupples = new ArrayList<Tupple>();
    private static String outputDir = "src/main/resources/lexicon/en/nouns/new/output/";
    public static String INPUT_LOCATION = "src/main/resources";
    public static String INPUT_TEXT = "questions.txt";
    

    public CreateTree(String inputFileName) throws IOException, Exception {
        inputTupples = getInputTupplesFromPython(inputFileName);
        treeLexicon = createTree(inputTupples);
    }

    public List<Tupple> getInputTupplesFromTextFile(String fileName) throws FileNotFoundException, IOException {
        // Open the file that is the first
        // command line parameter
        List<Tupple> inputTupples = new ArrayList<Tupple>();
        try {

            FileInputStream fstream = new FileInputStream(fileName);

            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            String token, tag;
            //Read File Line By Line
            String entry;
            String uri;
            String type;
            Pattern p = Pattern.compile("(.*?)\t(.*?)\t(.*?)$");
            Matcher matcher;
            while ((strLine = br.readLine()) != null) {
                // Print the content on the console

                matcher = p.matcher(strLine);

                if (matcher.find()) {
                    entry = matcher.group(2);
                    uri = matcher.group(1);
                    type = matcher.group(3);
                    inputTupples.add(new Tupple(entry, uri, type));
                    //System.out.println("entry: " + entry + " uri:" + uri + " type:" + type);
                }
            }
        } // doesn't matches with ArithmeticException 
        catch (Exception ex) {
            System.out.println("File not found exception!!!");
        }

        return inputTupples;
    }

    private TreeLexicon createTree(List<Tupple> inputTupples) throws FileNotFoundException, IOException {
        TreeLexicon lexicon = new TreeLexicon();

        for (Tupple tupple : inputTupples) {
            lexicon.insert(tupple.getEntry(), tupple.getUri(), tupple.getType());
        }

        //checkResultWhenTextFile(lexicon);
        return lexicon;
    }

    public List<Tupple> getInputTupplesFromPython(String fileName) throws FileNotFoundException, IOException {
        List<Tupple> inputTupples = new ArrayList<Tupple>();
        List<String> questions = new ArrayList<String>();
        List<String> answers = new ArrayList<String>();

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(fileName));
            String line = reader.readLine();
            while (line != null) {
                if (line.contains(":")) {
                    String[] info = line.split(":");
                    if (info[0].contains("Q")) {
                        questions.add(info[1]);
                    } else if (info[0].contains("A")) {
                        answers.add(info[1]);
                    }
                }
                // read next line
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Integer index = 0; index < questions.size(); index++) {
            String question = questions.get(index).toLowerCase().trim();
            String answer = answers.get(index).toLowerCase().trim();
            Tupple tupple = new Tupple(question, answer, "legal");
            inputTupples.add(tupple);
        }
        return inputTupples;

    }

    public TreeLexicon getTreeLexicon() {
        return treeLexicon;
    }

    public List<Tupple> getInputTupples() {
        return inputTupples;
    }

}
