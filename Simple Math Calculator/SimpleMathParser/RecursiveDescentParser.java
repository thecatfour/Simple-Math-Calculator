package SimpleMathParser;

import java.util.LinkedList;
import java.util.Stack;

public class RecursiveDescentParser
{
    // Errors named [non-terminal]_ERROR are more for debugging
    // the grammar, but they still can catch invalid token uses

    // EP_ERROR is the exception as it produces num terminals

    public static final int NO_ERROR = 0;
    public static final int NULL_ERROR = 1;
    public static final int EP_ERROR = 2;
    public static final int A_ERROR = 3;
    public static final int LPAREN_MISSING_ERROR = 4;
    public static final int RPAREN_MISSING_ERROR = 5;
    public static final int M_ERROR = 6;
    public static final int ZERO_DIV_ERROR = 7;
    public static final int E_ERROR = 8;

    public static final int SCANNER_ERROR = 10;
    
    /*

    Grammar (Rightmost parsing):

    S  ->  S' | ε
    S' ->  AA'
    A  ->  AA'+ | AA'- | ε
    A' ->  MM'
    M  ->  MM'* | MM'/ | MM'% | ε
    M' ->  EE'
    E  ->  EE'^ | ε
    E' ->  (S') | num

    */

    // Fields

    // Used to store information related to parsing
    private LinkedList<SimpleToken> tokens;
    private Stack<Integer> operators;
    private SimpleToken lookahead;

    // Used for error related stuff
    private String parsed;
    private int currentError;
    private SimpleToken currentErrorToken;

    // Used to show each step in the expression for console mode
    private boolean showSteps;

    // Used to show each step in the expression
    private String process;

    public RecursiveDescentParser()
    {
        tokens = null;
        operators = null;
        lookahead = null;

        parsed = "";
        currentError = 0;
        currentErrorToken = null;

        showSteps = false;

        process = "";
    }

    // This functions as parseS() for a string
    public double parseExpressionString(String input)
    {
        // Get tokens linked list
        tokens = SimpleScanner.stringToTokens(input);

        if(tokens == null)
        {
            currentError = SCANNER_ERROR;
            return 0;
        }

        // Set up other datastructures
        operators = new Stack<>();
        parsed = "";
        process = "";

        // Parse S'

        try
        {
            // Will go through if there are no errors
            currentError = NO_ERROR;

            // Stores output
            double output = parseSP();

            // If a Lparen is still on the tokens list, this is an unclosed parenthesis
            if(lookahead != null && lookahead.getDataType() == SimpleToken.LPAREN_TOKEN)
                error(RPAREN_MISSING_ERROR,lookahead);
            else
                return output;

            return 0;
        }
        catch (ArithmeticException e)
        {
            // Error will be documented with fields
            return 0;
        }
    }

    private double parseSP()
    {
        //
        // Parse A'
        //

        double aP = parseAP();

        //
        // Parse A
        //

        double a = parseA();

        // Do any calculations

        return calculate(a, aP);
    }

    private double parseA()
    {
        // Check lookahead
        lookahead = tokens.peekLast();

        if(lookahead == null || followA())
        {
            // Epsilon transition
            operators.push(SimpleToken.NO_OP_OPERATOR);
            return 0;
        }

        // Use lookahead to push opcode or give error
        if(lookahead.getDataType() == SimpleToken.ADD_OPERATOR || lookahead.getDataType() == SimpleToken.SUB_OPERATOR)
            operators.push(lookahead.getDataType());
        else
            error(A_ERROR,lookahead);

        // Remove operator from linked list
        parsed = tokens.removeLast().toString() + " " + parsed;

        //
        // Parse A'
        //

        double aP = parseAP();

        //
        // Parse A
        //

        double a = parseA();

        // Do any calculations
        return calculate(a, aP);
    }

    private double parseAP()
    {
        //
        // Parse M'
        //

        double mP = parseMP();

        //
        // Parse M
        //

        double m = parseM();

        // Do any calculations
        return calculate(m, mP);
    }

    private double parseM()
    {
        // Check lookahead
        lookahead = tokens.peekLast();

        if(lookahead == null || followM())
        {
            // Epsilon transition
            operators.push(SimpleToken.NO_OP_OPERATOR);
            return 0;
        }

        // Use lookahead to get opcode or give error
        if(lookahead.getDataType() == SimpleToken.MULT_OPERATOR || lookahead.getDataType() == SimpleToken.DIV_OPERATOR || lookahead.getDataType() == SimpleToken.MOD_OPERATOR)
            operators.push(lookahead.getDataType());
        else
            error(M_ERROR, lookahead);

        parsed = tokens.removeLast().toString() + " " + parsed;

        //
        // Parse M'
        //

        double mP = parseMP();

        //
        // Parse M
        //

        double m = parseM();

        // Do any calculations
        return calculate(m, mP);
    }

    private double parseMP()
    {
        //
        // Parse E'
        //

        double eP = parseEP();

        //
        // Parse E
        //

        double e = parseE();

        // Do any calculations
        return calculate(e, eP);
    }

    private double parseE()
    {
        // Check lookahead
        lookahead = tokens.peekLast();

        if(lookahead == null || followE())
        {
            // Epsilon transition
            operators.push(SimpleToken.NO_OP_OPERATOR);
            return 0;
        }

        // Use lookahead to get opcode or give error
        if(lookahead.getDataType() == SimpleToken.EXP_OPERATOR)
            operators.push(lookahead.getDataType());
        else
            error(E_ERROR,lookahead);

        // Remove operator from linked list
        parsed = tokens.removeLast().toString() + " " + parsed;

        //
        // Parse E'
        //

        double eP = parseEP();

        //
        // Parse E
        //

        double e = parseE();

        // Do any calculations
        return calculate(e, eP);
    }

    private double parseEP()
    {
        double output = 0;

        // Check lookahead
        lookahead = tokens.peekLast();

        // Check for null
        if(lookahead == null)
        {
            // No token found when there should be something
            error(NULL_ERROR, lookahead);
        }

        // Check all First(E')
        if(lookahead.getDataType() == SimpleToken.RPAREN_TOKEN)
        {
            // Remove ')' from linked list
            parsed = tokens.removeLast().toString() + " " + parsed;

            //
            // Parse S'
            //

            output = parseSP();

            // Try to remove '(' from linked list

            lookahead = tokens.peekLast();

            if(lookahead == null || lookahead.getDataType() != SimpleToken.LPAREN_TOKEN)
                error(LPAREN_MISSING_ERROR,lookahead);
            else
                parsed = tokens.removeLast().toString() + " " + parsed;
        }
        else if(lookahead.getDataType() == SimpleToken.NUMBER_TOKEN)
        {
            // Extract numeric value from token
            output = ((NumericToken)lookahead).getData();

            // Remove 'num' token from linked list
            parsed = tokens.removeLast().toString() + " " + parsed;
        }
        else
        {
            error(EP_ERROR,lookahead);
        }

        return output;
    }

    private double calculate(double left, double right)
    {
        int opcode;

        // Get current opcode
        if(operators.peek() != null)
            opcode = operators.pop();
        else
            throw new RuntimeException("Some method forgot to push an operator.");

        // Check for zero division
        if(right == 0 && (opcode == SimpleToken.DIV_OPERATOR || opcode == SimpleToken.MOD_OPERATOR))
            error(ZERO_DIV_ERROR, lookahead);


        // Do calculation
        double output =  switch (opcode)
        {
            case SimpleToken.NO_OP_OPERATOR -> right;
            case SimpleToken.ADD_OPERATOR   -> left + right;
            case SimpleToken.SUB_OPERATOR   -> left - right;
            case SimpleToken.MULT_OPERATOR  -> left * right;
            case SimpleToken.DIV_OPERATOR   -> left / right;
            case SimpleToken.MOD_OPERATOR   -> left % right;
            case SimpleToken.EXP_OPERATOR   -> Math.pow(left,right);
            default                         -> throw new RuntimeException("Unknown operator.");
        };

        // Make sure this calculation is not an epsilon transition
        if(opcode != SimpleToken.NO_OP_OPERATOR && showSteps)
            System.out.println(left + SimpleToken.TOKENS[opcode] + right + " = " + output);

        // Make sure this calculation is not an epsilon transition
        if(opcode != SimpleToken.NO_OP_OPERATOR)
            process = process + left + " " + SimpleToken.TOKENS[opcode] + " " + right + " = " + output + "\n";

        return output;
    }

    private boolean followA()
    {
        return lookahead.getDataType() == SimpleToken.LPAREN_TOKEN;
    }

    private boolean followM()
    {
        return followA() || lookahead.getDataType() == SimpleToken.ADD_OPERATOR || lookahead.getDataType() == SimpleToken.SUB_OPERATOR;
    }

    private boolean followE()
    {
        return followM() || lookahead.getDataType() == SimpleToken.MULT_OPERATOR || lookahead.getDataType() == SimpleToken.DIV_OPERATOR || lookahead.getDataType() == SimpleToken.MOD_OPERATOR;
    }

    private void error(int errorType, SimpleToken errorToken)
    {
        currentError = errorType;
        currentErrorToken = errorToken;

        String outStr = switch (errorType)
        {
            case NULL_ERROR             -> "Syntax Error: missing leftmost 'num' token.";
            case EP_ERROR               -> "Syntax Error: invalid token use " + errorToken + " where there should be a 'num' token to its right.";
            case A_ERROR                -> "Syntax Error: invalid token use " + errorToken + " when parsing A.";
            case LPAREN_MISSING_ERROR   -> "Syntax Error: missing '('.";
            case RPAREN_MISSING_ERROR   -> "Syntax Error: missing ')'.";
            case M_ERROR                -> "Syntax Error: invalid token use " + errorToken + " when parsing M.";
            case ZERO_DIV_ERROR         -> "Syntax Error: division or modulo by 0.";
            case E_ERROR                -> "Syntax Error: invalid token use " + errorToken + " when parsing E.";
            default -> "Unknown error has occurred.";
        };

        System.out.println("\n" + outStr + "\n");
        System.out.println("Consumed tokens:  " + parsed);
        System.out.println("Remaining tokens: " + tokens + "\n");
        throw new ArithmeticException();
    }

    public int getError()
    {
        return currentError;
    }

    public SimpleToken getErrorToken()
    {
        return currentErrorToken;
    }

    public void setShowSteps(boolean newValue)
    {
        showSteps = newValue;
    }

    public boolean getShowSteps()
    {
        return showSteps;
    }

    public String getProcess()
    {
        return process;
    }
}
