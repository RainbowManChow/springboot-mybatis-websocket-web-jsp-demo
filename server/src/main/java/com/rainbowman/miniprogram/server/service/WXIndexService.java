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
//  @date 2019/12/5 0005 17:18
// 

package com.rainbowman.miniprogram.server.service;

import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.Map;

public interface WXIndexService {
    public List<Map<String, Object>> getAll(Map<String,Object> paramMap);
    public void insert(Map<String,Object> paramMap);
    public void insertInfo(Map<String, Object> paramMap);
    public List<Map<String, Object>> getAllInfo(Map<String, Object> paramMap);
    public PageInfo<Map<String, Object>> getAllRecord(Map<String, Object> paramMap, Integer pageNum, Integer pageSize);
    public void deleteRecord(Map<String, Object> paramMap);
    public void insertComment(Map<String, Object> paramMap);
}
