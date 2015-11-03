package zhenhe.li.util.sequence.hibernate.service;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import zhenhe.li.common.dao.HibernateDao;
import zhenhe.li.util.sequence.hibernate.entity.Seq;
import zhenhe.li.util.sequence.hibernate.entity.SeqRecycle;
import zhenhe.li.util.sequence.hibernate.entity.SeqRecyclePk;

/**
 * 序列生成器：用于根据客户提出的编码规则生成
 * @author lzh
 *
 */
@Component
public class SequenceGenerator {
	@Resource
	private HibernateDao<SeqRecycle,SeqRecyclePk> seqRecycleDao;
	@Resource
	private HibernateDao<Seq,String> seqDao;
	/**
	 * 
	 * @param prefix
	 * @param totalLen 要返回的字符串总长度，不足位左补0
	 * @return
	 */
	public synchronized String getId(String prefix){
		String id = "0";		
		Seq seq = seqDao.get(Seq.class,prefix);
		if(seq == null){
			seq = new Seq();
			seq.setId(prefix);
			seq.setCurrval("1");
			seqDao.save(seq);
		}else{
			id = seq.getCurrval();
			seq.setCurrval(seq.getCurrval()+1);
			seqDao.update(seq);
		}
		return id;		
	}
	public synchronized String getSeqCurrVal(String className,String prefix,int totalLen){
		/** 先判断序列回收表中有无可用序列 */
		Serializable recycleId = getIdFromRecycle(className+prefix);
		if(recycleId != null){
			return prefix+recycleId+"";
		}
		
		
		/** 回收表中无可用序列时，生成新序列 */
		//初始值，如果是新记录，默认返回 0 作为初始序列号
		String currval = String.format("%0"+totalLen+"d", 0);		
		Seq seq = seqDao.get(Seq.class, className+prefix);
		if(seq == null){
			seq = new Seq();
			seq.setId(className+prefix);
			seq.setCurrval(String.format("%0"+totalLen+"d", 1));
			seqDao.save(seq);
		}else{
			currval = seq.getCurrval();
			//序列加1后保存回序列表
			long currval_longVal = Long.parseLong(currval);
			seq.setCurrval(String.format("%0"+totalLen+"d", currval_longVal+1));
			seqDao.update(seq);
		}
		return prefix+currval;	
	}
	/**
	 * 
	 * @param prefix 序列前缀，建议用类全名（即带包名的类名）
	 * @param totalLen 序列全长
	 * @param start 起始值
	 * @param step 步长
	 * @return 下一个序列值
	 */
	public synchronized String getSeqCurrVal(String prefix,int totalLen,int start,int step){
		/** 先判断序列回收表中有无可用序列 */
		Serializable recycleId = getIdFromRecycle(prefix);
		if(recycleId != null){
			return prefix+recycleId+"";
		}
		//步进必须至少不小于1，如果是小于0的步进值，强制修改为1
		if(step <= 0){
			step = 1;
		}
		
		/** 回收表中无可用序列时，生成新序列 */
		//初始值，如果是新记录，默认返回 0 作为初始序列号
		String currval = String.format("%0"+totalLen+"d", start);		
		Seq seq = seqDao.get(Seq.class, prefix);
		if(seq == null){
			seq = new Seq();
			seq.setId(prefix);
			seq.setCurrval(String.format("%0"+totalLen+"d", step));
			seqDao.save(seq);
		}else{
			currval = seq.getCurrval();
			//序列加1后保存回序列表
			long currval_longVal = Long.parseLong(currval);
			seq.setCurrval(String.format("%0"+totalLen+"d", currval_longVal+step));
			seqDao.update(seq);
		}
		//把prefix 去掉，2015/04/02 李真河，原因，这里如果带着prefix，会把前缀一起返回，例如com.xxx.entity.Terminal00000002
		//return prefix+currval;	
		//-- 把prefix 去掉
		return currval;
	}
	/** 废号回收 */
	public void recyle(String prefix,String recyleVal){
		//取出以prfix打头的序号
		List<Seq> seqs = seqDao.findAllByCriteria(Seq.class, Restrictions.like("id",prefix,MatchMode.START));
		//不存在该序列 
		if(seqs.size() == 0)
			return;
		Seq seq = seqs.get(0);
		//确定原序列的 序号长度 
		int val_len = seq.getCurrval().length();
		SeqRecycle seqRecycle = new SeqRecycle();
		SeqRecyclePk seqRecyclePk = new SeqRecyclePk();
		seqRecyclePk.setPrefix(prefix + recyleVal.substring(0, recyleVal.length()-val_len));
		seqRecyclePk.setRecycleVal(recyleVal.substring(recyleVal.length()-val_len));
		seqRecycle.setId(seqRecyclePk);
		seqRecycleDao.save(seqRecycle);
	}
	public synchronized Serializable getIdFromRecycle(String prefix){
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(SeqRecycle.class);
		detachedCriteria.add(Restrictions.eq("id.prefix", prefix));
		detachedCriteria.addOrder(Order.asc("id.recycleVal"));
		List<SeqRecycle> seqRecycles = seqRecycleDao.findAllByCriteriaSpecification(detachedCriteria);
		if(seqRecycles.size()>0){
			String recycleId = seqRecycles.get(0).getId().getRecycleVal();
			//用完就把回收表中的记录清理
			SeqRecyclePk id = new SeqRecyclePk();
			id.setPrefix(prefix);
			id.setRecycleVal(recycleId+"");
			this.removeFromRecycle(id);
			return recycleId;
		}else{
			return null;
		}
	}
	public synchronized void removeFromRecycle(SeqRecyclePk id){
		SeqRecycle seqRecycle = seqRecycleDao.get(SeqRecycle.class, id);
		if(seqRecycle != null)
			seqRecycleDao.delete(SeqRecycle.class, id);		
	}
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public synchronized String getSeqCurrVal(String className,String prefix,int totalLen,int startWith,int step){
		/** 先判断序列回收表中有无可用序列 */
		Serializable recycleId = getIdFromRecycle(className+prefix);
		if(recycleId != null){
			return prefix+recycleId+"";
		}
		if(step==0){
			step=1;
		}
		/** 回收表中无可用序列时，生成新序列 */
		//初始值，如果是新记录，默认返回 0 作为初始序列号
		String currval = String.format("%0"+totalLen+"d", startWith);		
		Seq seq = seqDao.get(Seq.class, className+prefix);
		if(seq == null){
			seq = new Seq();
			seq.setId(className+prefix);
			seq.setCurrval(String.format("%0"+totalLen+"d", startWith+step));
			seqDao.save(seq);
		}else{
			currval = seq.getCurrval();
			//序列加1后保存回序列表
			long currval_longVal = Long.parseLong(currval);
			seq.setCurrval(String.format("%0"+totalLen+"d", currval_longVal+step));
			seqDao.update(seq);
		}
		seqDao.getHibernateSession().flush();
		return prefix+currval;	
	}
}
