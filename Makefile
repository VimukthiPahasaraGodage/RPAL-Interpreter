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
        com/proglangproj/group50/abstractsyntaxtree/AbstractSyntaxTree.java \
        com/proglangproj/group50/abstractsyntaxtree/AbstractSyntaxTreeNode.java \
        com/proglangproj/group50/abstractsyntaxtree/AbstractSyntaxTreeNodeType.java \
        com/proglangproj/group50/cse_machine/BetaConditionalEvaluation.java \
        com/proglangproj/group50/cse_machine/CSEMachine.java \
        com/proglangproj/group50/cse_machine/DeltaControlStructure.java \
        com/proglangproj/group50/cse_machine/Environment.java \
        com/proglangproj/group50/cse_machine/EtaRecursiveFixedPoint.java \
        com/proglangproj/group50/cse_machine/CopierOfNodes.java \
        com/proglangproj/group50/cse_machine/Tuple.java \
        com/proglangproj/group50/lexicalanalyzer/Scanner.java \
        com/proglangproj/group50/lexicalanalyzer/Token.java \
        com/proglangproj/group50/parser/Parser.java \
        rpal20.java \

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
