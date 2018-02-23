package user.normal.bean;

import java.util.Date;

import javax.ws.rs.FormParam;
import javax.ws.rs.PathParam;

public class CmdcUser {
	@PathParam("userId")
    private Integer userId;
	@FormParam("leagueNo")
    private String leagueNo;
	@FormParam("userName")
    private String userName;
	@FormParam("phoneNumber")
    private String phoneNumber;
	@FormParam("password")
    private String password;
	@FormParam("email")
    private String email;
	@FormParam("sex")
    private String sex;
	@FormParam("avatarPath")
    private String avatarPath;
	@FormParam("nickname")
    private String nickname;
	@FormParam("realName")
    private String realName;
	@FormParam("birthday")
    private String birthday;
	@FormParam("autograph")
    private String autograph;
	@FormParam("roleType")
    private String roleType;
	@FormParam("createTime")
    private Date createTime;
	@FormParam("updateTime")
    private Date updateTime;
	@FormParam("loginTime")
    private Date loginTime;
	@FormParam("logoutTime")
    private Date logoutTime;
	@FormParam("reserveField1")
    private String reserveField1;
	@FormParam("reserveField2")
    private String reserveField2;
	@FormParam("reserveField3")
    private String reserveField3;
	@FormParam("reserveField4")
    private String reserveField4;
	@FormParam("reserveField5")
    private String reserveField5;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getLeagueNo() {
        return leagueNo;
    }

    public void setLeagueNo(String leagueNo) {
        this.leagueNo = leagueNo == null ? null : leagueNo.trim();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName == null ? null : userName.trim();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber == null ? null : phoneNumber.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex == null ? null : sex.trim();
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath == null ? null : avatarPath.trim();
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname == null ? null : nickname.trim();
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName == null ? null : realName.trim();
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getAutograph() {
        return autograph;
    }

    public void setAutograph(String autograph) {
        this.autograph = autograph == null ? null : autograph.trim();
    }

    public String getRoleType() {
        return roleType;
    }

    public void setRoleType(String roleType) {
        this.roleType = roleType == null ? null : roleType.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
    }

    public Date getLogoutTime() {
        return logoutTime;
    }

    public void setLogoutTime(Date logoutTime) {
        this.logoutTime = logoutTime;
    }

    public String getReserveField1() {
        return reserveField1;
    }

    public void setReserveField1(String reserveField1) {
        this.reserveField1 = reserveField1 == null ? null : reserveField1.trim();
    }

    public String getReserveField2() {
        return reserveField2;
    }

    public void setReserveField2(String reserveField2) {
        this.reserveField2 = reserveField2 == null ? null : reserveField2.trim();
    }

    public String getReserveField3() {
        return reserveField3;
    }

    public void setReserveField3(String reserveField3) {
        this.reserveField3 = reserveField3 == null ? null : reserveField3.trim();
    }

    public String getReserveField4() {
        return reserveField4;
    }

    public void setReserveField4(String reserveField4) {
        this.reserveField4 = reserveField4 == null ? null : reserveField4.trim();
    }

    public String getReserveField5() {
        return reserveField5;
    }

    public void setReserveField5(String reserveField5) {
        this.reserveField5 = reserveField5 == null ? null : reserveField5.trim();
    }
}