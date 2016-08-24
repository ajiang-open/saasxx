package com.saasxx.core.module.common.schema;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;

import com.saasxx.framework.dao.orm.annotation.Comment;
import com.saasxx.framework.dao.orm.schema.IdEntity;

@Entity
@Table(indexes = {})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, include = "non-lazy")
@Comment("文本信息表")
@SelectBeforeUpdate(true)
@DynamicUpdate
@DynamicInsert
public class PText extends IdEntity {
    /**
     *
     */
    private static final long serialVersionUID = 8405027913083143517L;

    @Comment("文本内容")
    @NotNull
    @Column(length = 4096)
    String text;

    @Comment("后续文本")
    @ManyToOne(cascade = {CascadeType.REFRESH}, optional = false, fetch = FetchType.LAZY)
    PText next;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public PText getNext() {
        return next;
    }

    public void setNext(PText next) {
        this.next = next;
    }

    public String toFullString() {
        if (getNext() == null) {
            return text;
        }
        if (getNext().getId().equals(getId())) {
            return text;
        }
        return text.concat(next.toFullString());
    }

}
