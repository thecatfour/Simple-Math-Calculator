Good unit tests are valid inputs that should generate an output.
They are formatted as the following:

[Expression],[Answer]

Bad unit tests are invalid inputs to check if the right error was triggered.
They are formatted as the following:

[Expression],[RecursiveDescentParser.ERROR_TYPE]

Whitespace for the expression is removed during scanning.