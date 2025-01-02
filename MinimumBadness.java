import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MinimumBadness {

    public static void main(String[] args) {
        String inputFile = "large_text_source.txt";

        // 1. Read all words from file
        List<String> wordList = readWordsFromFile(inputFile);
        if (wordList.isEmpty()) {
            System.err.println("No words found in the file. Exiting...");
            return;
        }

        // 2. Compute min and max feasible widths
        String[] words = wordList.toArray(new String[0]);
        int maxLen = 0;           // length of the longest single word
        int totalChars = 0;       // sum of lengths of all words
        for (String w : words) {
            int len = w.length();
            totalChars += len;
            if (len > maxLen) {
                maxLen = len;
            }
        }

        int n = words.length;
        // The minimal possible width is at least the length of the longest word
        int minWidth = maxLen;
        // The maximal possible width is everything on one line:
        // sum of word lengths + (n-1) spaces if we put them all in a single line
        int maxWidth = totalChars + (n - 1);

        if (minWidth > maxWidth) {
            // Normally won't happen unless the file is empty or some anomaly
            System.err.println("Could not compute a valid width range.");
            return;
        }

        // 3. Binary-search the minimal feasible width
        int bestWidth = binarySearchWidth(words, minWidth, maxWidth);

        System.out.println("Minimal feasible width found: " + bestWidth);

        // 4. Compute the line breaks with the found width
        int[] lineBreaks = minimumBadness(words, bestWidth);

        // 5. Write the text in “unjustified” form
        writeUnjustifiedText(words, lineBreaks, "unjust.txt");

        // 6. Write the text in “justified” form
        justifyText(words, lineBreaks, bestWidth, "justified.txt");

        System.out.println("Done! Check 'unjust.txt' and 'justified.txt' for output.");
    }

    /**
     * Reads all non-empty tokens (trimmed) from the given file.
     */
    public static List<String> readWordsFromFile(String filename) {
        List<String> words = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split("\\s+");
                for (String token : tokens) {
                    token = token.trim();
                    if (!token.isEmpty()) {
                        words.add(token);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return words;
    }

    /**
     * Checks if the text can be justified at the given width without throwing an exception.
     * We do this by trying the DP-based line-break routine and seeing if it completes.
     */
    public static boolean canJustify(String[] W, int width) {
        try {
            // If this call doesn't throw, it means we *can* justify
            int[] lineBreaks = minimumBadness(W, width);
            return (lineBreaks != null && lineBreaks.length > 0);
        } catch (Exception e) {
            // If we get the "Unable to find a valid break" exception,
            // or any other, then we cannot justify at this width
            return false;
        }
    }

    /**
     * Does a binary search for the minimal feasible width in [low..high].
     */
    public static int binarySearchWidth(String[] W, int low, int high) {
        int answer = high;
        while (low <= high) {
            int mid = (low + high) / 2;
            if (canJustify(W, mid)) {
                // If mid is feasible, record it, then try smaller widths
                answer = mid;
                high = mid - 1;
            } else {
                // If mid isn't feasible, go larger
                low = mid + 1;
            }
        }
        return answer;
    }

    /**
     * Returns the “badness” of placing words from W[i] through W[j-1] on one line of width w.
     * If the words don’t fit, returns Integer.MAX_VALUE.
     */
    public static int badness(String[] W, int i, int j, int w) {
        int totalWidth = 0;
        for (int k = i; k < j; k++) {
            // Add word length + 1 space (except before the first word)
            totalWidth += W[k].length() + (k > i ? 1 : 0);
        }
        int extraSpaces = w - totalWidth;
        // If it doesn't fit, return infinite “badness”
        if (extraSpaces < 0) {
            return Integer.MAX_VALUE;
        }
        
        return extraSpaces * extraSpaces * extraSpaces;
    }

    /**
     * Wrapper function that sets up the memoization arrays and returns the line break positions.
     */
    public static int[] minimumBadness(String[] W, int w) {
        int n = W.length;
        int[] memo = new int[n + 1];          // memo[i] = minimal badness from word i to the end
        int[] lineBreaksMemo = new int[n + 1]; // lineBreaksMemo[i] = best break index after i

        // Initialize memo with -1 to indicate “uncomputed”
        for (int i = 0; i <= n; i++) {
            memo[i] = -1;
        }

        memoizedMinimumBadness(W, 0, memo, lineBreaksMemo, w);
        return lineBreaksMemo;
    }

    /**
     * Returns the minimal badness starting at index i, while also recording
     * the best next-break index in lineBreaksMemo[i].
     */
    public static int memoizedMinimumBadness(String[] W, int i, int[] memo, int[] lineBreaksMemo, int w) {
        int n = W.length;
        // Base case: if we’re at or beyond the last word, no more badness
        if (i >= n) {
            return 0;
        }
        // If already computed, just return it
        if (memo[i] != -1) {
            return memo[i];
        }

        int minBadness = Integer.MAX_VALUE;
        int bestBreak = -1;

        // Try breaking between i..(j-1) for j in [i+1, n]
        for (int j = i + 1; j <= n; j++) {
            int currentBadness;
            if (j == n) {
                // Last line => 0 penalty if it fits
                if (badness(W, i, j, w) == Integer.MAX_VALUE) {
                    break;
                }
                currentBadness = 0;
            } else {
                // Normal line
                currentBadness = badness(W, i, j, w);
            }

            // If it doesn't fit, stop checking longer lines
            if (currentBadness == Integer.MAX_VALUE) {
                break;
            }

            // Recurse
            int nextBadness = memoizedMinimumBadness(W, j, memo, lineBreaksMemo, w);
            int totalBadness = currentBadness + nextBadness;

            if (totalBadness < minBadness) {
                minBadness = totalBadness;
                bestBreak = j;
            }
        }

        // Fallback: if no valid break found, force a break at the next word
        if (bestBreak == -1) {
            bestBreak = i + 1;
            minBadness = badness(W, i, bestBreak, w);
            if (minBadness == Integer.MAX_VALUE) {
                throw new IllegalStateException("Unable to find a valid break for index " + i
                        + ". Possibly there's a very long token or input anomaly.");
            }
        }

        memo[i] = minBadness;
        lineBreaksMemo[i] = bestBreak;
        return minBadness;
    }

    /**
     * Writes fully-justified lines to the given file using the computed line breaks.
     */
    public static void justifyText(String[] W, int[] lineBreaks, int w, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (int i = 0; i < W.length;) {
                int end = lineBreaks[i];
                if (end > W.length || end <= i) {
                    break;
                }

                // First, build the line with a single space between words
                StringBuilder line = new StringBuilder(W[i]);
                for (int j = i + 1; j < end; j++) {
                    line.append(" ").append(W[j]);
                }

                // Compute how many extra spaces are needed
                int extraSpaces = w - line.length();
                int numGaps = end - i - 1; // the number of spaces between words

                // If there's only one word (or no gap), just write the line as-is
                if (numGaps <= 0) {
                    writer.write(line.toString());
                    writer.newLine();
                    i = end;
                    continue;
                }

                // Distribute extra spaces among the gaps
                int[] spaceGaps = new int[numGaps];
                int gapIndex = 0;
                while (extraSpaces > 0) {
                    spaceGaps[gapIndex]++;
                    extraSpaces--;
                    gapIndex = (gapIndex + 1) % numGaps;
                }

                // Build the justified line
                StringBuilder justifiedLine = new StringBuilder();
                for (int j = i, gap = 0; j < end; j++, gap++) {
                    justifiedLine.append(W[j]);
                    if (j < end - 1) {
                        // normal 1 space + the extra
                        justifiedLine.append(" ".repeat(1 + spaceGaps[gap]));
                    }
                }

                writer.write(justifiedLine.toString());
                writer.newLine();
                i = end;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes the text with single spacing (unjustified) using the computed line breaks.
     */
    public static void writeUnjustifiedText(String[] W, int[] lineBreaks, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (int i = 0; i < W.length;) {
                int end = lineBreaks[i];
                if (end > W.length || end <= i) {
                    break;
                }
                for (int j = i; j < end; j++) {
                    writer.write(W[j]);
                    if (j < end - 1) {
                        writer.write(" ");
                    }
                }
                writer.newLine();
                i = end;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
