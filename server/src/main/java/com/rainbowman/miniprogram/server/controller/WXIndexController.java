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
//  @date 2019/12/5 0005 14:43
// 

package com.rainbowman.miniprogram.server.controller;


import com.alibaba.fastjson.JSONObject;
import com.rainbowman.miniprogram.server.service.WXIndexService;
import com.rainbowman.miniprogram.server.utils.AesUtil;
import com.rainbowman.miniprogram.server.utils.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/WXIndex")
public class WXIndexController {
    //springboot自带日志系统(slf4j+logback)引入starterweb 即可自动引入所需包
    private static final Logger LOG = LoggerFactory.getLogger(WXIndexController.class);
    //小程序唯一标识   (在微信小程序管理后台获取)
    private static final String wxspAppid="sfsdf";
    //小程序的 app secret (在微信小程序管理后台获取)
    private static final String wxspSecret="sdfsdf";

    @Autowired
    private WXIndexService wXIndexService;


    @RequestMapping("/test")
    @ResponseBody  //自动返回格式化json数据，springboot自带jackson包可自动化
    public List<Map<String,Object>> getDeviceAlarmData(HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.error("进入test方法");
        List<Map<String,Object>> result= wXIndexService.getAll(new HashMap<>());
        System.out.println(result.toString());
        return result;
    }


    @ResponseBody
    @RequestMapping(value = "/getOpenid", method = RequestMethod.GET)
    public Map getOpenid(String code, String encryptedData, String iv ) throws Exception{ return getUser(code,encryptedData,iv);
    }


    public Map getUser(String code, String encryptedData, String iv){
        LOG.error("调用getOpenid方法");
        Map<String,Object> map = new HashMap<String,Object>();
        //code = "081ZExyD0qnP4j2LV5yD0hFLyD0ZExyK";
        //登录凭证不能为空
        if (code == null || code.length() == 0) {
            map.put("status", 0);
            map.put("msg", "code 不能为空");
            System.out.println("map1:" + map);
            return map;
        }
        String wxspAppid = WXIndexController.wxspAppid;
        String wxspSecret = WXIndexController.wxspSecret;
        //授权（必填）
        String grant_type = "authorization_code";
        //////////////// 1、向微信服务器 使用登录凭证 code 获取 session_key 和 openid ////////////////
        //请求参数
        String params = "appid=" + wxspAppid + "&secret=" + wxspSecret + "&js_code=" + code + "&grant_type=" + grant_type;
        //发送请求
        String sr = HttpRequest.sendGet("https://api.weixin.qq.com/sns/jscode2session", params);
        //解析相应内容（转换成json对象）
        JSONObject json = JSONObject.parseObject(sr);
        //获取会话密钥（session_key）
        String session_key = json.get("session_key").toString();
        //用户的唯一标识（openid）
        String openid = (String) json.get("openid");
        System.out.println("openid:" + openid);
        //////////////// 2、对encryptedData加密数据进行AES解密 ////////////////
        try {
            String result = AesUtil.decrypt(encryptedData, session_key, iv, "UTF-8");
            if (null != result && result.length() > 0) {
                map.put("status", 1);
                map.put("msg", "解密成功");

                JSONObject userInfoJSON = JSONObject.parseObject(result);
                Map<String,Object> userInfo = new HashMap<String,Object>();
                userInfo.put("openId", userInfoJSON.get("openId"));
                userInfo.put("nickName", userInfoJSON.get("nickName"));
                userInfo.put("gender", userInfoJSON.get("gender"));
                userInfo.put("city", userInfoJSON.get("city"));
                userInfo.put("province", userInfoJSON.get("province"));
                userInfo.put("country", userInfoJSON.get("country"));
                userInfo.put("avatarUrl", userInfoJSON.get("avatarUrl"));
                userInfo.put("unionId", userInfoJSON.get("unionId"));
                map.put("userInfo", userInfo);
                System.out.println("map2:" + map);
                return map;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("解密失败",e);
        }
        map.put("status", 0);
        map.put("msg", "解密失败");
        System.out.println("map3:" + map);
        return map;
    }
}



