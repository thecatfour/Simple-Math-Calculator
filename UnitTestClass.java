import SimpleMathParser.RecursiveDescentParser;

import java.io.*;
import java.nio.file.Path;
import java.util.Scanner;

import static java.lang.String.format;

// This class is purely meant to perform unit tests and outputs to a file
public class UnitTestClass
{

    public static final String GOOD_INPUT_FILENAME = "UnitTestsGood.txt";
    public static final String BAD_INPUT_FILENAME = "UnitTestsBad.txt";
    public static final String OUTPUT_FILENAME = "IncorrectResults.txt";

    // Output file will be empty if there are no discrepancies
    public static final String USE_FILENAME = GOOD_INPUT_FILENAME;

    public static final String DELIMITER = ",";

    // These are used to force outputs even if they are identical
    public static final boolean FORCE_GOOD_OUTPUT = true;
    public static final boolean FORCE_BAD_OUTPUT = true;

    @SuppressWarnings("unused")
    public static void main(String[] args)
    {
        FileInputStream inFile;

        try
        {
            inFile = new FileInputStream(USE_FILENAME);
        }
        catch (FileNotFoundException notFoundOne)
        {
            System.out.println("First look failed.");

            try
            {
                inFile = new FileInputStream(String.valueOf(Path.of(System.getProperty("user.dir"),"src", USE_FILENAME)));
            }
            catch (FileNotFoundException notFoundTwo)
            {
                System.out.println("Second look failed.");
                System.out.println("Could not find " + USE_FILENAME);
                return;
            }
        }

        // Set up scanner to read file
        Scanner scanner = new Scanner(inFile);

        // Delete old results file
        File outFile = new File(OUTPUT_FILENAME);

        // Create new results file
        try
        {
            outFile.createNewFile();
        }
        catch (IOException e)
        {
            System.out.println("Error while creating new results.");
            return;
        }

        // Create file writer for results
        FileWriter writer;
        try
        {
            writer = new FileWriter(outFile);
            writer.write("Line, Real Answer, Parser Answer\n\n");
        }
        catch (IOException e)
        {
            System.out.println("Error while trying to setup a file writer.");
            return;
        }

        //
        // Create variables for actual unit testing
        //

        // Reads one line from the test file
        String line;

        // Stores the expression part of the input
        String expression;

        // Stores the answer part of the input
        double realAnswer;

        // Stores the answer from the parser
        double answer;

        // Used to print any discrepancies of answers
        int currentRow = 1;

        // Does the calculations
        RecursiveDescentParser parser = new RecursiveDescentParser();

        while(scanner.hasNext())
        {
            line = scanner.nextLine();

            expression = line.substring(0, line.indexOf(DELIMITER));
            realAnswer = Double.parseDouble(line.substring(line.indexOf(DELIMITER) + 1));

            // Answers are rounded to 4 decimal places as shown by the method "roundDouble"
            realAnswer = roundDouble(realAnswer);

            answer = parser.parseExpressionString(expression);

            answer = roundDouble(answer);

            // Check output
            if((FORCE_GOOD_OUTPUT && USE_FILENAME.equals(GOOD_INPUT_FILENAME)) || (parser.getError() == RecursiveDescentParser.NO_ERROR && (realAnswer != answer)))
            {
                // Real answer and parsed answer differ, so log it to the text file
                try
                {
                    writer.write(currentRow + ",    " + realAnswer + " , " + answer + "\n\n");
                }
                catch (IOException e)
                {
                    System.out.println("Error writing to error file.");
                    return;
                }
            }
            else if((FORCE_BAD_OUTPUT && USE_FILENAME.equals(BAD_INPUT_FILENAME)) || (parser.getError() != RecursiveDescentParser.NO_ERROR && parser.getError() != realAnswer))
            {
                // In this case, "realAnswer" is used to document the error type

                // Error found, so log the type
                try
                {
                    writer.write(currentRow + ",    " + realAnswer + " , " + parser.getError() + "\n\n");
                }
                catch (IOException e)
                {
                    System.out.println("Error writing to error file.");
                    return;
                }
            }

            currentRow++;
        }

        try
        {
            writer.close();
        }
        catch (IOException e)
        {
            System.out.println("Error when closing file.");
        }
        scanner.close();
    }

    private static double roundDouble(double input)
    {
        String temp = format("%.4f",input);

        return Double.parseDouble(temp);
    }
}
