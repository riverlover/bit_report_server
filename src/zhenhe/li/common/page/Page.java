package zhenhe.li.common.page;

import java.util.List;

/**
 * 
 * 项目名称：reporter   
 * 类名称：Page   
 * 类描述：   用于分页的页面对象
 * 创建人：lzh   
 * 创建时间：2013-5-29 上午9:23:30   
 * 修改人：lzh   
 * 修改时间：2013-5-29 上午9:23:30   
 * 修改备注：   
 * @version
 */
public class Page<T> {
	/** 页码，第几页 */
	private int page = 1;
	/** 每页的大小  */
	private int rows = 10;
	/** 总记录数  */
	private long total;
	/** 排序，asc|desc，如果有多个以 ， 隔开*/
	private String order;
	/** 排序字段 ，如果有多个以，隔开 ，并与 order的个数及位置要一一对应*/
	private String sort;
	
	/** 此页携带的数据 */
	private List<T> data;
	
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public int getRows() {
		return rows;
	}
	public void setRows(int rows) {
		this.rows = rows;
	}
	public long getTotal() {
		return total;
	}
	public void setTotal(long total) {
		this.total = total;
	}
	public List<T> getData() {
		return data;
	}
	public void setData(List<T> data) {
		this.data = data;
	}
	public String getOrder() {
		return order;
	}
	public void setOrder(String order) {
		this.order = order;
	}
	public String getSort() {
		return sort;
	}
	public void setSort(String sort) {
		this.sort = sort;
	}
	
}
