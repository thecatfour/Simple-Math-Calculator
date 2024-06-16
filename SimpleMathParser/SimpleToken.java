package SimpleMathParser;

public class SimpleToken
{
    // Constants to identify type of token

    public static final int NO_OP_OPERATOR = -1;
    public static final int ADD_OPERATOR = 0;
    public static final int SUB_OPERATOR = 1;
    public static final int MULT_OPERATOR = 2;
    public static final int DIV_OPERATOR = 3;
    public static final int MOD_OPERATOR = 4;
    public static final int EXP_OPERATOR = 5;

    public static final int NUMBER_TOKEN = 10;
    public static final int LPAREN_TOKEN = 11;
    public static final int RPAREN_TOKEN = 12;

    // Array to help identify tokens

    public final static String[] TOKENS = new String[]
            {
                    "+", "-", "*", "/", "%", "^", "", "", "", "",
                    "num", "(", ")"
            };

    // Fields

    private final int dataType;

    // Constructor
    public SimpleToken(int dataType)
    {
        this.dataType = dataType;
    }


    // Static Methods


    // Checks if an inputted string can make a Token and makes the token
    public static SimpleToken stringToToken(String input)
    {
        // Check each string in TOKENS
        for(int i = 0; i < TOKENS.length; i++)
        {
            if(input.equals(TOKENS[i]))
                return new SimpleToken(i);
        }

        return null;
    }

    // Checks if an inputted operator has higher priority than multiplication to make negative numbers
    // If true, '-1' and '*' should be pushed
    // If false, directly multiply -1 to the number
    public static boolean checkToPushNegative(String input)
    {
        return input.equals(TOKENS[EXP_OPERATOR]) || input.equals(TOKENS[LPAREN_TOKEN]);
    }


    // Methods


    public boolean isRightGrouping()
    {
        return this.dataType == RPAREN_TOKEN;
    }

    // Gets dataType
    public int getDataType()
    {
        return this.dataType;
    }

    public String toString()
    {
        return TOKENS[this.dataType];
    }
}
