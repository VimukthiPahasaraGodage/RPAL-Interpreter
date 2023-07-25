JFLAGS = -g
JC = javac

.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

# This uses the line continuation character (\) for readability
# You can list these all on a single line, separated by a space instead.
# If your version of make can't handle the leading tabs on each
# line, just remove them (these are also just added for readability).
CLASSES = \
        com/proglangproj/group50/abstractsyntaxtree/AST.java \
        com/proglangproj/group50/abstractsyntaxtree/ASTNode.java \
        com/proglangproj/group50/abstractsyntaxtree/ASTNodeType.java \
        com/proglangproj/group50/abstractsyntaxtree/StandardizeException.java \
        com/proglangproj/group50/cse_machine/Beta.java \
        com/proglangproj/group50/cse_machine/CSEMachine.java \
        com/proglangproj/group50/cse_machine/Delta.java \
        com/proglangproj/group50/cse_machine/Environment.java \
        com/proglangproj/group50/cse_machine/Eta.java \
        com/proglangproj/group50/cse_machine/EvaluationError.java \
        com/proglangproj/group50/cse_machine/NodeCopier.java \
        com/proglangproj/group50/cse_machine/Tuple.java \
        com/proglangproj/group50/lexicalanalyzer/Scanner.java \
        com/proglangproj/group50/lexicalanalyzer/Token.java \
        com/proglangproj/group50/parser/ParseException.java \
        com/proglangproj/group50/parser/Parser.java \
        rpal20.java \

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
