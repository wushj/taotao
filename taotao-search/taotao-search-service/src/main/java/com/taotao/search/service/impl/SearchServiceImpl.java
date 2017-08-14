package com.taotao.search.service.impl;

import com.taotao.common.pojo.SearchResult;
import com.taotao.search.dao.SearchDao;
import com.taotao.search.service.SearchService;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private SearchDao searchDao;

    @Override
    public SearchResult search(String queryString, int page, int rows) throws Exception {
        // 1、创建一个SolrQuery对象。
        SolrQuery query = new SolrQuery();
        // 2、设置查询条件
        query.setQuery(queryString);
        // 3、设置分页条件
        if(page < 1) page = 1;
        query.setStart((page - 1) * rows);
        query.setRows(rows);
        // 4、需要指定默认搜索域。
        query.set("df", "item_title");
        // 5、设置高亮
        query.setHighlight(true);
        query.addHighlightField("item_title");
        query.setHighlightSimplePre("<em style=\"color:red\">");
        query.setHighlightSimplePost("</em>");
        // 6、执行查询，调用SearchDao。得到SearchResult
        SearchResult result = searchDao.search(query);
        // 7、需要计算总页数。
        long recordCount = result.getRecordCount();
        long pageCount = recordCount / rows;
        if (recordCount % rows > 0) {
            pageCount++;
        }
        result.setPageCount(pageCount);
        result.setTotalPages(pageCount);
        // 8、返回SearchResult
        return result;
    }

}
