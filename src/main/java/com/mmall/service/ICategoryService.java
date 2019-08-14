package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;

import java.util.List;

/**
 * @Author xuqian
 * @Date 2019/6/19 21:02
 */
public interface ICategoryService {
    ServerResponse addCategory(String categoryName, Integer parateId);

    ServerResponse updateCategoryName(Integer categoryId, String categoryName);

    ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId);

    ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId);
}
