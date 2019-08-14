package com.mmall.service.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @Author xuqian
 * @Date 2019/7/3 22:41
 */
@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    //增加地址,将id返回给前端
    public ServerResponse add(Integer userId, Shipping shipping){
        shipping.setUserId(userId);
        int rowCount = shippingMapper.insert(shipping);
        if(rowCount > 0){
            Map result = Maps.newHashMap();
            result.put("shippingId",shipping.getId());
            return ServerResponse.createBySuccess("新建地址成功",result);
        }
        return ServerResponse.createByErrorMessage("新建地址失败");
    }

    //删除地址 注意横向越权，重写mapper中的sql语句(根据userId和shippingId来删除)
    public ServerResponse<String> del(Integer userId, Integer shippingId){
        int resultCount = shippingMapper.deleteByShippingIdUserId(userId,shippingId);
        if(resultCount > 0){
            return ServerResponse.createBySuccess("删除地址成功");
        }
        return ServerResponse.createByErrorMessage("删除地址失败");
    }

    //更新地址 注意横向越权，重写mapper中的sql语句(根据userId和shippingId来更新)
    public ServerResponse update(Integer userId, Shipping shipping){
        shipping.setUserId(userId); //避免横向越权，将session的userId赋给shipping
        int rowCount = shippingMapper.updateByShipping(shipping);
        if(rowCount > 0){
            return ServerResponse.createBySuccess("更新地址成功");
        }
        return ServerResponse.createByErrorMessage("更新地址失败");
    }

    //查询地址 注意横向越权，重写mapper中的sql语句(根据userId和shippingId来查询)
    public ServerResponse<Shipping> select(Integer userId, Integer shippingId){
        Shipping shipping = shippingMapper.selectByShippingIdUserId(userId,shippingId);
        if(shipping == null){
            return ServerResponse.createByErrorMessage("无法查询到该地址");
        }
        return ServerResponse.createBySuccess("查询地址成功",shipping);
    }

    //分页列表
    public ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize){
        //pageHelper使用方法
        //1. startPage--start
        PageHelper.startPage(pageNum,pageSize);
        //2. 填充自己的sql逻辑
        List<Shipping> shippingList = shippingMapper.selectByUserId(userId);
        //3. pageHelper--收尾
        PageInfo pageInfo = new PageInfo(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }

}
