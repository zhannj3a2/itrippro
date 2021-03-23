package cn.itrip.auth.service;

import cn.itrip.beans.pojo.ItripUser;
import cn.itrip.common.MD5;
import cn.itrip.common.RedisAPI;
import cn.itrip.dao.user.ItripUserMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("userService")
public class UserServiceImpl implements  UserService {
    @Resource
    private ItripUserMapper itripUserMapper;
    @Resource
    private MailService mailService;
    @Resource
    private RedisAPI redisAPI;
    @Resource
    private SmsService smsService;
    public void itriptxCreateUser(ItripUser user) throws Exception {
        //1.添加用户信息
        itripUserMapper.insertItripUser(user);
        //2.生成激活码
        String activationCode = MD5.getMd5(new Date().toLocaleString(), 32);
        //3.发送邮箱
        mailService.sendActivationMail(user.getUserCode(), activationCode);
        //4.激活码存在redis
        redisAPI.set("activation:" + user.getUserCode(), 30 * 60, activationCode);
    }

    @Override
    public void itriptxCreateUserByPhone(ItripUser user) throws Exception {
      //1.创建用户
        itripUserMapper.insertItripUser(user);
        //2.生成验证码
        int code=MD5.getRandomCode();
        //3.发送验证码
        smsService.send(user.getUserCode(),"1",new String[]{String.valueOf(code),"1"});
        //4.缓存验证码到Redis中
        redisAPI.set("activation:" + user.getUserCode(), 5* 60, String.valueOf(code));
    }
    public boolean activatePhone(String phone, String code) throws Exception{
        //1.比对验证码
        String key="activation:" +phone;
        String value=redisAPI.get(key);

        if(value!=null&&value.equals(code)){
        ItripUser itripUser=this.findUserByUserCode(phone);
        if(itripUser!=null){
            //2.更新用户激活状态
            itripUser.setFlatID(itripUser.getId());
            itripUser.setUserType(0);
            itripUser.setActivated(1);
            itripUserMapper.updateItripUser(itripUser);
            return  true;
        }
        }
        return false;
    }
    @Override
    public boolean activate(String mail, String code) throws Exception {
        //1.验证激活码
        String value = redisAPI.get("activation:" + mail);
        if (value.equals(code)) {
            Map<String, Object> map = new HashMap<>();
            map.put("userCode", mail);
            List<ItripUser> itripUsers = itripUserMapper.getItripUserListByMap(map);
            if (itripUsers.size() > 0) {
                ItripUser user = itripUsers.get(0);
                user.setActivated(1);//已激活
                user.setUserType(0);//已注册用户
                user.setFlatID(user.getId());
                //2.更新用户
                return true;
            }

        } else {
            return false;
        }
        return false;
    }

    @Override
    public ItripUser findUserByUserCode(String userCode) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("userCode", userCode);
        List<ItripUser> itripUsers = itripUserMapper.getItripUserListByMap(map);
        if (itripUsers.size() > 0) {
            return itripUsers.get(0);
        }else{
            return null;
        }
        }
    }
