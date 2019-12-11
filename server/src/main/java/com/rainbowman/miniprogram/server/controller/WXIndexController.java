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


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPObject;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.rainbowman.miniprogram.server.service.WXIndexService;
import com.rainbowman.miniprogram.server.utils.AesUtil;
import com.rainbowman.miniprogram.server.utils.FileUtil;
import com.rainbowman.miniprogram.server.utils.HttpRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/WXIndex")
public class WXIndexController {
    //springboot自带日志系统(slf4j+logback)引入starterweb 即可自动引入所需包
    private static final Logger LOG = LoggerFactory.getLogger(WXIndexController.class);
    //小程序唯一标识   (在微信小程序管理后台获取)
    private static final String wxspAppid = "wx1da383f6062172ae";
    //小程序的 app secret (在微信小程序管理后台获取)
    private static final String wxspSecret = "b4cbdf24d4af736fe5b12f79fdca144c";

    @Autowired
    private WXIndexService wXIndexService;


    @RequestMapping("/test")
    @ResponseBody  //自动返回格式化json数据，springboot自带jackson包可自动化
    public List<Map<String, Object>> getDeviceAlarmData(HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.error("进入test方法");
        List<Map<String, Object>> result = wXIndexService.getAll(new HashMap<>());
        System.out.println(result.toString());
        return result;
    }


    @ResponseBody
    @RequestMapping(value = "/getOpenid", method = RequestMethod.POST)
    public Map getOpenid(@RequestParam("code") String code, String userInfo) throws Exception {
        Map<String, String> data = new HashMap<>();
        try {
            System.out.println("welcome");
            System.out.println("code：" + code);
            System.out.println("userinfo：" + userInfo.toString());
            if (userInfo.length() <= 0) {
                data.put("respcode", "15");
                throw new RuntimeException();
            }
            String uid = getWXId(code).getString("openid");
            insertUser(userInfo, uid);
            data.put("uid", uid);
            data.put("respcode", "0");
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("出错", e);
            data.put("respcode", "15");
        }
        return data;
    }

    public void insertUser(String userInfo, String uid) {
        boolean flag = false;
        List<Map<String, Object>> allUser = wXIndexService.getAll(new HashMap<>());
        for (Map<String, Object> stringObjectMap : allUser) {
            for (Map.Entry<String, Object> a : stringObjectMap.entrySet()) {
                if (a.getKey().toString().equalsIgnoreCase("openId")) {
                    if (a.getValue().toString().equalsIgnoreCase(uid)) {
                        flag = true;
                    }
                }
            }
        }
        if (!flag) {
            JSONObject jsons = JSON.parseObject(userInfo);
            jsons.put("openId", uid);
            jsons.put("unionId", "");
            jsons.put("recentdate", new Date());
            Map<String, Object> inserts = StringToMap(jsons);
            wXIndexService.insert(inserts);
        }
    }

    public Map<String, Object> StringToMap(JSONObject object) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (Object k : object.keySet()) {
            Object v = object.get(k);
            map.put(k.toString(), v);
        }
        return map;
    }


    public Map getUser(String code, String encryptedData, String iv) {
        Map<String, Object> map = new HashMap<String, Object>();
        JSONObject json = getWXId(code);
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
                Map<String, Object> userInfo = new HashMap<String, Object>();
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
            LOG.error("解密失败", e);
        }
        map.put("status", 0);
        map.put("msg", "解密失败");
        System.out.println("map3:" + map);
        return map;
    }

    public JSONObject getWXId(String code) {
        LOG.error("调用getid方法");

        //code = "081ZExyD0qnP4j2LV5yD0hFLyD0ZExyK";
        //登录凭证不能为空
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
        return json;
    }

    @ResponseBody
    @RequestMapping(value = "/fileUploadN", method = RequestMethod.POST)
    public String uploadMusicFile(HttpServletRequest request, @RequestParam("file") MultipartFile[] files) {
        LOG.info("进入上传...");
        String uploadPath = "E:/pic/";//存放到本地路径（示例）
        if (files != null && files.length >= 1) {
            BufferedOutputStream bw = null;
            try {
                String fileName = files[0].getOriginalFilename();
                //判断是否有文件
                if (StringUtils.isNoneBlank(fileName)) {
                    //输出到本地路径
                    File outFile = new File(uploadPath + UUID.randomUUID().toString() + FileUtil.getFileType(fileName));
                    LOG.info("path==" + uploadPath + UUID.randomUUID().toString() + FileUtil.getFileType(fileName));
                    // FileUtils.copyInputStreamToFile(files[0].getInputStream(), outFile);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (bw != null) {
                        bw.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "success";
    }


    @ResponseBody
    @RequestMapping(value = "/fileUpload", method = RequestMethod.POST)
    public String uploadPicture(HttpServletRequest request, HttpServletResponse response) throws IOException {
        MultipartHttpServletRequest req = (MultipartHttpServletRequest) request;

        //对应前端的upload的name参数"file"
        MultipartFile multipartFile = req.getFile("file");

        //realPath填写电脑文件夹路径
        String realPath = "E:/pic/";

        //格式化时间戳
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
        String nowTime = sdf.format(new Date().getTime());

        //裁剪用户id
        String originalFirstName = multipartFile.getOriginalFilename();
        String picFirstName = originalFirstName.substring(0, originalFirstName.indexOf("."));

        //取得图片的格式后缀
        String originalLastName = multipartFile.getOriginalFilename();
        String picLastName = originalLastName.substring(originalLastName.lastIndexOf("."));

        //拼接：名字+时间戳+后缀
        String picName = picFirstName + "." + nowTime + picLastName;
        try {
            File dir = new File(realPath);
            //如果文件目录不存在，创建文件目录
            if (!dir.exists()) {
                dir.mkdir();
                System.out.println("创建文件目录成功：" + realPath);
            }
            File file = new File(realPath, picName);
            multipartFile.transferTo(file);
            LOG.info("上传图片目录为" + realPath + picName);
            return realPath + picName;
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error("上传失败", e);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            LOG.error("上传失败", e);
        }
        return null;
    }


    @ResponseBody
    @RequestMapping(value = "/saveInfo", method = RequestMethod.POST)
    public Map saveInfo(HttpServletRequest request, HttpServletResponse response, String user_id, String content, String images, String latitude, String longitude) throws IOException {
        Map<String, Object> paramMap = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        try {
            paramMap.put("openid", user_id);
            paramMap.put("description", content);
            paramMap.put("images", images);
            paramMap.put("latitude", latitude);
            paramMap.put("longitude", longitude);
            paramMap.put("iconpath", "");
            wXIndexService.insertInfo(paramMap);
            System.out.println(user_id + "//" + content + "//" + images);
            result.put("status", "0");
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("保存出错", e);
            result.put("status", "15");
        }
        return result;

    }


    @RequestMapping("/getImages/{imageurl}")
    public void getImages(HttpServletRequest request, HttpServletResponse response, @PathVariable("imageurl") String imageurl) throws Exception {
        LOG.error("getImages");
        String url = "E:/pic/" + imageurl;
        File file = null;
        FileInputStream fis = null;
        try {
            file = new File(url);
            if (!file.exists()) {
                return;
            }
            fis = new FileInputStream(file);
            final byte[] buf = new byte[1024];
            while (fis.read(buf) > 0) {
                response.getOutputStream().write(buf);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            LOG.error("读取资源失败", e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    fis = null;
                }
            }
            response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        }

    }

    //获取markers
    @ResponseBody
    @RequestMapping(value = "/getRecentHelp", method = RequestMethod.POST)
    public Map<String, Object> getRecentHelp(String latitude, String longitude) {
        Map<String,Object> resultss=new HashMap<>();
        List<Map<String, Object>> results = new ArrayList<>();
        List<Map<String, Object>> gets = wXIndexService.getAllInfo(new HashMap<>());
        for (Map<String, Object> stringObjectMap : gets) {
            Map<String, Object> result =  new HashMap<>();
            Map<String, Object> resultChild =  new HashMap<>();
            result.put("id",stringObjectMap.get("id"));
            result.put("latitude",stringObjectMap.get("latitude"));
            result.put("longitude",stringObjectMap.get("longitude"));
            result.put("iconPath","");
            resultChild.put("content",stringObjectMap.get("description").toString());//stringObjectMap.get("description").toString().substring(0,2)+"..."
            resultChild.put("bgColor","#fff");
            resultChild.put("padding","5px");
            resultChild.put("borderRadius","2px");
            resultChild.put("borderWidth","1px");
            resultChild.put("borderColor","#673bb7");
            result.put("callout",resultChild);
            results.add(result);
        }
        resultss.put("markers",results);
        return resultss;

    }


}



