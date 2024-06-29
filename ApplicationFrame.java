import SimpleMathParser.RecursiveDescentParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import static java.lang.String.format;

public class ApplicationFrame extends JFrame
{
    //
    // Constants
    //

    // Help organize elements
    private static final int[] FIRST_ROW_ELEMENT = new int[] {5, 10};
    private static final int[] SECOND_ROW_ELEMENT = new int[] {5, 40};
    private static final int[] THIRD_ROW_ELEMENT = new int[] {55, 70};

    // Number of rows for the text area
    private static final int TEXT_AREA_ROWS = 10;

    // Font for most labels
    private static final Font FONT = new Font("Arial", Font.PLAIN, 14);

    //
    // Fields
    //

    private final int X;
    private final int Y;

    // Used to not overwrite duplicate button presses
    private char state = 'h';

    // Buttons
    private final JButton buttonCalculate;
    private final JButton buttonHistory;
    private final JButton buttonProcess;
    private final JButton buttonOperators;
    private final JButton buttonGrammar;

    // Text to be displayed under the button of the current state
    private final JLabel labelState;

    // Text input for the equation
    private final JTextField inputExpr;

    // Labels to signal where to input the equation and the location of the answer
    private final JLabel labelTitleExpr;
    private final JLabel labelTitleAnswer;
    private final JTextArea textAreaAnswer;

    // Displays other data
    private final JTextArea textAreaBottom;

    // Parser
    private final RecursiveDescentParser parser;

    // Linked Lists to hold Strings for history and process
    private final LinkedList<String> history;

    public ApplicationFrame(int width, int height)
    {
        X = width;
        Y = height;

        buttonCalculate = new JButton("Calculate");

        buttonHistory = new JButton("History");
        buttonProcess = new JButton("Process");
        buttonOperators = new JButton("Operators");
        buttonGrammar = new JButton("Grammar");

        labelState = new JLabel("State");

        labelTitleExpr = new JLabel("Expression: ");
        inputExpr = new JTextField(35);

        labelTitleAnswer = new JLabel("Answer:    ");
        textAreaAnswer = new JTextArea(1,35);

        textAreaBottom = new JTextArea(TEXT_AREA_ROWS,52);

        parser = new RecursiveDescentParser();

        history = new LinkedList<>();

        setupButtonEvents();
    }

    public void startApplication()
    {
        setSize(X, Y);
        setTitle("Simple Math Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(null);

        // Set up things on the first row
        quickSet(labelTitleExpr, FIRST_ROW_ELEMENT[0], FIRST_ROW_ELEMENT[1]);
        quickSet(inputExpr, labelTitleExpr.getX() + labelTitleExpr.getWidth() + 5, FIRST_ROW_ELEMENT[1]);
        quickSet(buttonCalculate, inputExpr.getX() + inputExpr.getWidth() + 5, FIRST_ROW_ELEMENT[1]);

        buttonCalculate.setSize(X - buttonCalculate.getX() - FIRST_ROW_ELEMENT[0] * 4, inputExpr.getHeight());

        add(labelTitleExpr);
        add(inputExpr);
        add(buttonCalculate);

        // Set up things on the second row
        quickSet(labelTitleAnswer, SECOND_ROW_ELEMENT[0], SECOND_ROW_ELEMENT[1]);

        // Needs to be set up manually
        textAreaAnswer.setFont(FONT);
        textAreaAnswer.setBounds(inputExpr.getX(), SECOND_ROW_ELEMENT[1], inputExpr.getWidth() + buttonCalculate.getWidth(), labelTitleAnswer.getHeight());
        textAreaAnswer.setBackground(null);
        textAreaAnswer.setEditable(false);
        textAreaAnswer.setLineWrap(false);

        add(labelTitleAnswer);
        add(textAreaAnswer);

        // Set up things on the third row
        quickSet(buttonHistory, THIRD_ROW_ELEMENT[0], THIRD_ROW_ELEMENT[1]);
        quickSet(buttonProcess, buttonHistory.getX() + buttonHistory.getWidth() + 40, THIRD_ROW_ELEMENT[1]);
        quickSet(buttonGrammar, buttonProcess.getX() + buttonProcess.getWidth() + 40, THIRD_ROW_ELEMENT[1]);
        quickSet(buttonOperators, buttonGrammar.getX() + buttonGrammar.getWidth() + 40, THIRD_ROW_ELEMENT[1]);

        // quickSet is slightly too small to display the button name
        buttonHistory.setSize(buttonHistory.getWidth()+1, buttonHistory.getHeight());

        add(buttonHistory);
        add(buttonProcess);
        add(buttonGrammar);
        add(buttonOperators);

        // Set up the state button
        labelState.setFont(FONT);
        Dimension size = labelState.getPreferredSize();
        labelState.setBounds(buttonHistory.getX() + 5, buttonHistory.getY() + 5, size.width + 10, size.height + 1);
        labelState.setBackground(new Color(0,150,0));
        labelState.setOpaque(true);
        labelState.setHorizontalAlignment(SwingConstants.CENTER);

        // Default position is under buttonHistory
        moveStateLabel(buttonHistory);

        add(labelState);

        // Set up the Text Area
        textAreaBottom.setFont(FONT);
        size = textAreaBottom.getPreferredSize();
        textAreaBottom.setBounds(7, buttonHistory.getY() + buttonHistory.getHeight() + 38, size.width + 1, size.height + 1);
        textAreaBottom.setLineWrap(false);
        textAreaBottom.setEditable(false);

        size = null;

        add(textAreaBottom);

        setVisible(true);
    }

    private void setupButtonEvents()
    {
        ActionListener buttonListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Object o = e.getSource();

                if(o == buttonCalculate)
                {
                    double output = parser.parseExpressionString(inputExpr.getText());

                    switch (parser.getError())
                    {
                        case RecursiveDescentParser.NO_ERROR:
                            // Write answer to answer label
                            textAreaAnswer.setText(String.valueOf(output));

                            // Change process

                            // Add to history
                            history.addLast(inputExpr.getText() + " = " + format("%.2f",output) + "\n");

                            // Check if history is larger than TEXT_AREA_ROWS
                            // If it is, then pop the last elements, which should be the oldest
                            if(history.size() > TEXT_AREA_ROWS)
                            {
                                String temp = history.removeFirst();

                                // Only remove the last line from the text area if history is visible
                                if (state == 'h')
                                {
                                    textAreaBottom.select(textAreaBottom.getText().length() - temp.length(), textAreaBottom.getText().length());
                                    textAreaBottom.replaceSelection("");
                                }
                            }
                            break;
                        case RecursiveDescentParser.SCANNER_ERROR:
                            // Scanner failed to convert input into tokens
                            textAreaAnswer.setText("Scanner Error: unknown character inputted or incorrect decimal use.");
                            break;
                        case RecursiveDescentParser.NULL_ERROR:
                            // Missing a leftmost number
                            textAreaAnswer.setText("Syntax Error: missing number on left side of the expression.");
                            break;
                        case RecursiveDescentParser.EP_ERROR:
                            // Missing a number but found an operator
                            textAreaAnswer.setText("Syntax Error: invalid use of '" + parser.getErrorToken() + "'.");
                           break;
                        case RecursiveDescentParser.LPAREN_MISSING_ERROR:
                            // Missing a left parenthesis to pair with a right parenthesis
                            textAreaAnswer.setText("Syntax Error: missing '('.");
                            break;
                        case RecursiveDescentParser.RPAREN_MISSING_ERROR:
                            // Missing a right parenthesis to pair with a left parenthesis
                            textAreaAnswer.setText("Syntax Error: missing ')'.");
                            break;
                        case RecursiveDescentParser.ZERO_DIV_ERROR:
                            // Zero division
                            textAreaAnswer.setText( "Syntax Error: division or modulo by 0.");
                            break;
                        case RecursiveDescentParser.A_ERROR:
                        case RecursiveDescentParser.M_ERROR:
                        case RecursiveDescentParser.E_ERROR:
                            // Invalid token found
                            textAreaAnswer.setText("Syntax Error: invalid use of '" + parser.getErrorToken() + "'.");
                            break;
                        default:
                            textAreaAnswer.setText("Unknown error has occurred.");
                            break;
                    }

                    // Based on state, text area might be changed
                    if(state == 'h' && parser.getError() == RecursiveDescentParser.NO_ERROR)
                    {
                        textAreaBottom.insert(history.peekLast(), 0);
                    }
                    else if(state == 'p' && parser.getError() == RecursiveDescentParser.NO_ERROR)
                    {
                        textAreaBottom.selectAll();
                        textAreaBottom.replaceSelection("");

                        textAreaBottom.setText(parser.getProcess());
                    }
                }
                else if(o == buttonHistory)
                {
                    if(state == 'h')
                        return;

                    moveStateLabel(buttonHistory);

                    textAreaBottom.selectAll();
                    textAreaBottom.replaceSelection("");

                    for(String line:history)
                    {
                        textAreaBottom.insert(line, 0);
                    }

                    state = 'h';
                }
                else if(o == buttonProcess)
                {
                    if(state == 'p')
                        return;

                    moveStateLabel(buttonProcess);

                    textAreaBottom.selectAll();
                    textAreaBottom.replaceSelection("");

                    textAreaBottom.setText(parser.getProcess());

                    state = 'p';
                }
                else if(o == buttonGrammar)
                {
                    if(state == 'g')
                        return;

                    moveStateLabel(buttonGrammar);

                    textAreaBottom.selectAll();
                    textAreaBottom.replaceSelection("");

                    textAreaBottom.append("S  ->  S'\n");
                    textAreaBottom.append("S' ->  AA'\n");
                    textAreaBottom.append("A  ->  AA'+ | AA'- | ε\n");
                    textAreaBottom.append("A' ->  MM'\n");
                    textAreaBottom.append("M  ->  MM'* | MM'/ | MM'% | ε\n");
                    textAreaBottom.append("M' ->  EE'\n");
                    textAreaBottom.append("E  ->  EE'^ | ε\n");
                    textAreaBottom.append("E' ->  (S') | num\n");

                    state = 'g';
                }
                else if(o == buttonOperators)
                {
                    if(state == 'o')
                        return;

                    moveStateLabel(buttonOperators);

                    textAreaBottom.selectAll();
                    textAreaBottom.replaceSelection("");

                    textAreaBottom.append("+   Addition\n");
                    textAreaBottom.append("-    Subtraction or entering negative number\n");
                    textAreaBottom.append("*    Multiplication\n");
                    textAreaBottom.append("/    Division\n");
                    textAreaBottom.append("%  Modulo\n");
                    textAreaBottom.append("^    Exponent\n");
                    textAreaBottom.append("()   Parenthesis\n");
                    textAreaBottom.append("\nNote: nested exponents need to be in parenthesis.\n");
                    textAreaBottom.append("Example: 2^(3^2) is different from 2^3^2.");

                    state = 'o';
                }
            }
        };

        buttonCalculate.addActionListener(buttonListener);
        buttonHistory.addActionListener(buttonListener);
        buttonProcess.addActionListener(buttonListener);
        buttonOperators.addActionListener(buttonListener);
        buttonGrammar.addActionListener(buttonListener);
    }

    private void quickSet(Component com, int xPos, int yPos)
    {
        com.setFont(FONT);
        Dimension size = com.getPreferredSize();
        com.setBounds(xPos, yPos, size.width + 1, size.height + 1);
    }

    private void moveStateLabel(JButton b)
    {
        labelState.setLocation((b.getX() + b.getWidth()/2) - labelState.getWidth()/2, (b.getY() + b.getHeight()) + 10);
    }
}
