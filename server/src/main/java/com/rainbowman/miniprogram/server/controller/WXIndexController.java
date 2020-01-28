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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPObject;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.github.pagehelper.PageInfo;
import com.rainbowman.miniprogram.server.config.WebSocketServer;
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

import javax.annotation.Resource;
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
    public static final String wxspAppid = "wx1da383f6062172ae";
    //小程序的 app secret (在微信小程序管理后台获取)
    public static final String wxspSecret = "b4cbdf24d4af736fe5b12f79fdca144c";
    public static final String iconpath = "/images/point.png";
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm") ;

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


    public Map<String, Object> getUserMap(List<Map<String, Object>> allUser, String uid) {
        for (Map<String, Object> stringObjectMap : allUser) {
            for (Map.Entry<String, Object> a : stringObjectMap.entrySet()) {
                if (a.getKey().toString().equalsIgnoreCase("openId")) {
                    if (a.getValue().toString().equalsIgnoreCase(uid)) {
                        return stringObjectMap;
                    }
                }
            }
        }
        return null;
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
    public Map saveInfo(HttpServletRequest request, HttpServletResponse response, String user_id, String content, String images, String latitude, String longitude, String location, String title) throws IOException {
        Map<String, Object> paramMap = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        try {
            paramMap.put("openid", user_id);
            paramMap.put("description", content);
            paramMap.put("images", images);
            paramMap.put("latitude", latitude);
            paramMap.put("longitude", longitude);
            paramMap.put("location", location);
            paramMap.put("title", title);
            paramMap.put("recentdate", new Date());
            paramMap.put("iconpath", iconpath);
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


    @RequestMapping("/getImages")
    public void getImages(HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.error("getImages");
        String url = request.getParameter("imgurl");
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
        Map<String, Object> resultss = new HashMap<>();
        List<Map<String, Object>> results = new ArrayList<>();
        List<Map<String, Object>> gets = wXIndexService.getAllInfo(new HashMap<>());
        for (Map<String, Object> stringObjectMap : gets) {
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> resultChild = new HashMap<>();
            result.put("id", stringObjectMap.get("id"));
            result.put("latitude", stringObjectMap.get("latitude"));
            result.put("longitude", stringObjectMap.get("longitude"));
            result.put("needtitle", stringObjectMap.get("title"));
            result.put("needimages", stringObjectMap.get("images"));
            result.put("needlocation", stringObjectMap.get("location"));
            result.put("needdescription", stringObjectMap.get("description"));
            result.put("needusername", getUserInfo(stringObjectMap.get("openid").toString()).get("nickName"));
            result.put("needrecentdate",dateFormat.format(stringObjectMap.get("recentdate")));
            result.put("iconPath", stringObjectMap.get("iconpath"));
            result.put("width", 35);
            result.put("height", 52.5);
            resultChild.put("content", stringObjectMap.get("title").toString());//stringObjectMap.get("description").toString().substring(0,2)+"..."
            resultChild.put("bgColor", "#fff");
            resultChild.put("padding", "5px");
            resultChild.put("borderRadius", "2px");
            resultChild.put("borderWidth", "1px");
            resultChild.put("borderColor", "#673bb7");
            result.put("callout", resultChild);
            results.add(result);
        }
        resultss.put("markers", results);
        return resultss;

    }

    //获取最近发布的信息
    @ResponseBody
    @RequestMapping(value = "/getRecentInfoByOpenid", method = RequestMethod.POST)
    public JSONObject getRecentHelp(String userid) {
        JSONObject finalresult = new JSONObject();
        try {
            finalresult.put("items", getRecentHelpByOpenid(userid));
            finalresult.put("comments", getRecentCommentByOpenid(userid));
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("操作出错", e);
        }
        return finalresult;
    }

    public JSONArray getRecentCommentByOpenid(String userid) throws Exception {
        JSONArray resultarray = new JSONArray();
        List<Map<String, Object>> results = new ArrayList<>();
        List<Map<String, Object>> gets = wXIndexService.getAllRecord(getCurrentParam("sys_comment", "openid", userid, null, null,"0", "inserttime"),null,null).getList();
        for (Map<String, Object> stringObjectMap : gets) {
            Map<String, Object> result = new HashMap<>();
            result.put("id", stringObjectMap.get("id"));
            result.put("isTouchMove", false);
            result.put("txt", stringObjectMap.get("comment"));
            result.put("markerid", stringObjectMap.get("markerid"));
            result.put("needrecentdate", dateFormat.format(stringObjectMap.get("inserttime")));
            results.add(result);
        }
        resultarray.addAll(results);
        return resultarray;
    }

    public Map<String, Object> getCurrentParam(String table, String paramone, Object valueone, String paramtwo, Object valuewtwo, String delflag, String orderby) throws Exception {
        Map<String, Object> param = new HashMap<>();
        if (StringUtils.isEmpty(table)) throw new RuntimeException("缺少table");
        param.put("table",table);
        if (!StringUtils.isEmpty(paramone)) {
            param.put("paramone", paramone);
            param.put("valueone", valueone);
        }
        if (!StringUtils.isEmpty(paramtwo)) {
            param.put("paramtwo", paramtwo);
            param.put("valuewtwo", valuewtwo);
        }
        if (!StringUtils.isEmpty(delflag)) {
            param.put("delflag", delflag);
        }
        if (!StringUtils.isEmpty(orderby)) param.put("orderby", orderby+" DESC ");
        return param;
    }

    public JSONArray getRecentHelpByOpenid(String userid) throws Exception {
        JSONArray resultarray = new JSONArray();
        List<Map<String, Object>> results = new ArrayList<>();
        List<Map<String, Object>> gets = wXIndexService.getAllInfo(new HashMap<>());
        for (Map<String, Object> stringObjectMap : gets) {
            if (!stringObjectMap.get("openid").toString().equalsIgnoreCase(userid)) {
                continue;
            }
            Map<String, Object> result = new HashMap<>();
            result.put("id", stringObjectMap.get("id"));
            result.put("isTouchMove", false);
            result.put("latitude", stringObjectMap.get("latitude"));
            result.put("longitude", stringObjectMap.get("longitude"));
            result.put("txt", stringObjectMap.get("title"));
            result.put("needtitle", stringObjectMap.get("title"));
            result.put("needimages", stringObjectMap.get("images"));
            result.put("needlocation", stringObjectMap.get("location"));
            result.put("needdescription", stringObjectMap.get("description"));
            result.put("needusername", getUserInfo(stringObjectMap.get("openid").toString()).get("nickName"));
            result.put("needrecentdate", dateFormat.format(stringObjectMap.get("recentdate")));
            results.add(result);
        }
        resultarray.addAll(results);
        return resultarray;
    }


    public Map<String, Object> getUserInfo(String oid) {
        List<Map<String, Object>> result = wXIndexService.getAll(new HashMap<>());
        for (Map<String, Object> stringObjectMap : result) {
            if (stringObjectMap.get("openId").toString().equalsIgnoreCase(oid)) {
                return stringObjectMap;
            }
        }
        return new HashMap<>();
    }


    @RequestMapping("/getComment")
    public @ResponseBody
    List<Map<String, Object>> getComment(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String pageNum = request.getParameter("pageNum");
        String pageSize = request.getParameter("pageSize");
        String sourceId = request.getParameter("sourceId");
        List<Map<String, Object>> allUser = wXIndexService.getAll(new HashMap<>());
        PageInfo<Map<String, Object>> a = wXIndexService.getAllRecord(getCurrentParam("sys_comment","markerid",sourceId,null,null,"0","inserttime"), Integer.parseInt(pageNum), Integer.parseInt(pageSize));
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> results = new ArrayList<>();
        for (Map<String, Object> stringObjectMap : a.getList()) {
            result = new HashMap<>();
            result.putAll(stringObjectMap);
            result.put("userName", getUserMap(allUser, stringObjectMap.get("openid").toString()).get("nickName"));
            result.put("userPhoto", getUserMap(allUser, stringObjectMap.get("openid").toString()).get("avatarUrl"));
            result.put("replyUserName", stringObjectMap.get("replyopenid") == null || stringObjectMap.get("replyopenid").toString().equalsIgnoreCase("") ? "" : getUserMap(allUser, stringObjectMap.get("replyopenid").toString()).get("nickName"));
            result.put("inserttime",dateFormat.format(stringObjectMap.get("inserttime")));
            results.add(result);
        }
        System.out.println(results.size());
        return results;
    }


    @RequestMapping("/insertComment")
    public @ResponseBody
    Map<String, Object> insertComment(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> result = new HashMap<>();
        try {
            String userId = request.getParameter("userId");
            String comment = request.getParameter("comment");
            String sourceId = request.getParameter("sourceId");
            String replyCommentId = request.getParameter("replyCommentId");
            String replyopenid = request.getParameter("replyopenid");
            Map<String, Object> params = new HashMap<>();
            params.put("parentid", replyCommentId);
            params.put("markerid", sourceId);
            params.put("openid", userId);
            params.put("comment", comment);
            params.put("inserttime", new Date());
            params.put("replyopenid", replyopenid);
            wXIndexService.insertComment(params);
            result.put("success", "success");
        } catch (Exception e) {
            LOG.error("插入失败", e);
            result.put("error", "error");
        }
        return result;
    }


    @RequestMapping("/deleteComment")
    public @ResponseBody
    Map<String, Object> deleteComment(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> result = new HashMap<>();
        try {
            String commentId = request.getParameter("commentId");
            Map<String, Object> params = new HashMap<>();
            wXIndexService.deleteRecord(getCurrentParam("sys_comment","id",commentId,null,null,null,null));
            result.put("success", "success");
        } catch (Exception e) {
            LOG.error("删除失败", e);
            result.put("error", "error");
        }
        return result;
    }

    @RequestMapping("/deleteComment2")
    public @ResponseBody
    Map<String, Object> deleteComment2(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> result = new HashMap<>();
        try {
            String commentId = request.getParameter("commentId");
            Map<String, Object> params = new HashMap<>();
            Map<String, Object> param = new HashMap<>();
            List<Map<String, Object>> paramss = new ArrayList<>();
            params.put("table", "sys_comment");
            param.put("param", "delflag");
            param.put("paramvalue", 1);
            paramss.add(param);
            params.put("whereparam", "id");
            params.put("wherevalue", commentId);
            params.put("list", paramss);
            wXIndexService.updateRecord(params);
            result.put("success", "success");
        } catch (Exception e) {
            LOG.error("插入失败", e);
            result.put("error", "error");
        }
        return result;
    }

    @RequestMapping("/deleteHelp")
    public @ResponseBody
    Map<String, Object> deleteHelp(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> result = new HashMap<>();
        try {
            String id = request.getParameter("id");
            Map<String, Object> params = new HashMap<>();
            Map<String, Object> param = new HashMap<>();
            List<Map<String, Object>> paramss = new ArrayList<>();
            params.put("table", "sys_information");
            param.put("param", "delflag");
            param.put("paramvalue", 1);
            paramss.add(param);
            params.put("whereparam", "id");
            params.put("wherevalue", id);
            params.put("list", paramss);
            wXIndexService.updateRecord(params);
            result.put("success", "success");
        } catch (Exception e) {
            LOG.error("插入失败", e);
            result.put("error", "error");
        }
        return result;
    }

    @RequestMapping("/testpush")
    public @ResponseBody String testPush(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String result = "go";
        try {
            for (String s : WebSocketServer.webSocketMap.keySet()) {
                System.out.println("给"+s+"发送消息成功");
                WebSocketServer.sendInfo("你们都是傻逼",s);
            }
        } catch (Exception e) {
            LOG.error("插入失败", e);
        }
        return result;
    }


}



