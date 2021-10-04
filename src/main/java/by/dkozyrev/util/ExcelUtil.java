package by.dkozyrev.util;

import org.apache.poi.hssf.converter.ExcelToHtmlConverter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.usermodel.Picture;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.List;

public class ExcelUtil {
    private static ExcelUtil INSTANCE;

    public static ExcelUtil getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ExcelUtil();
        }
        return INSTANCE;
    }

    private void cleanOutputDirectory(File fileToBeDeleted) {
        File[] allContents = fileToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                if(!file.getName().equals("home.html")) {
                    cleanOutputDirectory(file);
                }
            }
        }
        fileToBeDeleted.delete();
    }

    public String excelToHtml(String fileName, File newFile) throws Exception {
        String ctxPath = "src/main/resources/templates";
        File file = new File(ctxPath);
//        cleanOutputDirectory(file);
//        file.mkdirs();
        InputStream input = new FileInputStream(newFile);
        HSSFWorkbook excelBook = new HSSFWorkbook(input);
        ExcelToHtmlConverter excelToHtmlConverter = new ExcelToHtmlConverter(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        excelToHtmlConverter.processWorkbook(excelBook);
        List pics = excelBook.getAllPictures();
        if (pics != null) {
            for (int i = 0; i < pics.size(); i++) {
                Picture pic = (Picture) pics.get(i);
                try {
                    pic.writeImageContent(new FileOutputStream(ctxPath + pic.suggestFullFileName()));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        Document htmlDocument = excelToHtmlConverter.getDocument();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        DOMSource domSource = new DOMSource(htmlDocument);
        StreamResult streamResult = new StreamResult(outStream);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer serializer = tf.newTransformer();
        serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        serializer.setOutputProperty(OutputKeys.INDENT, "yes");
        serializer.setOutputProperty(OutputKeys.METHOD, "html");
        serializer.transform(domSource, streamResult);
        outStream.close();
        fileName = fileName.replaceAll(" ", "");
        File resFile = new File(ctxPath + "/" + fileName + ".html");
        OutputStream os = new FileOutputStream(resFile);
        final PrintStream printStream = new PrintStream(os);
        printStream.print(outStream);
        printStream.close();
        return resFile.getName();
    }

}
