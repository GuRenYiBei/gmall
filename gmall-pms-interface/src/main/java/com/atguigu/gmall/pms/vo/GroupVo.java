package com.atguigu.gmall.pms.vo;

import lombok.Data;

import java.util.List;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/18/14:00
 * @Description:
 ******************************************/
@Data
public class GroupVo {

    private Long groupId;
    private String groupName;
    private List<AttrValueVo> attrs;
}
