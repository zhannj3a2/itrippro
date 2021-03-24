package cn.itrip.auth.service;

import cn.itrip.beans.pojo.ItripUser;

public interface UserService {
public void itriptxCreateUser(ItripUser user)throws Exception;//创建用户
    public void itriptxCreateUserByPhone(ItripUser user)throws Exception;//通过手机创建用户
public boolean activate(String mail,String code)throws Exception;//激活验证码
public ItripUser findUserByUserCode(String userCode) throws Exception;
    public boolean activatePhone(String phone, String code) throws Exception;
    public ItripUser login(String userCode,String userPassword) throws Exception;
}
