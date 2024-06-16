package SimpleMathParser;


import java.util.LinkedList;

/*
    SimpleScanner

    Class is used to transform a string into an array of tokens that might be an equation.
    Only accepts input that are valid tokens.
*/
public class SimpleScanner
{
    public static final String[] NUM_STRINGS = new String[]
    {
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"
    };

    // Translates a string into an array list of tokens
    public static LinkedList<SimpleToken> stringToTokens(String input)
    {
        // Remove whitespace-like chars
        input = input.replaceAll("\\s+","");

        // Output linked list
        LinkedList<SimpleToken> output = new LinkedList<>();

        // Index of character being converted to token
        int leftPtr = 0;

        String currentChar;
        SimpleToken temp;

        // Used for negative exponents
        boolean isExp = false;

        boolean isNegative = false;

        // Conversion loop
        while(leftPtr < input.length())
        {
            currentChar = input.substring(leftPtr, leftPtr + 1);

            // This section is meant to determine if a negative number is being pushed or a subtraction operator
            if(currentChar.equals(SimpleToken.TOKENS[SimpleToken.SUB_OPERATOR]) && output.peekLast() != null && (output.peekLast().isRightGrouping() || output.peekLast().getDataType() == SimpleToken.NUMBER_TOKEN))
            {
                // Subtraction operator
                temp = SimpleToken.stringToToken(currentChar);
                output.add(temp);
                leftPtr++;
                isNegative = false;
                continue;
            }
            else if(currentChar.equals(SimpleToken.TOKENS[SimpleToken.SUB_OPERATOR]))
            {
                // Negative number operations
                isNegative = !isNegative;
                leftPtr++;
                continue;
            }

            temp = SimpleToken.stringToToken(currentChar);

            // Check if a token was made
            if(temp != null)
            {
                if(isNegative && SimpleToken.checkToPushNegative(currentChar))
                {
                    output.add(new NumericToken(-1));
                    output.add(new SimpleToken(SimpleToken.MULT_OPERATOR));
                }

                isNegative = false;
                output.add(temp);
                leftPtr++;
            }
            else if(validNumberChar(currentChar) || currentChar.equals("."))
            {
                // No token was made, so check if it is a number

                boolean decimal = false;
                String numStr = "";

                // Check if the current char is a decimal and change numStr
                if(currentChar.equals("."))
                {
                    decimal = true;
                    numStr = "0.";
                    leftPtr++;

                    if(leftPtr < input.length())
                    {
                        currentChar = input.substring(leftPtr, leftPtr + 1);

                        if(!validNumberChar(currentChar))
                        {
                            // Error: token is just a decimal point
                            System.out.println("\nScanning Error: Number at index " + leftPtr + " is only a decimal point.");
                            errorArea(leftPtr, input);
                            return null;
                        }
                    }
                    else
                    {
                        // Error: last token is just a decimal point
                        System.out.println("\nScanning Error: Last number at index " + leftPtr + " is only a decimal point.");
                        errorArea(leftPtr, input);
                        return null;
                    }
                }

                // Loop through string until the number is over
                while(leftPtr < input.length() && (validNumberChar(currentChar) || currentChar.equals(".")))
                {
                    // Check for a number
                    if(validNumberChar(currentChar))
                    {
                        numStr = numStr + currentChar;
                    }
                    // Check if there is already a decimal point
                    else if(currentChar.equals(".") && decimal)
                    {
                        // Error: too many decimal points
                        System.out.println("\nScanning Error: Number at index " + leftPtr + " has multiple decimal points.");
                        errorArea(leftPtr + 1, input);
                        return null;
                    }
                    // Add decimal point
                    else
                    {
                        numStr = numStr + currentChar;
                        decimal = true;
                    }

                    leftPtr++;
                    if(leftPtr < input.length())
                        currentChar = input.substring(leftPtr, leftPtr + 1);
                }

                if(numStr.charAt(numStr.length()-1) == '.')
                    numStr += "0";

                // Loop ends when number is over

                // Check if the number should be negative or not
                if(isNegative && SimpleToken.checkToPushNegative(currentChar))
                {
                    // Should push '-1' and '*' as something with higher priority should affect numStr first
                    output.add(new NumericToken(-1));
                    output.add(new SimpleToken(SimpleToken.MULT_OPERATOR));

                    // Push numStr
                    output.add(new NumericToken(Double.parseDouble(numStr)));
                }
                else if(isNegative)
                {
                    // Should multiply numStr by -1
                    output.add(new NumericToken(Double.parseDouble(numStr) * -1));
                }
                else
                {
                    // Just add the number
                    output.add(new NumericToken(Double.parseDouble(numStr)));
                }

                // Reset negative
                isNegative = false;
            }
            else
            {
                // Error: character does not exist in grammar
                System.out.println("\nScanning Error: Character at index " + leftPtr + " does not exist in grammar.");
                errorArea(leftPtr + 1, input);
                return null;
            }
        }

        return output;
    }

    private static boolean validNumberChar(String input)
    {
        for(int i = 0; i < SimpleScanner.NUM_STRINGS.length; i++)
        {
            if(input.equals(SimpleScanner.NUM_STRINGS[i]))
                return true;
        }
        return false;
    }

    private static void errorArea(int index, String str)
    {
        String problemChar;

        if(index > 1)
            problemChar = str.substring(index - 1, index);
        else
            problemChar = String.valueOf(str.charAt(0));

        System.out.println("\nExpression:   " + str.substring(0, index - 1) + " " + problemChar + " " + str.substring(index));
        System.out.println("Error Area:   " + str.substring(0, index - 1) + "{" + problemChar + "}\n");
    }
}
