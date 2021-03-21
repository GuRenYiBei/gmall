package com.atguigu.gmall.search.listener;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValue;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.amqp.core.Message;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/15/21:16
 * @Description:
 ******************************************/
@Component
public class GoodsListener {

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "SEARCH_SAVE_QUEUE",durable = "true"),
            exchange = @Exchange(value = "PMS_SPU_EXCHANGE",ignoreDeclarationExceptions = "true"
            ,type = ExchangeTypes.TOPIC),
            key = {"item.insert"}
    ))
    public void listener(Long spuId, Channel channel,Message message) throws IOException {
        //先执行redis的setnx，如果返回为true表示redis中没有，进行消费，即进行后续的业务逻辑代码
        //如果返回false，表示已经消费过，是重复消费，直接将此消息通过basicAck确定就可
        //这样可以用来防止重复消费
        try {
            ResponseVo<List<SkuEntity>> skuResponseVo = this.pmsClient.querySkuBySpuId(spuId);
            List<SkuEntity> skuEntities = skuResponseVo.getData();
            if (!CollectionUtils.isEmpty(skuEntities)) {
                List<Goods> goodsList = skuEntities.stream().map(skuEntity -> {
                    Goods goods = new Goods();
                    //商品列表需要的字段
                    goods.setSkuId(skuEntity.getId());
                    goods.setTitle(skuEntity.getTitle());
                    goods.setSubTitle(skuEntity.getSubtitle());
                    goods.setDefaultImage(skuEntity.getDefaultImage());
                    goods.setPrice(skuEntity.getPrice().doubleValue());

                    //新品排序，本质就是创建时间
                    ResponseVo<SpuEntity> spuEntityResponseVo = this.pmsClient.querySpuById(spuId);
                    SpuEntity spuEntity = spuEntityResponseVo.getData();
                    if (spuEntity != null) {
                        goods.setCreateTime(spuEntity.getCreateTime());
                    }

                    //设置库存和销量
                    ResponseVo<List<WareSkuEntity>> wareSkuResponseVo = this.wmsClient.queryWareSkuBySkuId(skuEntity.getId());
                    List<WareSkuEntity> wareSkuEntities = wareSkuResponseVo.getData();
                    if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                        goods.setStock(wareSkuEntities.stream().allMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
                        goods.setSales(wareSkuEntities.stream().map(WareSkuEntity::getSales).reduce((a, b) -> a + b).get());
                    }
                    //查询品牌
                    ResponseVo<BrandEntity> brandEntityResponseVo = this.pmsClient.queryBrandById(skuEntity.getBrandId());
                    BrandEntity brandEntity = brandEntityResponseVo.getData();
                    if (brandEntity != null) {
                        goods.setBrandId(brandEntity.getId());
                        goods.setBrandName(brandEntity.getName());
                        goods.setLogo(brandEntity.getLogo());
                    }
                    //查询分类
                    ResponseVo<CategoryEntity> categoryEntityResponseVo = this.pmsClient.queryCategoryById(skuEntity.getCategoryId());
                    CategoryEntity categoryEntity = categoryEntityResponseVo.getData();
                    if (categoryEntityResponseVo != null) {
                        goods.setCategoryId(categoryEntity.getId());
                        goods.setCategoryName(categoryEntity.getName());
                    }

                    //查询嵌套的规格参数
                    List<SearchAttrValue> searchAttrValues = new ArrayList<>();

                    //销售类型的规格参数以及值
                    ResponseVo<List<SkuAttrValueEntity>> skuAttrValueResponseVo = this.pmsClient.querySearchAttrValueBySkuIdAndCid(skuEntity.getId(), skuEntity.getCategoryId());
                    List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueResponseVo.getData();
                    if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                        searchAttrValues.addAll(skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                            SearchAttrValue searchAttrValue = new SearchAttrValue();
                            BeanUtils.copyProperties(skuAttrValueEntity, searchAttrValue);
                            return searchAttrValue;
                        }).collect(Collectors.toList()));
                    }

                    //基本类型的规格参数以及值
                    ResponseVo<List<SpuAttrValueEntity>> spuAttrValueResponseVo = this.pmsClient.querySearchAttrValueBySpuIdAndCid(skuEntity.getSpuId(), skuEntity.getCategoryId());
                    List<SpuAttrValueEntity> spuAttrValueEntities = spuAttrValueResponseVo.getData();
                    if (!CollectionUtils.isEmpty(spuAttrValueEntities)) {

                        searchAttrValues.addAll(spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                            SearchAttrValue searchAttrValue = new SearchAttrValue();
                            BeanUtils.copyProperties(spuAttrValueEntity, searchAttrValue);
                            return searchAttrValue;
                        }).collect(Collectors.toList()));
                    }

                    goods.setSearchAttrs(searchAttrValues);
                    return goods;

                }).collect(Collectors.toList());

                this.goodsRepository.saveAll(goodsList);
            }


            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            e.printStackTrace();
            if (message.getMessageProperties().getRedelivered()) {
                //当requeue参数为false，消息被认为是死信消息；如果当前队列没有绑定死信队列，消息就会被丢失
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            } else {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
                //第三个参数是是否重试入队，失败了重试一次
            }
        }
    }
}
