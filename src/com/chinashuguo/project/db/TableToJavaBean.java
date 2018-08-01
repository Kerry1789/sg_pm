package com.chinashuguo.project.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class TableToJavaBean {

    private static final String LINE = "\r\n";
    private static final String TAB = "\t";

    String packages = this.getClass().getPackage().getName().replace("common", "model");;
    private static Map<String, String> map;

    static {
        map = new HashMap<String, String>();
        map.put("VARCHAR", "String");
        map.put("NVARCHAR", "String");
        map.put("BIGINT", "Long");
        map.put("TEXT", "String");
        map.put("NTEXT", "String");
        map.put("INTEGER", "Integer");
        map.put("INT", "Integer");
        map.put("FLOAT", "float");
        map.put("TIMESTAMP", "java.sql.Timestamp");
        map.put("CHAR", "String");
        map.put("DATETIME", "java.sql.Timestamp");
        map.put("TIMESTAMP_IMPORT", "import java.sql.Timestamp");
        map.put("DATETIME_IMPORT","import java.sql.Timestamp");

        map.put("VARCHAR".toLowerCase(), "String");
        map.put("NVARCHAR".toLowerCase(), "String");
        map.put("TEXT".toLowerCase(), "String");
        map.put("NTEXT".toLowerCase(), "String");
        map.put("BIGINT".toLowerCase(), "Long");
        map.put("INTEGER".toLowerCase(), "Integer");
        map.put("INT".toLowerCase(), "Integer");
        map.put("FLOAT".toLowerCase(), "float");
        map.put("TIMESTAMP".toLowerCase(), "java.sql.Timestamp");
        map.put("CHAR".toLowerCase(), "String");
        map.put("DATETIME".toLowerCase(), "java.sql.Timestamp");
        map.put("TIMESTAMP_IMPORT".toLowerCase(), "import java.sql.Timestamp");
        map.put("DATETIME_IMPORT".toLowerCase(),"import java.sql.Timestamp");
    }

    /// get the data filed type
    public static String getPojoType(String dataType) {
        StringTokenizer st = new StringTokenizer(dataType);
        return map.get(st.nextToken());
    }

    // import the package
    public static String getImport(String dataType) {
        if (map.get(dataType)==null||"".equals(map.get(dataType))) {
            return null;
        }else{
            return map.get(dataType);
        }
    }


    public void tableToBean(Connection connection, String tableName) throws SQLException {
        System.out.println("tablename = " + tableName);
        String sql = "select * from " + tableName + " where 1 <> 1";
        String realTableName = tableName;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ps = connection.prepareStatement(sql);
        rs = ps.executeQuery();
        ResultSetMetaData md = rs.getMetaData();
        int columnCount = md.getColumnCount();
        StringBuffer sb = new StringBuffer();
        tableName = tableName.substring(0, 1).toUpperCase() + tableName.subSequence(1, tableName.length());
        tableName = this.dealLine(tableName);
        sb.append("package " + this.packages + " ;");
        sb.append(LINE);
        sb.append("import com.chinashuguo.project.db.SqlBean;");
        sb.append(LINE);
        importPackage(md, columnCount, sb);
        sb.append(LINE);
        sb.append(LINE);
        sb.append("public class " + tableName + " implements SqlBean {");
        sb.append(LINE);
        implementsMethods(rs,md, columnCount,realTableName, sb);
        sb.append(LINE);
        defProperty(md, columnCount, sb);
        genSetGet(md, columnCount, sb);
        sb.append("}");
        String paths = System.getProperty("user.dir");
        String endPath = paths + "\\src\\" + (packages.replace("/", "\\")).replace(".", "\\");
        buildJavaFile(endPath + "\\" + tableName + ".java", sb.toString());
    }

    /**
     * implements sqlbean's method
     */
    private void implementsMethods(ResultSet rs,ResultSetMetaData md, int columnCount,String tableName, StringBuffer sb) {
        // method getTableName
        sb.append("@Override");
        sb.append(LINE);
        sb.append("public String getTableName() {");
        sb.append(LINE);
        sb.append(TAB);
        sb.append("return \"" + tableName +"\";");
        sb.append(LINE);
        sb.append("}");
        sb.append(LINE);

        // method getPrimaryKeyColumnName
        sb.append("@Override");
        sb.append(LINE);
        sb.append("public String getPrimaryKeyColumnName() {");
        sb.append(LINE);
        try {
            sb.append(TAB);
            sb.append("return \"" +
                    md.getColumnName(1) +
                    "\";");
        }catch (Exception e) {}
        sb.append(LINE);
        sb.append("}");
        sb.append(LINE);

        //method getColumnNames
        sb.append("@Override");
        sb.append(LINE);
        sb.append("public String[] getColumnNames() {");
        sb.append(LINE);
        sb.append(TAB);
        sb.append("return new String[]{");
        for (int i = 1; i <= columnCount; i++) {
            sb.append(LINE);
            try {
                sb.append("\"" + md.getColumnName(i) + "\"");
            }catch (Exception e) {

            }
            if(i!=columnCount)sb.append(",");
        }
        sb.append("};");
        sb.append(LINE);
        sb.append("}");
        sb.append(LINE);

        // method toString
        sb.append("@Override");
        sb.append(LINE);
        sb.append("public String toString() {");
        sb.append(LINE);
        sb.append(TAB);
        sb.append("StringBuilder sb = new StringBuilder();");
        sb.append(LINE);
        for (int i = 1; i <= columnCount; i++) {
            sb.append(TAB);
            sb.append(LINE);
            try {
                String columnName = dealLine(md, i);
                if(i!=1)
                    sb.append("sb.append(\"," + columnName + "=\");");
                else
                    sb.append("sb.append(\"" + columnName + "=\");");
                sb.append(TAB);
                sb.append(LINE);
                sb.append("sb.append(" + columnName + ");");
            }catch (Exception e) {

            }
        }
        sb.append(LINE);
        sb.append(TAB);
        sb.append("return sb.toString();");
        sb.append(LINE);
        sb.append("}");
    }

    //create geter\ setser method
    private void genSetGet(ResultSetMetaData md, int columnCount, StringBuffer sb) throws SQLException {
        for (int i = 1; i <= columnCount; i++) {
            sb.append(TAB);
            String pojoType = getPojoType(md.getColumnTypeName(i));
            String columnName = dealLine(md, i);
            String getName = null;
            String setName = null;
            if (columnName.length() > 1) {
                getName = "public " + pojoType + " get" + columnName.substring(0, 1).toUpperCase()
                        + columnName.substring(1, columnName.length()) + "() {";
                setName = "public void set" + columnName.substring(0, 1).toUpperCase()
                        + columnName.substring(1, columnName.length()) + "(" + pojoType + " " + columnName + ") {";
            } else {
                getName = "public get" + columnName.toUpperCase() + "() {";
                setName = "public set" + columnName.toUpperCase() + "(" + pojoType + " " + columnName + ") {";
            }
            sb.append(LINE).append(TAB).append(getName);
            sb.append(LINE).append(TAB).append(TAB);
            sb.append("return " + columnName + ";");
            sb.append(LINE).append(TAB).append("}");
            sb.append(LINE);
            sb.append(LINE).append(TAB).append(setName);
            sb.append(LINE).append(TAB).append(TAB);
            sb.append("this." + columnName + " = " + columnName + ";");
            sb.append(LINE).append(TAB).append("}");
            sb.append(LINE);

        }
    }

    /// import package
    private void importPackage(ResultSetMetaData md, int columnCount, StringBuffer sb) throws SQLException {
        for (int i = 1; i <= columnCount; i++) {
            String im=getImport(md.getColumnTypeName(i)+"_IMPORT");
            if (im!=null) {
                sb.append(im+ ";");
                sb.append(LINE);
            }
        }
    }
    /// define the property filed
    private void defProperty(ResultSetMetaData md, int columnCount, StringBuffer sb) throws SQLException {

        for (int i = 1; i <= columnCount; i++) {
            sb.append(TAB);
            String columnName = dealLine(md, i);
            sb.append("private " + getPojoType(md.getColumnTypeName(i)) + " " + columnName + ";");
            sb.append(LINE);
        }
    }

    /// upper char
    private String dealLine(ResultSetMetaData md, int i) throws SQLException {
        String columnName = md.getColumnName(i);
        columnName = dealName(columnName);
        return columnName;
    }

    private String dealLine(String tableName) {
        tableName = dealName(tableName);
        return tableName;
    }

    private String dealName(String columnName) {
        if (columnName.contains("_")) {
            StringBuffer names = new StringBuffer();
            String arrayName[] = columnName.split("_");
            names.append(arrayName[0]);
            for (int i = 1; i < arrayName.length; i++) {
                String arri=arrayName[i];
                String tmp=arri.substring(0, 1).toUpperCase()+ arri.substring(1, arri.length());
                names.append(tmp);
            }
            columnName=names.toString();
        }
        return columnName;
    }
    /// create java files
    public void buildJavaFile(String filePath, String fileContent) {
        try {
            File file = new File(filePath);
            FileOutputStream osw = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(osw);
            pw.println(fileContent);
            pw.close();
        } catch (Exception e) {
            System.out.println("create file failed：" + e.getMessage());
        }
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
//        String aaa="代码库名:{sourcedb_name}";
//        aaa = aaa.replaceAll("\\{sourcedb_name\\}","fdafad");
//        System.out.println(aaa);
//        if(true)return;
        String jdbcString = "jdbc:mysql://127.0.0.1:3306/sg_pm?useUnicode=true&characterEncoding=utf8";
        Class.forName("com.mysql.jdbc.Driver");
        Connection con = DriverManager.getConnection(jdbcString, "root", "mysql");
        DatabaseMetaData databaseMetaData = con.getMetaData();
        String[] tableType = { "TABLE","VIEW" };
        ResultSet rs = databaseMetaData.getTables(null, null, "%",tableType);
        TableToJavaBean d = new TableToJavaBean();
        while(rs.next()){
            String tableName=rs.getString(3).toString();
            System.out.println("tableName=" + tableName);
//            if(!tableName.startsWith("t_") && !tableName.startsWith("v_"))
//                continue;
            try {
//                if(tableName.equals("t_compile_his"))
                d.tableToBean(con, tableName);
            }catch (Exception e) {
               System.out.println(e.getMessage());
            }
        }
    }
}
