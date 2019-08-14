package com.mmall.service.Impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import java.util.List;

/**
 * @Author xuqian
 * @Date 2019/6/28 16:23
 */
@Service("iCartService")
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;

    //加入购物车
    /*步骤：1. 先判断该产品是否在购物车中
    *      2. 不在，则加入-->更新购物车相关属性
    *      3. 在，则更新购物车的数量，给前端返回购物车其他属性（封装了一个高复用核心方法：购物车VO）    *
    */

    public ServerResponse<CartVo> add(Integer userId, Integer count, Integer productId){
        //校验输入参数是否为空
        if(productId == null || count == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        //从数据库中根据userId和productId 获取Cart 所以要先在CartMapper接口中声明方法，然后在相应的xml文件中实现对应的sql语句
        Cart cart = cartMapper.selectCartByUserIdProductId(userId,productId);
        if(cart == null){
            //这个产品不在购物车中，需要新增这个产品的记录
            Cart cartItem = new Cart();
            cartItem.setQuantity(count);
            cartItem.setChecked(Const.Cart.CHECKED); //购物车中的产品自动设置为选中状态，去Const类中设置一个常量
            cartItem.setProductId(productId);
            cartItem.setUserId(userId);
            cartMapper.insert(cartItem); //调用cartMapper接口的Insert方法将该产品增加到数据表Cart中
        } else{
            //这个产品已经在购物车中，则更新产品数量
            count = cart.getQuantity() + count;
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        return this.list(userId);
    }

    //更新购物车商品-->改变购物车产品的数量
    public ServerResponse<CartVo> update(Integer userId, Integer count, Integer productId){
        //校验输入参数是否为空
        if(productId == null || count == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectCartByUserIdProductId(userId,productId);
        if(cart != null){
            cart.setQuantity(count);
        }
        cartMapper.updateByPrimaryKeySelective(cart); //更新购物车中这个产品的数量
        return this.list(userId);
    }

    //删除购物车商品（批量删除,产品之间用逗号分割，所以传入的参数类型为String）-->减少购物车产品相应的数量
    public ServerResponse<CartVo> deleteProduct(Integer userId, String productIds){
        //用guava的Splitter类的on(char c)方法直接将字符串按给定字符拆分并放入List中，不用自己拆分之后放在String[] 数组中，然后再遍历数组将元素放入List中
        List<String> productList = Splitter.on(",").splitToList(productIds);
        if(CollectionUtils.isEmpty(productList)){
            //若转换之后的List为空
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        //依次删除对应产品--即将其数量减去-->去CartMapper中写delete的sql语句
        cartMapper.deleteByUserIdAndProductIds(userId,productList);
        return this.list(userId);
    }

    //返回购物车列表 该方法内的两个语句可以作为封装，其他方法返回this.list就行
    public ServerResponse<CartVo> list(Integer userId){
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    //全选or全不选or单选or单不选（全选与单选的区别是是否需要productId）-->要更新对应数据库中的checked字段
    public ServerResponse<CartVo> selectOrUnSelect(Integer userId, Integer productId, Integer checked){
        cartMapper.checkedOrUncheckedProduct(userId,productId,checked); //checked从Cartcontroller中直接传过来
        return this.list(userId);
    }

    //查询当前用户的购物车中产品的数量
    public ServerResponse<Integer> getCartProductCount(Integer userId){
        if(userId == null){
            return ServerResponse.createBySuccess(0);
        }
        //写查询购物车中产品总数量的sql实现
        return ServerResponse.createBySuccess(cartMapper.selectCartProductCount(userId));
    }


    //封装的高复用的核心方法：购物车VO
    private CartVo getCartVoLimit(Integer userId) {

        CartVo cartVo = new CartVo();

        //根据userId查询一个购物车集合，去CartMapper中写具体的sql实现
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);

        //初始化cartProductVoList（已经声明过只不过还没赋值，这里进行赋值），将CartProductVo集合放入CartVo中
        List<CartProductVo> cartProductVoList = Lists.newArrayList(); //用guava的Lists方法
        //初始化cartTotalPrice（已经声明过只不过还没赋值，这里进行赋值）
        BigDecimal cartTotalPrice = new BigDecimal("0"); //用String"0"来初始化cartTotalPrice的值，这里讲解了浮点型丢失精度问题,用BigDecimal解决
        //组建CartProductVo--set它
        if (CollectionUtils.isNotEmpty(cartList)) {
            for (Cart cartItem : cartList) {
                //设置CartProductVo中关于cart部分的参数
                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(userId);
                cartProductVo.setProductId(cartItem.getProductId());

                //设置CartProductVo中关于cart部分的参数，所以得先获得product对象，下面这句话实现了
                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId()); //通过ProductId来查询product
                if (product != null) {
                    //设置CartProductVo中关于cart部分的参数
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());
                    //判断库存
                    int buyLimitCount = 0;
                    if (product.getStock() >= cartItem.getQuantity()) { //当库存充足时，设置buyLimitCount的值，前端返回成功
                        buyLimitCount = cartItem.getQuantity();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    } else {
                        //购物车中产品数量超过库存，则将购物车产品数量重置为库存
                        buyLimitCount = product.getStock();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        //购物车中更新有效库存
                        Cart cartForQuantity = new Cart();
                        cartForQuantity.setId(cartItem.getId());
                        cartForQuantity.setQuantity(buyLimitCount);
                        cartMapper.updateByPrimaryKeySelective(cartForQuantity);
                    }
                    cartProductVo.setQuantity(buyLimitCount);
                    //计算某产品总价=该产品的数量*单价（计算用BigDecimalUtil工具类）
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), cartProductVo.getQuantity()));
                    cartProductVo.setProductChecked(cartItem.getChecked());
                }

                //计算购物车中产品总价：cartTotalPrice
                //先判断某个产品是否勾选
                if (cartItem.getChecked() == Const.Cart.CHECKED) {
                    //如果已经勾选，则增加到购物车总价中
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(), cartProductVo.getProductTotalPrice().doubleValue());
                }
                //将组建好的CartProductVo加到cartProductVoList中
                cartProductVoList.add(cartProductVo);
            }
        }
        //组建CartVo--set它
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setAllChecked(this.getAllCheckedStatus(userId)); //告诉前端是否全选
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return cartVo;

    }

    //为cartVo.setAllChecked()写的一个方法
    private boolean getAllCheckedStatus(Integer userId){
        if(userId == null){
            return false;
        }
        return cartMapper.selectCartProductCheckedStatusByUserId(userId) == 0; //若返回结果为0，则没有查找到符合条件（没勾选）的数据，则是全选，返回true，反之则是未全选
    }

}
