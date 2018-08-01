package com.chinashuguo.project.db ;
import com.chinashuguo.project.db.SqlBean;
import java.sql.Timestamp;
import java.sql.Timestamp;


public class VUserInfo implements SqlBean {
@Override
public String getTableName() {
	return "v_user_info";
}
@Override
public String getPrimaryKeyColumnName() {
	return "id";
}
@Override
public String[] getColumnNames() {
	return new String[]{
"id",
"username",
"password",
"username_str",
"depart_id",
"level_num",
"create_by",
"create_date",
"update_by",
"update_date",
"depart_name"};
}
@Override
public String toString() {
	StringBuilder sb = new StringBuilder();
	
sb.append("id=");	
sb.append(id);	
sb.append(",username=");	
sb.append(username);	
sb.append(",password=");	
sb.append(password);	
sb.append(",usernameStr=");	
sb.append(usernameStr);	
sb.append(",departId=");	
sb.append(departId);	
sb.append(",levelNum=");	
sb.append(levelNum);	
sb.append(",createBy=");	
sb.append(createBy);	
sb.append(",createDate=");	
sb.append(createDate);	
sb.append(",updateBy=");	
sb.append(updateBy);	
sb.append(",updateDate=");	
sb.append(updateDate);	
sb.append(",departName=");	
sb.append(departName);
	return sb.toString();
}
	private Long id;
	private String username;
	private String password;
	private String usernameStr;
	private Long departId;
	private Integer levelNum;
	private String createBy;
	private java.sql.Timestamp createDate;
	private String updateBy;
	private java.sql.Timestamp updateDate;
	private String departName;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getUsernameStr() {
		return usernameStr;
	}

	public void setUsernameStr(String usernameStr) {
		this.usernameStr = usernameStr;
	}
	
	public Long getDepartId() {
		return departId;
	}

	public void setDepartId(Long departId) {
		this.departId = departId;
	}
	
	public Integer getLevelNum() {
		return levelNum;
	}

	public void setLevelNum(Integer levelNum) {
		this.levelNum = levelNum;
	}
	
	public String getCreateBy() {
		return createBy;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}
	
	public java.sql.Timestamp getCreateDate() {
		return createDate;
	}

	public void setCreateDate(java.sql.Timestamp createDate) {
		this.createDate = createDate;
	}
	
	public String getUpdateBy() {
		return updateBy;
	}

	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}
	
	public java.sql.Timestamp getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(java.sql.Timestamp updateDate) {
		this.updateDate = updateDate;
	}
	
	public String getDepartName() {
		return departName;
	}

	public void setDepartName(String departName) {
		this.departName = departName;
	}
}
