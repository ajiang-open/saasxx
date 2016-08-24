package com.saasxx.core.module.common.schema;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;

import com.alibaba.fastjson.annotation.JSONField;
import com.saasxx.framework.dao.orm.annotation.Comment;
import com.saasxx.framework.dao.orm.schema.IdEntity;

@Entity
@Table(indexes = {})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, include = "non-lazy")
@Comment("地址区域表")
@SelectBeforeUpdate(true)
@DynamicUpdate
@DynamicInsert
public class PArea extends IdEntity {
    /**
     *
     */
    private static final long serialVersionUID = 6028694136360973285L;

    @Column(unique = true, length = 8)
    @Comment("代码")
    String code;

    @Comment("名称")
    String name;

    @Comment("父区域")
    @ManyToOne(cascade = {CascadeType.REFRESH}, optional = false, fetch = FetchType.LAZY)
    PArea parent;

    @Comment("区列表")
    @OneToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY, mappedBy = "parent")
    @JSONField(serialize = false)
    List<PArea> children;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PArea getParent() {
        return parent;
    }

    public void setParent(PArea parent) {
        this.parent = parent;
    }

    public List<PArea> getChildren() {
        return children;
    }

    public void setChildren(List<PArea> children) {
        this.children = children;
    }

}
