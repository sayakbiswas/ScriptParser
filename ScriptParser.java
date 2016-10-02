package edu.ufl.sayak;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.StanfordCoreNLPClient;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.util.StringUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sayak on 7/9/16.
 */
public class ScriptParser {
    public static void main(String[] args) throws IOException {
        int counter = 0;
        String line;

        int dialogueTurnsPM = 0;
        int dialogueTurnsSH = 0;

        int totalWordsPM = 0;
        int totalWordsSH = 0;

        int wordLengthPM = 0;
        int wordLengthSH = 0;

        //Utterances of a specific word
        int utteranceWordPM = 0;
        int utteranceWordSH = 0;

        //Words per utterance
        List<Integer> utteranceCollectionPM = new ArrayList<Integer>();
        List<Integer> utteranceCollectionSH = new ArrayList<Integer>();
        HashMap<String, Integer> tokenCountPM = new HashMap<String, Integer>();
        HashMap<String, Integer> tokenCountSH = new HashMap<String, Integer>();
        HashMap<String, Integer> tokenCountTotal = new HashMap<String, Integer>();

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        BufferedReader stopwordFile = new BufferedReader(new InputStreamReader(new FileInputStream(new File("src/edu/ufl/sayak/stopwords.txt"))));
        String stopword;
        HashSet<String> stopwords = new HashSet<String>();
        while ((stopword = stopwordFile.readLine()) != null) {
            //System.out.println(stopword);
            stopwords.add(stopword.toUpperCase());
        }
        HashMap<String, Integer> bigramMap = new HashMap<>();
        HashMap<String, Integer> trigramMap = new HashMap<>();

        BufferedReader file = new BufferedReader(new InputStreamReader(new FileInputStream(new File("src/edu/ufl/sayak/dialog.txt"))));

        String speakerIdentity = "noSpeaker";
        String prevSpeaker = "noSpeaker";
        while ((line = file.readLine()) != null) {
            if (line.equals ("PHIL") || line.equals ("SIDNEY")) {
                speakerIdentity = line;
                counter++;
                continue;
            }
            //speakerIdentity = line.substring(0, 2);
            //System.out.println(speakerIdentity);

            List<String> wordsList = new ArrayList<>();
            Pattern pattern = Pattern.compile("\\b(?!PM:|SH:)\\b[\\S]+");
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) {
                //if(!stopwords.contains(matcher.group().toUpperCase()))
                    if(!matcher.group().startsWith("PM") && !matcher.group().startsWith("SH"))
                        wordsList.add(matcher.group());
            }

            Collection<String> bigrams = StringUtils.getNgrams(wordsList, 2, 2);
            for (String string: bigrams) {
                //System.out.println(string);
                if(bigramMap.containsKey(string)) {
                    bigramMap.put(string, bigramMap.get(string) + 1);
                } else {
                    bigramMap.put(string, 1);
                }
            }

            Collection<String> trigrams = StringUtils.getNgrams(wordsList, 3, 3);
            for (String string: trigrams) {
                //System.out.println(string);
                if(trigramMap.containsKey(string)) {
                    trigramMap.put(string, trigramMap.get(string) + 1);
                } else {
                    trigramMap.put(string, 1);
                }
            }

            if(speakerIdentity.equals("PHIL"))
            {
                if(prevSpeaker == "SIDNEY" || prevSpeaker == "noSpeaker")
                    dialogueTurnsPM++;
                totalWordsPM = totalWordsPM + CountWords(line);
                wordLengthPM = wordLengthPM + WordLength(line);

                //Determine if utterance contains the word 'WAS'
                if (ContainsWord(line, "WAS"))
                    utteranceWordPM++;

                //Words per utterance
                utteranceCollectionPM.add(CountWordsAgain(line, tokenCountPM));
                prevSpeaker = "PHIL";
                speakerIdentity = "noSpeaker";
            }
            else if (speakerIdentity.equals("SIDNEY"))
            {
                if(prevSpeaker == "PHIL" || prevSpeaker == "noSpeaker")
                    dialogueTurnsSH++;
                totalWordsSH = totalWordsSH + CountWords(line);
                wordLengthSH = wordLengthSH + WordLength(line);

                //Determine if utterance contains the word 'WAS'
                if (ContainsWord(line, "WAS"))
                    utteranceWordSH++;

                //Words per utterance
                utteranceCollectionSH.add(CountWordsAgain(line, tokenCountSH));
                prevSpeaker = "SIDNEY";
                speakerIdentity = "noSpeaker";
            }
            //counter++;
        }
        int maxValue = 0;
        String maxString = null;

        for (Map.Entry<String, Integer> entry : tokenCountPM.entrySet()) {
            //System.out.println(entry.getKey() + " " +  entry.getValue());
            if(tokenCountTotal.containsKey(entry.getKey())) {
                if(entry.getValue() > tokenCountTotal.get(entry.getKey())) {
                    tokenCountTotal.put(entry.getKey(), entry.getValue());
                }
            } else {
                tokenCountTotal.put(entry.getKey(), entry.getValue());
            }
            if(entry.getValue() >= maxValue) {
                if(!entry.getKey().equals(":") && !entry.getKey().equals("PM") && !entry.getKey().equals("SH")) {
                    maxValue = entry.getValue();
                    maxString = entry.getKey();
                }
            }
            //System.out.println(maxValue);
        }
        System.out.println("Max entry PM" + maxString + " " + maxValue);

        int maxValueSH = 0;
        String maxStringSH = null;

        for (Map.Entry<String, Integer> entry : tokenCountSH.entrySet()) {
            System.out.println(entry.getKey() + " " +  entry.getValue());
            if(tokenCountTotal.containsKey(entry.getKey())) {
                if(entry.getValue() > tokenCountTotal.get(entry.getKey())) {
                    tokenCountTotal.put(entry.getKey(), entry.getValue());
                }
            } else {
                tokenCountTotal.put(entry.getKey(), entry.getValue());
            }
            if(entry.getValue() >= maxValueSH) {
                if(!entry.getKey().equals(":") && !entry.getKey().equals("PM") && !entry.getKey().equals("SH")) {
                    maxValueSH = entry.getValue();
                    maxStringSH = entry.getKey();
                }
            }
            //System.out.println(maxValue);
        }
        System.out.println("Max entry SH" + maxStringSH + " " + maxValueSH);

        int maxValueTotal = 0;
        String maxStringTotal = null;

        for (Map.Entry<String, Integer> entry : tokenCountTotal.entrySet()) {
            if(entry.getValue() >= maxValueTotal) {
                if(!entry.getKey().equals(":") && !entry.getKey().equals("PM") && !entry.getKey().equals("SH")) {
                    maxValueTotal = entry.getValue();
                    maxStringTotal = entry.getKey();
                }
            }
        }
        System.out.println("Max entry total" + maxStringTotal + " " + maxValueTotal);

        int maxValueBigram = 0;
        String maxBigram = null;
        for (Map.Entry<String, Integer> entry : bigramMap.entrySet()) {
            if(entry.getValue() >= maxValueBigram) {
                maxValueBigram = entry.getValue();
                maxBigram = entry.getKey();
            }
        }
        System.out.println("Max Bigram " + maxBigram + " " + maxValueBigram);

        int maxValueTrigram = 0;
        String maxTrigram = null;
        for (Map.Entry<String, Integer> entry : trigramMap.entrySet()) {
            if(entry.getValue() >= maxValueTrigram) {
                maxValueTrigram = entry.getValue();
                maxTrigram = entry.getKey();
            }
        }
        System.out.println("Max Trigram " + maxTrigram + " " + maxValueTrigram);

        file.close();
    }

    public static int CountWords(String s)
    {
        //Find each word uttered excluding SH: and PM:
        //Pattern pattern = new Pattern();
        List<String> allMatches = new ArrayList<String>();
        Pattern pattern = Pattern.compile("\\b(?!PM:|SH:)\\b[\\S]+");
        Matcher matcher = pattern.matcher(s);
        while (matcher.find()) {
            allMatches.add(matcher.group());
            //System.out.println(matcher.group());
        }
        return allMatches.size() - 1;
    }

    public static int CountWordsAgain(String s, HashMap<String, Integer> tokenCount)
    {
        //Find each word uttered excluding SH: and PM:
        //Pattern pattern = new Pattern();
        List<String> allMatches = new ArrayList<String>();
        Pattern pattern = Pattern.compile("\\b(?!PM:|SH:)\\b[\\S]+");
        Matcher matcher = pattern.matcher(s);
        while (matcher.find()) {
            allMatches.add(matcher.group());
            //System.out.println(matcher.group());
            if(tokenCount.containsKey(matcher.group())) {
                tokenCount.put(matcher.group(), tokenCount.get(matcher.group()) + 1);
            } else {
                tokenCount.put(matcher.group(), 1);
            }
        }
        return allMatches.size() - 1;
    }

    public static boolean ContainsWord(String s, String wordToCompare)
    {
        String wordToCompareLower = wordToCompare.toLowerCase();
        if (s.contains(wordToCompare) || s.contains(wordToCompareLower))
            return true;

        return false;
    }

    public static int WordLength(String s)
    {
        int totalCharacters = 0;

        //Find each word uttered excluding SH: and PM:
        //MatchCollection collection = Regex.Matches(s, @"\b(?!PM:|SH:)\b[\S]+");

        /*foreach (var w in collection)
        {
            if(Convert.ToString(w) != ":" && Convert.ToString(w) != "(OVERLAP)" && Convert.ToString(w) != "(overlap)")
                totalCharacters = totalCharacters + w.ToString().Length;
        }*/

        return totalCharacters;
    }
}
