import SimpleMathParser.RecursiveDescentParser;

import java.util.Scanner;

public class ConsoleMain {
    public static void main(String[] args)
    {
        String userInput;
        Scanner scanner = new Scanner(System.in);
        double answer;
        RecursiveDescentParser parser = new RecursiveDescentParser();

        while(true)
        {
            printOptions();

            System.out.println("Enter option: ");

            userInput = scanner.nextLine();

            if(userInput.equals("e") || userInput.equals("v"))
            {
                printOperators();

                if(userInput.equals("v"))
                    parser.setShowSteps(true);

                while(true)
                {
                    System.out.println("Enter an expression, 'q' to quit, or 'h' to see valid operators:\n");

                    userInput = scanner.nextLine();

                    if(userInput.equals("q"))
                    {
                        break;
                    }
                    else if(userInput.equals("h"))
                    {
                        printOperators();
                    }
                    else
                    {
                        if(parser.getShowSteps())
                            System.out.println();

                        answer = parser.parseExpressionString(userInput);

                        if(parser.getError() == RecursiveDescentParser.NO_ERROR)
                            System.out.println("\nAnswer: " + answer + "\n");
                    }
                }

                parser.setShowSteps(false);
            }
            else if(userInput.equals("g"))
            {
                printGrammar();
                System.out.println("Enter anything to continue: ");
                userInput = scanner.nextLine();

            }
            else if(userInput.equals("q"))
            {
                break;
            }
            else
            {
                System.out.println("Error: invalid option selection '" + userInput + "'.");
                System.out.println("Enter anything to continue: ");
                userInput = scanner.nextLine();
            }
        }
    }

    public static void printGrammar()
    {
        System.out.println("\nS  ->  S' | ε");
        System.out.println("S' ->  AA'");
        System.out.println("A  ->  AA'+ | AA'- | ε");
        System.out.println("A' ->  MM'");
        System.out.println("M  ->  MM'* | MM'/ | MM'% | ε");
        System.out.println("M' ->  EE'");
        System.out.println("E  ->  EE'^ | ε");
        System.out.println("E' ->  (S') | num\n");
    }

    public static void printOptions()
    {
        System.out.println("\nOptions:\n");
        System.out.println("e   Solve expression");
        System.out.println("v   Solve expression and show process");
        System.out.println("g   Print grammar");
        System.out.println("q   Quit\n");
    }

    public static void printOperators()
    {
        System.out.println("\nOperators:\n");
        System.out.println("+   Addition");
        System.out.println("-   Subtraction or entering negative number");
        System.out.println("*   Multiplication");
        System.out.println("/   Division");
        System.out.println("%   Modulo");
        System.out.println("^   Exponent");
        System.out.println("()  Parenthesis\n");
    }
}