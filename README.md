# Naive_Bayes_Language_Classifier
Naive Bayes language classifier in Java

The accompanying Java program is a Naïve Bayes Classifier for 15 languages.

1. Building the models

The first step in creating the language models was to remove all punctuation, including the single apostrophe, and numerals (which were not deemed useful for classification) and put each remaining word and its count into a dictionary data structure for quick lookup. When the process of removing punctuation created two or more new words, I chose to split the words with each of the “new” words retaining the count of the original. In some cases, a new word required a new entry in the dictionary, in other cases a previously encountered word was added to its already existing dictionary entry and its count was incremented accordingly. Splitting increased the total number of words in the probability calculation.

2. Smoothing

This program implements the lowest frequency smoothing method, which assigns all unknown words the same probability as the least frequent word in the given language model. This method was chosen over the Laplace add-one approach to avoid assigning unknowns disproportionate weight in models with a smaller number of overall word counts. (However, with the lowest frequency smoothing method unknowns in some languages still carry more “weight” than in other language models, cf. Danish and Norwegian discussion below.) The model thus incorporates a generic word “unk” which is added as an additional entry in the dictionary for each model and which shares the same probability as the lowest valued word in the model. “unk” functions by lending its probability to any unknown word encountered in the test sentences. After deletions and insertions, the language models sometimes decreased or increased in size (i.e. the total number of words in each model did not stay static at 1500). 

For each word in the language models the count value is replaced with the logprob value. The logprob values were calculated by dividing the count for a word by the total count of words in the model, and then taking the log10 of that value. The total count includes the corresponding value of “unk”, so that the probability space consistently adds up to 1.

3. Classifying the test sentences

The results are reported in the file output in ascending order for the test sentences (1, 2, 3, 4, etc.) and alphabetically when reporting the language model logprob values for each test sentence (dan, deu, dut, eng, etc.).
Overall, the classifier works well for being a simplistic algorithm. In one case, the model classifies a Danish sentence as Norwegian (test sentence #26), but the classification does not make me lose faith in the model – the two languages are closely related, sharing much vocabulary, and for this mis-classified sentence their logprobs were nearly identical. I investigated this output and found the incorrect classification to be the result of two things: 1) the Norwegian model is quite small and, as a result, its unknowns have a slightly smaller logprob value than the Danish model (which is much larger), which is corrected, in part, by the smoothing method, but is not resolved entirely; and 2) the test sentence contains two words which produce much lower logprobs in Norwegian than Danish: i) krig ‘war’ – a common word which appears in the Norwegian model, but not in the Danish model, and 2) landet ‘the country’ which appears with much higher frequency (leading to a lower logprob) in the Norwegian model than the Danish model.


4. Naïve Bayes Classifier - Extra

The extra code implements an additional task with the Naïve Bayes Classifier, i.e. to determine if a language match is valid (correct) or not.
This new task is approached in two steps:
  I.	For each extra test sentence the lowest and second lowest logprobs are compared to determine if the top language match stands out significantly, or if it is just the “best” bad match among many bad matches. Instead of comparing the logprob outputs as is, the average logprob is normalized for a word in the sentence. Then the percentage difference between the lowest and second lowest normalized logprobs are calculated and it was determined that a value of 0.1 (10%) or greater resulted in a valid match.
  II.	An additional test was added to compare the ratio of known to unknown words identified for test sentences by the model. This test was added based on the knowledge that a majority of human language is comprised of the ~1,000 most common words (the exact numbers vary by language, but it’s about 80% for the most common 1,000 words in English).  A fairly conservative test for this classification task is to check if the number of known words in a sentence is greater than the number of unknowns in the language model. This additional test is especially helpful when the top and second top language matches have close logprobs not because they are bad matches, but because of language relatedness (such as the Norwegian and Danish example discussed above).
  a.	This additional known > unknown words test was applied to cases where the logprobs fell in a gray area of classification: 0.01 < logprob < 0.1.
  
The file output-extra is in the same format as for the main output file (numbers in ascending order, language names alphabetical).
A manual check shows that the results are mostly accurate, with the exception of one Esperanto sentence mis-classified as French (test sentence #20). In this case, it is because the logprob score for French exceeds the minimum threshold (0.1). The tests were not further tweaked so as not to overfit to the data.
