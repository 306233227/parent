package com.pinyougou.solrutil;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.stereotype.Component;
import com.pinyougou.pojo.TbItemExample.Criteria;
import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private SolrTemplate solrTemplate;
    /**
     * 导入商品数据
     */
public void  importItemData(){
    TbItemExample itemExample=new TbItemExample();
    Criteria criteria = itemExample.createCriteria();
    criteria.andStatusEqualTo("1");
    List<TbItem> tbItems = itemMapper.selectByExample(itemExample);

    System.out.println("===商品列表===");
    for (TbItem tbItem : tbItems) {
        System.out.println(tbItem.getId()+" "+ tbItem.getTitle()+ " "+tbItem.getPrice());
        Map specMap = JSON.parseObject(tbItem.getSpec(),Map.class);
        tbItem.setSpecMap(specMap);


    }
    solrTemplate.saveBeans(tbItems);

    solrTemplate.commit();
    System.out.println("===结束==");
}
    public void testDeleteAll(){
        Query query=new SimpleQuery("*:*");
        solrTemplate.delete(query);
        solrTemplate.commit();
    }


    public void cx(){
    TbItemExample itemExample=new TbItemExample();
    Criteria criteria=itemExample.createCriteria();
    criteria.andCategoryEqualTo("半身裙");
        List<TbItem> tbItems = itemMapper.selectByExample(itemExample);
        for (TbItem tbItem : tbItems) {
            System.out.println(tbItem.getTitle());

        }

       solrTemplate.saveBeans(tbItems);

        solrTemplate.commit();
    }

    public static void main(String[] args) {

        ApplicationContext context=new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");

        SolrUtil solrUtil = (SolrUtil) context.getBean("solrUtil");
         solrUtil.importItemData();

        //   solrUtil.cx();

    }

    /*public static void main(String[] args) {
        ApplicationContext context=new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");

        SolrUtil solrUtil = (SolrUtil) context.getBean("solrUtil");
        solrUtil.testDeleteAll();
    }*/
}
