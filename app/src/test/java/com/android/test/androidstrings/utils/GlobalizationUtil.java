package com.android.test.androidstrings.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;

/**
 * Description : 这是一个将excel表中的多国语言转成android使用的文字资源的工具
 *
 */
public class GlobalizationUtil {

    /**
     * xls文件所在目录，也是生成的values文件夹的所在目录
     */
    private final static String DIR = "stringgenerator/";
    /**
     * xls文件名
     */
    private final static String XMS_PATH = DIR + "Nebula.xls";
    /**
     * key 为第几列， value 为国家代码
     */
    private static Map<Integer, String> sCodeMap = new HashMap<>();
    /**
     * key为国家代码， value 为多语言内容
     */
    private static Map<String, List<ValueModel>> sValueMap = new HashMap<>();

    public static void main() {
        getXlsData();
    }

    public static void getXlsData() {
        getXlsData(XMS_PATH, 1, 10);
    }

    /**
     * 只能支持xls格式，即97-2003Excel文件，暂不支持xlsx格式
     *
     * @param xlsPath      多语言xls文件
     * @param startColumns 从第几列开始是多语言内容，从0计数
     * @param count        一共有几列是多语言的内容
     */

    public static void getXlsData(String xlsPath, int startColumns, int count) {
        sValueMap.clear();
        sCodeMap.clear();
        File excelFile = new File(xlsPath);
        try {
            WorkbookSettings workbookSettings = new WorkbookSettings();
            //可以设置为UTF-8或者GBK或者ISO-8859-1，这里需要设置为ISO-8859-1，否则有可能其他国家文字会出现乱码
            workbookSettings.setEncoding("ISO-8859-1");
            Workbook workbook = Workbook.getWorkbook(excelFile, workbookSettings);
            //总共有几张表
            int sheetNum = workbook.getNumberOfSheets();
            for (int index = 0; index < sheetNum; index++) {

                //0代表获取的是第一张表
                Sheet sheet = workbook.getSheet(index);
                //表名
                String sheetName = sheet.getName();
                //表的行数
                int sheetRows = sheet.getRows();
                //表的列数
                int sheetColumns = sheet.getColumns();

                System.out.println("the num of sheets is " + sheetNum);
                System.out.println("the name of sheet is  " + sheetName);
                System.out.println("total rows is 行=" + sheetRows);
                System.out.println("total cols is 列=" + sheetColumns);

                //i=1，代表从第2行开始读取文件内容
                for (int i = 0; i < sheetRows; i++) {
                    //第2行是读取国家代码
                    if (i == 0) {
                        for (int number = 0; number < count; number++) {
                            int columnIndex = startColumns + number;
                            String code = sheet.getCell(columnIndex, i).getContents().toLowerCase();
                            if("he".equals(code)){
                                code = "iw";
                            }
                            sCodeMap.put(columnIndex, code);
                            sValueMap.put(code, new ArrayList<>());
                        }
                    } else {
                        String name = sheet.getCell(0, i).getContents();
                        if (name == null || name.equals("")) {
                            break;
                        }

                        String defaultValue = sheet.getCell(1, i).getContents();
                        for (int number = 0; number < count; number++) {
                            int columnIndex = startColumns + number;

                            String code = sCodeMap.get(columnIndex);

                            List<ValueModel> countryList = sValueMap.get(code);
                            ValueModel valueModel = new ValueModel();
                            valueModel.setName(name);
                            valueModel.setDefaultValue(defaultValue);
                            valueModel.setCountryCode(code);

                            valueModel.setValue(sheet.getCell(columnIndex, i).getContents());

                            if (countryList != null) {
                                countryList.add(valueModel);

                            }
                        }
                    }
                }


                createValuesStringsXml(excelFile.getName(), sheetName);

            }
            workbook.close();
        } catch (Exception e) {
            System.out.println("read error=" + e);
        }
    }

    /**
     * 创建values文件夹和strings文件，并写入内容
     */
    private static void createValuesStringsXml(String projectName, String sheetName) {
        Set<String> keySet = sValueMap.keySet();
        for (String code : keySet) {

            String path = "output/"+projectName+"/"+sheetName + "/values-" + code;
            File file = new File(path + "/strings.xml");
            new File(path).mkdirs();
            System.out.println("path=" + file.getAbsolutePath());

            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
                BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                bw.write("<resources>");
                bw.newLine();
                List<ValueModel> list = sValueMap.get(code);
                for (ValueModel model : list) {
                    bw.write("<string name=\"" + model.getName() + "\">" + model.getValue() + "</string>");
                    bw.newLine();
                }
                bw.write("</resources>");
                bw.close();
                System.out.println("写入完成" + path + " " + file.getName());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}