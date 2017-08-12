package com.taotao.content.service.impl;

import com.taotao.common.pojo.TaotaoResult;
import com.taotao.common.utils.JsonUtils;
import com.taotao.content.service.ContentService;
import com.taotao.jedis.JedisClient;
import com.taotao.mapper.TbContentMapper;
import com.taotao.pojo.TbContent;
import com.taotao.pojo.TbContentExample;
import com.taotao.pojo.TbContentExample.Criteria;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;
	@Autowired
	private JedisClient jedisClient;

	@Value("${INDEX_CONTENT}")
	private String indexContent;


	@Override
	public TaotaoResult addContent(TbContent content) {
		//补全pojo的属性
		content.setCreated( new Date());
		content.setUpdated(new Date());
		//插入到内容表
		contentMapper.insert(content);
		//删除对应的缓存
        jedisClient.hdel(indexContent,String.valueOf(content.getCategoryId()));
		return TaotaoResult.ok();
	}

	@Override
	public List<TbContent> getContentByCid(long cid) {
		//先查询缓存
		//添加缓存不能影响业务逻辑
		try {
		    //查询到结果转换成list返回
            String result = jedisClient.hget(indexContent, String.valueOf(cid));
            if (StringUtils.isNotBlank(result)){
                List<TbContent> tbContents = JsonUtils.jsonToList(result, TbContent.class);
                return tbContents;
            }

        }catch (Exception e){
            e.printStackTrace();
		}

		//缓存未命中,查询数据库
		TbContentExample example = new TbContentExample();
		Criteria criteria = example.createCriteria();
		//设置查询条件
		criteria.andCategoryIdEqualTo(cid);
		//执行查询
		List<TbContent> list = contentMapper.selectByExample(example);

		//结果添加缓存
		try {
			jedisClient.hset(indexContent,String.valueOf(cid), JsonUtils.objectToJson(list));
		}catch (Exception e){
			e.printStackTrace();
		}
		//返回结果
		return list;
	}

}
