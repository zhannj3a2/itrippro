package cn.itrip.auth.service;

import cn.itrip.beans.pojo.ItripUser;
import cn.itrip.common.MD5;
import cn.itrip.common.RedisAPI;
import com.alibaba.fastjson.JSON;
import nl.bitwalker.useragentutils.UserAgent;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Service("tokenService")
public class TokenServiceImpl implements TokenService {
  @Resource
   private RedisAPI redisAPI;
  public void save(String token,ItripUser user) throws Exception {
      if (token.startsWith("token:PC-")) {
          redisAPI.set(token, 2 * 60 * 60, JSON.toJSONString(user));//user信息要序列化，转化成json数据
      } else {
            redisAPI.set(token,JSON.toJSONString(user));
      }
  }
    public String generateToken(String userAgent, ItripUser user) throws  Exception{
        StringBuffer str=new StringBuffer();
        str.append("token:");
        UserAgent agent=UserAgent.parseUserAgentString(userAgent);
        if(agent.getOperatingSystem().isMobileDevice()){//判断它是不是移动设备
            str.append("MOBILE-");
        }
        else {
            str.append("PC-");
        }
        str.append(MD5.getMd5(user.getUserCode(),32)+"-");
        str.append(user.getId().toString()+"-");
       str.append(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+"-");
        str.append(MD5.getMd5(userAgent,6));
     return  str.toString();
    }

    @Override
    public Boolean validate(String token, String userAgent) throws Exception {
         if(!redisAPI.exist(token)){
             return  false;
         }
         String agentMD5=token.split("-")[4];
         if(!agentMD5.equals(MD5.getMd5(userAgent,6))){
             return false;
         }
        return true;
    }

    @Override
    public void delete(String token) throws Exception{
        redisAPI.delete(token);
    }
    private long protectedTime=30*60;
    private int delay=2*60;
    @Override
    public String reloadToken(String agent, String token) throws Exception{
      //1.验证token是否有效
        if(!redisAPI.exist(token)){
            throw new Exception("token无效");
        }
        //2.能不能置换token
            Date genTime=new SimpleDateFormat("yyyyMMddHHmmss").parse(token.split("-")[3]);
            long passed= Calendar.getInstance().getTimeInMillis()-genTime.getTime();
            if(passed<protectedTime){
                throw new Exception("token在保护期,不能置换,剩余"+(protectedTime-passed)/1000);
            }
        //3.进行转换
            String user=redisAPI.get(token);
            ItripUser itripUser=JSON.parseObject(user,ItripUser.class);
            String newToken=this.generateToken(user,itripUser);
        //4.旧的token延时过期
            redisAPI.set(token,delay,user);
        //5.新的token保存在Redis
          this.save(newToken,itripUser);
        return newToken;
    }
}
