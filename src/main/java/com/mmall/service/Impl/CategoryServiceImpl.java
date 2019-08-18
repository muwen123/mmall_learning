package com.mmall.service.Impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



import java.util.List;
import java.util.Set;


/**
 * @Author xuqian
 * @Date 2019/6/19 21:02
 */
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    //增加品类
    public ServerResponse addCategory(String categoryName, Integer parentId){
        if(parentId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("添加品类参数错误");
        }
        Category category = new Category(); //在pojo已经创建了这个Category类
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true); //这个分类是可用的
        //将category对象添加进来，调用CategoryMapper类的的insert方法
        int rowCount = categoryMapper.insert(category);
        if(rowCount > 0){
            return ServerResponse.createBySuccessMessage("添加品类成功");
        }
        return ServerResponse.createByErrorMessage("添加品类失败");
    }

    //更新categoryName
    public ServerResponse updateCategoryName(Integer categoryId, String categoryName){
        if(categoryId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("更新品类参数错误");
        }
        Category category = new Category(); //在pojo已经创建了这个Category类
        category.setId(categoryId);
        category.setName(categoryName);

        int rowCount = categoryMapper.updateByPrimaryKeySelective(category); //选择性更新
        if(rowCount > 0){
            return ServerResponse.createBySuccessMessage("更新品类名字成功");
        }
        return ServerResponse.createByErrorMessage("更新品类名字失败");
    }

    //获取当前categoryId节点下的所有子节点的category信息（平级，且不递归）
    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId){
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if(CollectionUtils.isEmpty(categoryList)){
            //如果返回的集合为空，不需要给前端展示，没必要，所以打印日志即可
            logger.info("未找到当前分类的子分类");
        }
        return ServerResponse.createBySuccess(categoryList); //若找到了，则给前端返回categoryList
    }

    /**
     * 获取当前category的Id，并且递归查询它的子节点的categoryId
     * @parm categoryId
     * @return
     */
    public ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId){
        Set<Category> categorySet = Sets.newHashSet();  //用guava中Sets对下面递归算法中参数：categorySet进行初始化（创建一个对象，需要new，跟这是一个道理）
        findChildCategory(categorySet, categoryId);
        //返回的是一个categoryId的集合--下面声明该数据结构，而categorySet存储的是Category对象
        List<Integer> categoryIdList = Lists.newArrayList();    //与new ArrayList()是一样的，只不过这里调用的是guava中的方法Lists下的一个方法
        if(categoryId != null){
            //遍历CategorySet对象，得到其Id(Set的调用getId()方法),然后add到categoryIdList中
            for(Category categoryItem : categorySet) {
                categoryIdList.add(categoryItem.getId());
            }
        }
        return ServerResponse.createBySuccess(categoryIdList);
    }

    //递归算法，算出子节点
    private Set<Category> findChildCategory(Set<Category> categorySet, Integer categoryId){ //将方法的返回值作为方法的一个参数，继续调用该方法
        Category category = categoryMapper.selectByPrimaryKey(categoryId); //通过categoryId获取Category对象
        if(category != null){
            categorySet.add(category);
        }
        //查找子节点，递归算法一定得有退出条件，这里是：子节点为空，则退出递归算法
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId); //上面方法
        for(Category categoryItem : categoryList){  //for each 循环
            findChildCategory(categorySet, categoryItem.getId()); //第二个参数是孩子节点（categoryItem）的Id
        }
        return categorySet;
    }
}
