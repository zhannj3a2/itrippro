package cn.itrip.auth.service;

public interface SmsService  {
    public void send(String To,String templateId,String[] datas)throws Exception;
}
