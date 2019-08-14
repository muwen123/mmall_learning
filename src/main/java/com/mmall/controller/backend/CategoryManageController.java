package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @Author xuqian
 * @Date 2019/6/19 20:41
 */
@Controller
@RequestMapping("/manage/category")
public class CategoryManageController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;

    //增加品类
    @RequestMapping("add_category.do")
    @ResponseBody
    public ServerResponse addCategory(HttpSession session, String categoryName, @RequestParam(value = "parentId",defaultValue = "0") int parentId){
        //判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);    //这里注意要将session对象强转为user类型
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录");
        }
        //校验是否为管理员  写在UserServiceImpl类中
        if(iUserService.checkAdminRole(user).isSuccess()){
            //是管理员
            //增加处理分类的逻辑，即此方法的主要内容
            return iCategoryService.addCategory(categoryName, parentId);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    //更新categoryName
    @RequestMapping("set_category_name.do")
    @ResponseBody
    public ServerResponse setCategoryName(HttpSession session, Integer categoryId, String categoryName){
        //判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);    //这里注意要将session对象强转为user类型
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //是管理员
            //增加此功能实现的逻辑，即此方法的主要内容
            return iCategoryService.updateCategoryName(categoryId, categoryName);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    //获取当前categoryId节点下的所有子节点的category信息（平级，且不递归）
    @RequestMapping("get_category.do")
    @ResponseBody
    public ServerResponse getChildrenParallelCategory(HttpSession session, @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId){
        //判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);    //这里注意要将session对象强转为user类型
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //是管理员
            //增加此功能实现的逻辑，即此方法的主要内容
            return iCategoryService.getChildrenParallelCategory(categoryId);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    //获取当前category的Id，并且递归查询它的子节点的categoryId
    @RequestMapping("get_deep_category.do")
    @ResponseBody
    public ServerResponse getCategoryAndDeepChildrenCategory(HttpSession session, @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId){
        //判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);    //这里注意要将session对象强转为user类型
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //是管理员
            //增加此功能实现的逻辑，即此方法的主要内容
            return iCategoryService.selectCategoryAndChildrenById(categoryId);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }


}
