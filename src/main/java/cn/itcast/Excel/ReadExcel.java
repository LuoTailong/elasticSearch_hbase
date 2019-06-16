package cn.itcast.Excel;

import cn.itcast.bean.Article;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by angel；
 */
public class ReadExcel {

    public static List<Article> readXlsx(String path) throws Exception {
        InputStream inputStream = new FileInputStream(path);
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook(inputStream);

        List<Article> list = new ArrayList<Article>();

        for (int numShhet = 0; numShhet < xssfWorkbook.getNumberOfSheets(); numShhet++) {
            XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(numShhet);
            if (xssfSheet == null) {
                continue;
            }
            for (int rowNum = 1; rowNum <= 150; rowNum++) {
                XSSFRow xssfRow = xssfSheet.getRow(rowNum);
                if (xssfRow != null) {
                    Article article = new Article();
                    XSSFCell title = xssfRow.getCell(0);
                    XSSFCell from = xssfRow.getCell(1);
                    XSSFCell times = xssfRow.getCell(2);
                    XSSFCell readCounts = xssfRow.getCell(3);
                    XSSFCell content = xssfRow.getCell(4);
                    article.setId(rowNum + "");
                    article.setContent(content.toString());
                    article.setTimes(times.toString());
                    article.setFrom(from.toString());
                    article.setReadCounts(readCounts.toString());
                    article.setTitle(title.toString());
                    list.add(article);
                }
            }
        }
        return list;
    }

}
