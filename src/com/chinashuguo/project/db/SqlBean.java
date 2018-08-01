package com.chinashuguo.project.db;

public interface SqlBean {
	String getTableName();
	String getPrimaryKeyColumnName();
	String[] getColumnNames();
}
