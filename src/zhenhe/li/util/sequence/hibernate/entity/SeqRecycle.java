package zhenhe.li.util.sequence.hibernate.entity;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="seq_recycle")
public class SeqRecycle {
	@EmbeddedId
	private SeqRecyclePk id;

	public SeqRecyclePk getId() {
		return id;
	}

	public void setId(SeqRecyclePk id) {
		this.id = id;
	}
}
