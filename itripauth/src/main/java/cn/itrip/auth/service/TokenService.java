package cn.itrip.auth.service;

import cn.itrip.beans.pojo.ItripUser;

public interface TokenService {
public void save(String token,ItripUser user) throws Exception;//将Token保存在session
public String generateToken(String userAgent, ItripUser user) throws  Exception;//生成token
public Boolean validate(String token,String userAgent) throws Exception;
public void delete(String token)throws Exception;
public String reloadToken(String agent,String token)throws Exception;//置换token
}
