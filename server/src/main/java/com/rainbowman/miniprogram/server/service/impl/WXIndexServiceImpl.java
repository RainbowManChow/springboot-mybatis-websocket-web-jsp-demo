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
//  @date 2019/12/5 0005 17:19
// 

package com.rainbowman.miniprogram.server.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rainbowman.miniprogram.server.annotation.Master;
import com.rainbowman.miniprogram.server.mapper.WXIndexMapper;
import com.rainbowman.miniprogram.server.service.WXIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WXIndexServiceImpl implements WXIndexService {

    @Autowired
    private WXIndexMapper wXIndexMapper;

    @Master
    @Override
    public List<Map<String, Object>> getAll(Map<String, Object> param) {
        return wXIndexMapper.getAll(param);
    }
    @Master
    @Override
    public void insert(Map<String, Object> paramMap) {
        wXIndexMapper.insert(paramMap);
    }

    @Master
    @Override
    public void insertInfo(Map<String, Object> paramMap) {
        wXIndexMapper.insertInfo(paramMap);
    }

    @Master
    @Override
    public List<Map<String, Object>> getAllInfo(Map<String, Object> paramMap) {
        return wXIndexMapper.getAllInfo(paramMap);
    }

    @Master
    @Override
    public PageInfo<Map<String, Object>> getAllRecord(Map<String, Object> paramMap,Integer pageNum,Integer pageSize) {
        if(pageNum!=null&&pageSize!=null){
            PageHelper.startPage(pageNum,pageSize);
        }
        PageInfo<Map<String, Object>> a=new PageInfo<>(wXIndexMapper.getAllRecord(paramMap));
        return a;
    }

    @Master
    @Override
    public void deleteRecord(Map<String, Object> paramMap) {
        wXIndexMapper.deleteRecord(paramMap);
    }

    @Override
    public void insertComment(Map<String, Object> paramMap) {
        wXIndexMapper.insertComment(paramMap);
    }

    @Override
    public void updateRecord(Map<String, Object> paramMap) {
        wXIndexMapper.updateRecord(paramMap);
    }
}
