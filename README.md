                                                            Text Justification Tool
Java
License

A Java-based tool that formats text to fit a specified width using dynamic programming and binary search. Minimizes "badness" (extra spaces) for clean, readable output. Perfect for exploring algorithmic optimization and file I/O in Java.

Features
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
Dynamic Programming: Uses memoization to compute optimal line breaks efficiently.

Binary Search: Finds the minimal feasible width for the text.

File I/O: Reads input from a file and writes formatted text to output files.

Edge Case Handling: Handles long words, large files, and other edge cases gracefully.



How It Works
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

Input: The program reads a text file (large_text_source.txt) containing the input text.

Width Calculation: Computes the minimal feasible width using binary search.

Text Justification: Formats the text into lines using dynamic programming to minimize "badness."

Output: Writes the formatted text to two files:

unjust.txt: Unjustified text with single spaces.

justified.txt: Fully justified text with evenly distributed spaces.


Algorithms Used
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
Dynamic Programming: The memoizedMinimumBadness method computes the optimal line breaks.

Binary Search: The binarySearchWidth method finds the minimal width that can accommodate the text.

Badness Calculation: The badness method calculates the penalty for extra spaces in a line.


Challenges and Solutions
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
Infinite Recursion: Fixed by adding a fallback mechanism to force line breaks when no valid break is found.

Large Files: Optimized the algorithm to handle larger inputs efficiently.

Edge Cases: Added checks for long words and empty files.


