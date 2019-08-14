package com.mmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

/**
 * @Author xuqian
 * @Date 2019/7/4 23:35
 */
@Controller
@RequestMapping("/order/")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private IOrderService iOrderService;

    //第十一章---订单模块

    //创建订单
    @RequestMapping("create.do")
    @ResponseBody
    public ServerResponse create(HttpSession session, Integer shippingId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.createOrder(user.getId(),shippingId);
    }

    //取消订单
    @RequestMapping("cancel.do")
    @ResponseBody
    public ServerResponse cancel(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.cancel(user.getId(),orderNo);
    }

    //获取购物车中选中产品的详情
    @RequestMapping("get_order_cart_product.do")
    @ResponseBody
    public ServerResponse getOrderCartProduct(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderCartProduct(user.getId());
    }

    //订单详情
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse detail(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderDetail(user.getId(),orderNo);
    }

    //订单列表
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse list(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum, @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderList(user.getId(),pageNum,pageSize);
    }





























    //以下为第10章内容--支付模块
    //当面付扫码支付
    @RequestMapping("pay.do")
    @ResponseBody
    public ServerResponse pay(HttpSession session, Long orderNo, HttpServletRequest request){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        String path = request.getSession().getServletContext().getRealPath("upload");
        return iOrderService.pay(user.getId(),path,orderNo);
    }

    //支付宝回调 与 支付宝SDK源码解析
    @RequestMapping("alipay_callback.do")
    @ResponseBody
    //返回参数类型按照alipay要求的返回参数进行设置，先写成Object
    //入参只有request，因为支付宝回调会将所有返回的参数放在request中，我们需要从request中取出参数
    public Object alipayCallback(HttpServletRequest request){
        Map<String,String> params = Maps.newHashMap();
        Map requestParams = request.getParameterMap();  //getParameterMap(String key, String[] value)
        for(Iterator iter = requestParams.keySet().iterator();iter.hasNext();){
            String name = (String) iter.next(); //得到key
            String[] values = (String[]) requestParams.get(name);
            String valueStr = ""; //valueStr得到的是：将values数据中的所有value值用逗号分割并拼接起来的字符串
            for(int i = 0;i<values.length;i++){
                valueStr = (i == values.length-1)?valueStr+values[i]:valueStr+values[i]+",";
            }
            params.put(name,valueStr); //从request中拿到key和value
        }
        logger.info("支付宝回调,sign:{},trade_status:{},参数:{}",params.get("sign"),params
        .get("trade_status"),params.toString());

        //验证回调的正确性(使用RSA2验签方法，SDK源码中有，直接调用即可（SDK包已经导入了）)，是不是支付发的，并且还要避免重复通知---非常重要！ 如何验证参考支付宝官方文档
        params.remove("sign_type"); //因为SDK源码中没有移除"sign_type"，支付宝官方文档中要求移除这个和"sign"
        try {
            boolean alipayRSACheckedV2 = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(),"utf-8",Configs.getSignType());
            if(!alipayRSACheckedV2){ //验证不通过
                return ServerResponse.createByErrorMessage("非法请求，验证不通过，再恶意请求我就报警了");
            }
        } catch (AlipayApiException e) {
            logger.error("支付宝验证回调异常",e);
        }

        //todo 验证各种数据---参考支付宝官方文档，会告诉你验证哪些数据

        //回调验证通过后，写处理alipay回调地址的逻辑（更新订单状态，判断该订单是否已经支付）-->去OrderServiceImpl中写
        ServerResponse serverResponse = iOrderService.aliCallback(params);
        if(serverResponse.isSuccess()){
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        }
        return Const.AlipayCallback.RESPONSE_FAILED;
    }

    //前台轮循查询订单的支付状态，与前端的约定是true或者false放到返回值中。若付款成功会给予一个指引：跳到订单详情页面.
    @RequestMapping("query_order_pay_status.do")
    @ResponseBody
    public ServerResponse<Boolean> queryOrderPayStatus(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //查询订单状态-->去OrderServiceImpl中写
        ServerResponse serverResponse = iOrderService.queryOrderPayStatus(user.getId(),orderNo);
        if(serverResponse.isSuccess()){
            return ServerResponse.createBySuccess(true);
        }
        return ServerResponse.createBySuccess(false);
    }




}
