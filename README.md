# RPAL-Interpreter
*An interpreter for the RPAL functional language.*

In this project, we implement a lexical analyzer and a parser for the RPAL language.
The output of the parser is the AST(Abstract Syntax Tree). Then we implement an algorithm to convert the AST to an ST(Standard Tree) 
which then will be evaluated by a CSE(Control Stack Environment) Machine.

**Team Members**
- JAYASEKARA G.H.B.J. 200250T
- PAHASARA G.G.V. 200440C
- RANGA J.A.L.K. 200523J
- THARINDA N.H.D. 200638P

## Check the correctness
Run **finalize.bat** file to build the project and to test whether the test programs in the rpal_test_programs produce a result identical to the RPAL interpreter by Steven V. Walstra
```bash
./finalize.bat
```

