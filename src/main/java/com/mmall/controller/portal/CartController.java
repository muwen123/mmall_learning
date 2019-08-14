package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICartService;
import com.mmall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @Author xuqian
 * @Date 2019/6/28 16:16
 */
@Controller
@RequestMapping("/cart/")
public class CartController {

    @Autowired
    private ICartService iCartService;

    //加入购物车
    @RequestMapping("add.do")
    @ResponseBody
    public ServerResponse<CartVo> add(HttpSession session, Integer count, Integer productId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //方法逻辑，在CartServiceImpl类中实现
        return iCartService.add(user.getId(),count,productId);
    }

    //更新购物车商品-->改变购物车产品的数量
    @RequestMapping("update.do")
    @ResponseBody
    public ServerResponse<CartVo> update(HttpSession session, Integer count, Integer productId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //方法逻辑，在CartServiceImpl类中实现
        return iCartService.update(user.getId(),count,productId);
    }

    //删除购物车商品（批量删除,产品之间用逗号分割，所以传入的参数类型为String）-->减少购物车产品相应的数量
    @RequestMapping("delete_product.do")
    @ResponseBody
    public ServerResponse<CartVo> deleteProduct(HttpSession session,String productIds){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //方法逻辑，在CartServiceImpl类中实现
        return iCartService.deleteProduct(user.getId(),productIds);
    }

    //返回购物车列表
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<CartVo> list(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //方法逻辑，在CartServiceImpl类中实现
        return iCartService.list(user.getId());
    }

    //全选，它和全反选封装成一个方法，只不过传不同的参数
    //此方法的意思就是要更新当前用户购物车cart数据库中的checked字段，然后返回CartVo对象（其中包括购物车商品总价）
    @RequestMapping("select_all.do")
    @ResponseBody
    public ServerResponse<CartVo> selectAll(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //方法逻辑，在CartServiceImpl类中实现
        return iCartService.selectOrUnSelect(user.getId(),null,Const.Cart.CHECKED);
    }
    //全反选
    @RequestMapping("un_select_all.do")
    @ResponseBody
    public ServerResponse<CartVo> unSelectAll(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //方法逻辑，在CartServiceImpl类中实现
        return iCartService.selectOrUnSelect(user.getId(),null,Const.Cart.UN_CHECKED);
    }

    //单选
    @RequestMapping("select.do")
    @ResponseBody
    public ServerResponse<CartVo> Select(HttpSession session,Integer productId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //方法逻辑，在CartServiceImpl类中实现
        return iCartService.selectOrUnSelect(user.getId(),productId,Const.Cart.CHECKED);
    }
    //单反选
    @RequestMapping("un_select.do")
    @ResponseBody
    public ServerResponse<CartVo> unSelect(HttpSession session,Integer productId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //方法逻辑，在CartServiceImpl类中实现
        return iCartService.selectOrUnSelect(user.getId(),productId,Const.Cart.UN_CHECKED);
    }

    //查询当前用户的购物车中产品的数量，其显示在前端的位置是右上角。原则：如果一个产品的数量是10，那么数量就是10（即按产品的总体数量算，不按产品的种类算）
    @RequestMapping("get_cart_product_count.do")
    @ResponseBody
    public ServerResponse<Integer> getCartProductCount(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createBySuccess(0);
        }
        //方法逻辑，在CartServiceImpl类中实现
        return iCartService.getCartProductCount(user.getId());
    }



}
