package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;


/**
 * @Author xuqian
 * @Date 2019/6/12 10:30
 */

@Controller
@RequestMapping("/user/")
public class UserController {

    //将IUserService注入，赋给iUserService，
    // 注入后就可以直接用“iUserService.方法名”来调用方法，不用创建类来调用方法，相当于static类的功能
    @Autowired
    private IUserService iUserService; //iUserService该名字与UserServiceImpl中@Service注入时一致

    /**
     * 用户登录
     * @parm username
     * @parm password
     * @parm session
     * @return
     */

    //用户登录
    //下面这两为注解配置
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session){
        //service-->mybatis-->dao
        ServerResponse response = iUserService.login(username,password);
        if(response.isSuccess()){//如果登录成功，则将该用户放入session
            session.setAttribute(Const.CURRENT_USER,response.getData()); //参数：key和value
        }
        return response;
    }

    //用户登出：退出登录
    @RequestMapping(value = "logout.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session){ //没有参数username和password
        //退出登录：将session中添加的currentUser删除
        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createBySuccess();
    }

    //注册
    @RequestMapping(value = "register.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user){  //所传参数是User对象，其内有用户名，密码，邮箱等信息
        return iUserService.register(user);
    }

    //用户名和email校验
    @RequestMapping(value = "check_valid.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str, String type){  //type:用户名和eamil两种值
        return iUserService.checkValid(str,type);
    }

    //获取用户登录信息
    @RequestMapping(value = "get_user_info.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);    //这里注意要将session对象强转为user类型
        if(user != null){
            return ServerResponse.createBySuccess(user);
        }
        return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户的信息");
    }

    //忘记密码问题的获取
    @RequestMapping(value = "forget_get_question.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username){
        return iUserService.selectQuestion(username);
    }

    //校验问题答案是否正确。 将token放入本地缓存Cache中
    @RequestMapping(value = "forget_check_answer.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username, String question, String answer){
        return iUserService.checkAnswer(username, question, answer);
    }

    //忘记密码后的重置密码
    @RequestMapping(value = "forget_reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken){
        return iUserService.forgetResetPassword(username, passwordNew, forgetToken);
    }

    //登录状态下重置密码
    @RequestMapping(value = "reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(HttpSession session, String passwordOld, String passwordNew) {  //因为是登录状态下，所以将session信息传递进来
        //判断用户是否登录，只有在登录状态下才能重置密码
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        return iUserService.resetPassword(passwordOld, passwordNew, user);
    }

    //更新用户个人信息，返回类型为User,会将更新完的用户信息放入session中，并返回给前端进行显示
    @RequestMapping(value = "update_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> update_information(HttpSession session,User user){
        //首先判断用户是否登录，只有登录状态下才能更新用户信息
        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        //防止id被变化,所以传参用户信息的id都是从登录用户获取的
        user.setId(currentUser.getId());
        //防止username被变化,所以传参用户信息的username都是从登录用户获取的
        user.setUsername(currentUser.getUsername());
        ServerResponse<User> response = iUserService.updateInformation(user);
        if(response.isSuccess()){   //若response是成功的，则创建session
            response.getData().setUsername(currentUser.getUsername());
            session.setAttribute(Const.CURRENT_USER,response.getData());
        }
        return response;
    }

    //获取用户详细信息
    @RequestMapping(value = "get_information.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> get_information(HttpSession session){
        //先判断用户是否登录，若没有则强制登录
        User currentuser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentuser == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需要强制登录status=10");
        }
        return iUserService.getInformation(currentuser.getId());
    }

}

