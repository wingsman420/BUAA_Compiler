import LLVM.LLVMGenerator;
import LLVM.TempCounter;
import Lexer.Lexer;
import frontend.*;
import parser.GrammaticalAnalyser;
import parser.SymbolAnalyser;

import java.io.IOException;

public class Compiler {
    public static void main(String[] args) {
        try {

            TempCounter tempCounter = new TempCounter();
            // 创建 FileProcessor 读取 "testfile.txt"，输出到 "lexer_output.txt"

            FileProcessor tokenFileProcessor = new FileProcessor("testfile.txt", "llvm_ir.txt");

            // 创建 FileProcessor 单独处理 error.txt 文件
            FileProcessor errorFileProcessor = new FileProcessor("testfile.txt", "error.txt");

            // 创建 Lexer 对象，并传入 tokenFileProcessor 处理词法分析
            Lexer lexer = new Lexer(tokenFileProcessor);

            // 开始词法分析
            lexer.analyze();

            GrammaticalAnalyser grammaticalAnalyser = new GrammaticalAnalyser(lexer.getTokens(),lexer.getErrorInOrder());


            SymbolAnalyser symbolAnalyser = new SymbolAnalyser(grammaticalAnalyser.getRoot(),grammaticalAnalyser.getErrors());

            //symbolAnalyser.writeTokens(tokenFileProcessor);
            //symbolAnalyser.writeErrors(errorFileProcessor);
            if (!symbolAnalyser.errors.isEmpty())
            {
                symbolAnalyser.writeErrors(errorFileProcessor);
                errorFileProcessor.close();
                return;
            }
            LLVMGenerator lg = new LLVMGenerator(grammaticalAnalyser.getRoot());
            lg.analyseCompUnit();
            lg.print(tokenFileProcessor);
            tokenFileProcessor.close();

            // 输出词法分析结果到 lexer.txt
            //grammaticalAnalyser.writeTokens(tokenFileProcessor);

            // 输出错误信息到 error.txt
            //grammaticalAnalyser.writeErrors(errorFileProcessor);

            // 关闭文件写入器


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}