package com.mmall.controller.backend;

import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.util.PropertiesUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author xuqian
 * @Date 2019/6/22 15:02
 */
@Controller
@RequestMapping("/manage/product")
public class ProductManageController {

    @Autowired
    private IUserService iUserService;
    @Autowired
    private IProductService iProductService;
    @Autowired
    private IFileService iFileService;

    //保存商品
    @RequestMapping("save.do")
    @ResponseBody
    public ServerResponse productSave(HttpSession session, Product product){
        //判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);   //这里注意要将session对象强转为user类型
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //是管理员
            //增加此功能实现的逻辑，即此方法的主要内容
            return iProductService.saveOrUpdateProduct(product);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    //产品上下架（更新产品销售状态）---代码与上述方法类似
    @RequestMapping("set_sale_status.do")
    @ResponseBody
    public ServerResponse setSaleStatus(HttpSession session, Integer productId, Integer status){
        //判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);   //这里注意要将session对象强转为user类型
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //是管理员
            //增加此功能实现的逻辑，即此方法的主要内容
            return iProductService.setSaleStatus(productId, status);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    //获取产品详情
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse getDetail(HttpSession session, Integer productId){
        //判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);   //这里注意要将session对象强转为user类型
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //是管理员
            //填充业务--其实现包括properties配置文件的读取和vo的建立
            return iProductService.manageProductDetail(productId);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    //后台商品列表动态分页（第几页，页面容量，二者均有默认值，用RequestParam来实现）
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse getList(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum, @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        //判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);   //这里注意要将session对象强转为user类型
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //是管理员
            //填充业务--List+分页
            return iProductService.getProductList(pageNum,pageSize);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    //后台商品搜索
    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse productSearch(HttpSession session, String productName, Integer productId, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum, @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        //判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);   //这里注意要将session对象强转为user类型
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //是管理员
            //填充业务--搜索+List分页展示（与上一功能相同）
            return iProductService.searchProduct(productName,productId,pageNum,pageSize);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    //后台商品图片的SpringMVC上传到ftp服务器  具体实现在FileServiceImpl中，其实现IFileService接口
    @RequestMapping("upload.do")
    @ResponseBody
    public ServerResponse upload(HttpSession session,@RequestParam(value = "upload_file",required = false) MultipartFile file,HttpServletRequest request) {
        //判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);   //这里注意要将session对象强转为user类型
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //是管理员
            //实现功能
            //request.getSession().getServletContext() 这句代码获取的是servlet三大作用域之一的application，servlet三大作用域有：request、session、application
            //其实就是servletContext,也就是servlet容器，可以当做是tomcat容器) 是一个全局的储存信息的空间，服务器开始就存在，服务器关闭才释放。，所以得到的路径是Tomcat下的
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upload(file,path);//上传文件并返回文件名
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName; //拼接出url=ftp前缀+目标文件名
            //用Map组装url
            Map fileMap = Maps.newHashMap();
            fileMap.put("uri",targetFileName);
            fileMap.put("url",url);
            return ServerResponse.createBySuccess(fileMap);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    //富文本图片上传到ftp服务器，大体逻辑与SpringMVC文件上传类似，直接复制粘贴代码，小改动即可
    @RequestMapping("richtext_img_upload.do")
    @ResponseBody
    public Map richtextImgUpload(HttpSession session, @RequestParam(value = "upload_file",required = false) MultipartFile file, HttpServletRequest request, HttpServletResponse response) {
        Map resultMap = Maps.newHashMap();
        //判断用户是否登录
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            resultMap.put("success",false);
            resultMap.put("msg","请登录管理员");
            return resultMap;
        }
        //富文本中对于返回值有自己的要求，我们使用的是simditor，所以按照simditor的要求进行返回，如下：所以要用返回值类型为Map，原来为ServerResponse
//        {
//           “success”:true/false;
//           “msg”:error message, #optional
//           “file_path”: “[real file path]”
//        }

        if(iUserService.checkAdminRole(user).isSuccess()){
            //是管理员
            //实现功能
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upload(file,path);
            if(StringUtils.isBlank(targetFileName)){
                resultMap.put("success",false);
                resultMap.put("msg","上传失败");
                return resultMap;
            }
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
            resultMap.put("success",true);
            resultMap.put("msg","上传成功");
            resultMap.put("file_path",url);
            response.addHeader("Access-Control-Allow-Headers","X-File-Name");
            return resultMap;
        }else{
            resultMap.put("success",false);
            resultMap.put("msg","无权限操作");
            return resultMap;
        }
    }




}
