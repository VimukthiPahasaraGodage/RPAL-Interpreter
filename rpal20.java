import com.proglangproj.group50.lexicalanalyzer.*;
import com.proglangproj.group50.parser.*;
import com.proglangproj.group50.abstractsyntaxtree.*;
import com.proglangproj.group50.cse_machine.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class rpal20 {
    public static void main(String[] args) {
        String rpalProgFile;
        String outputFile = "";
        boolean saveToFile = false;

        // When the command is of the form -> java rpal20 rpal_test_programs/rpal_01 > output.01
        if (args.length == 3){
            rpalProgFile = args[0];
            outputFile = args[2];
            saveToFile = true;
        }
        // When the command is of the form -> java rpal20 rpal_test_programs/rpal_01 > output.01
        else if (args.length == 1) {
            rpalProgFile = args[0];
        }
        // When no input RPAL file is specified
        else {
            System.out.println("Please specify a RPAL source file: java rpal20 input_file > output_file");
            return;
        }
        AST ast = buildAST(rpalProgFile);
        if (ast != null) {
            ast.standardize();
            String evaluationResult = evaluateST(ast);
            if (saveToFile) {
                saveOutput(outputFile, evaluationResult);
            }else {
                System.out.println(evaluationResult);
            }
        }
    }

    private static AST buildAST(String fileName){
        AST ast = null;
        try{
            Scanner scanner = new Scanner(fileName);
            Parser parser = new Parser(scanner);
            ast = parser.buildAST();
        }catch(IOException e){
            throw new ParseException("ERROR: Could not read from file: " + fileName);
        }
        return ast;
    }

    private static String evaluateST(AST ast){
        CSEMachine cseMachine = new CSEMachine(ast);
        cseMachine.evaluateProgram();
        return cseMachine.evaluationResult;
    }

    private static void saveOutput(String outputFileName, String evaluationResult){
        File outputFile = new File(outputFileName);
        try(FileOutputStream fos = new FileOutputStream(outputFile, false)) {
            byte[] array = evaluationResult.getBytes();
            fos.write(array);
        } catch (IOException e) {
            System.out.println("IO Error occurred while saving the result to the file:" + outputFileName);
        }
    }
}
