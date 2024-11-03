package frontend;

import java.io.*;

public class FileProcessor {
    private final FileReader fileReader;
    private final String code;
    private final FileWriter fileWriter;

    public FileProcessor(String inputFile, String outputFile) throws IOException {
        fileReader = new FileReader(inputFile);
        code = file2Code();
        fileWriter = new FileWriter(new File(outputFile));
    }

    //把代码读出来
    private String file2Code() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        StringBuilder stringBuilder = new StringBuilder();
        String temp;
        while ((temp = bufferedReader.readLine()) != null)
        {
            stringBuilder.append(temp).append("\n");
        }
        return stringBuilder.toString();
    }

    public String getCode()
    {
        return code;
    }

    public void writeByLine(String line) throws IOException {
        fileWriter.write(line);
        fileWriter.write("\n");
    }

    public void close() throws IOException {
        fileWriter.close();  // 关闭FileWriter
    }

}
