package com.atguigu.gmall.index.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/16/0:24
 * @Description:
 ******************************************/
@Controller
public class IndexController {

    @Autowired
    private IndexService indexService;

    //跳转到首页并显示一级分类
    @GetMapping({"/", "/index"})
    public String toIndex(Model model) {

        List<CategoryEntity> categoryEntities = this.indexService.queryLvl1Categories();
        model.addAttribute("categories", categoryEntities);
        return "index";
    }

    //查询一级分类对应的二级分类、三级分类
    @GetMapping("index/cates/{pid}")
    @ResponseBody
    public ResponseVo<List<CategoryEntity>> queryLvl2CategoriesWithSubsByPid(@PathVariable("pid") Long pid) {
        List<CategoryEntity> categoryEntities = this.indexService.queryLvl2CategoriesWithSubsByPid(pid);
        return ResponseVo.ok(categoryEntities);
    }

    @GetMapping("index/test")
    @ResponseBody
    public ResponseVo jvmLockTest() {
        this.indexService.jvmLockTest();
        return ResponseVo.ok();
    }

    @GetMapping("index/write")
    @ResponseBody
    public ResponseVo writeTest() {
        this.indexService.writeTest();
        return ResponseVo.ok();
    }

    @GetMapping("index/read")
    @ResponseBody
    public ResponseVo readTest() {
        this.indexService.readTest();
        return ResponseVo.ok();
    }

    @GetMapping("index/latch")
    @ResponseBody
    public ResponseVo latch() {
        this.indexService.latch();
        return ResponseVo.ok();
    }

    @GetMapping("index/countDown")
    @ResponseBody
    public ResponseVo countDown() {
        this.indexService.countDown();
        return ResponseVo.ok();
    }
}
