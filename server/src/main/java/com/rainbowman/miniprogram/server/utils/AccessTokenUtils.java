//
//                    _ooOoo_
//                   o8888888o
//                   88" . "88
//                   (| -_- |)
//                    O\ = /O
//                ____/`---'\____
//                . ' \\| |// `.
//               / \\||| : |||// \
//             / _||||| -:- |||||- \
//              | | \\\ - /// | |
//             | \_| ''\---/'' | |
//              \ .-\__ `-` ___/-. /
//           ___`. .' /--.--\ `. . __
//        ."" '< `.___\_<|>_/___.' >'  "".
//       | | : `- \`.;`\ _ /`;.`/ - ` : | |
//         \ \ `-. \_ __\ /__ _/ .-` / /
// ======`-.____`-.___\_____/___.-`____.-'======
//                    `=---='
//
// .............................................
//         佛祖镇楼             BUG避易
//
//  佛曰:
//         写字楼里写字间，写字间里程序员；
//         程序人员写程序，又拿程序换酒钱。
//         酒醒只在网上坐，酒醉还来网下眠；
//         酒醉酒醒日复日，网上网下年复年。
//         但愿老死电脑间，不愿鞠躬老板前；
//         奔驰宝马贵者趣，公交自行程序员。
//
//  @author a man is so handsome
//  @花开堪折直须折 莫待无花空折枝
//  @date 2019/12/24 0024 11:52
// 

package com.rainbowman.miniprogram.server.utils;

import com.alibaba.fastjson.JSONObject;
import com.rainbowman.miniprogram.server.controller.WXIndexController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AccessTokenUtils {
    private static final Logger LOG = LoggerFactory.getLogger(AccessTokenUtils.class);
    private static Map<String,String> tokenMaps=new HashMap<>();
    private static Map<String,String> tokenTimes=new HashMap<>();
    public static boolean  putToken(String type,String value){
        tokenMaps.put(type,value);
        tokenTimes.put(type,System.currentTimeMillis()+"");
        return true;
    }

    public static String  getToken(String type){
        if(tokenTimes.containsKey(type)&&System.currentTimeMillis()-Long.parseLong(tokenTimes.get(type))>=7000*1000){
            return tokenMaps.get(type);
        }else{
         String  newToken=getAccess_token();
         putToken(type,newToken);
         return newToken;
        }
    }

    public static String getAccess_token() {
        LOG.error("调用getid方法");
        try {
            String wxspAppid = WXIndexController.wxspAppid;
            String wxspSecret = WXIndexController.wxspSecret;
            String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+ wxspAppid+ "&secret=" + wxspSecret;
            //发送请求
            String sr = HttpRequest.sendGet(url, null);
            //解析相应内容（转换成json对象）
            JSONObject json = JSONObject.parseObject(sr);
            return json.getString("access_token");
        }catch (Exception e){
            LOG.error("获取token出错");
            return null;
        }
    }


}
