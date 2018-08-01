package com.chinashuguo.project.db ;
import com.chinashuguo.project.db.SqlBean;
import java.sql.Timestamp;
import java.sql.Timestamp;


public class ProjectInfo implements SqlBean {
@Override
public String getTableName() {
	return "project_info";
}
@Override
public String getPrimaryKeyColumnName() {
	return "id";
}
@Override
public String[] getColumnNames() {
	return new String[]{
"id",
"project_name",
"project_desc",
"project_owner_id",
"create_by",
"create_date",
"update_by",
"update_date"};
}
@Override
public String toString() {
	StringBuilder sb = new StringBuilder();
	
sb.append("id=");	
sb.append(id);	
sb.append(",projectName=");	
sb.append(projectName);	
sb.append(",projectDesc=");	
sb.append(projectDesc);	
sb.append(",projectOwnerId=");	
sb.append(projectOwnerId);	
sb.append(",createBy=");	
sb.append(createBy);	
sb.append(",createDate=");	
sb.append(createDate);	
sb.append(",updateBy=");	
sb.append(updateBy);	
sb.append(",updateDate=");	
sb.append(updateDate);
	return sb.toString();
}
	private Long id;
	private String projectName;
	private String projectDesc;
	private Long projectOwnerId;
	private String createBy;
	private java.sql.Timestamp createDate;
	private String updateBy;
	private java.sql.Timestamp updateDate;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
	public String getProjectDesc() {
		return projectDesc;
	}

	public void setProjectDesc(String projectDesc) {
		this.projectDesc = projectDesc;
	}
	
	public Long getProjectOwnerId() {
		return projectOwnerId;
	}

	public void setProjectOwnerId(Long projectOwnerId) {
		this.projectOwnerId = projectOwnerId;
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
}
