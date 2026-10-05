package com.mrsoft.arabicreference.content.infrastructure.persistence;

import java.io.Serializable;
import java.util.UUID;

public class ArticleTagLinkKey implements Serializable {

    private UUID articleId;
    private UUID tagId;

    public ArticleTagLinkKey() {
    }

    public ArticleTagLinkKey(UUID articleId, UUID tagId) {
        this.articleId = articleId;
        this.tagId = tagId;
    }

    public UUID getArticleId() { return articleId; }
    public void setArticleId(UUID articleId) { this.articleId = articleId; }
    public UUID getTagId() { return tagId; }
    public void setTagId(UUID tagId) { this.tagId = tagId; }

    @Override
    public boolean equals(Object other) {
        return other instanceof ArticleTagLinkKey key && articleId.equals(key.articleId) && tagId.equals(key.tagId);
    }

    @Override
    public int hashCode() {
        return articleId.hashCode() * 31 + tagId.hashCode();
    }
}
