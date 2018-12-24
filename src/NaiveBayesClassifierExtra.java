import java.util.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;


public class NaiveBayesClassifierExtra {

    public static void main(String[] args) throws IOException {

        // build language probability models and format extra test file to process
        FileInputStream testExtra = new FileInputStream(args[0]);
        Dictionary dictTestExtra = new Dictionary();
        TreeMap<Object, String[]> testDictExtra = dictTestExtra.makeDictionary(testExtra);
        TreeMap<Object, String> testSentencesExtra = dictTestExtra.getSentences();
        TreeMap<String, HashMap<String , Double>> langProbModels = makeLangProbModels(args[1]);

        // Run comparison of extra test sentences against language models
        testLanguage(langProbModels, testDictExtra, testSentencesExtra);

    }

    public static void testLanguage(TreeMap<String, HashMap<String , Double>> langProbModels,
                                    TreeMap<Object, String[]> sentences,
                                    TreeMap<Object, String> fullSentences) {

        Object id;
        String[] words;
        String currentLangName;
        double logProbSum = 0.0;
        double logProbSumMax = 0.0;
        double secondLogProbSumMax = 0.0;
        int countKnown = 0;
        int countUnk = 0;
        int countKnownMax = 0;
        int countUnkMax = 0;
        int wordCount = 0;
        int wordCountMax = 0;
        String langMax = "";
        HashMap<String, Double> currentLangModel;

        // Loop through each example sentence in test set and also through each lang. model
        // calculating the logprob for each word given the language model
        for (HashMap.Entry<Object,String[]> sentence: sentences.entrySet()) {
            id = sentence.getKey();
            System.out.println(id + "\t" + fullSentences.get(id));
            words = sentence.getValue();
            for (Map.Entry<String, HashMap<String , Double>> currentLangEntry : langProbModels.entrySet()) {
                currentLangName = currentLangEntry.getKey();
                currentLangModel = currentLangEntry.getValue();
                for (String word : words) {
                    wordCount++;
                    if (currentLangModel.containsKey(word)) {
                        logProbSum += currentLangModel.get(word);
                        countKnown++;
                    } else {
                        logProbSum += currentLangModel.get("unk");
                        countUnk++;
                    }
                }
                System.out.println(currentLangName + "\t" + logProbSum);
                // Determine if the language is the best fit so far, i.e. has the lowest logprob value seen so far
                if (logProbSumMax != 0) {
                    if (logProbSum > logProbSumMax) {
                        secondLogProbSumMax = logProbSumMax;
                        logProbSumMax = logProbSum;
                        langMax = currentLangName;
                        countKnownMax = countKnown;
                        countUnkMax = countUnk;
                        wordCountMax = wordCount;
                    } else {
                        // do nothing
                    }
                } else {
                    logProbSumMax = logProbSum;
                    langMax = currentLangName;
                    countKnownMax = countKnown;
                    countUnkMax = countUnk;
                    wordCountMax = wordCount;
                }
                logProbSum = 0;
                countKnown = 0;
                countUnk = 0;
                wordCount = 0;
            }

            // This is the section that makes the decision about the language classification being valid
            // or if the language should instead be marked unknown
            double difference = (logProbSumMax-secondLogProbSumMax) / wordCountMax;
            double thresholdCheck = -(difference / (logProbSumMax/wordCountMax));
            if (thresholdCheck > 0.1 || (thresholdCheck > 0.01 && countKnownMax > countUnkMax) ) {
                // do nothing -- match passes as "valid"
            } else {
                // difference between top 2 matches too narrow, no clear match emerges
                langMax = "unk";
            }

            System.out.println("result\t" + langMax);
            System.out.println("");
            logProbSumMax = 0;
        }
    }

    public static TreeMap<String, HashMap<String , Double>> makeLangProbModels(String lm_file) throws IOException {
        File models = new File(lm_file);
        File[] languageModels = models.listFiles();
        Arrays.sort(languageModels);

        TreeMap<String, HashMap<String , Double>> langProbModels  = new TreeMap<>();

        for (File model : languageModels) {
            FileInputStream currentLang = new FileInputStream(model);
            String currentLangName = model.getName().substring(0,3);
            Model lang = new Model();
            HashMap<String, Double> currentLangModel = lang.makeModel(currentLang);
            langProbModels.put(currentLangName, currentLangModel);
        }

        return langProbModels;
    }

    private static class Dictionary {
        TreeMap<Object, String[]> dictionary;
        TreeMap<Object, String> fullSentences;

        public Dictionary() {
            this.dictionary = new TreeMap<>();
            this.fullSentences = new TreeMap<>();
        }

        public TreeMap<Object, String[]> makeDictionary(FileInputStream file) throws IOException {
            BufferedReader br = new BufferedReader(new InputStreamReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String[] entries = line.split("\t");
                String cleanPunct = entries[1].replaceAll("[¹²³«»!¡¥$£¿;:\"\'\\.\\[\\]\\)\\(,+%\\-0-9\\s+]", " ");
                String[] words = cleanPunct.split("\\s+");
                try {
                    this.dictionary.put(Integer.parseInt(entries[0]), words);
                    this.fullSentences.put(Integer.parseInt(entries[0]), entries[1]);
                } catch (NumberFormatException ex){
                    this.dictionary.put(entries[0], words);
                    this.fullSentences.put(entries[0], entries[1]);
                }
            }
            br.close();
            return this.dictionary;
        }

        public TreeMap<Object, String> getSentences() {
            return this.fullSentences;
        }
    }

    private static class Model {
        double modelCount;
        HashMap<String, Double> model;
        double count;
        double lowestCount;
        double increment;

        public Model() {
            this.modelCount = 0.0;
            this.model = new HashMap<>();
            this.lowestCount = -1.0; // set lowest count to negative number to initialize
            this.increment = 0;
        }

        public HashMap<String, Double> makeModel(FileInputStream file) throws IOException {
            HashMap<String, Double> probModel = new HashMap<>();
            Double getLogProb;
            this.getWordList(file);
            for (HashMap.Entry<String,Double> entry: this.model.entrySet()) {
                String word = entry.getKey();
                Double wordCount = entry.getValue();
                getLogProb = Math.log10(wordCount/(this.modelCount)); // calculate the log-prob for each word
                probModel.put(word, getLogProb);
            }
            return probModel;
        }

        private void getWordList(FileInputStream file) throws IOException {
            BufferedReader br = new BufferedReader(new InputStreamReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String[] entries = line.split("\t");
                String cleanPunct = entries[0].replaceAll("[¹²³«»!¡¥$£¿;:\"\'\\.\\[\\]\\)\\(,+%\\-0-9\\s+]", " ");
                String[] words = cleanPunct.split("\\s+");
                for (String word : words) {
                    this.count = Double.parseDouble(entries[1]);
                    if (!word.equals("")) {
                        // if word is already a key in hashmap, then increment its value, otherwise insert it as new key-value pair
                        if (this.model.containsKey(word)) {
                            this.model.replace(word, this.model.get(word) + this.count);
                        } else {
                            this.model.put(word, this.count);
                        }
                        this.modelCount += this.count;
                        // keep track of the word with the lowest value/count and use it to assign the value to the "unk" word
                        if (this.lowestCount < 0) {
                            this.lowestCount = this.count;
                        } else if (this.count < this.lowestCount) {
                            this.lowestCount = this.count;
                        } else {
                            // do nothing
                        }
                    }
                }
            }
            this.model.put("unk", this.lowestCount); // add placeholder for "unknown" word in model that has value of lowest-value word(s)
            this.modelCount += this.lowestCount; // add value of "unk" to total count for the language model
            br.close();
        }
    }
}