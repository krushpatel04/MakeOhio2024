import java.util.Iterator;

import components.map.Map;
import components.map.Map.Pair;
import components.map.Map1L;
import components.queue.Queue;
import components.queue.Queue1L;
import components.set.Set;
import components.set.Set1L;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;

/**
 * This program counts word occurrences in a given input file and outputs an
 * HTML document with a table of the words and how many times that word is in
 * the passage listed in alphabetical order.
 *
 * @author K. Bradley
 *
 */
public final class WordCounter {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private WordCounter() {
    }

    /**
     * Global String containing list of separators.
     */
    private static final String SEPARATORS = "., ()-_?/!@#$%^&*\t1234567890:"
            + ";[]{}+=~`><";

    /**
     * Outputs the opening tags in the generated HTML file.
     *
     * @param out
     *            the output stream
     * @param inFile
     *            the name of the file to read from
     * @updates {@code out.content}
     * @requires {@code out.is_open and [inFile is not null]}
     * @ensures {@code out.content = #out.content * [the HTML opening tags]}
     */
    private static void outputHTMLHeader(SimpleWriter out, String inFile) {
        assert out != null : "Violation of: out is not null";
        assert out.isOpen() : "Violation of: out.is_open";
        assert inFile != null : "Violation of: inFile is not null";

        out.println("<html>");
        out.println("<head>");
        out.println("<title>Words Counted in " + inFile + "</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h2>Words Counted in " + inFile + "</h2>");
        out.println("<hr />");
        out.println("<table border=\"1\">");
        out.println("<tr>");
        out.println("<th>Words</th>");
        out.println("<th>Counts</th>");
        out.println("</tr>");
    }

    /**
     * Takes all of the keys in the map, puts them in alphabetical order, and
     * places them into a new queue.
     *
     * @param mapOfWords
     *            a map of terms as keys and their definitions as values
     * @return a queue with alphabetically sorted keys from mapOfWords
     *
     * @requires {@code [mapOfWords is not null] and mapOfWords /= empty_string}
     * @ensures {@code q = [#q ordered by the relation computed by String.CASE_INSENSITIVE_ORDER]}
     */
    private static Queue<String> sortKeys(Map<String, Integer> mapOfWords) {
        assert mapOfWords != null : "Violation of: mapOfWords is not null";
        assert mapOfWords.size() > 0 : "Violation of: mapOfWords is not empty";

        Iterator<Pair<String, Integer>> iterator = mapOfWords.iterator();

        Queue<String> list = new Queue1L<>();

        while (iterator.hasNext()) {
            Pair<String, Integer> temp = iterator.next();
            list.enqueue(temp.key());
            list.sort(String.CASE_INSENSITIVE_ORDER);
        }
        return list;
    }

    /**
     * Puts the set of characters in the {@code String} into the {@code Set}.
     *
     * @param str
     *            the given {@code String}
     * @param strSet
     *            the {@code Set} to be replaced
     * @replaces {@code strSet}
     * @ensures {@code strSet = entries(str)}
     */
    private static void generateElements(String str, Set<Character> strSet) {
        assert str != null : "Violation of: str is not null";
        assert strSet != null : "Violation of: strSet is not null";

        for (int i = 0; i < str.length(); i++) {
            if (!strSet.contains(str.charAt(i))) {
                strSet.add(str.charAt(i));
            }
        }
    }

    /**
     * Returns the first word or seperator string in the text and puts it in the
     * given position.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @param separators
     *            the {@code Set} of separator characters
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires {@code 0 <= position < |text|}
     * @ensures {@code nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and if
     *          entries(text[position, position + 1)) intersection separators =
     *          {} then entries(nextWordOrSeparator) intersection separators =
     *          {} and (position + |nextWordOrSeparator| = |text| or
     *          entries(text[position, position + |nextWordOrSeparator| + 1))
     *          intersection separators /= {}) else entries(nextWordOrSeparator)
     *          is subset of separators and (position + |nextWordOrSeparator| =
     *          |text| or entries(text[position, position +
     *          |nextWordOrSeparator| + 1)) is not subset of separators)}
     */
    private static String nextWordOrSeparator(String text, int position,
            Set<Character> separators) {
        assert text != null : "Violation of: text is not null";
        assert separators != null : "Violation of: separators is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        char first = text.charAt(position);
        String nextWordOrSeparator = "";
        int i = position + 1;
        nextWordOrSeparator = nextWordOrSeparator + first;
        if (!separators.contains(first)) {
            while (i < text.length() && !separators.contains(first)) {
                first = text.charAt(i);
                if (!separators.contains(first)) {
                    String concat = "" + first;
                    nextWordOrSeparator = nextWordOrSeparator.concat(concat);

                }
                i++;
            }
        } else {
            while (i < text.length() && separators.contains(first)) {
                first = text.charAt(i);
                if (separators.contains(first)) {
                    String concat = "" + first;
                    nextWordOrSeparator = nextWordOrSeparator.concat(concat);
                }
                i++;
            }
        }
        return nextWordOrSeparator;
    }

    /**
     * Reads the file and finds how many times each word is used and puts it in
     * a table.
     *
     * @param out
     *            the output stream
     * @param lines
     *            a queue containing each line from the input text
     *
     */
    private static void processTable(SimpleWriter out, Queue<String> lines) {

        Set<Character> separators = new Set1L<>();
        generateElements(SEPARATORS, separators);

        Queue<String> words = new Queue1L<>();
        while (lines.length() > 0) {
            String newLine = lines.dequeue();
            int position = 0;
            while (position < newLine.length()) {
                String temp = nextWordOrSeparator(newLine, position,
                        separators);
                position = position + temp.length();
                if (temp.length() > 0 && !separators.contains(temp.charAt(0))) {
                    words.enqueue(temp);
                }
            }
        }
        Map<String, Integer> wordsAndCounts = new Map1L<>();
        while (words.length() > 0) {
            String key = words.dequeue();
            if (!wordsAndCounts.hasKey(key)) {
                wordsAndCounts.add(key, 1);
            } else {
                Map.Pair<String, Integer> temp = wordsAndCounts.remove(key);
                int value = temp.value();
                value++;
                wordsAndCounts.add(key, value);
            }
        }
        /*
         * Create a queue of sorted keys from the map.
         */
        Queue<String> orderedWords = sortKeys(wordsAndCounts);
        /*
         * Turn the queue of sorted words into a table.
         */
        while (orderedWords.length() > 0) {
            String word = orderedWords.dequeue();
            out.println("<tr>");
            out.println("<td>" + word + "</td>");
            out.println("<td>" + wordsAndCounts.value(word) + "</td>");
            out.println("</tr>");
        }
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();

        out.print("Enter the name of the input text file: ");
        String inFile = in.nextLine();
        out.print("Enter the name of the output file: ");
        String outFile = in.nextLine();
        /*
         * Create the HTML file
         */
        SimpleWriter output = new SimpleWriter1L(outFile);

        outputHTMLHeader(output, inFile);

        SimpleReader input = new SimpleReader1L(inFile);

        Queue<String> lines = new Queue1L<>();
        while (!input.atEOS()) {
            String temp = input.nextLine();
            lines.enqueue(temp);
        }

        processTable(output, lines);

        input.close();
        output.close();

        in.close();
        out.close();
    }

}