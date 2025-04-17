package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceimpl implements DishService {
//    @Override
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetMealDishMapper setMealDishMapper;

    /**
     * 保存菜品口味
     * @param dishDTO
     */
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        //插入一条数据
        Dish dish=new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.insert(dish);
        //口味n条数据
        Long id=dish.getId();
        List<DishFlavor> flavors=dishDTO.getFlavors();
        if (flavors!=null && flavors.size()>0){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(id);
            });
            dishFlavorMapper.insertBatch(flavors);
        }

    }

    /**
     * 菜品删除
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {

        //能不能删
        for (Long id : ids) {
            Dish dish=dishMapper.getByid(id);
            if (dish.getStatus()== StatusConstant.ENABLE){
                throw  new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //有没有被关联
        List<Long>setMealId =setMealDishMapper.getSetMealIdByDishId(ids);
        if (setMealId!=null&& setMealId.size()>0){
            throw  new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }
        //删除菜品and口味
        for (Long id : ids) {
            setMealDishMapper.deleteById(id);
            dishFlavorMapper.deleteById(id);
        }


    }

    @Override
    public DishVO getByIdWithFlavor(long id) {
        //查菜品
        Dish dish=dishMapper.getByid(id);
        //查口味
        List<DishFlavor> dishFlavors=dishFlavorMapper.getByDishId(id);
        //
        DishVO dishVO=new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    @Override
    public void uptateWithFlavor(DishDTO dishDTO) {
        //修改基本信息
        Dish dish=new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);
        //删除口味消息
        dishFlavorMapper.deleteById(dishDTO.getId());
        //重新全插入
        List<DishFlavor>flavors=dishDTO.getFlavors();
        if (flavors!=null && flavors.size()>0){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 实现菜品分页
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page=dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }
    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
    /**
     * 菜品起售停售
     * @param status
     * @param id
     */
    @Override
    @Transactional
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);

//        if (status == StatusConstant.DISABLE) {
//            // 如果是停售操作，还需要将包含当前菜品的套餐也停售
//            List<Long> dishIds = new ArrayList<>();
//            dishIds.add(id);
//            // select setmeal_id from setmeal_dish where dish_id in (?,?,?)
//            List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(dishIds);
//            if (setmealIds != null && setmealIds.size() > 0) {
//                for (Long setmealId : setmealIds) {
//                    Setmeal setmeal = Setmeal.builder()
//                            .id(setmealId)
//                            .status(StatusConstant.DISABLE)
//                            .build();
//                    setmealMapper.update(setmeal);
//                }
//            }
//        }
    }
}
