package com.atguigu.gmall.search.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/12/15:32
 * @Description:
 ******************************************/
@Controller
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping
    //public ResponseVo<SearchResponseVo> search(SearchParamVo searchParamVo) {
    public String search(SearchParamVo searchParamVo, Model model) {
        SearchResponseVo searchResponseVo = searchService.search(searchParamVo);
//        return ResponseVo.ok(searchResponseVo);
        model.addAttribute("response", searchResponseVo);
        model.addAttribute("searchParam", searchParamVo);
        return "search";
    }
}
