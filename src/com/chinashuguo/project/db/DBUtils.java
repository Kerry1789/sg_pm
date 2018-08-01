package com.chinashuguo.project.db;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by kui.li on 2014/9/2.
 */
public class DBUtils {
    public static final String DRIVER_CLASS = "com.mysql.jdbc.Driver";
    public static final String URL = "jdbc:mysql://127.0.0.1:3306/sg_pm?useUnicode=true&characterEncoding=utf8";
    public static final String USER = "root";
    public static final String PASSWORD = "mysql";
    private static final Logger logger = Logger.getLogger(DBUtils.class.getName());

    private DBUtils() {}

    public static Connection openConnection() {
        Connection conn = null;
        try {
            Class.forName(DRIVER_CLASS);
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch(Exception e) {
            logger.log(Level.FINE, "DBUtils openConnection error!", e);
        }
        return conn;
    }

//    public static Connection openConnection() {
//        Connection dbConn;
//        try {
//            Context initCtx = new InitialContext();
//            if (initCtx == null)
//                throw new Exception("不能获取Context!");
//            Context ctx = (Context) initCtx.lookup("java:comp/env");
//            Object obj = (Object) ctx.lookup("jdbc/sqlserver");//获取连接池对象
//            DataSource ds = (javax.sql.DataSource) obj; //类型转换
//            dbConn = ds.getConnection();
//            // System.out.println("连接池连接成功!");
//            return dbConn;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//
//    }

    public static boolean closeConnection(Connection conn) {
        boolean res = true;
        if(conn != null) {
            try {
                conn.close();
            } catch(SQLException e) {
                logger.log(Level.FINE, e.toString(), e);
                res = false;
            }
        }
        return res;
    }

    public abstract static class DBHandler<T> {
        public DBHandler() {}
        public abstract void handle(Connection conn) throws Exception;
        public void handleFinally() throws Exception {};
        public abstract T getResult();
        public void handleException(Connection conn) throws Exception {};
    }
    public static <T> T operator(DBHandler<T> handler) {
        Connection conn = openConnection();
        if(conn != null) {
            try {
                handler.handle(conn);
            } catch(Exception e) {
                try {
                    handler.handleException(conn);
                } catch(Exception e1) {
                    logger.log(Level.FINE, e.toString(), e1);
                }
                logger.log(Level.FINE, e.toString(), e);
            } finally {
                try {
                    handler.handleFinally();
                } catch(Exception e) {
                    logger.log(Level.FINE, e.toString(), e);
                }
                closeConnection(conn);
            }
        }
        return handler.getResult();
    }

    /******************************* query ******************************************/

    /**
     * select top 10 * from t_userinfo where _id not in(select top 10000 _id from t_userinfo);
     * 以上含义是查询t_userinfo表中10001-10010条记录，共10条。
     *
     * @param selection 查询条件
     * @param className
     * @param pagerInfo 分页信息，需预先设置pageSize、pageIndex属性，
     * 		执行该方法中会设置其recordCount、pageCount属性，且有可能更新其pageIndex属性。
     * @return
     */
    public static <T extends SqlBean> List<T> query(final String selection, final List<String> selectionArgs,
                                                    final String sort, final Class<T> className, final PagerInfo pagerInfo) {
        return operator(new DBHandler<List<T>>() {
            List<T> list = new ArrayList<T>();
            @Override
            public void handle(Connection conn) throws Exception {
                T t = className.newInstance();
                String tableName = t.getTableName();
                String countSql = getCountSql(tableName, selection);
                PreparedStatement pstmt = conn.prepareStatement(countSql);
                if(selectionArgs != null) {
                    for(int i=1;i<=selectionArgs.size();i++) {
                        pstmt.setObject(i, selectionArgs.get(i-1));
                    }
                }
                ResultSet rs = pstmt.executeQuery();
                if(rs.next()) {
                    int recordCount = rs.getInt(1);
                    pagerInfo.setRecordCount(recordCount);
                }
                rs.close();
                pstmt.close();

                String pagingSql = getPagingSql(tableName, selection, t.getPrimaryKeyColumnName(), sort, pagerInfo);
                pstmt = conn.prepareStatement(pagingSql);
                if(selectionArgs != null) {
                    int i,size=selectionArgs.size();
                    for(i=1;i<=size;i++) {
                        pstmt.setObject(i, selectionArgs.get(i-1));
                    }
                    //执行两次是因为显示页非第一页时有2次top语句
                    if(pagerInfo.getPageIndex()>1) {
                        for(;i<=size*2;i++) {
                            pstmt.setObject(i, selectionArgs.get(i-size-1));
                        }
                    }
                }
                rs = pstmt.executeQuery();
                if(rs != null) {
                    while(rs.next()) {
                        t = getBeanFromResultSet(rs, className);
                        list.add(t);
                    }
                    rs.close();
                }
            }
            @Override
            public List<T> getResult() {
                return list;
            }
        });
		/*
		List<T> list = new ArrayList<T>();
		Connection conn = openConnection();
		PreparedStatement pstmt = null;
		if(conn!=null && pagerInfo!=null) {
			try {
				T t = className.newInstance();
				String tableName = t.getTableName();
				String countSql = getCountSql(tableName, selection);
				pstmt = conn.prepareStatement(countSql);
				if(selectionArgs != null) {
					for(int i=1;i<=selectionArgs.size();i++) {
						pstmt.setObject(i, selectionArgs.get(i-1));
					}
				}
				ResultSet rs = pstmt.executeQuery();
				if(rs.next()) {
					int recordCount = rs.getInt(1);
					pagerInfo.setRecordCount(recordCount);
				}
				rs.close();
				pstmt.close();

				String pagingSql = getPagingSql(tableName, selection, t.getPrimaryKeyColumnName(), sort, pagerInfo);
				pstmt = conn.prepareStatement(pagingSql);
				if(selectionArgs != null) {
					int i,size=selectionArgs.size();
					for(i=1;i<=size;i++) {
						pstmt.setObject(i, selectionArgs.get(i-1));
					}
					//执行两次是因为显示页非第一页时有2次top语句
					if(pagerInfo.getPageIndex()>1) {
						for(;i<=size*2;i++) {
							pstmt.setObject(i, selectionArgs.get(i-size-1));
						}
					}
				}
				rs = pstmt.executeQuery();
				if(rs != null) {
					while(rs.next()) {
						t = getBeanFromResultSet(rs, className);
						list.add(t);
					}
					rs.close();
				}
				pstmt.close();
			} catch(Exception e) {
				logger.log(Level.FINE, e.toString(), e);
			} finally {
				closeConnection(conn);
			}
		}
		return list;*/
    }
    private static String getCountSql(String tableName, String selection) {
        selection = selection==null?null:selection.trim();
        StringBuilder sql = new StringBuilder();
        sql.append("select count(*) from ");
        sql.append(tableName);
        if(selection!=null && !"".equals(selection)) {
            sql.append(" where ");
            sql.append(selection);
        }
        return sql.toString();
    }
    /**
     * 示例：取11-20条共10条记录的sql如下
     * select top 10 * from 访问统计表 where 访问日期>'2013-04-23' and ID not in
     * 		(select top 10 ID from 访问统计表 where 访问日期>'2013-04-23' order by ID asc)
     * 		order by ID asc;
     * @param tableName
     * @param selection
     * @param primaryKey
     * @param sort
     * @param pagerInfo
     * @return
     */
    private static String getPagingSql(String tableName, String selection, String primaryKey,
                                       String sort, PagerInfo pagerInfo) {
        selection = selection==null?null:selection.trim();
        StringBuilder sb = new StringBuilder();
        sb.append("select top ");
        sb.append(pagerInfo.getPageSize());
        sb.append(" * from ");
        sb.append(tableName);
        if(pagerInfo.getPageIndex() == 1) {
            if(selection!=null && !selection.trim().equals("")) {
                sb.append(" where ");
                sb.append(selection);
            }
            if(sort!=null && !sort.trim().equals("")) {
                sb.append(" order by ");
                sb.append(sort);
            }
        } else {
            sb.append(" where ");
            if(selection!=null && !selection.trim().equals("")) {
                sb.append(selection);
                sb.append(" and ");
            }
            sb.append(primaryKey);
            sb.append(" not in ( select top ");
            sb.append(pagerInfo.getStartRecord());
            sb.append(" ");
            sb.append(primaryKey);
            sb.append(" from ");
            sb.append(tableName);
            if(selection!=null && !selection.trim().equals("")) {
                sb.append(" where ");
                sb.append(selection);
            }
            if(sort!=null && !sort.trim().equals("")) {
                sb.append(" order by ");
                sb.append(sort);
            }
            sb.append(")");
            if(sort!=null && !sort.trim().equals("")) {
                sb.append(" order by ");
                sb.append(sort);
            }
        }
        logger.info("getPagingSql pagingSql="+sb.toString());
        return sb.toString();

    }

    public static <T extends SqlBean> List<T> query(final String sql, final Object[] args, final Class<T> className) {
        return operator(new DBHandler<List<T>>(){
            List<T> list = new ArrayList<T>();
            PreparedStatement pstmt = null;
            @Override
            public void handle(Connection conn) throws Exception {
                pstmt = conn.prepareStatement(sql);
                if(args!=null && args.length>0) {
                    for(int i=1;i<=args.length;i++) {
                        pstmt.setObject(i, args[i-1]);
                    }
                }
                ResultSet rs = pstmt.executeQuery();
                while(rs.next()) {
                    T t = getBeanFromResultSet(rs, className);
                    list.add(t);
                }
            }
            @Override
            public void handleFinally() throws Exception {
                if(pstmt != null) pstmt.close();
            }
            @Override
            public List<T> getResult() {
                return list;
            }
        });
    }

    public static <T extends SqlBean> List<T> query(final String sql, final Class<T> className) {
        return operator(new DBHandler<List<T>>() {
            List<T> list = new ArrayList<T>();
            Statement stmt = null;
            @Override
            public void handle(Connection conn) throws Exception {
                stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                while(rs.next()) {
                    T t = getBeanFromResultSet(rs, className);
                    list.add(t);
                }
            }
            @Override
            public void handleFinally() throws Exception {
                if(stmt != null) stmt.close();
            }
            @Override
            public List<T> getResult() {
                return list;
            }
        });
		/*
		List<T> list = new ArrayList<T>();
		Connection conn = openConnection();
		Statement stmt = null;
		if(sql!=null && conn != null) {
			try {
				stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				while(rs.next()) {
					T t = getBeanFromResultSet(rs, className);
					list.add(t);
				}
			} catch(Exception e) {
				logger.log(Level.FINE, e.toString(), e);
			} finally {
				if(stmt!=null)
					try {
						stmt.close();
					} catch (SQLException e) {
						logger.log(Level.FINE, e.toString(), e);
					}
				closeConnection(conn);
			}
		}
		return list;
		*/
    }
    @SuppressWarnings("rawtypes")
    private static <T extends SqlBean> T getBeanFromResultSet(ResultSet rs, Class<T> className) throws Exception {
        T t = className.newInstance();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        for(int i=1;i<=columnCount;i++) {
            Class columnClass = Class.forName(metaData.getColumnClassName(i));
            String setMethodName = getSetMethodName(metaData.getColumnName(i));
            Method setMethod = className.getMethod(setMethodName, columnClass);
            if(setMethod != null) {
                Object obj = rs.getObject(i);
                if(!className.getName().endsWith("TConfig") && !setMethodName.endsWith("escs")) {
                    if (obj instanceof String && obj != null && ((String) obj).length() > 0 && (((String) obj).indexOf("\"") > 0 || ((String) obj).indexOf("\'") > 0)) {
                        obj = ((String) obj).replaceAll("\"", "&#34;");
                        obj = ((String) obj).replaceAll("\'", "&#39;");
                    }
                }
                setMethod.invoke(t, obj);
            }
        }
        return t;
    }

    public static boolean exist(final SqlBean bean) {
        return operator(new DBHandler<Boolean>() {
            boolean exist = false;
            PreparedStatement pstmt = null;
            @Override
            public void handle(Connection conn) throws Exception {
                String primaryKeyColumnName = bean.getPrimaryKeyColumnName();
                Object primaryKeyValue = DBUtils.getValueByColumnName(bean, primaryKeyColumnName);
                if(primaryKeyValue != null) {
                    StringBuilder sql = new StringBuilder();
                    sql.append("select ");
                    sql.append(primaryKeyColumnName);
                    sql.append(" from ");
                    sql.append(bean.getTableName());
                    sql.append(" where ");
                    sql.append(primaryKeyColumnName);
                    sql.append("=?");
                    pstmt = conn.prepareStatement(sql.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    pstmt.setObject(1, primaryKeyValue);
                    ResultSet rs = pstmt.executeQuery();
                    rs.last();
                    int count = rs.getRow();
                    logger.info("exist : sql="+sql.substring(0, sql.length()-1)+primaryKeyValue+",count="+count);
                    if(count == 1) {
                        exist = true;
                    }
                }
            }
            @Override
            public void handleFinally() throws Exception {
                if(pstmt != null) pstmt.close();
            }
            @Override
            public Boolean getResult() {
                return exist;
            }
        });
		/*
		boolean exist = false;
		String primaryKeyColumnName = bean.getPrimaryKeyColumnName();
		Object primaryKeyValue = DBUtils.getValueByColumnName(bean, primaryKeyColumnName);
		if(primaryKeyValue != null) {
			Connection conn = openConnection();
			PreparedStatement pstmt = null;
			try {
				StringBuilder sql = new StringBuilder();
				sql.append("select ");
				sql.append(primaryKeyColumnName);
				sql.append(" from ");
				sql.append(bean.getTableName());
				sql.append(" where ");
				sql.append(primaryKeyColumnName);
				sql.append("=?");
				pstmt = conn.prepareStatement(sql.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
				pstmt.setObject(1, primaryKeyValue);
				ResultSet rs = pstmt.executeQuery();
				rs.last();
				int count = rs.getRow();
				logger.info("exist : sql="+sql.substring(0, sql.length()-1)+primaryKeyValue+",count="+count);
				if(count == 1) {
					exist = true;
				}
			} catch(Exception e) {
				logger.log(Level.FINE, e.toString(), e);
			} finally {
				if(pstmt!=null)
					try {
						pstmt.close();
					} catch (SQLException e) {
						logger.log(Level.FINE, e.toString(), e);
					}
				closeConnection(conn);
			}
		}

		return exist;*/
    }

    public static long queryMinId(final Class<? extends SqlBean> beanClass) {
        return operator(new DBHandler<Long>() {
            Long id = -1l;
            @Override
            public void handle(Connection conn) throws Exception {
                SqlBean bean = beanClass.newInstance();
                String tableName = bean.getTableName();
                String primaryKey = bean.getPrimaryKeyColumnName();
                String sql = "select min(" + primaryKey + ") from " + tableName;
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                if(rs!=null && rs.next()) {
                    id = rs.getLong(1);
                    rs.close();
                }
                stmt.close();
            }
            @Override
            public Long getResult() {
                return id;
            }
        });
    }

    /******************************* execute ******************************************/
    @SuppressWarnings("rawtypes")
    public static int executeUpdate(String sql, List params) {
        return executeUpdate(sql, params, null, null);
    }

    private static boolean exist(Connection conn, String existSql, String[] existArgs) throws Exception {
        boolean exist = false;
        if(existSql != null) {
            PreparedStatement pstmt = conn.prepareStatement(existSql);
            if(existArgs!=null && existArgs.length>0) {
                for(int i=1;i<=existArgs.length;i++) {
                    pstmt.setObject(i, existArgs[i-1]);
                }
            }
            ResultSet rs = pstmt.executeQuery();
            if(rs!=null) {
                if(rs.next()) exist = true;
                rs.close();
                pstmt.close();
            }
        }
        return exist;
    }
    @SuppressWarnings("rawtypes")
    private static int update(Connection conn, String sql, List params) throws Exception {
        int result = -1;
        conn.setAutoCommit(false);
        PreparedStatement pstmt = conn.prepareStatement(sql);
        for(int i=1;i<=params.size();i++) {
            pstmt.setObject(i, params.get(i-1));
        }
        result = pstmt.executeUpdate();
        if(result > 0) {
            conn.commit();
        }
        pstmt.close();
        return result;
    }
    @SuppressWarnings("rawtypes")
    public static int executeUpdate(final String sql, final List params, final String existSql, final String[] existArgs) {
        return operator(new DBHandler<Integer>() {
            int result = -1;
            @Override
            public void handle(Connection conn) throws Exception {
                if(exist(conn, existSql, existArgs)) {
                    result = -2;
                } else {
                    result = update(conn, sql, params);
                }
            }
            @Override
            public void handleException(Connection conn) throws Exception {
                conn.rollback();
            }
            @Override
            public Integer getResult() {
                return result;
            }
        });
		/*
		int result = -1;
		Connection conn = openConnection();
		PreparedStatement pstmt = null;
		if(conn != null && sql != null && params != null) {
			try {
				conn.setAutoCommit(false);
				if(exist(conn, existSql, existArgs)) {
					result = -2;
				} else {
					pstmt = conn.prepareStatement(sql);
					for(int i=1;i<=params.size();i++) {
						pstmt.setObject(i, params.get(i-1));
					}
					result = pstmt.executeUpdate();
					if(result > 0) {
						conn.commit();
					}
				}
			} catch(Exception e) {
				if(conn != null) {
					try {
						conn.rollback();
					} catch(Exception e1) {}
				}
				logger.log(Level.FINE, e.toString(), e);
			} finally {
				if(pstmt != null)
					try {
						pstmt.close();
					} catch (SQLException e) {
						logger.log(Level.FINE, e.toString(), e);
					}
				closeConnection(conn);
			}
		}
		return result;*/
    }

    @SuppressWarnings("rawtypes")
    public static long execute(String sql, List params) {
        return execute(sql, params, null, null);
    }

    @SuppressWarnings("rawtypes")
    private static long execute(Connection conn, String sql, List params) throws Exception {
        long id = -1l;
        conn.setAutoCommit(false);
        PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        for(int i=1;i<=params.size();i++) {
            pstmt.setObject(i, params.get(i-1));
        }
        pstmt.execute();
        ResultSet rs = pstmt.getGeneratedKeys();
        if(rs.next()) {
            id = rs.getLong(1);
            if(id>0) {
               // conn.commit();
            }
            conn.commit();
        }
        return id;
    }
    @SuppressWarnings("rawtypes")
    public static long execute(final String sql, final List params, final String existSql, final String[] existArgs) {
        return operator(new DBHandler<Long>() {
            Long id = -1l;
            @Override
            public void handle(Connection conn) throws Exception {
                if(exist(conn, existSql, existArgs)) {
                    id = -2l;
                } else {
                    id = execute(conn, sql, params);
                }
            }
            @Override
            public void handleException(Connection conn) throws Exception {
                conn.rollback();
            }
            @Override
            public Long getResult() {
                return id;
            }
        });
		/*
		long id = -1l;
		Connection conn = openConnection();
		PreparedStatement pstmt = null;
		if(conn != null && sql != null && params != null) {
			try {
				conn.setAutoCommit(false);
				boolean exist = false;
				if(existSql != null) {
					pstmt = conn.prepareStatement(existSql);
					if(existArgs!=null && existArgs.length>0) {
						for(int i=1;i<=existArgs.length;i++) {
							pstmt.setObject(i, existArgs[i-1]);
						}
					}
					ResultSet rs = pstmt.executeQuery();
					if(rs!=null && rs.next()) {
						exist = true;
						id = -2l;
					} else {
						rs.close();
						pstmt.close();
					}
				}
				if(!exist) {
					pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
					for(int i=1;i<=params.size();i++) {
						pstmt.setObject(i, params.get(i-1));
					}
					pstmt.execute();
					ResultSet rs = pstmt.getGeneratedKeys();
					if(rs.next()) {
						id = rs.getLong(1);
						if(id>0) {
							conn.commit();
						}
					}
				}
			} catch(Exception e) {
				if(conn != null) {
					try {
						conn.rollback();
					} catch(Exception e1) {}
				}
				logger.log(Level.FINE, e.toString(), e);
			} finally {
				if(pstmt != null)
					try {
						pstmt.close();
					} catch (SQLException e) {
						logger.log(Level.FINE, e.toString(), e);
					}
				closeConnection(conn);
			}
		}
		logger.info("insert generatedKey = "+id);
		return id;*/
    }

    public static boolean execute(final List<String> updateSqls) {
        return operator(new DBHandler<Boolean>() {
            Boolean res = false;
            @Override
            public void handle(Connection conn) throws Exception {
                conn.setAutoCommit(false);
                Statement stmt = conn.createStatement();
                for(String sql : updateSqls) {
                    stmt.executeUpdate(sql);
                }
                conn.commit();
                res = true;
            }
            @Override
            public void handleException(Connection conn) throws Exception {
                conn.rollback();
            }
            @Override
            public Boolean getResult() {
                return res;
            }
        });
    }

    /******************************* update ******************************************/
    @SuppressWarnings("rawtypes")
    public static int update(SqlBean bean, String existSelection, String[] selectionArgs) {
        List params = new ArrayList();
        String sql = getUpdateSql(bean, params);
        if(existSelection != null && !"".equals(existSelection)) {
            String existSql = "select * from "+bean.getTableName()+" where "+existSelection;
            logger.info("insret: existSql="+existSql);
            return executeUpdate(sql, params, existSql, selectionArgs);
        }
        return executeUpdate(sql, params);
    }
    public static int update(SqlBean bean) {
        return update(bean, null, null);
    }
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static String getUpdateSql(SqlBean bean, List params) {
        try {
            String tableName = bean.getTableName();
            String primaryKeyColumnName = bean.getPrimaryKeyColumnName();
            String[] columns = bean.getColumnNames();

            Object primaryKeyObj = null;
            StringBuilder sql = new StringBuilder();
            sql.append("update ");
            sql.append(tableName);
            sql.append(" set ");
            Class cla = bean.getClass();
            for(String column : columns) {
                String getMethodName = getGetMethodName(column);
                Method getMethod = cla.getMethod(getMethodName);
                Object obj = getMethod.invoke(bean);
                if(obj != null) {
                    if(primaryKeyColumnName.equalsIgnoreCase(column)) {
                        primaryKeyObj = obj;
                    } else {
                        sql.append(column);
                        sql.append("=?,");
                        params.add(obj);
                    }
                }
            }
            if(primaryKeyObj != null && sql.charAt(sql.length()-1) == ',') {
                sql.deleteCharAt(sql.length()-1);
                sql.append(" where ");
                sql.append(primaryKeyColumnName);
                sql.append("=?");
                params.add(primaryKeyObj);
                logger.info("updateSql = "+sql.toString());
                return sql.toString();
            }
            return null;
        } catch(Exception e) {
            logger.log(Level.FINE, e.toString(), e);
            return null;
        }
    }

    /******************************* insert ******************************************/
    @SuppressWarnings("rawtypes")
    public static long insert(SqlBean bean, String existSelection, String[] selectionArgs) {
        List params = new ArrayList();
        String sql = getInsertSql(bean, params);
        if(existSelection != null && !"".equals(existSelection)) {
            String existSql = "select * from "+bean.getTableName()+" where "+existSelection;
            logger.info("insret: existSql="+existSql);
            return execute(sql, params, existSql, selectionArgs);
        }

        return execute(sql, params);
    }
    public static long insert(SqlBean bean) {
        return insert(bean, null, null);
    }
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static String getInsertSql(SqlBean bean, List params) {
        try {
            String tableName = bean.getTableName();
            String[] columns = bean.getColumnNames();

            StringBuilder sql = new StringBuilder();
            sql.append("insert into ");
            sql.append(tableName);
            sql.append("(");
            Class cla = bean.getClass();
            for(String column : columns) {
                String getMethodName = getGetMethodName(column);
                Method getMethod = cla.getMethod(getMethodName);
                Object obj = getMethod.invoke(bean);
                if(obj != null) {
                    sql.append(column);
                    sql.append(",");
                    params.add(obj);
                }
            }
            if(sql.charAt(sql.length()-1) == ',') {
                sql.deleteCharAt(sql.length()-1);
                sql.append(") values(");
                for(int i=0;i<params.size();i++) {
                    if(i==params.size()-1) {
                        sql.append("?)");
                    } else {
                        sql.append("?,");
                    }
                }
                logger.info("InsertSql = "+sql.toString());
                return sql.toString();
            }
            return null;
        } catch(Exception e) {
            logger.log(Level.FINE, e.toString(), e);
            return null;
        }
    }

    /******************************* delete ******************************************/
    @SuppressWarnings("rawtypes")
    public static int delete(SqlBean bean) {
        List params = new ArrayList();
        String sql = getDeleteSql(bean, params);
        return executeUpdate(sql, params);
    }
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static String getDeleteSql(SqlBean bean, List params) {
        try {
            String tableName = bean.getTableName();
            String primaryKeyColumnName = bean.getPrimaryKeyColumnName();

            StringBuilder sql = new StringBuilder();
            sql.append("delete from ");
            sql.append(tableName);
            sql.append(" where ");
            sql.append(primaryKeyColumnName);
            sql.append("=?");
            Class cla = bean.getClass();
            String getMethodName = getGetMethodName(primaryKeyColumnName);
            Method getMethod = cla.getMethod(getMethodName);
            Object primaryKeyObj = getMethod.invoke(bean);
            if(primaryKeyObj != null) {
                params.add(primaryKeyObj);
                logger.info("updateSql = "+sql.toString());
                return sql.toString();
            }
            return null;
        } catch(Exception e) {
            logger.log(Level.FINE, e.toString(), e);
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    public static int delete(List<? extends SqlBean> beanlist) {
        if(beanlist!=null && beanlist.size()>0) {
            List params = new ArrayList();
            String sqls = getDeleteSqls(beanlist, params);
            return executeUpdate(sqls, params);
        }
        return -1;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static String getDeleteSqls(List<? extends SqlBean> beanlist, List params) {
        try {
            SqlBean bean = beanlist.get(0);
            String tableName = bean.getTableName();
            String primaryKeyColumnName = bean.getPrimaryKeyColumnName();

            StringBuilder sql = new StringBuilder();
            sql.append("delete from ");
            sql.append(tableName);
            sql.append(" where ");
            sql.append(primaryKeyColumnName);
            sql.append(" in(");
            Class cla = bean.getClass();
            String getMethodName = getGetMethodName(primaryKeyColumnName);
            Method getMethod = cla.getMethod(getMethodName);
            Object primaryKeyObj = null;
            for(int i=0;i<beanlist.size();i++) {
                bean = beanlist.get(i);
                primaryKeyObj = getMethod.invoke(bean);
                if(i!=beanlist.size()-1) {
                    sql.append("?,");
                } else {
                    sql.append("?)");
                }
                params.add(primaryKeyObj);
            }
            logger.info("updateSqls = "+sql.toString());
            return sql.toString();
        } catch(Exception e) {
            logger.log(Level.FINE, e.toString(), e);
            return null;
        }
    }


    /******************************* others ******************************************/

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Object getValueByColumnName(SqlBean bean, String columnName) {
        Object obj = null;
        try {
            Class cla = bean.getClass();
            String getMethodName = getGetMethodName(columnName);
            Method getMethod = cla.getMethod(getMethodName);
            obj = getMethod.invoke(bean);
        } catch(Exception e) {
            logger.log(Level.FINE, e.toString(), e);
        }
        return obj;
    }

    private static String getSetMethodName(String fieldName) {
        StringBuilder sb = new StringBuilder();
        sb.append("set");
        if(fieldName!=null && fieldName.length()>0) {
            fieldName = fieldName.replaceAll("_+", "_");
            int start = 0;
            if(fieldName.charAt(0) == '_') {
                start = 1;
            }
            int end = fieldName.indexOf("_", start);
            while(end > 0) {
                sb.append(Character.toUpperCase(fieldName.charAt(start)));
                sb.append(fieldName.substring(start+1, end));
                start = end+1;
                end = fieldName.indexOf("_", start);
            }
            if(start < fieldName.length()) {
                sb.append(Character.toUpperCase(fieldName.charAt(start)));
                sb.append(fieldName.substring(start+1));
            }
        }
        return sb.toString();
    }

    private static String getGetMethodName(String fieldName) {
        StringBuilder sb = new StringBuilder();
        sb.append("get");
        if(fieldName!=null && fieldName.length()>0) {
            fieldName = fieldName.replaceAll("_+", "_");
            int start = 0;
            if(fieldName.charAt(0) == '_') {
                start = 1;
            }
            int end = fieldName.indexOf("_", start);
            while(end > 0) {
                sb.append(Character.toUpperCase(fieldName.charAt(start)));
                sb.append(fieldName.substring(start+1, end));
                start = end+1;
                end = fieldName.indexOf("_", start);
            }
            if(start < fieldName.length()) {
                sb.append(Character.toUpperCase(fieldName.charAt(start)));
                sb.append(fieldName.substring(start+1));
            }
        }
        return sb.toString();
    }

    public static void main(String []args) {

        Connection conn = DBUtils.openConnection();

    }
}
