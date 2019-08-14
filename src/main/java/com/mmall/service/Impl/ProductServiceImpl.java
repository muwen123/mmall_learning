package com.mmall.service.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.ImageProducer;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author xuqian
 * @Date 2019/6/22 15:15
 */
@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ICategoryService iCategoryService; //平级调用注解就写在类中开头，且用@Autowired

    //保存（新增）或者更新产品--->后台是一个接口，区别是 insert 后者 update
    public ServerResponse saveOrUpdateProduct(Product product) {
        if (product != null) {
            //判断子图是否为空，不为空则将子图分割，然后赋值给主图
            if (StringUtils.isNotBlank(product.getSubImages())) {
                String[] subImageArray = product.getSubImages().split(",");
                if (subImageArray.length > 0) {
                    product.setMainImage(subImageArray[0]);
                }
            }
            //更新产品则前端一定得将productId传过来，不然不知道更新哪个产品
            if (product.getId() != null) {    //productId不为空则更新
                int rowCount = productMapper.updateByPrimaryKey(product); //更新产品成功的数量
                if (rowCount > 0) {
                    return ServerResponse.createBySuccessMessage("更新产品成功");
                }
                return ServerResponse.createBySuccessMessage("更新产品失败");
            } else {  //productId为空则新增
                int rowCount = productMapper.insert(product);
                if (rowCount > 0) {
                    return ServerResponse.createBySuccessMessage("新增产品成功");
                }
                return ServerResponse.createBySuccessMessage("新增产品失败");
            }
        }
        return ServerResponse.createByErrorMessage("新增或更新产品不正确");   //product为空时返回
    }

    //产品上下架（更新产品销售状态）---代码与上述方法类似
    //返回值类型为String,
    public ServerResponse<String> setSaleStatus(Integer productId, Integer status) {
        if (productId == null || status == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int rowCount = productMapper.updateByPrimaryKeySelective(product);
        if (rowCount > 0) {
            return ServerResponse.createBySuccessMessage("修改产品销售状态成功");
        }
        return ServerResponse.createByErrorMessage("修改产品销售状态失败");
    }

    //后台获取产品详情  为了与前台区分，这里有关后台的方法名前缀都是manage
    public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId) {
        if (productId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId); //通过productId获取product对象
        if (product == null) {
            return ServerResponse.createByErrorMessage("产品已下架或者删除");
        }
        //如果产品不为空，则返回一个VO对象---value object，用于承载对象各个值
        //复杂模型：pojo->bo(business object)->vo(view object)  简单模型：pojo->vo(value object)这里我们采用简单模型
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    private ProductDetailVo assembleProductDetailVo(Product product) {
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());

        //imageHost 从Properties配置文件中获取，是配置和代码分离,若图片服务器修改url，则只需要修改配置文件即可
        //获取的是ftp服务器前缀：http://image.imooc.com/;，其要通过key:ftp.server.http.prefix作为PropertiesUtil工具类的静态方法getProperty的参数来获取
        //工具类放在Util下面，名字都叫XXUtil
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://image.imooc.com/"));


        //set: parentCategoryId 根据它来获取相应的category，而这需要用到CategoryMapper中的方法，所以得将其注入到此类中
        //为什么set这个参数的时候不是像上面那样的呢？？productDetailVo.setParentCategoryId(product.getCategoryId());
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if (category == null) {
            productDetailVo.setParentCategoryId(0); //若所得的category为空，即数据库中没有对应的id的产品，则将该产品的ParentCategoryId置为0，它挂在根节点下面
        } else {
            productDetailVo.setParentCategoryId(category.getParentId());
        }

        //createTime，他们在数据库中通过mybatis拿出来的时候是一个毫秒数，不是我们经常看到的YYYY-XX-MM这样的，所以要进行转化，在Util中创建一个时间转化的工具类
        //updateTime
        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));

        return productDetailVo;
    }

    //后台商品列表动态分页
    public ServerResponse<PageInfo> getProductList(int pageNum, int pageSize){
        //pageHelper使用方法
        //1. startPage--start
        PageHelper.startPage(pageNum,pageSize);

        //2. 填充自己的sql逻辑
        List<Product> productList = productMapper.selectList();
        //list中不需要有product全部的属性，只需要：id, categoryId，name，subTitle, mainImage，price，status，imageHost,所以再创建一个ProductListVo
        //对所建的ProductListVo类中属性赋值，即set，则通过一个组装方法assembleProductListVo来实现（封装！）
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for(Product productItem: productList){
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }

        //3. pageHelper--收尾
        PageInfo pageResult = new PageInfo(productList);  //这里PageInfo参数只能选择productList，而我们想要在前端展示的是productListVo呢？
        //所以只需要重置pageResult即可
        pageResult.setList(productListVoList);
        return ServerResponse.createBySuccess(pageResult);
    }

    private ProductListVo assembleProductListVo(Product product){
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setName(product.getName());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://image.imooc.com/"));
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setStatus(product.getStatus());
        return productListVo;
    }

    //后台商品搜索
    public ServerResponse<PageInfo> searchProduct(String productName, Integer productId, int pageNum, int pageSize) {
        //与上面的方法类似，只不过这里多了按用户给定条件查询，最后展示与上面一模一样
        //1. startPage--start
        PageHelper.startPage(pageNum, pageSize);
        if (StringUtils.isNotBlank(productName)) {
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }
        //2. 填充自己的sql逻辑
        List<Product> productList = productMapper.selectByNameAndProductId(productName, productId);
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for (Product productItem : productList) {
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }
        //3. pageHelper--收尾
        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVoList);
        return ServerResponse.createBySuccess(pageResult);
    }

    //前台商品详情，其实现与后台类似
    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId) {
        if (productId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId); //通过productId获取product对象
        if (product == null) {
            return ServerResponse.createByErrorMessage("产品已下架或者删除");
        }
        if(product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()){
            return ServerResponse.createByErrorMessage("产品已下架或者删除");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    //用户搜索产品
    public ServerResponse<PageInfo> getProductByKeywordCategory(String keyword, Integer categoryId, int pageNum, int pageSize,String orderBy){
        if(StringUtils.isBlank(keyword) && categoryId==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        List<Integer> categoryIdList = new ArrayList<>(); //用于存储一个父分类及其下面的所有子分类，要得到这个信息得调用之前写的递归算法

        if(categoryId != null){
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if(category == null && StringUtils.isBlank(keyword)){
                //没有该分类,并且还没有关键字,这个时候返回一个空的结果集(带分页的),不报错
                PageHelper.startPage(pageNum,pageSize); //PageHelper.startPage-start
                List<ProductListVo> productListVoList = Lists.newArrayList(); //填充自己的SQL逻辑，这里只是创建一个空集合（返回）
                PageInfo pageInfo = new PageInfo(productListVoList); //pageHelper--收尾
                return ServerResponse.createBySuccess(pageInfo);
            }
            categoryIdList = iCategoryService.selectCategoryAndChildrenById(category.getId()).getData();
        }
        if(StringUtils.isNotBlank(keyword)){
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString(); //%为通配符
        }
        PageHelper.startPage(pageNum, pageSize); //1. startPage--start
        //填充自己的sql实现--动态排序
        if(StringUtils.isNotBlank(orderBy)){
            if(Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)){
                String[] orderByArray = orderBy.split("_");
                PageHelper.orderBy(orderByArray[0]+" "+orderByArray[1]); //拼接出PageHelper.orderBy()参数的格式：String orderBy
            }
        }
        //搜索product,，需要写sql语句，即先去ProductMapper中声明方法，然后去对应的xml中写select语句
        List<Product> productList = productMapper.selectByNameAndCategoryIds(StringUtils.isBlank(keyword)?null:keyword, categoryIdList.size()==0?null:categoryIdList);
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for (Product productItem : productList) {
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }
        //3. pageHelper--收尾
        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVoList);
        return ServerResponse.createBySuccess(pageResult);
    }
}




