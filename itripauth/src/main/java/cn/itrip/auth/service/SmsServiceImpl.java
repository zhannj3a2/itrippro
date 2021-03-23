package cn.itrip.auth.service;

import com.cloopen.rest.sdk.CCPRestSmsSDK;

import java.util.HashMap;

public class SmsServiceImpl implements SmsService {
    @Override
    public void send(String To, String templateId, String[] datas) throws Exception {
        CCPRestSmsSDK sdk=new CCPRestSmsSDK();
        sdk.init("app.cloopen.com","8883");
        sdk.setAccount("8a216da8681259360168262f3e870841","390492fd1028433b8c304c3d32c61824");
        sdk.setAppId("8a216da8681259360168262f3ed30847");
       HashMap result= sdk.sendTemplateSMS(To,templateId,datas);
       if(result.get("statusCode").equals("000000")){
           System.out.println("短信发送成功");
      }
      else{
           throw  new Exception(result.get("statusCode").toString()+":"+result.get("statusMsg").toString());
       }
    }
}
