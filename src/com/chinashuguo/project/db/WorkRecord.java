package com.chinashuguo.project.db ;
import com.chinashuguo.project.db.SqlBean;
import java.sql.Timestamp;
import java.sql.Timestamp;
import java.sql.Timestamp;
import java.sql.Timestamp;
import java.sql.Timestamp;


public class WorkRecord implements SqlBean {
@Override
public String getTableName() {
	return "work_record";
}
@Override
public String getPrimaryKeyColumnName() {
	return "id";
}
@Override
public String[] getColumnNames() {
	return new String[]{
"id",
"project_id",
"user_id",
"start_time",
"end_time",
"work_date",
"work_hours",
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
sb.append(",projectId=");	
sb.append(projectId);	
sb.append(",userId=");	
sb.append(userId);	
sb.append(",startTime=");	
sb.append(startTime);	
sb.append(",endTime=");	
sb.append(endTime);	
sb.append(",workDate=");	
sb.append(workDate);	
sb.append(",workHours=");	
sb.append(workHours);	
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
	private Long projectId;
	private Long userId;
	private java.sql.Timestamp startTime;
	private java.sql.Timestamp endTime;
	private java.sql.Timestamp workDate;
	private float workHours;
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
	
	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}
	
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	public java.sql.Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(java.sql.Timestamp startTime) {
		this.startTime = startTime;
	}
	
	public java.sql.Timestamp getEndTime() {
		return endTime;
	}

	public void setEndTime(java.sql.Timestamp endTime) {
		this.endTime = endTime;
	}
	
	public java.sql.Timestamp getWorkDate() {
		return workDate;
	}

	public void setWorkDate(java.sql.Timestamp workDate) {
		this.workDate = workDate;
	}
	
	public float getWorkHours() {
		return workHours;
	}

	public void setWorkHours(float workHours) {
		this.workHours = workHours;
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
