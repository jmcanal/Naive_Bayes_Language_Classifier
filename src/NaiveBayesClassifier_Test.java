import java.util.*;
        import java.io.*;
        import java.io.BufferedReader;
        import java.io.File;
        import java.io.IOException;


public class NaiveBayesClassifier_Test {

    public static void main(String[] args) throws IOException {

        FileInputStream train = new FileInputStream("/Users/jmcanal/Dropbox/CLMS/ling473/project5/train.txt");
        FileInputStream test = new FileInputStream("/Users/jmcanal/Dropbox/CLMS/ling473/project5/test.txt");
        Dictionary dictTrain = new Dictionary();
        Dictionary dictTest = new Dictionary();
        TreeMap<String, String[]> trainDict = dictTrain.makeDictionary(train);
        TreeMap<String, String[]> testDict = dictTest.makeDictionary(test);
        TreeMap<String, String> trainSentences = dictTrain.getSentences();
        TreeMap<String, String> testSentences = dictTest.getSentences();
        FileInputStream trainExtra = new FileInputStream("/Users/jmcanal/Dropbox/CLMS/ling473/project5/extra-train.txt");
        FileInputStream testExtra = new FileInputStream("/Users/jmcanal/Dropbox/CLMS/ling473/project5/extra-test.txt");
        Dictionary dictTrainExtra = new Dictionary();
        Dictionary dictTestExtra = new Dictionary();
        TreeMap<String, String[]> trainDictExtra = dictTrainExtra.makeDictionary(trainExtra);
        TreeMap<String, String[]> testDictExtra = dictTestExtra.makeDictionary(testExtra);
        TreeMap<String, String> trainSentencesExtra = dictTrainExtra.getSentences();
        TreeMap<String, String> testSentencesExtra = dictTestExtra.getSentences();

        TreeMap<String, HashMap<String , Double>> langProbModels = makeLangProbModels();

//        File models = new File("/Users/jmcanal/Dropbox/CLMS/ling473/project5/language-models/");
//        File[] languageModels = models.listFiles();
//        Arrays.sort(languageModels);
//
//        TreeMap<String, HashMap<String , Double>> langProbModels  = new TreeMap<>();
//
//        for (File model : languageModels) {
//            FileInputStream currentLang = new FileInputStream(model);
//            String currentLangName = model.getName().substring(0,3);
//            //languages[count++] = currentLangName;
//            //System.out.println(" --- " + currentLangName + " --- ");
//            Model lang = new Model();
//            HashMap<String, Double> currentLangModel = lang.makeModel(currentLang);
//            langProbModels.put(currentLangName, currentLangModel);
//        }

        testLanguage(langProbModels, testDict, testSentences);

    }


    public static void testLanguage(TreeMap<String, HashMap<String , Double>> langProbModels,
                                    TreeMap<String, String[]> sentences,
                                    TreeMap<String, String> fullSentences) {
        int countKnown = 0;
        int countUnk = 0;
        double logProbSum = 0;
        String id;
        String[] words;
        String currentLangName;
        double logProbSumMax = 0;
        String langMax = "";
        HashMap<String, Double> currentLangModel;

        for (HashMap.Entry<String,String[]> sentence: sentences.entrySet()) {
            id = sentence.getKey();
            System.out.println(id + "\t" + fullSentences.get(id));
            words = sentence.getValue();
            for (Map.Entry<String, HashMap<String , Double>> currentLangEntry : langProbModels.entrySet()) {
                currentLangName = currentLangEntry.getKey();
                currentLangModel = currentLangEntry.getValue();
                for (String word : words) {
                    if (currentLangModel.containsKey(word)) {
                        logProbSum += currentLangModel.get(word);
//                        if (currentLangName.equals("dan") || currentLangName.equals("nob")) {
//                            System.out.println("Known: " + word + "\t" + logProbSum);
//                        }
                        countKnown++;
                    } else {
                        logProbSum += currentLangModel.get("unk");
                        countUnk++;
//                        if (currentLangName.equals("dan") || currentLangName.equals("nob")) {
//                            System.out.println("Unknown: " + word + "\t" + logProbSum);
//                        }
                    }
                }
                //System.out.println(currentLangName + "\t" + logProbSum + " Known words: " + countKnown + " Unknown words: " + countUnk);
                System.out.println(currentLangName + "\t" + logProbSum);
                if (logProbSumMax != 0) {
                    if (logProbSum > logProbSumMax) {
                        logProbSumMax = logProbSum;
                        langMax = currentLangName;
                    } else {
                        // do nothing
                    }
                } else {
                    logProbSumMax = logProbSum;
                    langMax = currentLangName;
                }
                logProbSum = 0;
                countKnown = 0;
                countUnk = 0;
            }
            System.out.println("result\t" + langMax);
            System.out.println("");
            logProbSumMax = 0;
        }
    }

    public static TreeMap<String, HashMap<String , Double>> makeLangProbModels() throws IOException {
        File models = new File("/Users/jmcanal/Dropbox/CLMS/ling473/project5/language-models/");
        File[] languageModels = models.listFiles();
        Arrays.sort(languageModels);

        TreeMap<String, HashMap<String , Double>> langProbModels  = new TreeMap<>();

        for (File model : languageModels) {
            FileInputStream currentLang = new FileInputStream(model);
            String currentLangName = model.getName().substring(0,3);
            //languages[count++] = currentLangName;
            //System.out.println(" --- " + currentLangName + " --- ");
            Model lang = new Model();
            HashMap<String, Double> currentLangModel = lang.makeModel(currentLang);
            langProbModels.put(currentLangName, currentLangModel);
        }

        return langProbModels;
    }

    private static class Dictionary {
        TreeMap<String, String[]> dictionary;
        TreeMap<String, String> fullSentences;

        public Dictionary() {
            this.dictionary = new TreeMap<>();
            this.fullSentences = new TreeMap<>();
        }

        public TreeMap<String, String[]> makeDictionary(FileInputStream file) throws IOException {
            BufferedReader br = new BufferedReader(new InputStreamReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                // ADD MORE REGEX'S
                String[] entries = line.split("\t");
                String cleanPunct = entries[1].replaceAll("[¹²³«»!¡¥$£¿;:\"\'\\.\\[\\]\\)\\(,+%\\-0-9\\s+]", " ");
                String[] words = cleanPunct.split("\\s+");
//                for (String word : words) {
//                    System.out.println(word);
//                }
                this.dictionary.put(entries[0], words);
                this.fullSentences.put(entries[0], entries[1]);
            }
            br.close();
            return this.dictionary;
        }

        public TreeMap<String, String> getSentences() {
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
                // also get rid of numeric and other chars in model entries
                // put words into separate entries which have been split based on punctuation
                String[] entries = line.split("\t");
                String cleanPunct = entries[0].replaceAll("[¹²³«»!¡¥$£¿;:\"\'\\.\\[\\]\\)\\(,+%\\-0-9\\s+]", " ");
                String[] words = cleanPunct.split("\\s+");
                //System.out.println(cleanPunct);
                // Figure out how to split words with spaces into new entries
                // add words to pre-existing entries where applicable
                for (String word : words) {
                    this.count = Double.parseDouble(entries[1]);
                    //System.out.println(word + ": " + this.count);
                    if (!word.equals("")) {
                        //System.out.println(word + "\t" + this.count);
                        // here do an if - else statement; if entry already exist, then add it, otherwise insert it
                        if (this.model.containsKey(word)) {
                            //increment = this.model.get(word) + this.count;
                            this.model.replace(word, this.model.get(word) + this.count);
                            //System.out.println(word + ": " + this.count + " : " + this.model.get(word));
                        } else {
                            this.model.put(word, this.count);
                            //System.out.println(word + ": " + this.count);
                        }
                        this.modelCount += this.count;
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
            //System.out.println("Total words in model\t" + this.modelCount);
            br.close();
        }
    }
}
