package com.mmall.service.Impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * @Author xuqian
 * @Date 2019/6/12 11:11
 */

@Service("iUserService") //将Service（其实是UserServiceImpl这个类）注入到UserController 上供其使用

//UserServiceImpl 来实现 IUserService接口
public class UserServiceImpl implements IUserService {

    @Autowired //该注解将UserMapper类注入，赋值给userMapper
    private UserMapper userMapper;


    //登录
    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);//检查登录的用户名是否存在
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("用户名不存在");
        }

        //todo 密码登录MD5（加密的）
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username, md5Password);
        if(user == null){//user为Null,说明查询结果为null，即没有匹配上查询条件，所以密码错误
            return ServerResponse.createByErrorMessage("密码错误");
        }

        //上述两种情况：用户不存在和密码错误 都不存在的话，将密码置为空，然后返回登录成功
        user.setPassword(StringUtils.EMPTY);

        return ServerResponse.createBySuccess("登录成功",user);
    }

    //注册
    public ServerResponse<String> register(User user){
        //下面这段注释掉的代码功能：校验用户名已存在，与下面的checkValid中的代码重复，为了提高代码重用，所以改为下面的
//        int resultCount = userMapper.checkUsername(user.getUsername()); //校验用户名已存在
//        if(resultCount > 0){
//            return ServerResponse.createByErrorMessage("用户名已存在");
//        }
        ServerResponse validResponse = this.checkValid(user.getUsername(),Const.USERNAME);
        if(!validResponse.isSuccess()){
            return validResponse;
        }
        //下面这段注释掉的代码功能：校验邮箱已存在，与下面的checkValid中的代码重复，为了提高代码重用，所以改为下面的
//        int resultCount = userMapper.checkEmail(user.getEmail());
//        if(resultCount > 0){
//            return ServerResponse.createByErrorMessage("email已存在");
//        }
        validResponse = this.checkValid(user.getEmail(),Const.EMAIL);
        if(!validResponse.isSuccess()){
            return validResponse;
        }
        //若以上两个return都没有，则证明没有注册过
        user.setRole(Const.Role.ROLE_CUSTOMER);//设置为普通用户
        //MD5加密（非对称加密）
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);  //注册就是将用户添加进来，调用userMapper接口的insert方法
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    //用户名和email校验
    public ServerResponse<String> checkValid(String str, String type){  //str是值，type有用户名和email两种值
        if(org.apache.commons.lang3.StringUtils.isNotBlank(type)){  //如果type不为空
            //开始校验
            if(Const.USERNAME.equals(type)){
                int resultCount = userMapper.checkUsername(str); //校验用户名已存在
                if(resultCount > 0){
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
            if(Const.EMAIL.equals(type)){
                int resultCount = userMapper.checkEmail(str);
                if(resultCount > 0){
                    return ServerResponse.createByErrorMessage("email已存在");
                }
            }
        }else {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    //忘记密码问题的获取
    public ServerResponse<String> selectQuestion(String username){
        ServerResponse validResponse = this.checkValid(username,Const.USERNAME); //复用checkValid方法
        if(validResponse.isSuccess()){
            //用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }

        String question = userMapper.selectQuestionByUsername(username);
        if(StringUtils.isNotBlank(question)){
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("找回密码的问题是空的");
    }

    //校验问题及问题答案是否正确
    public ServerResponse<String> checkAnswer(String username, String question, String answer){
        int resultConut = userMapper.checkAnswer(username, question, answer);
        if(resultConut > 0){ //如果返回值大于0，则证明select找到了符合条件的记录，说明问题及问题答案是这个用户的，并且是正确的
            String forgetToken = UUID.randomUUID().toString();
            //将forgetToken放入本地缓存Cache中，设置其有效期为12小时
            //TokenCache.setKey("token_"+username, forgetToken);  //setKey(key, value)为静态方法，通过类名.方法名来调用
            //上面语句也是对的，只是下面语句将token_声明为常量，更方便。因为其与token紧密相关，所以在TokenCache类中声明
            TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetToken);
            return ServerResponse.createBySuccess(forgetToken); //同理：createBySuccess为静态方法，通过类名.方法名来调用
        }
        return ServerResponse.createByErrorMessage("问题的答案错误");
    }

    //忘记密码后的重置密码
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken){
        //校验forgetToken
        if(StringUtils.isBlank(forgetToken)){  //如果传参forgetToken是空的，则返回失败
            return ServerResponse.createByErrorMessage("参数错误，token需要传递");
        }

        //校验username，若username为空，则你拿到的key是token_+空,，则key是没有意义的，你当然可以拿到value即forgetToken，但不符合我们逻辑
        //实现此功能，直接复用上面的方法，如下：
        ServerResponse validResponse = this.checkValid(username,Const.USERNAME); //复用checkValid方法
        if(validResponse.isSuccess()){
            //用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }

        //从TokenCache中获取token，调用静态方法getKey(key)得到value值就是token
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        if(org.apache.commons.lang3.StringUtils.isBlank(token)){   //对TokenCache中的token也要做校验
            return ServerResponse.createByErrorMessage("token无效或过期");
        }

        //比较获取的token与本地缓存的forgetToken
        if(org.apache.commons.lang3.StringUtils.equals(forgetToken,token)){  //若一样，则可以开始修改密码了
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            //返回uodate生效行数，若大于0则生效
            int rowCount = userMapper.updatePasswordByUsername(username, md5Password); //这里参数新密码要用MD5加密后的，不是与原来的passwordNew
            if(rowCount > 0){
                return ServerResponse.createBySuccessMessage("修改密码成功");
            }
        }else{  //否则，返回在前端错误提示信息
            return ServerResponse.createByErrorMessage("token错误，请重新获取重置密码的token");
        }

        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    //登录状态下重置密码
    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user){
        //防止横向越权，要校验此用户的旧密码一定要指定是这个用户，因为我们会查询一个count(1)，如果不指定id，那么结果就是true,count>0
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld), user.getId());
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("旧密码错误");
        }

        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user); //选择性更新：将不为空的更新
        if(updateCount > 0){
            return ServerResponse.createBySuccessMessage("密码更新成功");
        }
        return ServerResponse.createByErrorMessage("密码更新失败");
    }

    //更新用户个人信息，返回类型为User,会将更新完的用户信息放入session中，并返回给前端进行显示
    public ServerResponse<User> updateInformation(User user){
        //username不能被更新
        //eamil也要进行校验，校验新的email是否存在，并且存在的email如果相同的话，不能是我们当前的这个用户的，因为这样此用户的email还是原来的
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if(resultCount > 0){
            return ServerResponse.createByErrorMessage("email已经存在，请更换email并尝试更新");
        }

        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);   //选择性更新：只更新上述字段
        if(updateCount > 0){
            return ServerResponse.createBySuccess("更新个人信息成功",updateUser);
        }
        return ServerResponse.createByErrorMessage("更新个人信息失败");
    }

    public ServerResponse<User> getInformation(Integer userId){
        User user = userMapper.selectByPrimaryKey(userId);
        if(user == null){
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }
        user.setPassword(StringUtils.EMPTY);  //是当前用户，则将密码置空
        return ServerResponse.createBySuccess(user);
    }

    //backend
    //以用于：下分类管理模块：CategoryManageController类中的addCategory方法
    /**
     * 校验是否是管理员
     * @parm user
     * @return
     */
    public ServerResponse checkAdminRole(User user) {
        if(user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

}
