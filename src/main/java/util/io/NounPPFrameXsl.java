/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.io;

import java.io.IOException;

/**
 *
 * @author elahi
 */
public class NounPPFrameXsl {
//LemonEntry	partOfSpeech	writtenForm (singular)	writtenForm (plural)	preposition	SyntacticFrame	copulativeArg	prepositionalAdjunct	sense	reference	domain	range	GrammarRule1:question1	SPARQL	GrammarRule1: question2	SPARQL Question 2	GrammarRule 1: questions	SPARQL 	NP (Grammar Rule 2)		grammar rules	numberOfQuestions
//birthPlace_of	noun	birth place	-	of	NounPPFrame	range	domain	1	dbo:birthPlace	dbo:Person	dbo:Place	#NAME?	#NAME?	#NAME?	#NAME?	#NAME?	#NAME?	#NAME?		2	

    private String LemonEntry = "birthPlace_of";
    private String partOfSpeech = "noun";
    private String writtenForm_singular = "birth place";
    private String writtenForm_plural = "-";
    private String preposition = "of";
    private String SyntacticFrame = "NounPPFrame";
    private String copulativeArg = "range";
    private String prepositionalAdjunct = "domain";
    private String sense = "1";
    private String reference = "dbo:birthPlace";
    private String range = "dbo:Place";
    private String domain = "dbo:Person";

    public NounPPFrameXsl(String LemonEntry, String partOfSpeech, String writtenForm_singular, String writtenForm_plural,
            String preposition, String SyntacticFrame, String copulativeArg, String prepositionalAdjunct,
            String sense, String reference, String domain) {

        this.LemonEntry = LemonEntry;
        this.partOfSpeech = partOfSpeech;
        this.writtenForm_singular = writtenForm_singular;
        this.writtenForm_plural = writtenForm_plural;

        this.preposition = preposition;
        this.SyntacticFrame = SyntacticFrame;
        this.copulativeArg = copulativeArg;
        this.prepositionalAdjunct = prepositionalAdjunct;

        this.sense = sense;
        this.reference = reference;
        this.domain = domain;

    }

    private NounPPFrameXsl(String LemonEntry, String writtenForm_singular, String preposition, String reference, String domain, String range) {
        this.LemonEntry = LemonEntry;
        this.writtenForm_singular = writtenForm_singular;
        this.preposition = preposition;
        this.reference = reference;
        this.domain = domain;
        this.range = range;
    }

    public String getLemonEntry() {
        return LemonEntry;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public String getWrittenForm_singular() {
        return writtenForm_singular;
    }

    public String getWrittenForm_plural() {
        return writtenForm_plural;
    }

    public String getPreposition() {
        return preposition;
    }

    public String getSyntacticFrame() {
        return SyntacticFrame;
    }

    public String getCopulativeArg() {
        return copulativeArg;
    }

    public String getPrepositionalAdjunct() {
        return prepositionalAdjunct;
    }

    public String getSense() {
        return sense;
    }

    public String getReference() {
        return reference;
    }

    public String getDomain() {
        return domain;
    }
    
    public static void main(String args[]) throws IOException {
        

        String lemonEntry = "birthPlace_of";
        String partOfSpeech = "noun";
        String writtenForm_singular = "birth place";
        String writtenForm_plural = "-";
        String preposition = "of";
        String SyntacticFrame = "NounPPFrame";
        String copulativeArg = "range";
        String prepositionalAdjunct = "domain";
        String sense = "1";
        String reference = "dbo:birthPlace";
        String domain = "dbo:Person";
         String range = "dbo:Place";
         String resources="src/test/resources/lexicon/input/";
         String fileName="lexiconNew"+"-"+lemonEntry+".ttl";

        /*NounPPFrameXsl nounPPFrameXsl = new NounPPFrameXsl(LemonEntry, partOfSpeech, writtenForm_singular, writtenForm_plural,
                preposition, SyntacticFrame, copulativeArg, prepositionalAdjunct,
                sense, reference, domain);*/
        
        NounPPFrameXsl nounPPFrameXsl = new NounPPFrameXsl(lemonEntry,writtenForm_singular, preposition, reference, domain, range);
        String tutleString=nounPPFrameXsl.makeTurtle();
        FileUtils.stringToFile(tutleString, resources+fileName);
        System.out.println(tutleString);

    }

    private String makeTurtle() {
        if (this.reference.contains(":")) {
            String[] info = this.reference.split(":");
            this.reference = info[1];

        }
        if (this.domain.contains(":")) {
            String[] info = this.domain.split(":");
            this.domain = info[1];

        }
        if (this.range.contains(":")) {
            String[] info = this.range.split(":");
            this.range = info[1];

        }
        
        String template = "@prefix :        <http://localhost:8080/lexicon#> .\n"
                + "\n"
                + "@prefix lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo#> .\n"
                + "@prefix lemon:   <http://lemon-model.net/lemon#> .\n"
                + "\n"
                + "@base            <http://localhost:8080#> .\n"
                + "\n"
                + ":lexicon_en a    lemon:Lexicon ;\n"
                + "  lemon:language \"en\" ;\n"
                + "  lemon:entry    :birthPlace_of ;\n"
                + "  lemon:entry    :of .\n"
                + "\n"
                + ":birthPlace_of a       lemon:LexicalEntry ;\n"
                + "  lexinfo:partOfSpeech lexinfo:noun ;\n"
                + "  lemon:canonicalForm  :birthPlace_form ;\n"
                + "  lemon:synBehavior    :birthPlace_of_nounpp ;\n"
                + "  lemon:sense          :birthPlace_sense_ontomap .\n"
                + "\n"
                + ":birthPlace_form a lemon:Form ;\n"
                + "  lemon:writtenRep \""+this.writtenForm_singular+"\"@en .\n"
                + "\n"
                + ":birthPlace_of_nounpp a        lexinfo:NounPPFrame ;\n"
                + "  lexinfo:copulativeArg        :arg1 ;\n"
                + "  lexinfo:prepositionalAdjunct :arg2 .\n"
                + "\n"
                + ":birthPlace_sense_ontomap a lemon:OntoMap, lemon:LexicalSense ;\n"
                + "  lemon:ontoMapping         :birthPlace_sense_ontomap ;\n"
                + "  lemon:reference           <http://dbpedia.org/ontology/"+this.reference+"> ;\n"
                + "  lemon:subjOfProp          :arg2 ;\n"
                + "  lemon:objOfProp           :arg1 ;\n"
                + "  lemon:condition           :birthPlace_condition .\n"
                + "\n"
                + ":birthPlace_condition a lemon:condition ;\n"
                + "  lemon:propertyDomain  <http://dbpedia.org/ontology/"+this.domain+"> ;\n"
                + "  lemon:propertyRange   <http://dbpedia.org/ontology/"+this.range+"> .\n"
                + "\n"
                + ":arg2 lemon:marker :of .\n"
                + "\n"
                + "## Prepositions ##\n"
                + "\n"
                + ":of a                  lemon:SynRoleMarker ;\n"
                + "  lemon:canonicalForm  [ lemon:writtenRep \""+this.preposition+"\"@en ] ;\n"
                + "  lexinfo:partOfSpeech lexinfo:preposition .";
        return template;

    }

}
